package com.steven.solomon.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterType;
import com.steven.solomon.annotation.MongoDBCapped;
import com.steven.solomon.condition.MongoCondition;
import com.steven.solomon.converter.DateToLocalDateTimeConverter;
import com.steven.solomon.converter.LocalDateTimeToDateConverter;
import com.steven.solomon.enums.MongoModeEnum;
import com.steven.solomon.init.MongoInitUtils;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.properties.MongoProfile;
import com.steven.solomon.properties.TenantMongoProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.template.DynamicMongoTemplate;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.bson.Document;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.MongoTypeMapper;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@DependsOn({"springUtil"})
public class MongoConfig {

    private Logger logger = LoggerUtils.logger(getClass());

    @Resource
    private MongoProperties mongoProperties;

    @Resource
    private MongoProfile mongoProfile;

    @Resource
    private MongoMappingContext mongoMappingContext;

    @PostConstruct
    public void afterPropertiesSet() {
        List<TenantMongoProperties>         mongoPropertiesList                          = new ArrayList<>();
        List<MongoClientPropertiesService> abstractMongoClientPropertiesServices = new ArrayList<>(SpringUtil.getBeansOfType(MongoClientPropertiesService.class).values());
        if(ValidateUtils.isNotEmpty(mongoProfile) && MongoModeEnum.NORMAL.toString().equalsIgnoreCase(mongoProfile.getMode())){
            TenantMongoProperties tenantMongoProperties = new TenantMongoProperties(mongoProperties);
            tenantMongoProperties.setTenantCode("default");
            mongoPropertiesList.add(tenantMongoProperties);
        }

        abstractMongoClientPropertiesServices.forEach(service ->{
            service.setCappedCollectionNameMap();
            mongoPropertiesList.addAll(service.getMongoClient());
        });
        MongoInitUtils.init(mongoPropertiesList);
    }

    @Bean(name = "mongoTemplate")
    @Conditional(value = MongoCondition.class)
    public DynamicMongoTemplate dynamicMongoTemplate(MongoMappingContext context, BeanFactory beanFactory) {
        SimpleMongoClientDatabaseFactory factory = MongoTenantsContext.getFactoryMap().values().iterator().next();
        DbRefResolver         dbRefResolver    = new DefaultDbRefResolver(factory);
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
        mappingConverter.setCustomConversions(beanFactory.getBean(CustomConversions.class));
        List<Object>          list      = new ArrayList<>();
        list.add(new LocalDateTimeToDateConverter());
        list.add(new DateToLocalDateTimeConverter());
        mappingConverter.setCustomConversions(new MongoCustomConversions(list));
        mappingConverter.setTypeMapper(defaultMongoTypeMapper());
//        mappingConverter.afterPropertiesSet();
        return new DynamicMongoTemplate(factory,mappingConverter);
    }

    @Bean(name = "mongoDbFactory")
    @Conditional(value = MongoCondition.class)
    public MongoDatabaseFactory mongoDbFactory() {
        return MongoTenantsContext.getFactoryMap().values().iterator().next();
    }

    public MongoTypeMapper defaultMongoTypeMapper() {
        return new DefaultMongoTypeMapper(null);
    }
}
