package com.steven.solomon.code;

/**
 * 文件存储模块错误码定义。
 *
 * <p>定义文件上传、下载、分享等操作中可能出现的错误码常量。</p>
 */
public interface FileErrorCode extends BaseExceptionCode {

  /** 未找到可用的存储实现，未引入或未启用供应商模块。 */
  String NO_STORAGE_IMPLEMENTATION = "NO_STORAGE_IMPLEMENTATION";

  /** 分享时间超出允许范围。 */
  String MORE_THAN_THE_SHARING_TIME = "MORE_THAN_THE_SHARING_TIME";

  /** 文件下载失败。 */
  String FILE_DOWNLOAD_ERROR = "FILE_DOWNLOAD_ERROR";

  /** 方法未实现。 */
  String METHOD_NOT_IMPLEMENTED = "METHOD_NOT_IMPLEMENTED";

  /** 上传请求参数无效。 */
  String INVALID_UPLOAD_REQUEST = "INVALID_UPLOAD_REQUEST";
}
