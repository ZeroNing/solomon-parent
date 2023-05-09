package com.steven.solomon.config;

import com.steven.solomon.condition.MongoCondition;
import com.steven.solomon.converter.DateToLocalDateTimeConverter;
import com.steven.solomon.converter.LocalDateTimeToDateConverter;
import com.steven.solomon.init.MongoInitUtils;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.properties.MongoProfile;
import com.steven.solomon.properties.TenantMongoProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.template.DynamicMongoTemplate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
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

    @Resource
    private MongoTenantsContext context;

    @PostConstruct
    public void afterPropertiesSet() {
        List<TenantMongoProperties>         mongoPropertiesList                          = new ArrayList<>();
        List<MongoClientPropertiesService> abstractMongoClientPropertiesServices = new ArrayList<>(SpringUtil.getBeansOfType(MongoClientPropertiesService.class).values());
        TenantMongoProperties tenantMongoProperties = new TenantMongoProperties(mongoProperties);
        tenantMongoProperties.setTenantCode("default");
        mongoPropertiesList.add(tenantMongoProperties);

        for(MongoClientPropertiesService service : abstractMongoClientPropertiesServices){
            mongoPropertiesList.addAll(service.getMongoClient());
        }
        MongoInitUtils.init(mongoPropertiesList,context);
    }

    @Bean(name = "mongoTemplate")
    @Conditional(value = MongoCondition.class)
    public DynamicMongoTemplate dynamicMongoTemplate(MongoMappingContext mappingContext, BeanFactory beanFactory) {
        SimpleMongoClientDatabaseFactory factory = context.getFactoryMap().values().iterator().next();
        DbRefResolver         dbRefResolver    = new DefaultDbRefResolver(factory);
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, mappingContext);
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
        return context.getFactoryMap().values().iterator().next();
    }

    public MongoTypeMapper defaultMongoTypeMapper() {
        return new DefaultMongoTypeMapper(null);
    }
}
