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
| ------------------------ | ------------------------------------------------------------ |
| solomon-base             | 主要是封装了底层的异常捕获以及通用返回实体类                 |
| solomon-cache            | 引入了data模块，支持了动态切换缓存数据源以及增加租户编码前缀的缓存KEY |
| solomon-common           | 引入base模块基础上增加了对微服务单体服务以及单体服务的异常捕获 |
| solomon-constnt          | 主要就是写入了一部分异常编码常量以及缓存时间的值             |
| solomon-data             | 主要用于底层数据实体类以及动态切换数据源模板                 |
| solomon-file             | 主要封装了有关于S3协议下文分布式对象存储接口，如:阿里云、腾讯云、minio、百度云、华为云等等 |
| solomon-gateway-sentinel | 简单封装了gateway网关以及Sentinel的异常捕获，并支持动态修改nacos中的限流配置 |
| solomon-i18n             | 国际化配置，支持扫描Jar包内国际化文件并封装了底层通用常见的异常，并且返回国际化错误 |
| solomon-mongodb          | 引入了data模块，支持了动态切换缓存数据源，以及封装了部分底层查询方法 |
| solomon-mq               | 封装一些基础MQ类                                             |
| solomon-mqtt             | 支持Mqtt以注解形式配置消息质量以及主题，并将通用的业务抽出来，让用户只关注业务逻辑的实现 |
| solomon-rabbitmq         | 支持rabbitmq以注解形式配置重试次数以及注册队列，并将通用的业务抽出来，让用户只关注业务逻辑的实现 |
| solomon-mybatis          | 主要是加入了一个通用实体类，未来或许考虑加入动态切换数据源   |
| solomon-utils            | 主要封装了一些通用的工具并支持用@JsonEnum注解国际化数据库的值 |

## 注意事项

1.项目启动时侯需要扫描Jar包路径（"com.steven"）如：

```java
@SpringBootApplication(scanBasePackages = {"com.test","com.steven"})
public class TestApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

}
```

```java
@ComponentScan(value = {"com.steven"})
public class TestApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

}
```



## 自定义配置说明

### 国际化配置说明

```yaml
i18n:
  all-locale:         #目前用到的国际化语言
  language:           #设置默认国际化语言
  path:               #国际化文件路径
```

### RabbitMq配置说明

```yaml
rabbit:
  host:               #连接地址
  port:               #端口 
  user-name:          #用户名
  password:           #密码
```

### Mqtt配置说明

```yaml
mqtt：
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



### 缓存配置说明

```yaml
cache:
  mode:              #缓存模式（NORMAL:单库、SWITCH_DB:切换数据源,TENANT_PREFIX:单库模式在key前面增加租户编码）   
  type:              #缓存类型（REDIS）
  redis-profile:     #redis配置
    host:            #连接地址
    port:            #端口
    password:        #密码
```

### 文件配置说明

```yaml
file:
  choice:             #文件选择器（MINIO:minio对象存储、DEFAULT:无文件存储实现、OSS:阿里云、OBS:华为云、COS:腾讯云、BOS:百度云）
  file-naming-method: #文件命名选择器(ORIGINAL:原文件名称、DATE:文件名精确到毫秒并且以年月做为文件夹名、UUID:UUID命名、SNOWFLAKE:雪花id命名)
  endpoint:        # 连接地址
  accessKey:       # 访问密钥
  secretKey:       # 密钥
  bucket-name:     # 桶名 我这是给出了一个默认桶名
  rootDirectory:   # 根目录
  region-name:     # 地区名
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

1.需要实现RedisClientPropertiesService接口的getRedisClient方法，将所有租户的redis配置信息都在这里返回，然后再项目启动时侯就会自动初始化

2.也可以手动初始化数据，调用RedisInitUtils此类中的init方法，就可以手动将多租户redis加入到当前项目中

## MongoDB多租户配置方式

1.需要实现MongoClientPropertiesService接口的getMongoClient方法，将所有租户的mongoDB配置信息都在这里返回，然后再项目启动时侯就会自动初始化

2.也可以手动初始化数据，调用MongoInitUtils此类中的init方法，就可以手动将多租户mongoDB加入到当前项目中

3.通过实现MongoClientPropertiesService接口的setCappedCollectionNameMap方法，可以实现MongoDB的限制大小和行数，并且在类中加上@MongoDBCapped注解配置大小和行数，在项目启动时候初始化，也可以手动初始化