package com.steven.solomon.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

public class JackJsonUtils {

    private static final Logger logger = LoggerUtils.logger(JackJsonUtils.class);

    private static final ObjectMapper mapper = SpringUtil.getBean(ObjectMapper.class);

    public static <T> T convertValue(Object obj, Class<T> clazz) {
        return convertValue(obj, clazz, false);
    }

    public static <T> T convertValue(Object obj, Class<T> clazz, boolean isIgnoreNull) {
        if (isIgnoreNull) {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return mapper.convertValue(obj, clazz);
    }

    /**
     * 转换对象
     */
    public static <T> T conversionClass(String json, Class<T> t) throws IOException {
        return mapper.readValue(json, t);
    }

    public static <T> T conversionClass(String json, TypeReference<T> typeReference) throws IOException {
        return mapper.readValue(json, typeReference);
    }

    /**
     * 转换数组对象
     */
    public static <T> List<T> conversionClassList(String json, Class<T> clazz) throws IOException {
        //忽略多余属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
        return mapper.readValue(json, listType);
    }

    /**
     * 转换json
     */
    public static String formatJsonByFilter(Object result) {
        return formatJsonByFilter(result, null);
    }


    /**
     * 转换json
     */
    public static String formatJsonByFilter(Object result, Class<?> filter) {
        try {
            return ValidateUtils.isEmpty(filter) ? mapper.writeValueAsString(result) : mapper.writerWithView(filter).writeValueAsString(result);
        } catch (Throwable e) {
            logger.error("转换Json出现异常:", e);
            return "";
        }
    }

}
