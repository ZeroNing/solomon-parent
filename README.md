# solomon-parent基础框架

## 引言

这个项目主要是总结了工作上遇到的问题以及学习一些框架用于整合



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

| 项目名                   | 说明                                                         |
| :----------------------- | ------------------------------------------------------------ |
| solomon-base             | 主要是封装了底层的异常捕获以及通用返回实体类                 |
| solomon-cache            | 引入了data模块，支持了动态切换缓存数据源以及增加租户编码前缀的缓存KEY |
| solomon-common           | 引入base模块基础上增加了对微服务单体服务以及单体服务的异常捕获 |
| solomon-constnt          | 主要就是写入了一部分异常编码常量以及缓存时间的值，底层数据实体类以及动态切换数据源模板，国际化配置，支持扫描Jar包内国际化文件并封装了底层通用常见的异常，并且返回国际化错误 |
| solomon-file             | 主要封装了有关于S3协议下文分布式对象存储接口，如:阿里云、腾讯云、minio、百度云、华为云等等 |
| solomon-gateway-sentinel | 简单封装了gateway网关以及Sentinel的异常捕获，并支持动态修改nacos中的限流配置 |
| solomon-mongodb          | 引入了data模块，支持了动态切换缓存数据源，以及封装了部分底层查询方法 |
| solomon-mq               | 封装一些基础MQ类                                             |
| solomon-mqtt             | 支持Mqtt以注解形式配置消息质量以及主题，并将通用的业务抽出来，让用户只关注业务逻辑的实现 |
| solomon-rabbitmq         | 支持rabbitmq以注解形式配置重试次数以及注册队列，并将通用的业务抽出来，让用户只关注业务逻辑的实现 |
| solomon-mybatis          | 主要是加入了一个通用实体类，未来或许考虑加入动态切换数据源   |
| solomon-utils            | 主要封装了一些通用的工具并支持用@JsonEnum注解国际化数据库的值 |

## 自定义配置说明

### swagger版本号支持获取git最后一个提交记录版本号

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



### Swagger配置说明

```yaml
knife4j:
  package:         #需要扫描的包路径
  title:           #swagger文档名字
  open:            #是否开启swagger 线上环境建议为false
```



### 国际化配置说明

```yaml
i18n:
  all-locale:         #目前用到的国际化语言
  language:           #设置默认国际化语言
  path:               #国际化文件路径
```

### Mqtt配置说明

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



### 文件配置说明

```yaml
file:
  choice:             #文件选择器（MINIO:minio对象存储、DEFAULT:无文件存储实现、OSS:阿里云、OBS:华为云、COS:腾讯云、BOS:百度云、KODO:七牛云）
  file-naming-method: #文件命名选择器(ORIGINAL:原文件名称、DATE:文件名精确到毫秒并且以年月做为文件夹名、UUID:UUID命名、SNOWFLAKE:雪花id命名)
  endpoint:        # 连接地址
  accessKey:       # 访问密钥
  secretKey:       # 密钥
  bucket-name:     # 桶名 我这是给出了一个默认桶名
  rootDirectory:   # 根目录
  region-name:     # 地区名
```

### 缓存配置说明

```yaml
spring:
  cache:
    mode: NORMAL #NORMAL("单库"), SWITCH_DB("切换数据源"), TENANT_PREFIX("增加租户前缀");
    type: NONE   #NONE 不使用缓存 REDIS 使用redis缓存目前只支持这两个
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

## Rabbit队列注册以及消费用例

@Rabbit注解描述：

```java
/**
 * rabbitmq标注注解
 * @author huangweihua
 */
@Target(value = { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RabbitMq {

	@AliasFor(annotation = Component.class)
	String value() default "";

	/**
	 * 队列
	 */
	String[] queues();

	/**
	 * 交换器
	 */
	String exchange();

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
	 * @return
	 */
	String[] headers() default {};
}
```

1.继承AbstractConsumer抽象类并重写handleMessage(业务逻辑处理),saveFailMessage(失败消息保存)

2.加上@RabbitMq注解，并填写队列名以及交换器名

```java
@RabbitMq(queues = "test1",exchange = "test1")
public class TestMq extends AbstractConsumer<String> {

  @Override
  public void handleMessage(String body) throws Exception {
  }

  @Override
  public void saveFailMessage(Message message, Exception e) {

  }
}
```

3.如果需要重试则加上@RabbitMqRetry注解，配置重试次数

```java
@RabbitMq(queues = "test1",exchange = "test1")
@RabbitMqRetry(retryNumber = 5)
public class TestMq extends AbstractConsumer<String> {

  @Override
  public void handleMessage(String body) throws Exception {
  }

  @Override
  public void saveFailMessage(Message message, Exception e) {

  }
}
```

4.死信队列使用，需要在@RabbitMq注解增加dlxClazz配置

```java
@RabbitMq(queues = "test1",exchange = "test1",dlxClazz = TestDlxMq.class)
@RabbitMqRetry(retryNumber = 5)
public class TestMq extends AbstractConsumer<String> {

  @Override
  public void handleMessage(String body) throws Exception {
  }

  @Override
  public void saveFailMessage(Message message, Exception e) {

  }
}
```

```java
@Component
public class TestDlxMq extends AbstractConsumer<String> {

  @Override
  public void handleMessage(String body) throws Exception {

  }

  @Override
  public void saveFailMessage(Message message, Exception e) {

  }
}
```

## MQTT消费用例

1.继承AbstractConsumer抽象类并重写handleMessage(业务逻辑处理),saveFailMessage(失败消息保存)

2.加上@Mqtt注解，并填写主题以及消息质量，这样子在项目启动时侯，就会自动订阅该主题

```java
@Mqtt(topics = "topic",qos = 2)
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

## Redis多租户配置方式

1.需要在配置文件增加配置mode: NORMAL("单库"), SWITCH_DB("切换数据源"), TENANT_PREFIX("增加租户前缀");type是选择缓存类型目前只支持REDIS

```properties
spring:
  redis:
    mode: SWITCH_DB
    type: REDIS
    host: 
    port: 6379
    password: 
    database:
```

2.如果选择的是切换数据源的话可以选择配置tenant配置,租户编码则是不同客户的租户编码，到时候切换也是根据租户编码切换的

```properties
spring:
  redis:
    mode: SWITCH_DB
    type: REDIS
    host: 
    port: 6379
    password: 
    database: 
    tenant:
    	租户编码:
    		host: 
            port: 6379
            password: 
            database: 
```

3.如果不想选择配置文件配置的话也可以用代码方面调用 RedisInitUtils.init的方法，传入租户编码和redis配置以及注入一个RedisTenantContext对象



## MongoDB多租户配置方式

1.需要在配置文件增加配置mode: NORMAL("单库"), SWITCH_DB("切换数据源");

2.如果选择的是切换数据源的话可以选择配置tenant配置,租户编码则是不同客户的租户编码，到时候切换也是根据租户编码切换的

```properties
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

3.如果不想选择配置文件配置的话也可以用代码方面调用 MongoInitUtils.init的方法，传入租户编码和mongodb配置以及注入一个MongoTenantsContext对象

## 切换租户主要设置

```java
HeardHolder.setTenantCode("租户编码");
```

## 切换时区主要设置

需要在请求头中添加"Timezone"然后value值就是时区例：UTC+8或者GMT+8，如果不填则默认是系统时区，然后切换时区逻辑为：将传入的参数转换为"Timezone"时区时间后转换为系统默认时区，返回的时候就将返回参数中的时间转换为"Timezone"的时区时间

