package com.steven.solomon.config;

import com.steven.solomon.converter.DateToLocalDateTimeConverter;
import com.steven.solomon.converter.LocalDateTimeToDateConverter;
import com.steven.solomon.enums.SwitchModeEnum;
import com.steven.solomon.init.MongoInitUtils;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.properties.TenantMongoProperties;
import com.steven.solomon.template.DynamicMongoTemplate;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
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

  private final TenantMongoProperties mongoProperties;

  private final MongoTenantsContext context;

  private final MongoProperties properties;

  private boolean isSwitchDb = false;

  public MongoConfig(TenantMongoProperties mongoProperties, MongoTenantsContext context,
      MongoProperties properties) {
    this.mongoProperties = mongoProperties;
    this.context         = context;
    this.properties      = properties;
    this.isSwitchDb = ValidateUtils.equalsIgnoreCase(SwitchModeEnum.SWITCH_DB.toString(), mongoProperties.getMode());
  }

  @PostConstruct
  public void afterPropertiesSet() {
    if (isSwitchDb) {
      MongoInitUtils.init(mongoProperties.getTenant(), context);
    } else {
      SimpleMongoClientDatabaseFactory factory = MongoInitUtils.initFactory(properties);
      MongoInitUtils.initDocument(factory);
      context.setFactory("default",factory);
    }
  }

  @Bean(name = "mongoTemplate")
  public MongoTemplate dynamicMongoTemplate(MongoMappingContext mappingContext) {
    SimpleMongoClientDatabaseFactory factory          = context.getFactoryMap().values().iterator().next();
    DbRefResolver                    dbRefResolver    = new DefaultDbRefResolver(factory);
    MappingMongoConverter            mappingConverter = new MappingMongoConverter(dbRefResolver, mappingContext);

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
