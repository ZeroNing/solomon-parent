package com.steven.solomon.utils;

import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.util.List;
import java.util.Properties;

/**
 * Nacos Sentinel 规则动态管理工具类
 * 支持将Sentinel的限流、熔断、热点、系统规则持久化到Nacos配置中心
 * 支持动态发布规则，实时生效，无需重启服务
 *
 * @author steven
 * @since 1.0.0
 */
public class NacosSentinelWritableUtils {
  
  /**
   * Nacos服务地址
   */
  private String url;
  
  /**
   * Nacos用户名
   */
  private String userName;
  
  /**
   * Nacos密码
   */
  private String password;
  
  /**
   * Nacos命名空间
   */
  private String namespace;

  /**
   * Nacos配置服务客户端
   */
  private ConfigService configService;

  /**
   * 获取Nacos配置服务客户端
   * 根据配置参数创建ConfigService实例
   *
   * @return Nacos配置服务客户端
   * @throws NacosException 创建客户端失败时抛出
   */
  private ConfigService getConfigService() throws NacosException {
    Properties properties = new Properties();
    // Nacos服务地址
    properties.setProperty(PropertyKeyConst.SERVER_ADDR, url);
    // 用户名（可选）
    if (ValidateUtils.isNotEmpty(userName)) {
      properties.setProperty(PropertyKeyConst.USERNAME, userName);
    }
    // 密码（可选）
    if (ValidateUtils.isNotEmpty(password)) {
      properties.setProperty(PropertyKeyConst.PASSWORD, password);
    }
    // 命名空间（可选）
    if (ValidateUtils.isNotEmpty(namespace)) {
      properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
    }
    return ConfigFactory.createConfigService(properties);
  }
  
  /**
   * 构造方法，初始化Nacos客户端
   *
   * @param url Nacos服务地址
   * @param userName Nacos用户名
   * @param password Nacos密码
   * @param namespace Nacos命名空间
   * @throws NacosException 创建客户端失败时抛出
   */
  public NacosSentinelWritableUtils(String url, String userName, String password, String namespace) throws NacosException {
    this.url = url;
    this.userName = userName;
    this.password = password;
    this.namespace = namespace;
    this.configService = getConfigService();
  }

  /**
   * 发布Sentinel流量控制规则到Nacos
   * 规则会实时同步到所有Sentinel客户端，立即生效
   *
   * @param dataId Nacos配置的dataId
   * @param groupId Nacos配置的groupId
   * @param obj 流量规则列表（全量覆盖）
   * @param type 配置文件类型（JSON/YAML等）
   * @throws Exception 发布配置失败时抛出
   */
  public void publishSentinelFlowRule(String dataId, String groupId, List<FlowRule> obj, ConfigType type) throws Exception {
    // 将规则序列化为JSON并发布到Nacos
    configService.publishConfig(dataId, groupId, JackJsonUtils.formatJsonByFilter(obj), type.getType());
    
    // 创建Nacos数据源并注册到Sentinel规则管理器，实现动态更新
    SentinelProperty<List<FlowRule>> sentinelProperty = getSentinelFlowRuleDataSource(groupId, dataId).getProperty();
    FlowRuleManager.register2Property(sentinelProperty);
  }

  /**
   * 发布Sentinel熔断降级规则到Nacos
   * 规则会实时同步到所有Sentinel客户端，立即生效
   *
   * @param dataId Nacos配置的dataId
   * @param groupId Nacos配置的groupId
   * @param obj 熔断规则列表（全量覆盖）
   * @param type 配置文件类型（JSON/YAML等）
   * @throws Exception 发布配置失败时抛出
   */
  public void publishSentinelDegradeRule(String dataId, String groupId, List<DegradeRule> obj, ConfigType type) throws Exception {
    // 将规则序列化为JSON并发布到Nacos
    configService.publishConfig(dataId, groupId, JackJsonUtils.formatJsonByFilter(obj), type.getType());
    
    // 创建Nacos数据源并注册到Sentinel规则管理器，实现动态更新
    SentinelProperty<List<DegradeRule>> sentinelProperty = getSentinelDegradeRuleDataSource(groupId, dataId).getProperty();
    DegradeRuleManager.register2Property(sentinelProperty);
  }

  /**
   * 发布Sentinel热点参数限流规则到Nacos
   * 规则会实时同步到所有Sentinel客户端，立即生效
   *
   * @param dataId Nacos配置的dataId
   * @param groupId Nacos配置的groupId
   * @param obj 热点规则列表（全量覆盖）
   * @param type 配置文件类型（JSON/YAML等）
   * @throws Exception 发布配置失败时抛出
   */
  public void publishSentinelParamFlowRule(String dataId, String groupId, List<ParamFlowRule> obj, ConfigType type) throws Exception {
    // 将规则序列化为JSON并发布到Nacos
    configService.publishConfig(dataId, groupId, JackJsonUtils.formatJsonByFilter(obj), type.getType());
    
    // 创建Nacos数据源并注册到Sentinel规则管理器，实现动态更新
    SentinelProperty<List<ParamFlowRule>> sentinelProperty = getSentinelParamFlowRuleDataSource(groupId, dataId).getProperty();
    ParamFlowRuleManager.register2Property(sentinelProperty);
  }

  /**
   * 发布Sentinel系统保护规则到Nacos
   * 规则会实时同步到所有Sentinel客户端，立即生效
   *
   * @param dataId Nacos配置的dataId
   * @param groupId Nacos配置的groupId
   * @param obj 系统规则列表（全量覆盖）
   * @param type 配置文件类型（JSON/YAML等）
   * @throws Exception 发布配置失败时抛出
   */
  public void publishSentinelSystemRule(String dataId, String groupId, List<SystemRule> obj, ConfigType type) throws Exception {
    // 将规则序列化为JSON并发布到Nacos
    configService.publishConfig(dataId, groupId, JackJsonUtils.formatJsonByFilter(obj), type.getType());
    
    // 创建Nacos数据源并注册到Sentinel规则管理器，实现动态更新
    SentinelProperty<List<SystemRule>> sentinelProperty = getSentinelSystemRuleDataSource(groupId, dataId).getProperty();
    SystemRuleManager.register2Property(sentinelProperty);
  }

  /**
   * 获取Sentinel流量规则的Nacos数据源
   * 用于监听Nacos配置变化，动态更新规则
   *
   * @param groupId Nacos配置的groupId
   * @param dataId Nacos配置的dataId
   * @return Nacos数据源实例
   * @throws Exception 创建数据源失败时抛出
   */
  public NacosDataSource getSentinelFlowRuleDataSource(String groupId, String dataId) throws Exception {
    return new NacosDataSource<>(url, groupId, dataId, source -> JSON
        .parseObject(source, new TypeReference<List<FlowRule>>() {}));
  }

  /**
   * 从Nacos加载Sentinel流量规则
   *
   * @param groupId Nacos配置的groupId
   * @param dataId Nacos配置的dataId
   * @return 流量规则列表
   * @throws Exception 加载配置失败时抛出
   */
  public List<FlowRule> getSentinelFlowRule(String groupId, String dataId) throws Exception {
    return (List<FlowRule>) getSentinelFlowRuleDataSource(groupId, dataId).loadConfig();
  }

  /**
   * 获取Sentinel熔断规则的Nacos数据源
   * 用于监听Nacos配置变化，动态更新规则
   *
   * @param groupId Nacos配置的groupId
   * @param dataId Nacos配置的dataId
   * @return Nacos数据源实例
   * @throws Exception 创建数据源失败时抛出
   */
  public NacosDataSource getSentinelDegradeRuleDataSource(String groupId, String dataId) throws Exception {
    return new NacosDataSource<>(url, groupId, dataId, source -> JSON
        .parseObject(source, new TypeReference<List<DegradeRule>>() {}));
  }

  /**
   * 从Nacos加载Sentinel熔断规则
   *
   * @param groupId Nacos配置的groupId
   * @param dataId Nacos配置的dataId
   * @return 熔断规则列表
   * @throws Exception 加载配置失败时抛出
   */
  public List<DegradeRule> getSentinelDegradeRule(String groupId, String dataId) throws Exception {
    return (List<DegradeRule>) getSentinelDegradeRuleDataSource(groupId, dataId).loadConfig();
  }

  /**
   * 获取Sentinel热点参数限流规则的Nacos数据源
   * 用于监听Nacos配置变化，动态更新规则
   *
   * @param groupId Nacos配置的groupId
   * @param dataId Nacos配置的dataId
   * @return Nacos数据源实例
   * @throws Exception 创建数据源失败时抛出
   */
  public NacosDataSource getSentinelParamFlowRuleDataSource(String groupId, String dataId) throws Exception {
    return new NacosDataSource<>(url, groupId, dataId, source -> JSON
        .parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
  }

  /**
   * 从Nacos加载Sentinel热点参数限流规则
   *
   * @param groupId Nacos配置的groupId
   * @param dataId Nacos配置的dataId
   * @return 热点规则列表
   * @throws Exception 加载配置失败时抛出
   */
  public List<ParamFlowRule> getSentinelParamFlowRule(String groupId, String dataId) throws Exception {
    return (List<ParamFlowRule>) getSentinelParamFlowRuleDataSource(groupId, dataId).loadConfig();
  }

  /**
   * 获取Sentinel系统保护规则的Nacos数据源
   * 用于监听Nacos配置变化，动态更新规则
   *
   * @param groupId Nacos配置的groupId
   * @param dataId Nacos配置的dataId
   * @return Nacos数据源实例
   * @throws Exception 创建数据源失败时抛出
   */
  public NacosDataSource getSentinelSystemRuleDataSource(String groupId, String dataId) throws Exception {
    return new NacosDataSource<>(url, groupId, dataId, source -> JSON
        .parseObject(source, new TypeReference<List<SystemRule>>() {}));
  }

  /**
   * 从Nacos加载Sentinel系统保护规则
   *
   * @param groupId Nacos配置的groupId
   * @param dataId Nacos配置的dataId
   * @return 系统规则列表
   * @throws Exception 加载配置失败时抛出
   */
  public List<SystemRule> getSentinelSystemRule(String groupId, String dataId) throws Exception {
    return (List<SystemRule>) getSentinelSystemRuleDataSource(groupId, dataId).loadConfig();
  }
}
