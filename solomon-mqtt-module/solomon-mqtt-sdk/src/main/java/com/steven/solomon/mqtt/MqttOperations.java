package com.steven.solomon.mqtt;

import com.steven.solomon.mqtt.model.MqttMessageModel;
import java.util.concurrent.CompletableFuture;

/**
 * MQTT 客户端统一操作接口。
 */
public interface MqttOperations {

  /**
   * 发送普通消息。
   */
  void send(MqttMessageModel<?> data) throws Exception;

  /**
   * 异步发送普通消息。
   *
   * <p>支持异步客户端的实现会直接绑定底层客户端发送回调；不支持异步回调的实现可以自行降级处理。</p>
   */
  default CompletableFuture<Void> sendAsync(MqttMessageModel<?> data) {
    prepareMessage(data);
    return CompletableFuture.runAsync(() -> {
      try {
        send(data);
      } catch (Exception e) {
        throw new IllegalStateException("MQTT 异步发送失败", e);
      }
    });
  }

  /**
   * 发送前补充当前请求租户，显式指定的租户编码保持不变。
   */
  default void prepareMessage(MqttMessageModel<?> data) {
    data.inheritTenantCode();
  }

  /**
   * 订阅主题。
   */
  void subscribe(String tenantCode, String topic, int qos, Object consumer) throws Exception;

  /**
   * 取消订阅主题。
   */
  void unsubscribe(String tenantCode, String[] topics) throws Exception;

  /**
   * 断开租户连接。
   */
  void disconnect(String tenantCode) throws Exception;

  /**
   * 重连租户连接。
   */
  void reconnect(String tenantCode) throws Exception;
}
