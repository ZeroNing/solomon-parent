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
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

public class MongoInitUtils {

  public static void init(List<TenantMongoProperties> propertiesList){
    propertiesList.forEach(properties ->{
      init(properties);
    });
  }

  public static void init(TenantMongoProperties properties){
    MongoCredential mongoCredential = MongoCredential.createCredential(properties.getUsername(),properties.getDatabase(),properties.getPassword());
    MongoClientSettings settings = MongoClientSettings.builder().credential(mongoCredential).applyToClusterSettings(builder -> {
      builder.hosts(Arrays.asList(new ServerAddress(properties.getHost(),properties.getPort()))).mode(
          ClusterConnectionMode.MULTIPLE).requiredClusterType(ClusterType.STANDALONE);
    }).build();
    SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(MongoClients.create(settings),properties.getTenantCode());
    MongoTenantsContext.setFactoryMap(properties.getTenantCode(),factory);

    List<String>  collectionNameList = new ArrayList<>();
    MongoDatabase mongoDatabase      = factory.getMongoDatabase();
    mongoDatabase.listCollectionNames().forEach(name->{
      collectionNameList.add(name);
    });

    MongoTenantsContext.getCappedCollectionNameMap().forEach((key,value)->{
      boolean isCreate = collectionNameList.contains(key);
      if(!isCreate){
        MongoDBCapped mongoDBCapped = AnnotationUtils.getAnnotation(value, MongoDBCapped.class);
        if(ValidateUtils.isNotEmpty(mongoDBCapped)){
          mongoDatabase.createCollection(key,new CreateCollectionOptions().capped(true).maxDocuments(mongoDBCapped.maxDocuments()).sizeInBytes(mongoDBCapped.size()));
        } else {
          mongoDatabase.createCollection(key);
        }
      } else {
        MongoDBCapped mongoDBCapped = AnnotationUtils.getAnnotation(value, MongoDBCapped.class);
        if(ValidateUtils.isNotEmpty(mongoDBCapped)){
          Document command  = new Document("collStats", key);
          Boolean  isCapped = mongoDatabase.runCommand(command, ReadPreference.primary()).getBoolean("capped");
          if(!isCapped){
            command = new Document("convertToCapped", key).append("maxSize", mongoDBCapped.size()).append("max",mongoDBCapped.maxDocuments()).append("capped",true);
            mongoDatabase.runCommand(command, ReadPreference.primary());
          }
        }
      }
    });
  }
}
