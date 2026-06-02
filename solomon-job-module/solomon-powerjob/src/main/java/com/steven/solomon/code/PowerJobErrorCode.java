package com.steven.solomon.code;

/**
 * PowerJob 模块错误码常量接口。
 *
 * <p>定义 PowerJob 模块中所有业务异常的标识符，
 * 配合 {@link com.steven.solomon.exception.BaseException} 使用。</p>
 */
public interface PowerJobErrorCode extends BaseExceptionCode {

  /** 指定的任务信息为空。 */
  String POWER_JOB_TASK_IS_NULL = "POWER_JOB_TASK_IS_NULL";

  /** PowerJob 管理端地址为空。 */
  String POWER_JOB_URL_NULL = "POWER_JOB_URL_NULL";

  /** PowerJob 登录用户名为空。 */
  String POWER_JOB_USER_NAME_NULL = "POWER_JOB_USER_NAME_NULL";

  /** PowerJob 登录密码为空。 */
  String POWER_JOB_PASSWORD_NULL = "POWER_JOB_PASSWORD_NULL";

  /** PowerJob POST 请求执行失败。 */
  String POWER_JOB_EXECUTE_POST_ERROR = "POWER_JOB_EXECUTE_POST_ERROR";

  /** 任务 ID 为空，更新或操作需要有效的 ID。 */
  String POWER_JOB_ID_IS_NULL = "POWER_JOB_ID_IS_NULL";

  /** 任务 ID 不为空，创建操作要求 ID 为空。 */
  String POWER_JOB_ID_IS_NOT_NULL = "POWER_JOB_ID_IS_NOT_NULL";

  /** PowerJob GET 请求执行失败。 */
  String POWER_JOB_EXECUTE_GET_ERROR = "POWER_JOB_EXECUTE_GET_ERROR";

  /** 指定的命名空间不存在。 */
  String POWER_JOB_NAMESPACE_NOT_FOUND = "POWER_JOB_NAMESPACE_NOT_FOUND";

  /** 指定的应用不存在。 */
  String POWER_JOB_APP_NOT_FOUND = "POWER_JOB_APP_NOT_FOUND";
}
