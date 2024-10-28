package com.steven.solomon.utils;

import cn.hutool.core.annotation.AnnotationUtil;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterType;
import com.steven.solomon.annotation.MongoDBCapped;
import com.steven.solomon.config.MongoTenantsContext;
import com.steven.solomon.enums.MongoDbRoleEnum;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.util.Map;
import java.util.Map.Entry;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

public class MongoDbUtils {

  private static final Logger logger = LoggerUtils.logger(MongoDbUtils.class);

  public static void init(Map<String, MongoProperties> propertiesMap, MongoTenantsContext context){
    for(Entry<String,MongoProperties> entry : propertiesMap.entrySet()){
      init(entry.getKey(),entry.getValue(),context);
    }
  }

  public static void init(String tenantCode,MongoProperties properties,MongoTenantsContext context){
    SimpleMongoClientDatabaseFactory factory = initFactory(properties);
    context.setFactory(tenantCode,factory);
    initDocument(factory);
  }

  public static SimpleMongoClientDatabaseFactory initFactory(MongoProperties properties){
    MongoCredential mongoCredential = MongoCredential.createCredential(properties.getUsername(),properties.getDatabase(),properties.getPassword());
    MongoClientSettings settings = MongoClientSettings.builder().credential(mongoCredential).applyToClusterSettings(builder -> {
      builder.hosts(List.of(new ServerAddress(properties.getHost(), properties.getPort()))).mode(
          ClusterConnectionMode.MULTIPLE).requiredClusterType(ClusterType.STANDALONE);
    }).build();
    return new SimpleMongoClientDatabaseFactory(MongoClients.create(settings),properties.getDatabase());
  }

  public static void initDocument(MongoDatabaseFactory factory){

    List<String>  collectionNameList = new ArrayList<>();
    MongoDatabase mongoDatabase      = factory.getMongoDatabase();
    mongoDatabase.listCollectionNames().forEach(collectionNameList::add);

    for(Object obj : SpringUtil.getBeansWithAnnotation(MongoDBCapped.class).values()){
      MongoDBCapped                                          mongoDBCapped = AnnotationUtil.getAnnotation(obj.getClass(), MongoDBCapped.class);
      org.springframework.data.mongodb.core.mapping.Document document      = AnnotationUtil.getAnnotation(obj.getClass(), org.springframework.data.mongodb.core.mapping.Document.class);

      String name = ValidateUtils.isNotEmpty(document.collection()) ? document.collection() : ValidateUtils.isNotEmpty(document.value()) ? document.value() : "";
      boolean isCreate = collectionNameList.contains(name);
      if(!isCreate){
        if(ValidateUtils.isNotEmpty(mongoDBCapped)){
          mongoDatabase.createCollection(name,new CreateCollectionOptions().capped(true).maxDocuments(mongoDBCapped.maxDocuments()).sizeInBytes(mongoDBCapped.size()));
        } else {
          mongoDatabase.createCollection(name);
        }
      } else {
        logger.error("集合{}已存在,不允许修改为固定集合,防止数据丢失,请先备份数据后,手动创建固定集合",name);
//        if(ValidateUtils.isNotEmpty(mongoDBCapped)){
//          org.bson.Document command  = new org.bson.Document("collStats", name);
//          Boolean  isCapped = mongoDatabase.runCommand(command).getBoolean("capped");
//          if(!isCapped){
//            logger.info("修改集合{}为固定集合,限制字节为:{},记录数:{}",name,mongoDBCapped.size(),mongoDBCapped.maxDocuments());
//            command = new org.bson.Document("convertToCapped", name).append("capped",true).append("size", mongoDBCapped.size()).append("max",mongoDBCapped.maxDocuments());
//            mongoDatabase.runCommand(command);
//          }
//        }
      }
    }
  }

  /**
   * 检测数据库是否是固定集合
   * @param collectionName 集合名称
   */
  public static Document cherCheckCapped(String collectionName){
    return new Document("collStats", collectionName);
  }

  public static void checkCapped(MongoDatabase database, String collectionName, int size, int maxDocuments) {
    List<String> collectionNameList= new ArrayList<>();
    database.listCollectionNames().forEach(collectionNameList::add);
    if (collectionNameList.contains(collectionName)) {
      Document command = new Document("collStats", collectionName);
      Boolean isCapped = database.runCommand(command, ReadPreference.primary()).getBoolean("capped");

      if (!isCapped) {
        command = new Document("convertToCapped", collectionName).append("size", size).append("max",maxDocuments).append("capped",true);
        database.runCommand(command, ReadPreference.primary());
      }
    } else {
      database.createCollection(collectionName,new CreateCollectionOptions().capped(true).maxDocuments(maxDocuments).sizeInBytes(size));
    }
  }

  /**
   * 转换固定集合语句
   * @param collectionName 集合名称
   * @param max max则表示集合中文档的最大数量
   * @param size 集合的大小，单位为kb
   */
  public static Document convertToCapped(String collectionName,Long max,Long size){
    if(ValidateUtils.isEmpty(collectionName)){
      return null;
    }
    Document doc = new Document();
    doc.put("convertToCapped",collectionName);
    doc.put("capped",true);
    if(ValidateUtils.isNotEmpty(size)){
      doc.put("maxSize",size);
    }
    if (ValidateUtils.isNotEmpty(max)) {
      doc.put("max",max);
    }
    return doc;
  }

  /**
   * 创建mongodb数据库以及集合（创建数据库不创建集合会导致mongodb数据库自动删除）
   * @param mongoClient mongodb连接
   * @param dbName 数据库名称
   * @param collectionName 集合名词
   */
  public static MongoDatabase createDb(String mongoClient,String dbName,String collectionName){
    if(ValidateUtils.isEmpty(mongoClient) || ValidateUtils.isEmpty(dbName) || ValidateUtils.isEmpty(collectionName)){
      return null;
    }
    MongoDatabase mongoDatabase = createDb(mongoClient,dbName);
    createCollection(mongoDatabase, dbName);
    return mongoDatabase;
  }

  /**
   * 创建用户并赋予权限
   * @param mongoClient mongodb连接
   * @param userName 用户名
   * @param password 密码
   * @param roleEnum mongodb权限
   * @param dbName 数据库名（用户获取连接中的数据库名以及赋予数据库权限）
   */
  public static boolean createUser(String mongoClient,String userName,String password, MongoDbRoleEnum roleEnum,String dbName){
    if(ValidateUtils.isEmpty(mongoClient) || ValidateUtils.isEmpty(roleEnum) || ValidateUtils.isEmpty(dbName)){
      return false;
    }
    try {
      MongoClient mongoClients = MongoClients.create(mongoClient);
      Document doc = createUSerDocument(userName, password, roleEnum, dbName);
      mongoClients.getDatabase(dbName).runCommand(doc);
    }catch (Throwable e) {
      return false;
    }
    return true;
  }

  /**
   * 创建用户并赋予权限
   * @param mongoDatabase mongodb数据连接
   * @param userName 用户名
   * @param password 密码
   * @param roleEnum mongodb权限
   */
  public static boolean createUser(MongoDatabase mongoDatabase,String userName,String password, MongoDbRoleEnum roleEnum){
    if(ValidateUtils.isEmpty(mongoDatabase) || ValidateUtils.isEmpty(userName) || ValidateUtils.isEmpty(roleEnum) || ValidateUtils.isEmpty(password)){
      return false;
    }
    try {
      Document doc = createUSerDocument(userName, password, roleEnum, mongoDatabase.getName());
      mongoDatabase.runCommand(doc);
    }catch (Throwable e) {
      return false;
    }
    return true;
  }

  private static MongoDatabase createDb(String mongoClient, String dbName){
    if(ValidateUtils.isEmpty(mongoClient) || ValidateUtils.isEmpty(dbName)){
      return null;
    }
    MongoClient mongoClients = MongoClients.create(mongoClient);
    return mongoClients.getDatabase(dbName);
  }

  private static void createCollection(MongoDatabase mongoDatabase,String collectionName){
    if(ValidateUtils.isNotEmpty(mongoDatabase)){
      List<String> collectionNames = new ArrayList<>();
      mongoDatabase.listCollectionNames().forEach(collectionNames::add);
      if(!collectionNames.contains(collectionName)){
        mongoDatabase.createCollection(collectionName);
      }
    }
  }

  private static Document createUSerDocument(String userName,String password, MongoDbRoleEnum roleEnum,String dbName){
    Document doc = new Document();
    Document roleDoc = new Document();

    roleDoc.put("role", roleEnum.getValue());
    roleDoc.put("db", dbName);

    doc.put("createUser",userName);
    doc.put("pwd",password);
    doc.put("roles", Collections.singletonList(roleDoc));
    return doc;
  }
}
