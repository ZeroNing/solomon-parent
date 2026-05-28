package com.steven.solomon.code;

/**
 * 基础异常错误码接口，定义系统通用的异常编码。
 *
 * <p>错误码采用字母编码体系，用于国际化消息的键匹配。
 * 业务项目可扩展该接口定义自己的异常编码。</p>
 */
public interface BaseExceptionCode {

  /**
   * 切换数据源失败
   */
  String FAILED_TO_SWITCH_DATA_SOURCE = "FAILED_TO_SWITCH_DATA_SOURCE";

  /**
   * 请求方式错误
   */
  String REQUEST_METHOD_ERROR = "S9991";


  /**
   * 非法请求
   */
  String BAD_REQUEST = "S9992";

  /**
   * 服务调用失败
   */
  String SERVICE_CALL_ERROR="S9993";

  /**
   * 系统限流
   */
  String SYSTEM_LIMITING = "S9994";

  /**
   * 系统熔断
   */
  String SYSTEM_FUSING = "S10000";

  /**
   * 参数错误
   */
  String PARAMETER_ERROR_CODE            = "S9995";

  /**
   * 参数异常
   */
  String PARAMETER_EXCEPTION_CODE = "S9996";

  /**
   * 无权访问
   */
  String NO_ACCESS_EXCEPTION_CODE         = "S9997";
  /**
   * 对不起，请勿重复请求
   */
  String ACCESS_EXCEPTION_CODE            = "S9998";
  /**
   * 系统异常，请联系客服人员后，稍后在试
   */
  String BASE_EXCEPTION_CODE              = "S9999";
  /**
   * 文件不存在，稍后在试
   */
  String FILE_IS_NOT_EXIST_EXCEPTION_CODE = "F9999";
  /**
   * 对不起，登录失败，账号密码错误,稍后再试
   */
  String LOGIN_EXCEPTION_CODE             = "L0000";
  /**
   * 登陆已过期，请重新登录
   */
  String LOGIN_TOKEN_EXCEPTION_CODE       = "L0001";

  /**
   * id不能为空
   */
  String ID_NOT_NULL="ID_NOT_NULL";

  /**
   * mongo配置文件为空
   */
  String MONGODB_PROPERTIES_IS_NULL="MONGODB_PROPERTIES_IS_NULL";

  /**
   * 文件类型不在允许范围内
   */
  String FILE_TYPE_NOT_WITHIN_THE_ALLOWABLE_RANGE = "FILE_TYPE_NOT_WITHIN_THE_ALLOWABLE_RANGE";

  /**
   * 上传文件大小超过最大限制
   */
  String FILE_UPLOAD_MAX_SIZE = "FILE_UPLOAD_MAX_SIZE";

  /**
   * 文件存在高危风险（病毒/恶意内容）
   */
  String FILE_HIGH_RISK = "FILE_HIGH_RISK";
}
