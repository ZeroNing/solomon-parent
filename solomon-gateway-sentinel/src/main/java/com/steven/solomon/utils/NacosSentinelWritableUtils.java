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
import org.springframework.stereotype.Component;

public class NacosSentinelWritableUtils {
  
  private String url;
  
  private String userName;
  
  private String password;
  
  private String namespace;

  private ConfigService configService;

  private ConfigService getConfigService() throws NacosException {
    Properties properties = new Properties();
    properties.setProperty(PropertyKeyConst.SERVER_ADDR,url);
    if(ValidateUtils.isNotEmpty(userName)){
      properties.setProperty(PropertyKeyConst.USERNAME,userName);
    }
    if(ValidateUtils.isNotEmpty(password)){
      properties.setProperty(PropertyKeyConst.PASSWORD,password);
    }
    if(ValidateUtils.isNotEmpty(namespace)){
      properties.setProperty(PropertyKeyConst.NAMESPACE,namespace);
    }
    return ConfigFactory.createConfigService(properties);
  }
  
  public NacosSentinelWritableUtils(String url,String userName,String password,String namespace) throws NacosException {
    this.url = url;
    this.userName = userName;
    this.password = password;
    this.namespace = namespace;
    this.configService = getConfigService();
  }

  /**
   * ??????sentinel????????????
   * @param dataId nacos??????dataId
   * @param groupId nacos??????groupId
   * @param obj ?????????????????????????????????
   * @param type nacos????????????
   * @return
   * @throws Exception
   */
  public void publishSentinelFlowRule(String dataId,String groupId,List<FlowRule> obj, ConfigType type) throws Exception {

    configService.publishConfig(dataId,groupId, JackJsonUtils.formatJsonByFilter(obj),type.getType());

    SentinelProperty<List<FlowRule>> sentinelProperty = getSentinelFlowRuleDataSource(groupId,dataId).getProperty();
    FlowRuleManager.register2Property(sentinelProperty);
  }

  /**
   * ??????sentinel????????????
   * @param dataId nacos??????dataId
   * @param groupId nacos??????groupId
   * @param obj ?????????????????????????????????
   * @param type nacos????????????
   * @return
   * @throws Exception
   */
  public void publishSentinelDegradeRule(String dataId,String groupId,List<DegradeRule> obj, ConfigType type) throws Exception {

    configService.publishConfig(dataId,groupId, JackJsonUtils.formatJsonByFilter(obj),type.getType());

    SentinelProperty<List<DegradeRule>> sentinelProperty = getSentinelDegradeRuleDataSource(groupId,dataId).getProperty();
    DegradeRuleManager.register2Property(sentinelProperty);
  }

  /**
   * ??????sentinel????????????
   * @param dataId nacos??????dataId
   * @param groupId nacos??????groupId
   * @param obj ?????????????????????????????????
   * @param type nacos????????????
   * @return
   * @throws Exception
   */
  public void publishSentinelParamFlowRule(String dataId,String groupId,List<ParamFlowRule> obj, ConfigType type) throws Exception {

    configService.publishConfig(dataId,groupId, JackJsonUtils.formatJsonByFilter(obj),type.getType());

    SentinelProperty<List<ParamFlowRule>> sentinelProperty = getSentinelParamFlowRuleDataSource(groupId,dataId).getProperty();
    ParamFlowRuleManager.register2Property(sentinelProperty);
  }

  /**
   * ??????sentinel????????????
   * @param dataId nacos??????dataId
   * @param groupId nacos??????groupId
   * @param obj ?????????????????????????????????
   * @param type nacos????????????
   * @return
   * @throws Exception
   */
  public void publishSentinelSystemRule(String dataId,String groupId,List<SystemRule> obj, ConfigType type) throws Exception {

    configService.publishConfig(dataId,groupId, JackJsonUtils.formatJsonByFilter(obj),type.getType());

    SentinelProperty<List<SystemRule>> sentinelProperty = getSentinelSystemRuleDataSource(groupId,dataId).getProperty();
    SystemRuleManager.register2Property(sentinelProperty);
  }

  /**
   * ??????sentinel??????
   */
  public NacosDataSource getSentinelFlowRuleDataSource(String groupId,String dataId) throws Exception {
    return new NacosDataSource<>(url, groupId, dataId, source -> JSON
        .parseObject(source, new TypeReference<List<FlowRule>>() {}));
  }

  /**
   * ??????sentinel??????
   */
  public List<FlowRule> getSentinelFlowRule(String groupId,String dataId) throws Exception {
    return (List<FlowRule>) getSentinelFlowRuleDataSource(groupId,dataId).loadConfig();
  }

  /**
   * ??????sentinel??????
   */
  public NacosDataSource getSentinelDegradeRuleDataSource(String groupId,String dataId) throws Exception {
    return new NacosDataSource<>(url, groupId, dataId, source -> JSON
        .parseObject(source, new TypeReference<List<DegradeRule>>() {}));
  }

  /**
   * ??????sentinel??????
   */
  public List<DegradeRule> getSentinelDegradeRule(String groupId,String dataId) throws Exception {
    return (List<DegradeRule>) getSentinelDegradeRuleDataSource(groupId,dataId).loadConfig();
  }

  /**
   * ??????sentinel??????
   */
  public NacosDataSource getSentinelParamFlowRuleDataSource(String groupId,String dataId) throws Exception {
    return new NacosDataSource<>(url, groupId, dataId, source -> JSON
        .parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
  }

  /**
   * ??????sentinel??????
   */
  public List<ParamFlowRule> getSentinelParamFlowRule(String groupId,String dataId) throws Exception {
    return (List<ParamFlowRule>) getSentinelParamFlowRuleDataSource(groupId,dataId).loadConfig();
  }

  /**
   * ??????sentinel??????
   */
  public NacosDataSource getSentinelSystemRuleDataSource(String groupId,String dataId) throws Exception {
    return new NacosDataSource<>(url, groupId, dataId, source -> JSON
        .parseObject(source, new TypeReference<List<SystemRule>>() {}));
  }

  /**
   * ??????sentinel??????
   */
  public List<SystemRule> getSentinelSystemRule(String groupId,String dataId) throws Exception {
    return (List<SystemRule>) getSentinelSystemRuleDataSource(groupId,dataId).loadConfig();
  }
}
