# solomon-parent基础框架

## 引言

这个项目主要是总结了工作上遇到的问题以及学习一些框架用于整合



## 技术选型

| 框架                 | 说明                                     | 版本   |
| -------------------- | ---------------------------------------- | ------ |
| Spring cloud Alibaba | 微服务框架                               | 2021.1 |
| Spring cloud         | 微服务框架                               | 2020.0 |
| Nacos                | 配置中心 & 注册中心                      | 2.1.1  |
| RabbitMq             | 消息队列                                 | 2.4.2  |
| Sentinel             | 阿里流量防卫兵(限流、熔断降级、负载保护) | 1.8    |
| Redis                | 缓存                                     | 2.4.2  |
| SpringBoot           | Spring Boot                              | 2.4.2  |
| mongoDb              | 分布式文件存储的数据库                   | 2.4.2  |
| Minio                | 分布式对象存储服务器                     | 8.2.1  |
| Mysql                | 数据库服务器                             | 8.0+   |
| Mybatis-plus         | MyBatis 增强工具包                       | 3.5.1  |
| Easy-Excel           | Excel处理工具                            | 3.0.5  |

## 项目描述

| 项目名                   | 说明                                                         |
| ------------------------ | ------------------------------------------------------------ |
| solomon-base             | 主要是封装了底层的异常捕获以及通用返回实体类                 |
| solomon-cache            | 引入了data模块，支持了动态切换缓存数据源以及增加租户编码前缀的缓存KEY |
| solomon-common           | 引入base模块基础上增加了对微服务单体服务以及单体服务的异常捕获 |
| solomon-constnt          | 主要就是写入了一部分异常编码常量以及缓存时间的值             |
| solomon-data             | 主要用于底层数据实体类以及动态切换数据源模板                 |
| solomon-exception        | 主要封装了底层通用常见的异常，并且返回国际化错误             |
| solomon-file             | 主要封装了有关于S3协议下文分布式对象存储接口，如:阿里云、腾讯云、minio、百度云、华为云等等 |
| solomon-gateway-sentinel | 简单封装了gateway网关以及Sentinel的异常捕获，并支持动态修改nacos中的限流配置 |
| solomon-i18n             | 国际化配置，支持扫描Jar包内国际化文件                        |
| solomon-mongodb          | 引入了data模块，支持了动态切换缓存数据源，以及封装了部分底层查询方法 |
| solomon-mq               | 支持rabbitmq以注解形式配置重试次数以及注册队列，并将通用的业务抽出来，让用户只关注业务逻辑的实现 |
| solomon-mybatis          | 主要是加入了一个通用实体类，未来或许考虑加入动态切换数据源   |
| solomon-utils            | 主要封装了一些通用的工具并支持用@JsonEnum注解国际化数据库的值 |

