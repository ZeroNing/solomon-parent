package com.steven.solomon.template;

import com.mongodb.ReadPreference;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.steven.solomon.annotation.MongoDBCapped;
import com.steven.solomon.config.MongoTenantsContext;
import com.steven.solomon.spring.SpringUtil;
import java.util.Map;
import org.bson.Document;
import org.springframework.core.annotation.AnnotationUtils;
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
  protected MongoCollection<Document> doCreateCollection(String collectionName, Document collectionOptions) {
    return super.doCreateCollection(collectionName,collectionOptions);
  }
}
