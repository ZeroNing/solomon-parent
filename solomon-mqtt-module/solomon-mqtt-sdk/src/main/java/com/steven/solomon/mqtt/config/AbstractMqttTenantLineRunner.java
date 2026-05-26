package com.steven.solomon.mqtt.config;

import com.steven.solomon.init.AbstractMessageLineRunner;
import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.model.AbstractTenantMqttProfile;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.context.ApplicationContext;

/**
 * 多租户 MQTT 客户端初始化模板。
 *
 * <p>统一处理启用开关、租户配置遍历和初始化服务选择；具体模块只提供配置类型和默认服务。</p>
 *
 * @param <A> 消费者标记注解类型
 * @param <P> MQTT 客户端配置类型
 * @param <S> MQTT 初始化服务类型
 */
public abstract class AbstractMqttTenantLineRunner<
        A extends Annotation, P, S extends MqttClientInitService<P>>
    extends AbstractMessageLineRunner<A> {

  private final AbstractTenantMqttProfile<P> profile;
  private final Class<? extends MqttClientInitService> serviceType;
  private final Supplier<S> defaultServiceSupplier;

  protected AbstractMqttTenantLineRunner(
      Class<A> annotationType,
      AbstractTenantMqttProfile<P> profile,
      ApplicationContext applicationContext,
      Class<? extends MqttClientInitService> serviceType,
      Supplier<S> defaultServiceSupplier) {
    super(annotationType);
    this.profile = profile;
    this.serviceType = serviceType;
    this.defaultServiceSupplier = defaultServiceSupplier;
    SpringUtil.setContext(applicationContext);
  }

  @SuppressWarnings("unchecked")
  protected AbstractMqttTenantLineRunner(
      AbstractTenantMqttProfile<P> profile,
      ApplicationContext applicationContext,
      Class<? extends MqttClientInitService> serviceType,
      Supplier<S> defaultServiceSupplier) {
    this(
        (Class<A>) MessageListener.class,
        profile,
        applicationContext,
        serviceType,
        defaultServiceSupplier);
  }

  @Override
  public void init(List<Object> listenerList) throws Exception {
    if (!profile.getEnabled()) {
      logger.info("mqtt 未启用，跳过客户端初始化");
      return;
    }
    Map<String, P> tenantProfileMap = profile.getTenant();
    if (ValidateUtils.isEmpty(tenantProfileMap)) {
      logger.warn("未配置 MQTT 租户连接信息");
      return;
    }
    S initService = resolveInitService();
    for (Map.Entry<String, P> entry : tenantProfileMap.entrySet()) {
      initService.initMqttClient(entry.getKey(), entry.getValue(), listenerList);
    }
  }

  @SuppressWarnings("unchecked")
  private S resolveInitService() {
    Map<String, ? extends MqttClientInitService> serviceMap = SpringUtil.getBeansOfType(serviceType);
    return ValidateUtils.isNotEmpty(serviceMap)
        ? (S) serviceMap.values().iterator().next()
        : defaultServiceSupplier.get();
  }
}
