package com.steven.solomon.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterType;
import com.steven.solomon.condition.MongoDbCondition;
import com.steven.solomon.condition.TenantMongoDbCondition;
import com.steven.solomon.converter.DateToLocalDateTimeConverter;
import com.steven.solomon.converter.LocalDateTimeToDateConverter;
import com.steven.solomon.enums.SwitchModeEnum;
import com.steven.solomon.init.MongoInitUtils;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.properties.TenantMongoProperties;
import com.steven.solomon.template.DynamicMongoTemplate;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
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

@Component
@Order(2)
@DependsOn({"springUtil"})
public class MongoConfig {

  private Logger logger = LoggerUtils.logger(getClass());

  @Resource
  private TenantMongoProperties mongoProperties;

  @Resource
  private MongoTenantsContext context;

  private MongoDatabaseFactory factory;

  @Resource
  private MongoProperties properties;

  @PostConstruct
  public void afterPropertiesSet() {
    if (ValidateUtils.equalsIgnoreCase(SwitchModeEnum.SWITCH_DB.toString(), mongoProperties.getMode())) {
      MongoInitUtils.init(mongoProperties.getTenant(), context);
    } else {
      SimpleMongoClientDatabaseFactory factory = MongoInitUtils.initFactory(properties);
      MongoInitUtils.initDocument(factory);
      this.factory = factory;
    }
  }

  @Bean(name = "mongoTemplate")
  @Conditional(value = TenantMongoDbCondition.class)
  public DynamicMongoTemplate dynamicMongoTemplate(MongoMappingContext mappingContext) {
    SimpleMongoClientDatabaseFactory factory          = context.getFactoryMap().values().iterator().next();
    DbRefResolver                    dbRefResolver    = new DefaultDbRefResolver(factory);
    MappingMongoConverter            mappingConverter = new MappingMongoConverter(dbRefResolver, mappingContext);

    List<Object> list = new ArrayList<>();
    list.add(new LocalDateTimeToDateConverter());
    list.add(new DateToLocalDateTimeConverter());
    mappingConverter.setCustomConversions(new MongoCustomConversions(list));
    mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    return new DynamicMongoTemplate(factory, mappingConverter);
  }

  @Bean(name = "mongoDbFactory")
  @Conditional(value = TenantMongoDbCondition.class)
  public MongoDatabaseFactory tenantMongoDbFactory() {
    return context.getFactoryMap().values().iterator().next();
  }

  @Bean(name = "mongoTemplate")
  @Conditional(value = MongoDbCondition.class)
  public MongoTemplate mongoTemplate(MongoMappingContext mappingContext,MongoDatabaseFactory factory) {
    DbRefResolver                    dbRefResolver    = new DefaultDbRefResolver(factory);
    MappingMongoConverter            mappingConverter = new MappingMongoConverter(dbRefResolver, mappingContext);

    List<Object> list = new ArrayList<>();
    list.add(new LocalDateTimeToDateConverter());
    list.add(new DateToLocalDateTimeConverter());
    mappingConverter.setCustomConversions(new MongoCustomConversions(list));
    mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    return new MongoTemplate(factory, mappingConverter);
  }

  @Bean(name = "mongoDbFactory")
  @Conditional(value = MongoDbCondition.class)
  public MongoDatabaseFactory mongoDbFactory() {
    return this.factory;
  }
}
