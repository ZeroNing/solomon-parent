# solomon-parent基础框架

## 引言

这个项目主要是总结了工作上遇到的问题以及学习一些框架用于整合

## 进微信群二维码
解答各位的疑惑
![img.png](img.png)

## 技术选型

| 框架                 | 说明                                                         | 版本   |
| -------------------- | ------------------------------------------------------------ | ------ |
| Spring cloud Alibaba | 微服务框架                                                   | 2021.1 |
| Spring cloud         | 微服务框架                                                   | 2020.0 |
| Nacos                | 配置中心 & 注册中心                                          | 2.1.1  |
| RabbitMq             | 消息队列                                                     | 2.4.2  |
| Sentinel             | 阿里流量防卫兵(限流、熔断降级、负载保护)                     | 1.8    |
| Redis                | 缓存                                                         | 2.4.2  |
| SpringBoot           | Spring Boot                                                  | 2.4.2  |
| mongoDb              | 分布式文件存储的数据库                                       | 2.4.2  |
| Minio                | 分布式对象存储服务器                                         | 8.2.1  |
| Mysql                | 数据库服务器                                                 | 8.0+   |
| Mybatis-plus         | MyBatis 增强工具包                                           | 3.5.1  |
| Easy-Excel           | Excel处理工具                                                | 3.0.5  |
| Swagger              | API接口文档                                                  | 3.0.0  |
| MQTT                 | MQTT是专门针对物联网开发的轻量级传输协议。MQTT协议针对低带宽网络,低计算能力的设备,做了特殊的优化,使得其能适应各种物联网应用场景。 | 6.0.4  |

## 项目描述

| 项目名                      | 说明                                                                                      |
|:-------------------------|-----------------------------------------------------------------------------------------|
| docker                   | 主要是基础的组件的部署文件(已经是测试过，可以直接用)                                                             |
| solomon-base             | 主要是封装了底层的异常捕获以及通用返回实体类                                                                  |
| solomon-common           | 引入base模块基础上增加了对微服务单体服务以及单体服务的异常捕获                                                       |
| solomon-constnt          | 主要就是写入了一部分异常编码常量以及缓存时间的值，底层数据实体类以及动态切换数据源模板，国际化配置，支持扫描Jar包内国际化文件并封装了底层通用常见的异常，并且返回国际化错误 |
| solomon-gateway-sentinel | 简单封装了gateway网关以及Sentinel的异常捕获，并支持动态修改nacos中的限流配置                                        |
| solomon-mongodb          | 引入了data模块，支持了动态切换缓存数据源，以及封装了部分底层查询方                                                     |
| solomon-mqtt             | 支持Mqtt以注解形式配置消息质量以及主题，并将通用的业务抽出来，让用户只关注业务逻辑的实现                                          |
| solomon-mqtt5            | 支持Mqtt以注解形式配置消息质量以及主题，并将通用的业务抽出来，让用户只关注业务逻辑的实现                                          |
| solomon-rabbitmq         | 支持rabbitmq以注解形式配置重试次数以及注册队列，并将通用的业务抽出来，让用户只关注业务逻辑的实现                                    |
| solomon-redis            | 引入了data模块，支持了动态切换缓存数据源以及增加租户编码前缀的缓存KEY                                                  |
| solomon-s3               | 主要封装了有关于S3协议下文分布式对象存储接口，如:阿里云、腾讯云、minio、百度云、华为云、七牛云、天翼云、金山云等等                           |
| solomon-utils            | 主要封装了一些通用的工具并支持用@JsonEnum注解国际化数据库的值                                                     |
| solomon-xxlJob           | 主要是将原本的XXL-JOB的配置进行自动化,自动创建任务                                                           |
# 切换租户主要设置

```java
RequestHeader.setTenantCode("租户编码");
```

## 切换时区主要设置
需要在请求头中添加"Timezone"然后value值就是时区例：UTC+8或者GMT+8，如果不填则默认是系统时区，然后切换时区逻辑为：将传入的参数转换为"Timezone"时区时间后转换为系统默认时区，返回的时候就将返回参数中的时间转换为"Timezone"的时区时间

# Swagger配置
## swagger版本号支持获取git最后一个提交记录版本号
需要在项目的pom.xml配置以下代码,打包成功后即可读取到项目git最后一条记录版本号

```xml
<build>
    <plugins>
        <plugin>
            <groupId>pl.project13.maven</groupId>
            <artifactId>git-commit-id-plugin</artifactId>
            <version>2.1.5</version>
            <executions>
                <execution>
                    <id>get-the-git-infos</id>
                    <!-- 默认绑定阶段initialize -->
                    <phase>initialize</phase>
                    <goals>
                        <goal>revision</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <!--日期格式;默认值:dd.MM.yyyy '@' HH:mm:ss z;-->
                <dateFormat>yyyy-MM-dd_HH-mm-ss</dateFormat>
                <!--,构建过程中,是否打印详细信息;默认值:false;-->
                <verbose>true</verbose>
                <!-- ".git"文件路径;默认值:${project.basedir}/.git; ${project.basedir}：项目根目录，即包含pom.xml文件的目录-->
                <dotGitDirectory>${project.basedir}/../../../.git</dotGitDirectory>
                <!--若项目打包类型为pom,是否取消构建;默认值:true;-->
                <skipPoms>false</skipPoms>
                <!--是否生成"git.properties"文件;默认值:false;-->
                <generateGitPropertiesFile>true</generateGitPropertiesFile>
                <!--指定"git.properties"文件的存放路径(相对于${project.basedir}的一个路径);-->
                <generateGitPropertiesFilename>/src/main/resources/git.properties</generateGitPropertiesFilename>
                <!--".git"文件夹未找到时,构建是否失败;若设置true,则构建失败;若设置false,则跳过执行该目标;默认值:true;-->
                <failOnNoGitDirectory>true</failOnNoGitDirectory>

                <!--git描述配置,可选;由JGit提供实现;-->
                <gitDescribe>
                    <!--是否生成描述属性-->
                    <skip>false</skip>
                    <!--提交操作未发现tag时,仅打印提交操作ID,-->
                    <always>false</always>
                    <!--提交操作ID显式字符长度,最大值为:40;默认值:7; 0代表特殊意义;后面有解释;-->
                    <abbrev>7</abbrev>
                    <!--构建触发时,代码有修改时(即"dirty state"),添加指定后缀;默认值:"";-->
                    <dirty>-dirty</dirty>
                    <!--always print using the "tag-commits_from_tag-g_commit_id-maybe_dirty" format, even if "on" a tag.
                        The distance will always be 0 if you're "on" the tag.  -->
                    <forceLongFormat>false</forceLongFormat>
                </gitDescribe>
            </configuration>
        </plugin>
    </plugins>
</build>
```
## Swagger配置说明
```yaml
knife4j:
  package:         #需要扫描的包路径
  title:           #swagger文档名字
  open:            #是否开启swagger 线上环境建议为false
```

# 国际化配置
## 国际化配置文件说明
```yaml
i18n:
  all-locale:         #目前用到的国际化语言
  language:           #设置默认国际化语言
  path:               #国际化文件路径
```
## 枚举国际化用例

1.枚举类需要实现 BaseEnum 其中的<T>是数据库里的值的类型

```java
public interface BaseEnum<T> {

    /**
     * 获取I8N国际化key
     *
     * @return code
     */
    String key();

    /**
     * 获取存入数据库的值
     *
     * @return label
     */
    T label();

    /**
     * 获取I18N国际化信息
     *
     * @return 国际化信息
     */
    default String Desc() {
        return I18nUtils.getEnumMessage(getClass().getSimpleName()+"."+key());
    }

    /**
     * 获取存入数据库的值
     *
     * @return label
     */
    default T Value() {
        return label();
    }
}
```

例：

```java
public enum DelFlagEnum implements BaseEnum<String> {
    /**
     * 未删除
     */
    NOT_DELETE("0"),
    /**
     * 已删除
     */
    DELETE("1");

    private final String label;

    DelFlagEnum(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return this.label;
    }

    @Override
    public String key() {
        return this.name();
    }
}
```

2.在实体类中需要国际化的字段上加上@JsonEnum注解 ，并且可以支持自定义返回值的名称用fieldName指定，不指定的时候就在该字段后面增加Desc

例：

```java
  @JsonEnum(enumClass = DelFlagEnum.class)
private              String        delFlag;
```

3.添加国际化 枚举类名是枚举国际化的前缀必须有，然后拼接的是枚举类中的label名称

例：

```properties
DelFlagEnum.NOT_DELETE=未删除
DelFlagEnum.DELETE=删除
```

# Mqtt配置说明
## Mqtt配置文件说明
```yaml
mqtt:
  tenant:
    default:               #租户编码
      user-name:           #用户名
      password:            #密码
      url:                 #连接
      client-id:           #客户端的标识(不可重复,为空时侯用uuid)
      completion-timeout:  #连接超时
      automatic-reconnect: #是否自动重连
      clean-session:       #客户端掉线后,是否自动清除session
      keep-alive-interval: #心跳时间
      will:                #遗嘱消息
        topic:             #遗嘱主题
        message:           #遗嘱消息
        qos:               #遗嘱消息质量
        retained:          #遗嘱是否保留消息
```
## MQTT消费用例

1.继承AbstractConsumer抽象类并重写handleMessage(业务逻辑处理),saveFailMessage(失败消息保存)

2.加上@MessageListener，并填写主题以及消息质量，这样子在项目启动时侯，就会自动订阅该主题

```java
@MessageListener(topics = "topic",qos = 2)
public class Test extends AbstractConsumer<String> {

    private Logger logger = LoggerUtils.logger(Test.class);


    @Override
    public void handleMessage(String body) throws Exception {
        logger.info("消息为:{}",body);
    }

    @Override
    public void saveFailMessage(String topic, MqttMessage message, Exception e) {

    }
}
```

# S3配置说明
## S3配置文件说明

```yaml
file:
  choice:             #文件选择器（MINIO:minio对象存储、DEFAULT:无文件存储实现、OSS:阿里云、OBS:华为云、COS:腾讯云、BOS:百度云、KODO:七牛云、ZOS:天翼云、KS3:金山云）
  file-naming-method: #文件命名选择器(ORIGINAL:原文件名称、DATE:文件名精确到毫秒并且以年月做为文件夹名、UUID:UUID命名、SNOWFLAKE:雪花id命名)
  endpoint:        # 连接地址
  accessKey:       # 访问密钥
  secretKey:       # 密钥
  bucket-name:     # 桶名 我这是给出了一个默认桶名
  rootDirectory:   # 根目录
  region-name:     # 地区名
  part-size:       #分片大小(默认单位为:MB,默认为5MB)
  connection-timeout: #连接超时 默认60秒 单位毫秒
  socket-timeout:  #socket的超时时间 默认60秒 单位毫秒
```
## S3使用用例
### 文件命名选择器
```java
public enum FileNamingMethodEnum implements BaseEnum<String> {
  ORIGINAL("ORIGINAL","使用文件的文件名"),
  DATE("DATE","根据时间戳生成文件名"),
  UUID("UUID","根据UUID生成文件名"),
  SNOWFLAKE("SNOWFLAKE","根据雪花id生成文件名");

  private String label;

  private String desc;

  FileNamingMethodEnum(String label,String desc) {
    this.label = label;
    this.desc = desc;
  }

  @Override
  public String getDesc() {
    return desc;
  }

  @Override
  public String label() {
    return this.label;
  }

  @Override
  public String key() {
    return this.name();
  }
}
```
### 文件选择器枚举
```java
public enum FileChoiceEnum implements BaseEnum<String> {
  DEFAULT("DEFAULT","无文件存储实现"),
  MINIO("MINIO","minio对象存储"),
  OSS("OSS","阿里云对象存储"),
  OBS("OBS","华为云对象存储"),
  COS("COS","腾讯云对象存储"),
  BOS("BOS","百度云对象存储"),
  KODO("KODO","七牛云对象存储"),
  ZOS("ZOS","天翼云对象存储"),
  KS3("KS3","金山云对象存储"),
  EOS("EOS","移动云对象存储"),
  NOS("NOS","网易数帆对象存储"),
  B2("B2","B2云存储"),
  JD("JD","京东云存储"),
  YANDEX("YANDEX","Yandex对象存储"),
  AMAZON("AMAZON","亚马逊对象存储"),
  SHARKTECH("SHARKTECH","鲨鱼对象存储"),
  DIDI("DIDI","滴滴云对象存储"),
  BOTO3("BOTO3","交大云对象存储"),
  TOS("TOS","火山云对象存储"),
  R2("R2","R2对象存储"),
  GOOGLE_CLOUD_STORAGE("GOOGLE_CLOUD_STORAGE","谷歌云对象存储"),
  UOS("UOS","紫光云对象存储"),
  AZURE("AZURE","微软对象存储"),
  INSPUR("INSPUR","浪潮云对象存储"),
  S3("S3","S3协议对象存储")
  ;

  private String label;

  private String desc;

  FileChoiceEnum(String label,String desc) {
    this.label = label;
    this.desc = desc;
  }

  @Override
  public String getDesc() {
    return desc;
  }

  @Override
  public String label() {
    return this.label;
  }

  @Override
  public String key() {
    return this.name();
  }
}
```
## 文件上传下载分享使用用例

1.引文件jar包

```xml
<dependencies>
	<dependency>
      <groupId>com.steven</groupId>
      <artifactId>solomon-s3</artifactId>
      <version>1.0</version>
    </dependency>
</dependencies>
```

2.注入FileServiceInterface

```java
@RestController
@RequestMapping("/api/file")
@Api(tags = "文件接口")
public class FileController {

    private final FileServiceInterface fileServiceInterface;

    public FileController(FileServiceInterface fileServiceInterface) {this.fileServiceInterface = fileServiceInterface;}
}
```

3.方法使用

```java
@RestController
@RequestMapping("/api/file")
@Api(tags = "文件接口")
public class FileController {

    private final FileServiceInterface fileServiceInterface;

    public FileController(FileServiceInterface fileServiceInterface) {this.fileServiceInterface = fileServiceInterface;}

    @PostMapping("/{bucket}/upload")
    @ApiOperation(value = "文件上传")
    public ResultVO<FileUpload> upload(@RequestPart("file") MultipartFile file,
                                       @ApiParam("minio桶") @PathVariable("bucket") String bucket) throws Exception {
        //上传文件
        FileUpload fileUpload = fileServiceInterface.upload(file,"桶名");
        //分享文件
        String shareUrl = fileServiceInterface.share("文件名","桶名",分享文件超时时间,TimeUnit.SECONDS);
        //分片上传
        FileUpload fileUpload = fileServiceInterface.multipartUpload(file,"桶名");
        //删除文件
        fileServiceInterface.deleteFile("文件名","桶名");
        //拷贝文件
        boolean     flag        = fileServiceInterface.copyObject("原桶名","目标桶名","原文件名","目标文件名");
        //下载文件流
        InputStream inputStream = fileServiceInterface.download("文件名","桶名");

        return ResultVO.success(fileServiceInterface.upload(file,bucket));
    }

}
```

# rabbitmq配置
## rabbitmq配置文件说明
```yaml
spring:
  rabbitmq:
    username: guest
    password: guest
    host: localhost
    port: 5672
    auto-delete-queue: true  #是否自动删除队列以 true：自动删除 false：不删除
    auto-delete-exchange: true  #是否自动删除交换机 true：自动删除 false：不删除
    enabled: true #是否启用该组件
```

## rabbitmq使用说明
@MessageListener注解描述：

```java
/**
 * MessageListener标注注解
 */
@Target(value = {ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface MessageListener {

    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * 队列
     */
    String[] queues();

    /**
     * 交换器
     */
    String exchange() default "";

    /**
     * 路由规则
     */
    String routingKey() default "";

    /**
     * 是否持久化
     */
    boolean isPersistence() default true;

    /**
     * 确认模式（只支持手动提交，自动提交代码暂时不支持）
     */
    AcknowledgeMode mode() default AcknowledgeMode.MANUAL;

    /**
     * 每个队列消费者数量
     */
    int consumersPerQueue() default 1;

    /**
     * 每次的接收的消息数量最大数值(0:公平分发 1:不公平分发)
     */
    int prefetchCount() default AbstractMessageListenerContainer.DEFAULT_PREFETCH_COUNT;

    /**
     * 交换类型（暂时不支持system，只支持DIRECT、TOPIC、FANOUT、HEADERS）
     */
    String exchangeTypes() default ExchangeTypes.DIRECT;

    /**
     * 消息最大存活时间
     */
    long delay() default 0L;

    /**
     * 死信队列Class
     */
    Class dlxClazz() default void.class;

    /**
     * 是否启用插件内的ttl队列
     */
    boolean isDelayExchange() default false;

    /**
     * Headers交换器下需要配置 是否匹配全部头部属性 默认非全部
     */
    boolean matchAll() default false;

    /**
     * Headers交换器下需要配置 是否匹配值,true就是匹配值,false就是不匹配值，只判断是否存在
     */
    boolean matchValue() default false;

    /**
     * 需要匹配的头部消息,如matchAll为True清空则需要匹配全部headers存在,才可通过,false为只要匹配中其中一个即可通过
     * 如果matchValue为true,headers结果应为 0:key,1:value,2:key,3:value.........如此下去,false的话则全部为key
     */
    String[] headers() default {};

    /**
     * 是否创建惰性队列
     */
    boolean lazy() default false;
}
```

1.继承AbstractConsumer抽象类并重写handleMessage(业务逻辑处理),saveFailMessage(失败消息保存)

2.加上@MessageListener注解，并填写队列名以及交换器名

```java
@MessageListener(queues = "test1",exchange = "test1")
public class TestMq extends AbstractConsumer<String,String> {

    public TestMq(RabbitUtils rabbitUtils) {
        this.rabbitUtils = rabbitUtils;
    }
    
    @Override
    public String handleMessage(String body) throws Exception {
        return "";
    }

    @Override
    public void saveFailMessage(Message message, Exception e) {

    }
}
```

3.如果需要重试则加上@MessageListenerRetry注解，配置重试次数

```java
@MessageListener(queues = "test1",exchange = "test1")
@MessageListenerRetry(retryNumber = 5)
public class TestMq extends AbstractConsumer<String,String> {

    public TestMq(RabbitUtils rabbitUtils) {
        this.rabbitUtils = rabbitUtils;
    }
    
    @Override
    public String handleMessage(String body) throws Exception {
        return "";
    }

    @Override
    public void saveFailMessage(Message message, Exception e) {

    }
}
```

4.死信队列使用，需要在@MessageListener注解增加dlxClazz配置

```java
@MessageListener(queues = "test1",exchange = "test1",dlxClazz = TestDlxMq.class)
@MessageListenerRetry(retryNumber = 5)
public class TestMq extends AbstractConsumer<String,String> {

    public TestMq(RabbitUtils rabbitUtils) {
        this.rabbitUtils = rabbitUtils;
    }

    @Override
    public String handleMessage(String body) throws Exception {
        return "";
    }

    @Override
    public void saveFailMessage(Message message, Exception e) {

    }
}
```

```java
@DlxMessageListener
public class TestDlxMq extends AbstractConsumer<String,String> {

    public TestDlxMq(RabbitUtils rabbitUtils) {
        this.rabbitUtils = rabbitUtils;
    }

    @Override
    public String handleMessage(String body) throws Exception {
        return "";
    }

    @Override
    public void saveFailMessage(Message message, Exception e) {

    }
}
```
5.请求回复发送消息用例，最后会返回AbstractConsumer的Json结果，如果是非Json结构会报错
```java
@RestController
@RequestMapping
public class TestController {

    @Autowired
    private RabbitUtils utils;

    @PostMapping("/test")
    public Object test() throws Exception {
        RabbitMqModel<String> mqModel = new RabbitMqModel<String>("test","test","test");
        mqModel.setReplyTo("test");
        return  utils.convertSendAndReceive(mqModel);
    }
}
```
```java
@MessageListener(queues = "test",exchange = "test",routingKey = "test")
public class Handler extends AbstractConsumer<String,String> {

    protected Handler(RabbitUtils rabbitUtils) {
        super(rabbitUtils);
    }

    @Override
    public String handleMessage(String body) throws Exception {
        Map<String,Object> a = new HashMap<>();
        a.put("msg",body);
        return JSONUtil.toJsonStr(a);
    }

    @Override
    public void saveLog(String result, Throwable throwable, RabbitMqModel<String> rabbitMqModel) {

    }
}
```

# 缓存配置说明
## 缓存配置文件说明
```yaml
spring:
  cache:
    mode: NORMAL #NORMAL("单库"), SWITCH_DB("切换数据源"), TENANT_PREFIX("增加租户前缀");
    type: NONE   #NONE 不使用缓存 REDIS 使用redis缓存目前只支持这两个
```

## redis单机版配置文件说明
```yaml
spring:
  cache:
    mode: NORMAL #NORMAL("单库"), SWITCH_DB("切换数据源"), TENANT_PREFIX("增加租户前缀");
    type: REDIS #NONE 不使用缓存 REDIS 使用redis缓存目前只支持这两个
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
```

## redis多租户配置文件说明
```yaml
spring:
  cache:
    mode: SWITCH_DB #NORMAL("单库"), SWITCH_DB("切换数据源"), TENANT_PREFIX("增加租户前缀");
    type: REDIS #NONE 不使用缓存 REDIS 使用redis缓存目前只支持这两个
  redis:
    tenant:
      default: #租户编码
        host: 127.0.0.1
        port: 6379
        database: 0
      default1: #租户编码
        host: 127.0.0.1
        port: 6380
        database: 0
```
## Redis消息队列用例
### MessageListener注解说明

```java
/**
 * Redis消息队列
 */
@Target(value = {ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface MessageListener {

    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * 主题名
     */
    String topic();

    /**
     * 主题模式
     */
    TopicMode mode() default TopicMode.CHANNEL;
}
```

1.继承AbstractConsumer抽象类并重写handleMessage(业务逻辑处理),saveFailMessage(失败消息保存)

2.加上@MessageListener注解，并填写主题名,并设置主题模式
```java
@MessageListener(topic = "test",mode = TopicMode.CHANNEL)
public class Test extends AbstractConsumer<String,String> {

    @Override
    public String handleMessage(String body) throws Exception {
        System.out.println(body);
        return null;
    }

    @Override
    public void saveLog(String result, Message message, RedisQueueModel model) {

    }
}
```


# xxl-job配置说明
## xxl-job配置文件说明
```yaml
xxl:
  # 调度中心的地址，通常是XXL-JOB管理控制台的地址
  admin-addresses: http://localhost:8080/xxl-job-admin
  # 调度中心和执行器之间的访问令牌，用于确保安全性
  access-token: default_token
  # 当前执行器的应用名称，用于唯一标识一个执行器
  app-name: xxl-job-executor-sample
  # 执行器地址，用于注册到调度中心。可以指定具体的IP和端口，或者为自动模式（例如：AUTO）
  address: localhost:8080
  # 执行器的IP地址。如果为空，系统会自动获取本机IP
  ip: localhost
  # 执行器的端口。默认端口为0，表示随机生成一个端口
  port: 8080
  # 执行器的日志文件存储路径
  log-path: 日志路径
  # 执行器的日志文件的保留天数
  log-retention-days: 30
  # 调度中心的登录用户名
  user-name: admin
  # 调度中心的登录密码
  password: 123456
  # 是否启用该执行器，true表示启用，false表示禁用
  enabled: true
```

## xxl-job使用说明
### jobTask注解说明
```java
/**
 * xxl-job注解
 */
@Target(value = { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JobTask {

    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * 执行器主键ID
     */
    int jobGroup() default 1;
    /**
     * 任务描述 默认:当前类的类名
     */
    String taskName() default "";

    /**
     * 负责人 默认是配置文件的 spring.application.name 如果没有的情况下,继续默认当前类名
     */
    String author() default "";

    /**
     * 报警邮件
     */
    String alarmEmail() default "";

    /**
     * 调度类型 默认不调度
     */
    ScheduleTypeEnum scheduleType() default ScheduleTypeEnum.NONE;

    /**
     * 调度配置 CRON(* * * * * ?) FIX_RATE(30秒)
     */
    String scheduleConf() default "";

    /**
     * 运行模式
     */
    GlueTypeEnum glueType() default GlueTypeEnum.BEAN;

    /**
     * 执行器，任务Handler名称 默认:当前类的类名
     */
    String executorHandler() default "";

    /**
     * 执行器 任务参数
     */
    String executorParam() default "";

    /**
     * 路由策略
     */
    ExecutorRouteStrategyEnum executorRouteStrategy() default ExecutorRouteStrategyEnum.FIRST;

    /**
     * 子任务ID，多个逗号分隔
     */
    String childJobId() default "";

    /**
     * 调度过期策略
     */
    MisfireStrategyEnum misfireStrategy() default MisfireStrategyEnum.DO_NOTHING;

    /**
     * 阻塞处理策略
     */
    ExecutorBlockStrategyEnum executorBlockStrategy() default ExecutorBlockStrategyEnum.SERIAL_EXECUTION;

    /**
     * 任务执行超时时间，单位秒
     */
    int executorTimeout() default 0;

    /**
     * 失败重试次数
     */
    int executorFailRetryCount() default 0;

    /**
     * 是否启动 默认不启动
     */
    boolean start() default false;
}

```

## XXL-JOB自动创建任务用法
```java
@JobTask(taskName = "任务描述", author = "负责人", executorHandler = "JobHandler",scheduleType = ScheduleTypeEnum.FIX_RATE,scheduleConf = "1")
public class TestHandler extends AbstractJobConsumer {

    @Override
    public void handle(String jobParam) {
    }

    @Override
    public void saveLog(Throwable throwable) {

    }
}
```

# MongoDB配置说明
## MongoDB多租户配置方式
1.需要在配置文件增加配置mode: NORMAL("单库"), SWITCH_DB("切换数据源");

2.如果选择的是切换数据源的话可以选择配置tenant配置,租户编码则是不同客户的租户编码，到时候切换也是根据租户编码切换的

3.如果不想选择配置文件配置的话也可以用代码方面调用 MongoInitUtils.init的方法，传入租户编码和mongodb配置以及注入一个MongoTenantsContext对象

```yaml
spring:
    data:
      mongodb:
        mode: SWITCH_DB
        host:
        port:
        username:
        password:
        database:
        uri:
        tenant:
          租户编码:
            host:
            port: 
            username:
            password: 
            database:
            uri: 
```
## MongoDB配置固定集合
以下代码配置的是集合固定大小为2048字节,限制行数为五十万
```java
@MongoDBCapped(size = 2048,maxDocuments = 500000)
@Document
public class Entity implements Serializable {
    
    private String id;
    
    private Date created;
    
    private Date updated;
}
```

## 集合排序使用

```java
  public static void main(String[] args) {
    List<Person> b = new ArrayList<>();
    for (Integer i = 0; i < 10000000; i++) {
        b.add(new Person(String.valueOf(i*21), i*13));
    }

    System.out.println("============多字段排序算法降序开始=======================\n");
    System.out.println("总记录数:" + b.size() + "排序测试\n");
    for(SortTypeEnum typeEnum : SortTypeEnum.values()){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        SortUtil.sort(typeEnum,b, Comparator.comparing(Person::getAge).thenComparing(Person::getName).reversed());
        stopWatch.stop();
        System.out.println(typeEnum.getDesc() + "算法：降序耗时:" + stopWatch.getTotalTimeSeconds() + "秒");
        System.out.print("\n");
    }
}

static class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
```

# powerJob配置说明
## powerJob配置文件说明
```yaml
powerjob:
  worker:
    enabled: true  # 启用或禁用PowerJob Worker。如果设置为false，将不会启动worker。
    port: 27777  # Worker监听的端口，用于接收任务执行命令。
    app-name: 应用名称  # 应用名称，需要与PowerJob服务端注册的应用名称保持一致。
    server-address: localhost:7700  # PowerJob服务端的地址和端口，Worker会通过此地址与服务端通信。
    protocol: http  # 与PowerJob服务端通信使用的协议，通常为http或https。
    max-result-length: 4096  # 任务执行结果的最大长度，单位为字符。超出此长度的结果将被截断。
    max-lightweight-task-num: 1024  # 最大轻量级任务数量，即同时允许执行的轻量级任务的数量。
    user-name: 认证的用户名  # 用于认证的用户名，与服务端配置的用户名匹配。
    password: 认证的密码  # 用于认证的密码，与服务端配置的密码匹配。
```

# Docker Compose安装组件文件
详细的配置都在docker文件夹内，内涵Emqx的Mqtt组件、Minio对象存储组件、Mongodb组件、Mysql数据库、Portainer管理Docker可视化界面组件、PostgresSql数据库、RabbitMq消息队列组件、Redis缓存组件、RocketMq消息队列组件、sonarqube代码检查组件、Nacos组件

文件夹内的未命名.txt只是为了让文件夹正常提交才创建的