package com.steven.solomon.mqtt;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.pojo.entity.BaseMq;
import com.steven.solomon.verification.ValidateUtils;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * 通用消息监听器。
 *
 * <p>MQTT、Redis、RabbitMQ 等消息组件共用这套消费模板：先把原始 JSON 转成消息模型，
 * 再把 body 转成真实业务泛型，最后交给具体消费者处理。</p>
 */
public interface CommonMqttMessageListener<T, R, M extends BaseMq<T>> {

  /**
   * 处理业务消息体。
   *
   * @param body 已转换成业务类型的消息体
   * @return 消费结果，用于保存日志或后续处理
   */
  R handleMessage(T body) throws Exception;

  /**
   * 保存消费成功或失败的日志。
   *
   * @param result 消费结果；失败时可能为空
   * @param throwable 消费异常；成功时为空
   * @param model 原始消息模型
   */
  void saveLog(R result, Throwable throwable, M model);

  /**
   * 判断消息是否已经消费过。
   *
   * @return true 表示重复消费，false 表示可以继续消费
   */
  default boolean checkMessageKey(M model) {
    return false;
  }

  /**
   * 删除幂等校验 Key，通常用于消费失败后释放锁或标记。
   */
  default void deleteCheckMessageKey(M model) {}

  /**
   * 将原始 JSON 转为消息模型。
   *
   * <p>兼容两种消息格式：一种是完整消息模型包含 body，另一种是 JSON 本身就是业务 body。
   * 转换逻辑集中在这里，避免各消息中间件重复解析泛型和 JSON。</p>
   */
  default M conversion(String json) {
    M model = JSONUtil.toBean(json, getParameterizedType("M"), true);
    T body = resolveBody(json, model.getBody());
    model.setBody(body);
    return model;
  }

  /**
   * 解析业务 body。
   *
   * <p>Hutool 反序列化泛型字段时，body 可能先落成 JSONObject 或 JSONArray。
   * 遇到这种情况需要再按 T 泛型做一次强类型转换。</p>
   */
  @SuppressWarnings("unchecked")
  default T resolveBody(String json, T body) {
    if (ValidateUtils.isEmpty(body)) {
      return JSONUtil.toBean(json, getParameterizedType("T"), true);
    }
    if (body instanceof JSONObject || body instanceof JSONArray) {
      Type bodyType = TypeUtil.getTypeArgument(getClass(), 0);
      return JSONUtil.toBean(JSONUtil.toJsonStr(body), bodyType, true);
    }
    return body;
  }

  /**
   * 根据泛型变量名称获取真实类型。
   *
   * <p>例如传入 {@code T} 获取业务消息体类型，传入 {@code M} 获取消息模型类型。</p>
   */
  default Type getParameterizedType(String typeName) {
    Map<Type, Type> typeMap = TypeUtil.getTypeMap(getClass());
    for (Map.Entry<Type, Type> entry : typeMap.entrySet()) {
      if (StrUtil.equalsAnyIgnoreCase(typeName, entry.getKey().getTypeName())) {
        return entry.getValue();
      }
    }
    throw new IllegalStateException("未找到消息监听器泛型类型: " + typeName);
  }
}
