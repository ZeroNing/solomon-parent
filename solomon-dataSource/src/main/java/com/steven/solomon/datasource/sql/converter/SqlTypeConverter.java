package com.steven.solomon.datasource.sql.converter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * SQL类型转换器。
 *
 * <p>统一处理数据库返回值到Java字段类型的转换，以及Java参数到JDBC友好类型的转换。
 * 当前内置支持 {@link LocalDateTime}、{@link LocalDate}、{@link LocalTime}、
 * {@link Date}、{@link Long}、常见数字类型、布尔类型、枚举和字符串。</p>
 */
public final class SqlTypeConverter {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private SqlTypeConverter() {
  }

  /**
   * 判断目标类型是否可以按单列简单值读取。
   *
   * @param type 目标Java类型
   * @return true表示可以直接按单列值读取并转换
   */
  public static boolean isSimpleValueType(Class<?> type) {
    Class<?> targetType = wrapPrimitive(type);
    return CharSequence.class.isAssignableFrom(targetType)
        || Number.class.isAssignableFrom(targetType)
        || Boolean.class == targetType
        || Character.class == targetType
        || Date.class.isAssignableFrom(targetType)
        || java.sql.Date.class.isAssignableFrom(targetType)
        || Time.class.isAssignableFrom(targetType)
        || Timestamp.class.isAssignableFrom(targetType)
        || LocalDateTime.class == targetType
        || LocalDate.class == targetType
        || LocalTime.class == targetType
        || Instant.class == targetType
        || OffsetDateTime.class == targetType
        || Enum.class.isAssignableFrom(targetType);
  }

  /**
   * 将数据库返回值转换为指定Java类型。
   *
   * @param value 数据库原始值，可能来自ResultSet或聚合函数
   * @param targetType 目标Java类型，例如LocalDateTime、Date、Long
   * @param <T> 目标泛型类型
   * @return 转换后的Java值；value为null时返回null
   * @throws DataSourceException 转换失败时抛出国际化异常
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> T convertForJava(Object value, Class<T> targetType) throws DataSourceException {
    if (ObjectUtil.isNull(value)) {
      return null;
    }
    Class<?> wrappedType = wrapPrimitive(targetType);
    if (wrappedType.isInstance(value)) {
      return (T) value;
    }
    try {
      if (String.class == wrappedType) {
        return (T) String.valueOf(value);
      }
      if (Long.class == wrappedType) {
        return (T) Long.valueOf(toLong(value));
      }
      if (Integer.class == wrappedType) {
        return (T) Integer.valueOf(toNumber(value).intValue());
      }
      if (Short.class == wrappedType) {
        return (T) Short.valueOf(toNumber(value).shortValue());
      }
      if (Byte.class == wrappedType) {
        return (T) Byte.valueOf(toNumber(value).byteValue());
      }
      if (Double.class == wrappedType) {
        return (T) Double.valueOf(toNumber(value).doubleValue());
      }
      if (Float.class == wrappedType) {
        return (T) Float.valueOf(toNumber(value).floatValue());
      }
      if (BigDecimal.class == wrappedType) {
        return (T) toBigDecimal(value);
      }
      if (BigInteger.class == wrappedType) {
        return (T) toBigDecimal(value).toBigInteger();
      }
      if (Boolean.class == wrappedType) {
        return (T) Boolean.valueOf(toBoolean(value));
      }
      if (Character.class == wrappedType) {
        String str = String.valueOf(value);
        return StrUtil.isEmpty(str) ? null : (T) Character.valueOf(str.charAt(0));
      }
      if (LocalDateTime.class == wrappedType) {
        return (T) toLocalDateTime(value);
      }
      if (LocalDate.class == wrappedType) {
        return (T) toLocalDate(value);
      }
      if (LocalTime.class == wrappedType) {
        return (T) toLocalTime(value);
      }
      if (Instant.class == wrappedType) {
        return (T) toInstant(value);
      }
      if (OffsetDateTime.class == wrappedType) {
        return (T) OffsetDateTime.ofInstant(toInstant(value), ZoneId.systemDefault());
      }
      if (Timestamp.class == wrappedType) {
        return (T) Timestamp.from(toInstant(value));
      }
      if (java.sql.Date.class == wrappedType) {
        return (T) java.sql.Date.valueOf(toLocalDate(value));
      }
      if (Time.class == wrappedType) {
        return (T) Time.valueOf(toLocalTime(value));
      }
      if (Date.class.isAssignableFrom(wrappedType)) {
        return (T) Date.from(toInstant(value));
      }
      if (Enum.class.isAssignableFrom(wrappedType)) {
        return (T) Enum.valueOf((Class<Enum>) wrappedType, String.valueOf(value));
      }
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_TYPE_CONVERT_FAILED, e,
          value.getClass().getName(), targetType.getName(), String.valueOf(value));
    }
    throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_TYPE_CONVERT_FAILED,
        value.getClass().getName(), targetType.getName(), String.valueOf(value));
  }

  /**
   * 将Java参数转换为JDBC更稳定的参数类型。
   *
   * @param value Java原始参数，可能是LocalDateTime、Date、枚举或集合
   * @return 转换后的JDBC参数；集合会逐项转换
   */
  public static Object convertForJdbc(Object value) {
    if (ObjectUtil.isNull(value)) {
      return null;
    }
    if (value instanceof LocalDateTime localDateTime) {
      return Timestamp.valueOf(localDateTime);
    }
    if (value instanceof LocalDate localDate) {
      return java.sql.Date.valueOf(localDate);
    }
    if (value instanceof LocalTime localTime) {
      return Time.valueOf(localTime);
    }
    if (value instanceof Instant instant) {
      return Timestamp.from(instant);
    }
    if (value instanceof OffsetDateTime offsetDateTime) {
      return Timestamp.from(offsetDateTime.toInstant());
    }
    if (value instanceof java.sql.Date || value instanceof Time || value instanceof Timestamp) {
      return value;
    }
    if (value instanceof Date date) {
      return new Timestamp(date.getTime());
    }
    if (value instanceof Enum<?> enumValue) {
      return enumValue.name();
    }
    if (value instanceof Collection<?> collection) {
      List<Object> converted = new ArrayList<>(collection.size());
      for (Object item : collection) {
        converted.add(convertForJdbc(item));
      }
      return converted;
    }
    return value;
  }

  private static Number toNumber(Object value) {
    if (value instanceof Number number) {
      return number;
    }
    if (value instanceof Date date) {
      return date.getTime();
    }
    if (value instanceof LocalDateTime localDateTime) {
      return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    if (value instanceof LocalDate localDate) {
      return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    return new BigDecimal(String.valueOf(value).trim());
  }

  private static long toLong(Object value) {
    return toNumber(value).longValue();
  }

  private static BigDecimal toBigDecimal(Object value) {
    if (value instanceof BigDecimal bigDecimal) {
      return bigDecimal;
    }
    if (value instanceof BigInteger bigInteger) {
      return new BigDecimal(bigInteger);
    }
    if (value instanceof Number number) {
      return BigDecimal.valueOf(number.doubleValue());
    }
    return new BigDecimal(String.valueOf(value).trim());
  }

  private static boolean toBoolean(Object value) {
    if (value instanceof Boolean bool) {
      return bool;
    }
    if (value instanceof Number number) {
      return number.intValue() != 0;
    }
    String str = String.valueOf(value).trim();
    return "true".equalsIgnoreCase(str) || "1".equals(str) || "Y".equalsIgnoreCase(str)
        || "YES".equalsIgnoreCase(str);
  }

  private static LocalDateTime toLocalDateTime(Object value) {
    if (value instanceof Timestamp timestamp) {
      return timestamp.toLocalDateTime();
    }
    if (value instanceof java.sql.Date sqlDate) {
      return sqlDate.toLocalDate().atStartOfDay();
    }
    if (value instanceof Time time) {
      return LocalDate.now().atTime(time.toLocalTime());
    }
    if (value instanceof Date date) {
      return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
    if (value instanceof LocalDate localDate) {
      return localDate.atStartOfDay();
    }
    if (value instanceof Instant instant) {
      return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
    if (value instanceof OffsetDateTime offsetDateTime) {
      return offsetDateTime.toLocalDateTime();
    }
    if (value instanceof Number number) {
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()),
          ZoneId.systemDefault());
    }
    String str = String.valueOf(value).trim();
    try {
      return LocalDateTime.parse(str);
    } catch (DateTimeParseException ignored) {
      return LocalDateTime.parse(str.replace('T', ' '), DATE_TIME_FORMATTER);
    }
  }

  private static LocalDate toLocalDate(Object value) {
    if (value instanceof java.sql.Date sqlDate) {
      return sqlDate.toLocalDate();
    }
    if (value instanceof Timestamp timestamp) {
      return timestamp.toLocalDateTime().toLocalDate();
    }
    if (value instanceof Date date) {
      return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }
    if (value instanceof LocalDateTime localDateTime) {
      return localDateTime.toLocalDate();
    }
    if (value instanceof Instant instant) {
      return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
    }
    if (value instanceof Number number) {
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()),
          ZoneId.systemDefault()).toLocalDate();
    }
    return LocalDate.parse(String.valueOf(value).trim().substring(0, 10));
  }

  private static LocalTime toLocalTime(Object value) {
    if (value instanceof Time time) {
      return time.toLocalTime();
    }
    if (value instanceof Timestamp timestamp) {
      return timestamp.toLocalDateTime().toLocalTime();
    }
    if (value instanceof Date date) {
      return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalTime();
    }
    if (value instanceof LocalDateTime localDateTime) {
      return localDateTime.toLocalTime();
    }
    return LocalTime.parse(String.valueOf(value).trim());
  }

  private static Instant toInstant(Object value) {
    if (value instanceof Instant instant) {
      return instant;
    }
    if (value instanceof Timestamp timestamp) {
      return timestamp.toInstant();
    }
    if (value instanceof Date date) {
      return date.toInstant();
    }
    if (value instanceof LocalDateTime localDateTime) {
      return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
    if (value instanceof LocalDate localDate) {
      return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
    if (value instanceof Number number) {
      return Instant.ofEpochMilli(number.longValue());
    }
    return toLocalDateTime(value).atZone(ZoneId.systemDefault()).toInstant();
  }

  private static Class<?> wrapPrimitive(Class<?> type) {
    if (ObjectUtil.isNull(type) || !type.isPrimitive()) {
      return type;
    }
    if (long.class == type) {
      return Long.class;
    }
    if (int.class == type) {
      return Integer.class;
    }
    if (short.class == type) {
      return Short.class;
    }
    if (byte.class == type) {
      return Byte.class;
    }
    if (double.class == type) {
      return Double.class;
    }
    if (float.class == type) {
      return Float.class;
    }
    if (boolean.class == type) {
      return Boolean.class;
    }
    if (char.class == type) {
      return Character.class;
    }
    return type;
  }
}
