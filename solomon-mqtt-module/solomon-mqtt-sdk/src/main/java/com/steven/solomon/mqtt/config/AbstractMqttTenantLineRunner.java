package com.steven.solomon.mqtt.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import com.steven.solomon.context.AbstractTenantProperties;
import com.steven.solomon.init.AbstractMessageLineRunner;
import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.enums.MqttTenantMode;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.spring.SpringUtil;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * 多租户 MQTT 客户端初始化模板。
 *
 * <p>统一处理启用开关、租户配置遍历和初始化服务选择；具体模块只提供配置类型和默认服务。</p>
 *
 * <p>支持两种多租户连接模式（通过 {@code mqtt.tenant-mode} 配置切换）：</p>
 * <ul>
 *   <li>{@link MqttTenantMode#PER_TENANT_CONNECTION PER_TENANT_CONNECTION}（默认）：
 *       遍历 {@code mqtt.tenant} 为每个租户创建独立连接。</li>
 *   <li>{@link MqttTenantMode#SHARED_CONNECTION SHARED_CONNECTION}：
 *       只取一个租户配置创建共享连接，所有监听器订阅一次，消费时按消息体 tenantCode 路由。</li>
 * </ul>
 *
 * @param <A> 消费者标记注解类型
 * @param <P> MQTT 客户端配置类型
 * @param <S> MQTT 初始化服务类型
 */
public abstract class AbstractMqttTenantLineRunner<
        A extends Annotation, P, S extends MqttClientInitService<P>>
    extends AbstractMessageLineRunner<A> {

  /** 共享连接模式下使用的逻辑租户编码，用于在工具类中统一注册唯一的共享客户端。 */
  protected static final String SHARED_TENANT_CODE = "SHARED";

  /** SHARED 模式下优先选取的默认租户配置键名。 */
  private static final String DEFAULT_TENANT_KEY = "default";

  /** 多租户模式配置项键名。 */
  private static final String TENANT_MODE_PROPERTY = "mqtt.tenant-mode";

  /** 多租户配置属性。 */
  private final AbstractTenantProperties<P> profile;

  /** MQTT 初始化服务的 Spring Bean 类型，用于业务侧自定义实现时优先选取。 */
  private final Class<? extends MqttClientInitService> serviceType;

  /** 默认初始化服务提供者，当容器中不存在自定义实现时使用。 */
  private final Supplier<S> defaultServiceSupplier;

  /** Spring 环境，用于读取多租户模式配置项。 */
  private final Environment environment;

  protected AbstractMqttTenantLineRunner(
      Class<A> annotationType,
      AbstractTenantProperties<P> profile,
      ApplicationContext applicationContext,
      Class<? extends MqttClientInitService> serviceType,
      Supplier<S> defaultServiceSupplier) {
    super(annotationType);
    this.profile = profile;
    this.serviceType = serviceType;
    this.defaultServiceSupplier = defaultServiceSupplier;
    // 缓存 Spring 上下文，便于后续通过 SpringUtil 获取 Bean
    SpringUtil.setContext(applicationContext);
    this.environment = applicationContext.getEnvironment();
  }

  @SuppressWarnings("unchecked")
  protected AbstractMqttTenantLineRunner(
      AbstractTenantProperties<P> profile,
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

  /**
   * 根据多租户模式初始化 MQTT 客户端。
   *
   * <p>未启用或未配置租户连接信息时跳过初始化。启用后根据 {@code mqtt.tenant-mode}
   * 决定是逐租户建连（PER_TENANT_CONNECTION）还是共享单连接（SHARED_CONNECTION）。</p>
   *
   * @param listenerList 已扫描到的监听器实例列表
   * @throws Exception 客户端初始化过程中抛出的异常
   */
  @Override
  public void init(List<Object> listenerList) throws Exception {
    // 模块未启用时直接跳过，避免无意义的连接创建
    if (!profile.getEnabled()) {
      logger.info("MQTT 未启用，跳过客户端初始化");
      return;
    }
    Map<String, P> tenantProfileMap = profile.getTenant();
    if (ObjectUtil.isEmpty(tenantProfileMap)) {
      logger.warn("未配置 MQTT 租户连接信息，跳过客户端初始化");
      return;
    }
    S initService = resolveInitService();
    MqttTenantMode tenantMode = resolveTenantMode();
    logger.info("MQTT 多租户模式: {}", tenantMode);
    if (tenantMode == MqttTenantMode.SHARED_CONNECTION) {
      // 共享连接模式：只创建一个客户端，所有监听器在该连接上订阅一次
      initSharedClient(tenantProfileMap, listenerList, initService);
    } else {
      // 默认逐租户模式：为每个租户创建独立连接
      initPerTenantClients(tenantProfileMap, listenerList, initService);
    }
  }

  /**
   * 共享连接模式初始化：选取一个租户配置创建单一 MQTT 客户端。
   *
   * <p>选取优先级：名为 {@code default} 的租户配置 > Map 中的第一个条目。
   * 选取后使用 {@link #SHARED_TENANT_CODE} 作为逻辑租户编码注册客户端，
   * 所有监听器在消费时依据消息体中的 {@code tenantCode} 字段完成租户上下文绑定。</p>
   *
   * @param tenantProfileMap 租户编码到租户配置的映射
   * @param listenerList     监听器实例列表
   * @param initService      MQTT 初始化服务
   * @throws Exception 客户端初始化过程中抛出的异常
   */
  protected void initSharedClient(Map<String, P> tenantProfileMap, List<Object> listenerList,
      S initService) throws Exception {
    // 优先使用名为 default 的租户配置，其次取 Map 第一个条目，保证总有可用配置
    P sharedProfile = tenantProfileMap.get(DEFAULT_TENANT_KEY);
    if (sharedProfile == null) {
      Map.Entry<String, P> firstEntry = tenantProfileMap.entrySet().iterator().next();
      sharedProfile = firstEntry.getValue();
      logger.info("未配置 default 租户，共享连接使用首个租户配置: {}", firstEntry.getKey());
    }
    logger.info("共享连接模式：创建单一 MQTT 客户端，逻辑租户编码={}", SHARED_TENANT_CODE);
    initService.initMqttClient(SHARED_TENANT_CODE, sharedProfile, listenerList);
  }

  /**
   * 逐租户连接模式初始化：遍历所有租户配置，为每个租户创建独立 MQTT 客户端。
   *
   * @param tenantProfileMap 租户编码到租户配置的映射
   * @param listenerList     监听器实例列表
   * @param initService      MQTT 初始化服务
   * @throws Exception 客户端初始化过程中抛出的异常
   */
  protected void initPerTenantClients(Map<String, P> tenantProfileMap, List<Object> listenerList,
      S initService) throws Exception {
    for (Map.Entry<String, P> entry : tenantProfileMap.entrySet()) {
      initService.initMqttClient(entry.getKey(), entry.getValue(), listenerList);
    }
  }

  /**
   * 解析多租户模式配置项。
   *
   * <p>读取 {@code mqtt.tenant-mode}，未配置或为空时返回默认值
   * {@link MqttTenantMode#PER_TENANT_CONNECTION}，保证与历史部署行为一致。</p>
   *
   * @return 解析得到的多租户模式枚举
   */
  protected MqttTenantMode resolveTenantMode() {
    String value = environment.getProperty(TENANT_MODE_PROPERTY);
    if (StrUtil.isBlank(value)) {
      return MqttTenantMode.PER_TENANT_CONNECTION;
    }
    try {
      return MqttTenantMode.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      logger.warn("MQTT 多租户模式配置值 [{}] 无法识别，已回退为默认模式 PER_TENANT_CONNECTION", value);
      return MqttTenantMode.PER_TENANT_CONNECTION;
    }
  }

  /**
   * 解析 MQTT 初始化服务。
   *
   * <p>优先使用业务侧在 Spring 容器中自定义的实现；不存在时使用默认提供者创建。</p>
   *
   * @return MQTT 初始化服务实例
   */
  @SuppressWarnings("unchecked")
  private S resolveInitService() {
    Map<String, ? extends MqttClientInitService> serviceMap = SpringUtil.getBeansOfType(serviceType);
    return ObjectUtil.isNotEmpty(serviceMap)
        ? (S) serviceMap.values().iterator().next()
        : defaultServiceSupplier.get();
  }
}
