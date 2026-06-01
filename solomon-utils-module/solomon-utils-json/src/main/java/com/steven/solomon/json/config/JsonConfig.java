package com.steven.solomon.json.config;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfig {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        // 创建一个新的 ObjectMapper 实例，这是 Jackson 用于处理 JSON 数据的核心类
        ObjectMapper mapper = new ObjectMapper();

        // 创建一个 JavaTimeModule 模块，用于支持 Java 8 时间 API（如 LocalDateTime、LocalDate、LocalTime）
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        /** 序列化配置, 针对 Java 8 时间 **/

        // 为 LocalDateTime 类型配置自定义序列化器，使用指定的日期格式 "yyyy-MM-dd HH:mm:ss.SSS"
        javaTimeModule.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        // 为 LocalDate 类型配置序列化器，使用指定的日期格式 "yyyy-MM-dd"
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // 为 LocalTime 类型配置序列化器，使用指定的时间格式 "HH:mm:ss"
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // 为传统的 Date 类型配置序列化器，使用指定的日期格式 "yyyy-MM-dd HH:mm:ss:SSS"
        javaTimeModule.addSerializer(Date.class, new DateSerializer(false, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")));

        /** 反序列化配置, 针对 Java 8 时间 **/

        // 为 LocalDateTime 类型配置自定义反序列化器，使用指定的日期格式 "yyyy-MM-dd HH:mm:ss.SSS"
        javaTimeModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        // 为 LocalDate 类型配置反序列化器，使用指定的日期格式 "yyyy-MM-dd"
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // 为 LocalTime 类型配置反序列化器，使用指定的时间格式 "HH:mm:ss"
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // 为传统的 Date 类型配置反序列化器，需要自定义反序列化器
        javaTimeModule.addDeserializer(Date.class, new CustomDateDeserializer());

        // 创建一个 SimpleModule，用于添加自定义的序列化逻辑
        SimpleModule simpleModule = new SimpleModule();

        // 将 Long 类型及其基本类型序列化为字符串，防止 JavaScript 精度丢失
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 将 BigInteger 类型序列化为字符串
        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);

        // 禁用将日期序列化为时间戳，使用可读性更好的日期格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 禁用日期反序列化时调整到上下文时区的功能
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        // 禁用空 Bean 序列化时抛出异常的功能
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // 注册 JavaTimeModule 和 SimpleModule 到 ObjectMapper，以应用前面配置的所有序列化和反序列化规则
        mapper.registerModule(javaTimeModule)
                .registerModule(new ParameterNamesModule()) // 使 Jackson 能够识别 Java 8 的方法参数名称
                .registerModule(simpleModule);

        // 配置序列化时包含所有对象的属性，不忽略任何字段
        mapper.setSerializationInclusion(Include.ALWAYS);

        // 配置反序列化时遇到未知属性不抛出异常，增加容错性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 返回配置完成的 ObjectMapper 实例
        return mapper;
    }

}
