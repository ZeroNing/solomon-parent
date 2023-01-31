package com.steven.solomon.utils;

import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.steven.solomon.enums.MongoDbRoleEnum;
import com.steven.solomon.verification.ValidateUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MongoDbUtils {
  /**
   * 检测数据库是否是固定集合
   * @param collectionName 集合名称
   * @return
   */
  public static Document cherCheckCapped(String collectionName){
    return new Document("collStats", collectionName);
  }

  public static void checkCapped(MongoDatabase database, String collectionName, int size, int maxDocuments) {
    List<String> a= new ArrayList<>();
    database.listCollectionNames().forEach(s -> {
      a.add(s);
    });
    if (a.contains(collectionName)) {
      Document command = new Document("collStats", collectionName);
      Boolean isCapped = database.runCommand(command, ReadPreference.primary()).getBoolean("capped");

      if (!isCapped) {
        command = new Document("convertToCapped", collectionName).append("maxSize", size).append("max",maxDocuments).append("capped",true);
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
   * 创建mongodb数据库以及集合（创建数据库不创建集合会导致mongdb数据库自动删除）
   * @param mongoClient mongodb连接
   * @param dbName 数据库名称
   * @param collectionName 集合名词
   * @return
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
   * @return
   */
  public static boolean createUser(String mongoClient,String userName,String password, MongoDbRoleEnum roleEnum,String dbName){
    if(ValidateUtils.isEmpty(mongoClient) || ValidateUtils.isEmpty(dbName) || ValidateUtils.isEmpty(roleEnum) || ValidateUtils.isEmpty(dbName)){
      return false;
    }
    try {
      MongoClient mongoClients = MongoClients.create(mongoClient);
      Document doc = createUSerDocument(userName, password, roleEnum, dbName);
      mongoClients.getDatabase(dbName).runCommand(doc);
    }catch (Exception e) {
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
   * @return
   */
  public static boolean createUser(MongoDatabase mongoDatabase,String userName,String password, MongoDbRoleEnum roleEnum){
    if(ValidateUtils.isEmpty(mongoDatabase) || ValidateUtils.isEmpty(userName) || ValidateUtils.isEmpty(roleEnum) || ValidateUtils.isEmpty(password)){
      return false;
    }
    try {
      Document doc = createUSerDocument(userName, password, roleEnum, mongoDatabase.getName());
      mongoDatabase.runCommand(doc);
    }catch (Exception e) {
      return false;
    }
    return true;
  }

  private static MongoDatabase createDb(String mongoClient, String dbName){
    if(ValidateUtils.isEmpty(mongoClient) || ValidateUtils.isEmpty(dbName)){
      return null;
    }
    MongoClient mongoClients = MongoClients.create(mongoClient);
    MongoDatabase mongoDatabase = mongoClients.getDatabase(dbName);
    return mongoDatabase;
  }

  private static void createCollection(MongoDatabase mongoDatabase,String collectionName){
    if(ValidateUtils.isNotEmpty(mongoDatabase)){
      List<String> collectionNames = new ArrayList<>();
      mongoDatabase.listCollectionNames().forEach(name -> collectionNames.add(name));
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
