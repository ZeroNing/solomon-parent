package com.steven.solomon.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.steven.solomon.json.config.JsonConfig;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

/**
 * Jackson JSON 工具类。
 *
 * <p>项目已经统一使用 Jackson，这里保留常用的对象转换、集合转换和带视图过滤的序列化入口。</p>
 */
public final class JackJsonUtils {

  private static final Logger LOGGER = LoggerUtils.logger(JackJsonUtils.class);

  private static final ObjectMapper MAPPER = new JsonConfig().objectMapper();

  private JackJsonUtils() {}

  public static <T> T convertValue(Object obj, Class<T> clazz) {
    return convertValue(obj, clazz, false);
  }

  /**
   * 将任意对象转换成目标类型。
   *
   * @param isIgnoreNull true 时忽略未知字段，适合外部数据结构向内部对象转换
   */
  public static <T> T convertValue(Object obj, Class<T> clazz, boolean isIgnoreNull) {
    ObjectMapper targetMapper = isIgnoreNull
        ? MAPPER.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        : MAPPER;
    return targetMapper.convertValue(obj, clazz);
  }

  /**
   * 将 JSON 字符串转换成指定类型。
   */
  public static <T> T conversionClass(String json, Class<T> clazz) throws IOException {
    return MAPPER.readValue(json, clazz);
  }

  /**
   * 将 JSON 字符串转换成带泛型的类型。
   */
  public static <T> T conversionClass(String json, TypeReference<T> typeReference)
      throws IOException {
    return MAPPER.readValue(json, typeReference);
  }

  /**
   * 将 JSON 数组转换成指定元素类型的列表，并忽略目标类不存在的字段。
   */
  public static <T> List<T> conversionClassList(String json, Class<T> clazz)
      throws IOException {
    CollectionType listType =
        MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
    ObjectReader reader =
        MAPPER.readerFor(listType).without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return reader.readValue(json);
  }

  public static String formatJsonByFilter(Object result) {
    return formatJsonByFilter(result, null);
  }

  /**
   * 序列化对象。
   *
   * @param filter Jackson View 类型；为空时按默认规则序列化
   * @return 序列化失败时返回空字符串，保持历史调用方兼容
   */
  public static String formatJsonByFilter(Object result, Class<?> filter) {
    try {
      return ValidateUtils.isEmpty(filter)
          ? MAPPER.writeValueAsString(result)
          : MAPPER.writerWithView(filter).writeValueAsString(result);
    } catch (Throwable e) {
      LOGGER.error("转换 JSON 出现异常:", e);
      return "";
    }
  }
}
