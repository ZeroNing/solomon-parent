package com.steven.solomon.init;

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
import com.steven.solomon.config.MongoTenantsContext;
import com.steven.solomon.properties.TenantMongoProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.mapping.Document;

public class MongoInitUtils {

  public static void init(List<TenantMongoProperties> propertiesList,MongoTenantsContext context){
    propertiesList.forEach(properties ->{
      init(properties,context);
    });
  }

  public static void init(TenantMongoProperties properties,MongoTenantsContext context){
    MongoCredential mongoCredential = MongoCredential.createCredential(properties.getUsername(),properties.getDatabase(),properties.getPassword());
    MongoClientSettings settings = MongoClientSettings.builder().credential(mongoCredential).applyToClusterSettings(builder -> {
      builder.hosts(Arrays.asList(new ServerAddress(properties.getHost(),properties.getPort()))).mode(
          ClusterConnectionMode.MULTIPLE).requiredClusterType(ClusterType.STANDALONE);
    }).build();
    SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(MongoClients.create(settings),properties.getTenantCode());
    context.setFactory(properties.getTenantCode(),factory);

    List<String>  collectionNameList = new ArrayList<>();
    MongoDatabase mongoDatabase      = factory.getMongoDatabase();
    mongoDatabase.listCollectionNames().forEach(name->{
      collectionNameList.add(name);
    });
    ;
    for(Object obj : SpringUtil.getBeansWithAnnotation(MongoDBCapped.class).values()){
      MongoDBCapped mongoDBCapped = AnnotationUtils.getAnnotation(obj.getClass(), MongoDBCapped.class);
      Document      document = AnnotationUtils.getAnnotation(obj.getClass(), Document.class);

      String name = ValidateUtils.isNotEmpty(document.collection()) ? document.collection() : ValidateUtils.isNotEmpty(document.value()) ? document.value() : "";
      boolean isCreate = collectionNameList.contains(name);
      if(!isCreate){
        if(ValidateUtils.isNotEmpty(mongoDBCapped)){
          mongoDatabase.createCollection(name,new CreateCollectionOptions().capped(true).maxDocuments(mongoDBCapped.maxDocuments()).sizeInBytes(mongoDBCapped.size()));
        } else {
          mongoDatabase.createCollection(name);
        }
      } else {
        if(ValidateUtils.isNotEmpty(mongoDBCapped)){
          org.bson.Document command  = new org.bson.Document("collStats", name);
          Boolean  isCapped = mongoDatabase.runCommand(command, ReadPreference.primary()).getBoolean("capped");
          if(!isCapped){
            command = new org.bson.Document("convertToCapped", name).append("max",mongoDBCapped.maxDocuments()).append("size", mongoDBCapped.size()).append("capped",true);
            mongoDatabase.runCommand(command, ReadPreference.primary());
          }
        }
      }
    }
  }
}
