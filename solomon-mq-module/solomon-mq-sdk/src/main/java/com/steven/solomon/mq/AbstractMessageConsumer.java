package com.steven.solomon.mq;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.context.TenantRequestBinder;
import com.steven.solomon.context.TenantResourceScope;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.mq.model.BaseMq;
import com.steven.solomon.utils.logger.LoggerUtils;
import cn.hutool.extra.spring.SpringUtil;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;

/**
 * 通用消息消费者模板。
 *
 * <p>统一处理消息反序列化、重复消费校验、租户上下文绑定和消费日志回调。
 * RabbitMQ、RocketMQ、Kafka、MQTT 等各 MQ 实现的消费者基类均继承本类，
 * 只需关注 {@link #handleMessage(Object)} 业务处理即可。</p>
 *
 * @param <T> 业务消息体类型
 * @param <R> 消费结果类型
 * @param <M> 消息模型类型，必须继承 {@link BaseMq}
 * @author steven
 */
public abstract class AbstractMessageConsumer<T, R, M extends BaseMq<T>>
    implements MessageListenerSpi<T, R, M> {

  /** 日志记录器，统一使用项目封装的 LoggerUtils 获取。 */
  protected final Logger logger = LoggerUtils.logger(getClass());

  /** 缓存租户模式解析器（单例无状态），避免每条消息重复 getBean。 */
  private volatile TenantModeResolver cachedTenantModeResolver;

  /** 当前消息所属的订阅主题。 */
  protected String topic;

  /** 当前消息解析出的租户编码。 */
  protected String tenantCode;

  /**
   * 返回当前实现使用的消息模型类型，用于 JSON 反序列化。
   */
  protected abstract Class<M> messageModelType();

  @Override
  public M conversion(String json) {
    M model = JSONUtil.toBean(json, messageModelType(), true);
    T body = resolveBody(json, model.getBody());
    model.setBody(body);
    return model;
  }

  /**
   * 消费原始字节消息的统一入口。
   *
   * <p>处理流程：字节转字符串 → 反序列化为消息模型 → 解析租户 → 幂等校验 →
   * 绑定租户上下文 → 执行业务处理 → 保存日志 → 清理上下文。</p>
   *
   * @param topic 订阅主题
   * @param payload 消息体字节
   */
  protected void consumeMessage(String topic, byte[] payload) throws Exception {
    this.topic = topic;

    String json = new String(payload, StandardCharsets.UTF_8);
    Throwable throwable = null;
    R result = null;
    M model = null;
    TenantResourceScope tenantResourceScope = null;
    try {
      model = conversion(json);
      // 从消息体解析有效租户编码（单租户回退默认值，多租户校验）
      // 使用缓存的解析器避免每条消息重复 getBean（TenantModeResolver 是无状态单例）
      if (cachedTenantModeResolver == null) {
        cachedTenantModeResolver = SpringUtil.getBean(TenantModeResolver.class);
      }
      tenantCode = cachedTenantModeResolver.resolve(model.getTenantCode());
      logger.info(
          "线程名:{}, 租户编码:{}, 消息ID:{}, topic主题:{}, 消息消费者消息:{}",
          Thread.currentThread().getName(),
          tenantCode,
          model.getMsgId(),
          topic,
          json);
      // 幂等校验：消息已消费过则直接抛异常
      if (checkMessageKey(model)) {
        throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
      }
      // 绑定租户资源上下文（数据源、Redis、Mongo 等按租户切换）
      if (ObjectUtil.isNotEmpty(tenantCode)) {
        RequestHeaderHolder.setTenantCode(tenantCode);
        tenantResourceScope = TenantResourceScope.open(tenantCode,
            SpringUtil.getBeansOfType(TenantRequestBinder.class).values());
      }
      // 交给子类处理具体业务
      result = handleMessage(model.getBody());
    } catch (Throwable e) {
      logger.error("消息消费失败, 消息:{}, 异常:", json, e);
      throwable = e;
    } finally {
      try {
        deleteCheckMessageKey(model);
        saveLog(result, throwable, model);
      } finally {
        try {
          // 无论成功失败，都需清理租户上下文，避免线程池复用导致租户串号
          if (tenantResourceScope != null) {
            tenantResourceScope.close();
          }
        } finally {
          RequestHeaderHolder.remove();
        }
      }
    }
  }
}
