package com.steven.solomon.config;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.converter.DateToLocalDateConverter;
import com.steven.solomon.converter.DateToLocalDateTimeConverter;
import com.steven.solomon.converter.LocalDateTimeToDateConverter;
import com.steven.solomon.converter.LocalDateToDateConverter;
import com.steven.solomon.init.AbstractDataSourceInitService;
import com.steven.solomon.init.DefaultMongoDbInitService;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.properties.TenantMongoProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.template.DynamicMongoTemplate;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;
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

@Configuration
@EnableConfigurationProperties(value={MongoProperties.class,TenantMongoProperties.class})
@Import(value = {MongoTenantContext.class, MongoAutoConfiguration.class})
@Order(2)
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
public class MongoConfig {

  private final Logger logger = LoggerUtils.logger(getClass());

  private final TenantMongoProperties mongoProperties;

  private final MongoTenantContext context;

  private final MongoProperties properties;

  private boolean isSwitchDb = false;

  public MongoConfig(TenantMongoProperties mongoProperties, MongoTenantContext context,
      MongoProperties properties, ApplicationContext applicationContext) {
    this.mongoProperties = mongoProperties;
    this.context         = context;
    this.properties      = properties;
    this.isSwitchDb = ValidateUtils.equalsIgnoreCase(SwitchModeEnum.SWITCH_DB.toString(), mongoProperties.getMode().toString());
    SpringUtil.setContext(applicationContext);
  }

  @PostConstruct
  public void afterPropertiesSet() throws Throwable {
    if(!mongoProperties.getEnabled()){
      logger.error("mongoDb不启用,不初始化队列以及消费者");
      return;
    }
    logger.info("mongoDb当前模式为:{}",mongoProperties.getMode().getDesc());
    AbstractDataSourceInitService<MongoProperties,MongoTenantContext,SimpleMongoClientDatabaseFactory> service = getService();
    if (isSwitchDb) {
      Map<String, MongoProperties> tenantMap = ValidateUtils.getOrDefault(mongoProperties.getTenant(),new HashMap<>());
      if(!tenantMap.containsKey(BaseCode.DEFAULT)){
        tenantMap.put(BaseCode.DEFAULT, properties);
        mongoProperties.setTenant(tenantMap);
      }
      service.init(mongoProperties.getTenant(),context);
    } else {
      service.init(BaseCode.DEFAULT,properties,context);
    }
  }

  @Bean(name = "mongoTemplate")
  @ConditionalOnMissingBean(MongoTemplate.class)
  @Conditional(MongoCondition.class)
  public MongoTemplate dynamicMongoTemplate() {
    SimpleMongoClientDatabaseFactory factory          = context.getFactoryMap().values().iterator().next();
    DbRefResolver                    dbRefResolver    = new DefaultDbRefResolver(factory);
    MappingMongoConverter            mappingConverter = new MappingMongoConverter(dbRefResolver, new MongoMappingContext());

    List<Object> list = new ArrayList<>();
    list.add(new LocalDateTimeToDateConverter());
    list.add(new DateToLocalDateTimeConverter());
    list.add(new LocalDateToDateConverter());
    list.add(new DateToLocalDateConverter());
    mappingConverter.setCustomConversions(new MongoCustomConversions(list));
    mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    return isSwitchDb ? new DynamicMongoTemplate(factory, mappingConverter) : new MongoTemplate(factory, mappingConverter);
  }

  @Bean(name = "mongoDbFactory")
  @ConditionalOnMissingBean(MongoDatabaseFactory.class)
  @Conditional(MongoCondition.class)
  public MongoDatabaseFactory tenantMongoDbFactory() {
    return context.getFactoryMap().values().iterator().next();
  }

  private AbstractDataSourceInitService<MongoProperties,MongoTenantContext,SimpleMongoClientDatabaseFactory> getService(){
    return SpringUtil.getBeansOfType(ResolvableType.forClassWithGenerics(
            AbstractDataSourceInitService.class,
            ResolvableType.forClass(MongoProperties.class),  // 替换P为实际类型
            ResolvableType.forClass(MongoTenantContext.class),  // 替换C为实际类型
            ResolvableType.forClass(SimpleMongoClientDatabaseFactory.class)   // 替换F为实际类型
    ),new DefaultMongoDbInitService());
  }
}
