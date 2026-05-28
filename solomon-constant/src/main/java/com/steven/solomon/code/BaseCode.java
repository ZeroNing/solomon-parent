package com.steven.solomon.code;

/**
 * 基础常量接口，定义系统通用的键名、编码和前缀。
 *
 * <p>该接口集中管理异常响应体字段名、HTTP头名称、认证前缀等全局常量，
 * 避免业务代码中硬编码字符串。</p>
 */
public interface BaseCode {

  /** 字符编码 UTF-8。 */
  String UTF8 = "UTF-8";

  /** 异常响应体中的 HTTP 状态码字段名。 */
  String HTTP_STATUS = "status";

  /** 异常响应体中的错误码字段名。 */
  String ERROR_CODE = "errorCode";

  /** 异常响应体中的消息字段名。 */
  String MESSAGE = "message";

  /** 默认成功响应短语。 */
  String DEFAULT_SUCCESS_PHRASE = "Success";

  /** 响应体中的数据字段名。 */
  String JSON_DATA = "data";

  /** 异常响应体中的服务标识字段名。 */
  String SERVER_ID = "serverId";

  /** HTTP Accept-Language 头名称。 */
  String HTTP_ACCEPT_LANGUAGE = "Accept-Language";

  /** 枚举国际化消息的键前缀。 */
  String BASE_ENUM_CODE = "ENUM_CODE_";

  /** Authorization 头中 Bearer Token 的前缀。 */
  String HEADER_PREFIX = "Bearer ";

  /** 异常响应体中的请求ID字段名。 */
  String REQUEST_ID = "requestId";

  /** HTTP Timezone 头名称。 */
  String TIMEZONE = "Timezone";

  /** 默认租户标识。 */
  String DEFAULT = "default";
}
