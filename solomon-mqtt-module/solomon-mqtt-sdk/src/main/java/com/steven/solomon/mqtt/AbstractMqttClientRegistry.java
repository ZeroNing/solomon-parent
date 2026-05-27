package com.steven.solomon.mqtt;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.verification.ValidateUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQTT 客户端注册表。
 *
 * <p>统一维护租户与客户端、连接参数之间的映射，避免每个 MQTT 实现重复写 Map 管理代码。</p>
 *
 * @param <C> 客户端类型
 * @param <O> 连接参数类型
 */
public abstract class AbstractMqttClientRegistry<C, O> {

  private final Map<String, C> clientMap = new ConcurrentHashMap<>();

  private final Map<String, O> optionsMap = new ConcurrentHashMap<>();

  public Map<String, C> getClientMap() {
    return clientMap;
  }

  public void putClient(String tenantCode, C client) {
    clientMap.put(tenantCode, client);
  }

  public C removeClient(String tenantCode) {
    optionsMap.remove(tenantCode);
    return clientMap.remove(tenantCode);
  }

  public boolean containsClient(String tenantCode) {
    return clientMap.containsKey(tenantCode);
  }

  public Map<String, O> getOptionsMap() {
    return optionsMap;
  }

  public void putOptionsMap(String tenantCode, O options) {
    optionsMap.put(tenantCode, options);
  }

  public O getOptions(String tenantCode) {
    return optionsMap.get(tenantCode);
  }

  public void clearRegistry() {
    clientMap.clear();
    optionsMap.clear();
  }

  /**
   * 获取指定租户客户端，不存在时抛出业务异常。
   */
  protected C getRequiredClient(String tenantCode, String errorCode) throws BaseException {
    C client = clientMap.get(tenantCode);
    if (ValidateUtils.isEmpty(client)) {
      throw new BaseException(errorCode, tenantCode);
    }
    return client;
  }
}
