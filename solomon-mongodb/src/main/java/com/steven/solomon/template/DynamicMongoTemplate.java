package com.steven.solomon.template;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.steven.solomon.config.MongoTenantsContext;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.apache.commons.lang.ObjectUtils;
import org.bson.Document;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.lang.Nullable;

public class DynamicMongoTemplate extends MongoTemplate {

  public DynamicMongoTemplate(MongoDatabaseFactory mongoDbFactory) {
    super(mongoDbFactory);
  }

  public DynamicMongoTemplate(MongoDatabaseFactory mongoDbFactory, @Nullable MongoConverter mongoConverter) {
    super(mongoDbFactory,mongoConverter);
  }

  @Override
  protected MongoDatabase doGetDatabase() {
    MongoDatabaseFactory mongoDbFactory = SpringUtil.getBean(MongoTenantsContext.class).getFactory();
    return mongoDbFactory == null ? super.doGetDatabase() : mongoDbFactory.getMongoDatabase();
  }

  @Override
  public MongoDatabaseFactory getMongoDbFactory() {
    MongoDatabaseFactory mongoDbFactory = SpringUtil.getBean(MongoTenantsContext.class).getFactory();
    return mongoDbFactory == null ? super.getMongoDbFactory() : mongoDbFactory;
  }

  @Override
  public MongoDatabaseFactory getMongoDatabaseFactory() {
    MongoDatabaseFactory mongoDbFactory = SpringUtil.getBean(MongoTenantsContext.class).getFactory();
    return mongoDbFactory == null ? super.getMongoDatabaseFactory() : mongoDbFactory;
  }

  @Override
  protected MongoCollection<Document> doCreateCollection(String collectionName, Document collectionOptions) {
    return super.doCreateCollection(collectionName,collectionOptions);
  }

}
