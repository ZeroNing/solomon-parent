package com.steven.solomon.mqtt.enums;

/**
 * MQTT 多租户连接模式。
 *
 * <p>通过 {@code mqtt.tenant-mode} 配置项切换，决定 MQTT 客户端在多租户场景下的连接与路由策略。</p>
 *
 * <table border="1">
 *   <caption>两种模式对比</caption>
 *   <tr><th>模式</th><th>连接数</th><th>路由方式</th><th>适用场景</th></tr>
 *   <tr>
 *     <td>{@link #PER_TENANT_CONNECTION}</td>
 *     <td>每个租户一个独立连接</td>
 *     <td>连接级隔离</td>
 *     <td>各租户使用不同 Broker，需要物理隔离</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #SHARED_CONNECTION}</td>
 *     <td>所有租户共用一个连接</td>
 *     <td>消息体 tenantCode 路由</td>
 *     <td>所有租户共用同一 Broker，仅做逻辑隔离，节省连接资源</td>
 *   </tr>
 * </table>
 *
 * @author steven
 */
public enum MqttTenantMode {

  /**
   * 每租户独立连接模式（默认）。
   *
   * <p>遍历 {@code mqtt.tenant} 配置 Map，为每个租户创建独立的 MQTT 客户端连接、
   * 独立的连接参数和独立的订阅。租户之间在连接层面完全隔离，互不影响。</p>
   *
   * <p>适合各租户连接不同 Broker、或需要强物理隔离的场景。缺点是租户数量多时连接资源开销较大。</p>
   */
  PER_TENANT_CONNECTION,

  /**
   * 共享连接按租户编码路由模式。
   *
   * <p>只取 {@code mqtt.tenant} 中的第一个（或名为 {@code default} 的）租户配置创建单个 MQTT 连接，
   * 所有监听器在该共享连接上只订阅一次。消息到达后，由
   * {@code AbstractMessageConsumer} 根据消息体中的 {@code tenantCode} 字段
   * 在处理阶段绑定对应的租户上下文，实现逻辑层面的租户隔离。</p>
   *
   * <p>适合所有租户共用同一个 Broker、仅需逻辑隔离的场景，可显著节省连接资源。</p>
   */
  SHARED_CONNECTION
}
