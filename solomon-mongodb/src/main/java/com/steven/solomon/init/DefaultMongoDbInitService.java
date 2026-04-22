package com.steven.solomon.init;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.StrUtil;
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
import com.steven.solomon.config.MongoTenantContext;
import com.steven.solomon.enums.MongoDbRoleEnum;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.bson.Document;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认MongoDB租户初始化服务
 * 
 * <h2>功能说明</h2>
 * <p>负责为多租户环境创建和管理MongoDB连接工厂，支持固定集合（Capped Collection）</p>
 * 
 * <h2>核心功能</h2>
 * <ul>
 *   <li>创建MongoDB连接工厂</li>
 *   <li>自动创建数据库和集合</li>
 *   <li>支持固定集合（Capped Collection）配置</li>
 *   <li>支持用户权限管理</li>
 * </ul>
 * 
 * <h2>固定集合说明</h2>
 * <p>固定集合是大小固定的循环集合，当集合填满时，新文档会覆盖最旧的文档。</p>
 * <p>适用场景：日志存储、事件记录、缓存数据等</p>
 * 
 * @author steven
 * @since 1.0.0
 * @see MongoDBCapped
 * @see MongoTenantContext
 */
public class DefaultMongoDbInitService extends AbstractDataSourceInitService<MongoProperties, MongoTenantContext, SimpleMongoClientDatabaseFactory>{

    /**
     * 初始化租户MongoDB连接
     * 
     * <p>流程：</p>
     * <ol>
     *   <li>创建MongoDB连接工厂</li>
     *   <li>注册到租户上下文</li>
     *   <li>初始化文档集合（自动创建固定集合）</li>
     * </ol>
     * 
     * @param tenantCode 租户编码
     * @param properties MongoDB配置
     * @param context MongoDB租户上下文
     * @throws Throwable 创建失败时抛出
     */
    @Override
    public void init(String tenantCode, MongoProperties properties, MongoTenantContext context) throws Throwable {
        log.info("[MongoDB] 开始初始化租户MongoDB连接: tenantCode={}, host={}:{}, database={}", 
            tenantCode, properties.getHost(), properties.getPort(), properties.getDatabase());
        
        long startTime = System.currentTimeMillis();
        try {
            // Step 1: 创建连接工厂
            SimpleMongoClientDatabaseFactory factory = initFactory(properties);
            
            // Step 2: 注册到租户上下文
            context.registerFactory(tenantCode, factory);
            
            // Step 3: 初始化文档集合
            initDocument(factory);
            
            long cost = System.currentTimeMillis() - startTime;
            log.info("[MongoDB] 租户MongoDB连接初始化成功: tenantCode={}, host={}:{}, database={}, cost={}ms", 
                tenantCode, properties.getHost(), properties.getPort(), properties.getDatabase(), cost);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("[MongoDB] 租户MongoDB连接初始化失败: tenantCode={}, host={}:{}, cost={}ms, error={}", 
                tenantCode, properties.getHost(), properties.getPort(), cost, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建MongoDB连接工厂
     * 
     * <h3>配置说明</h3>
     * <ul>
     *   <li>使用用户名/密码认证</li>
     *   <li>配置为单机模式（STANDALONE）</li>
     *   <li>支持多节点连接（MULTIPLE模式）</li>
     * </ul>
     * 
     * @param properties MongoDB配置
     * @return MongoDB数据库工厂
     * @throws Throwable 配置错误时抛出
     */
    @Override
    public SimpleMongoClientDatabaseFactory initFactory(MongoProperties properties) throws Throwable {
        log.debug("[MongoDB] 开始创建连接工厂: host={}:{}, database={}, username={}", 
            properties.getHost(), properties.getPort(), properties.getDatabase(), properties.getUsername());
        
        // ========== Step 1: 创建认证信息 ==========
        MongoCredential mongoCredential = MongoCredential.createCredential(
            properties.getUsername(), 
            properties.getDatabase(), 
            properties.getPassword());
        
        log.debug("[MongoDB] 认证信息: username={}, authDatabase={}", 
            properties.getUsername(), properties.getDatabase());
        
        // ========== Step 2: 创建客户端设置 ==========
        MongoClientSettings settings = MongoClientSettings.builder()
            .credential(mongoCredential)
            .applyToClusterSettings(builder -> {
                builder.hosts(List.of(new ServerAddress(properties.getHost(), properties.getPort())))
                    .mode(ClusterConnectionMode.MULTIPLE)  // 多节点连接模式
                    .requiredClusterType(ClusterType.STANDALONE);  // 单机类型
            })
            .build();
        
        // ========== Step 3: 创建连接工厂 ==========
        SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(
            MongoClients.create(settings), 
            properties.getDatabase());
        
        log.info("[MongoDB] 连接工厂创建成功: host={}:{}, database={}", 
            properties.getHost(), properties.getPort(), properties.getDatabase());
        
        return factory;
    }

    /**
     * 初始化文档集合
     * 
     * <p>扫描所有带有{@link MongoDBCapped}注解的实体类，自动创建固定集合（Capped Collection）</p>
     * 
     * <h3>处理逻辑</h3>
     * <ul>
     *   <li>如果集合不存在：创建固定集合（如果配置了MongoDBCapped）或普通集合</li>
     *   <li>如果集合已存在：记录警告日志，不修改（防止数据丢失）</li>
     * </ul>
     * 
     * @param factory MongoDB数据库工厂
     */
    public void initDocument(MongoDatabaseFactory factory){
        log.debug("[MongoDB] 开始初始化文档集合");
        
        // ========== Step 1: 获取已存在的集合列表 ==========
        List<String> collectionNameList = new ArrayList<>();
        MongoDatabase mongoDatabase = factory.getMongoDatabase();
        mongoDatabase.listCollectionNames().forEach(collectionNameList::add);
        
        log.debug("[MongoDB] 已存在集合: {}", collectionNameList);
        
        // ========== Step 2: 扫描带有MongoDBCapped注解的实体 ==========
        int createCount = 0;
        int skipCount = 0;
        
        for(Object obj : SpringUtil.getBeansWithAnnotation(MongoDBCapped.class).values()){
            // 获取注解信息
            MongoDBCapped mongoDBCapped = AnnotationUtil.getAnnotation(obj.getClass(), MongoDBCapped.class);
            org.springframework.data.mongodb.core.mapping.Document document = 
                AnnotationUtil.getAnnotation(obj.getClass(), org.springframework.data.mongodb.core.mapping.Document.class);
            
            // 确定集合名称
            String name = ValidateUtils.isNotEmpty(document.collection()) ? document.collection() : 
                ValidateUtils.isNotEmpty(document.value()) ? document.value() : StrUtil.EMPTY;
            
            boolean isCreate = collectionNameList.contains(name);
            
            // ========== Step 3: 创建集合 ==========
            if(!isCreate){
                // 集合不存在，创建新集合
                if(ValidateUtils.isNotEmpty(mongoDBCapped)){
                    // 创建固定集合（Capped Collection）
                    mongoDatabase.createCollection(name, 
                        new CreateCollectionOptions()
                            .capped(true)
                            .maxDocuments(mongoDBCapped.maxDocuments())
                            .sizeInBytes(mongoDBCapped.size()));
                    
                    log.info("[MongoDB] 创建固定集合: name={}, maxDocuments={}, size={}bytes", 
                        name, mongoDBCapped.maxDocuments(), mongoDBCapped.size());
                } else {
                    // 创建普通集合
                    mongoDatabase.createCollection(name);
                    log.info("[MongoDB] 创建普通集合: name={}", name);
                }
                createCount++;
            } else {
                // 集合已存在，跳过创建
                log.warn("[MongoDB] 集合已存在，跳过创建: name={}（注意：修改为固定集合会导致数据丢失，请手动处理）", name);
                skipCount++;
            }
        }
        
        log.info("[MongoDB] 文档集合初始化完成: 创建={}, 跳过={}", createCount, skipCount);
    }

    /**
     * 检测数据库是否是固定集合
     * @param collectionName 集合名称
     */
    public Document cherCheckCapped(String collectionName){
        return new Document("collStats", collectionName);
    }

    public void checkCapped(MongoDatabase database, String collectionName, int size, int maxDocuments) {
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
    public Document convertToCapped(String collectionName,Long max,Long size){
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
    public MongoDatabase createDb(String mongoClient,String dbName,String collectionName){
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
    public boolean createUser(String mongoClient, String userName, String password, MongoDbRoleEnum roleEnum, String dbName){
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
    public boolean createUser(MongoDatabase mongoDatabase,String userName,String password, MongoDbRoleEnum roleEnum){
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

    private MongoDatabase createDb(String mongoClient, String dbName){
        if(ValidateUtils.isEmpty(mongoClient) || ValidateUtils.isEmpty(dbName)){
            return null;
        }
        try(MongoClient mongoClients = MongoClients.create(mongoClient);){
            return mongoClients.getDatabase(dbName);
        }
    }

    private void createCollection(MongoDatabase mongoDatabase,String collectionName){
        if(ValidateUtils.isNotEmpty(mongoDatabase)){
            List<String> collectionNames = new ArrayList<>();
            mongoDatabase.listCollectionNames().forEach(collectionNames::add);
            if(!collectionNames.contains(collectionName)){
                mongoDatabase.createCollection(collectionName);
            }
        }
    }

    private Document createUSerDocument(String userName,String password, MongoDbRoleEnum roleEnum,String dbName){
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
