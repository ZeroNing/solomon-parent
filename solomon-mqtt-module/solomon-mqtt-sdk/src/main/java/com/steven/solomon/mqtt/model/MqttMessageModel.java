package com.steven.solomon.mqtt.model;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.mq.model.BaseMq;

/**
 * MQTT 消息基础模型，统一维护各 MQTT 实现共用的主题、QoS 和保留消息标记。
 *
 * @param <T> 消息体类型
 */
public class MqttMessageModel<T> extends BaseMq<T> {

  /**
   * MQTT 主题。
   */
  private String topic;

  /**
   * 是否发送为 retained 消息。
   */
  private boolean retained;

  /**
   * MQTT 消息质量等级。
   */
  private int qos;

  public MqttMessageModel() {
    super();
  }

  public MqttMessageModel(String tenantCode) {
    super();
    setTenantCode(tenantCode);
  }

  public MqttMessageModel(String tenantCode, String topic, T body) {
    super(body);
    this.topic = topic;
    setTenantCode(tenantCode);
  }

  public int getQos() {
    return qos;
  }

  public void setQos(int qos) {
    this.qos = qos;
  }

  public boolean getRetained() {
    return retained;
  }

  public void setRetained(boolean retained) {
    this.retained = retained;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  /**
   * 获取消息租户，未显式设置时自动继承当前上下文。
   */
  @Override
  public String getTenantCode() {
    inheritTenantCode();
    return super.getTenantCode();
  }

  /**
   * 消息未指定租户时继承当前上下文租户。
   *
   * <p>显式设置的租户编码优先，适用于跨租户管理场景。</p>
   */
  public void inheritTenantCode() {
    if (StrUtil.isBlank(super.getTenantCode())) {
      setTenantCode(RequestHeaderHolder.getTenantCode());
    }
  }
}
