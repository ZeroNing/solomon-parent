package com.steven.solomon.job.xxl.code;
import com.steven.solomon.code.BaseExceptionCode;

/**
 * XXL-JOB 模块错误码常量接口。
 *
 * <p>定义 XXL-JOB 模块中所有业务异常的标识符，
 * 配合 {@link com.steven.solomon.exception.BaseException} 使用。</p>
 */
public interface XxlJobErrorCode extends BaseExceptionCode {

  /** XXL-JOB 管理端地址为空。 */
  String XXL_JOB_ADMIN_URL_IS_NULL = "XXL_JOB_ADMIN_URL_IS_NULL";

  /** XXL-JOB 登录用户名为空。 */
  String XXL_JOB_USERNAME_IS_NULL = "XXL_JOB_USERNAME_IS_NULL";

  /** XXL-JOB 登录密码为空。 */
  String XXL_JOB_PASSWORD_IS_NULL = "XXL_JOB_PASSWORD_IS_NULL";

  /** 登录响应未返回 Cookie。 */
  String XXL_JOB_COOKIE_IS_NULL = "XXL_JOB_COOKIE_IS_NULL";

  /** XXL-JOB 请求执行失败。 */
  String XXL_JOB_EXECUTE_ERROR = "XXL_JOB_EXECUTE_ERROR";

  /** 启动任务失败。 */
  String XXL_JOB_START_JOB_ERROR = "XXL_JOB_START_JOB_ERROR";

  /** 停止任务失败。 */
  String XXL_JOB_STOP_JOB_ERROR = "XXL_JOB_STOP_JOB_ERROR";

  /** 任务已存在，创建操作不允许重复。 */
  String XXL_JOB_TASK_IS_NOT_NULL ="XXL_JOB_TASK_IS_NOT_NULL";

  /** 任务不存在，更新或操作需要有效的任务。 */
  String XXL_JOB_TASK_IS_NULL ="XXL_JOB_TASK_IS_NULL";
}
