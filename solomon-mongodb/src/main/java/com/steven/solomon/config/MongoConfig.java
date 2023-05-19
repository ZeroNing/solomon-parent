package com.steven.solomon.config;

import com.steven.solomon.converter.DateToLocalDateTimeConverter;
import com.steven.solomon.converter.LocalDateTimeToDateConverter;
import com.steven.solomon.init.MongoInitUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.properties.TenantMongoProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.template.DynamicMongoTemplate;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.stereotype.Component;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(value={MongoProperties.class,TenantMongoProperties.class})
@Import(value = {MongoTenantsContext.class})
@Order(2)
public class MongoConfig {

  private Logger logger = LoggerUtils.logger(getClass());

  private final TenantMongoProperties mongoProperties;

  private final MongoTenantsContext context;

  private final MongoProperties properties;

  private boolean isSwitchDb = false;

  private final ApplicationContext applicationContext;

  public MongoConfig(TenantMongoProperties mongoProperties, MongoTenantsContext context,
      MongoProperties properties, ApplicationContext applicationContext) {
    this.mongoProperties = mongoProperties;
    this.context         = context;
    this.properties      = properties;
    this.isSwitchDb = ValidateUtils.equalsIgnoreCase(SwitchModeEnum.SWITCH_DB.toString(), mongoProperties.getMode().toString());
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void afterPropertiesSet() {
    logger.info("mongoDb当前模式为:{}",mongoProperties.getMode().getDesc());
    SpringUtil.setContext(applicationContext);
    if (isSwitchDb) {
      Map<String, MongoProperties> tenantMap = ValidateUtils.isEmpty(mongoProperties.getTenant()) ? new HashMap<>() : mongoProperties.getTenant();
      if(!tenantMap.containsKey("default")){
        tenantMap.put("default", properties);
        mongoProperties.setTenant(tenantMap);
      }
      MongoInitUtils.init(mongoProperties.getTenant(), context);
    } else {
      SimpleMongoClientDatabaseFactory factory = MongoInitUtils.initFactory(properties);
      MongoInitUtils.initDocument(factory);
      context.setFactory("default",factory);
    }
  }

  @Bean(name = "mongoTemplate")
  public MongoTemplate dynamicMongoTemplate() {
    SimpleMongoClientDatabaseFactory factory          = context.getFactoryMap().values().iterator().next();
    DbRefResolver                    dbRefResolver    = new DefaultDbRefResolver(factory);
    MappingMongoConverter            mappingConverter = new MappingMongoConverter(dbRefResolver, new MongoMappingContext());

    List<Object> list = new ArrayList<>();
    list.add(new LocalDateTimeToDateConverter());
    list.add(new DateToLocalDateTimeConverter());
    mappingConverter.setCustomConversions(new MongoCustomConversions(list));
    mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    return isSwitchDb ? new DynamicMongoTemplate(factory, mappingConverter) : new MongoTemplate(factory, mappingConverter);
  }

  @Bean(name = "mongoDbFactory")
  public MongoDatabaseFactory tenantMongoDbFactory() {
    return context.getFactoryMap().values().iterator().next();
  }
}
