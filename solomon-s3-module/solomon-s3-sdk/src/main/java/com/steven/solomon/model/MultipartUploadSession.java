package com.steven.solomon.model;

import java.io.Serializable;

/**
 * 分片上传会话信息。
 *
 * <p>记录一次分片上传的会话信息，包括存储桶名称、文件名和上传 ID，
 * 用于后续分片上传和完成操作。</p>
 */
public class MultipartUploadSession implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 存储桶名称。 */
  private String bucketName;

  /** 文件名。 */
  private String fileName;

  /** 分片上传任务 ID。 */
  private String uploadId;

  /**
   * 构造分片上传会话。
   *
   * @param bucketName 存储桶名称
   * @param fileName   文件名
   * @param uploadId   分片上传任务 ID
   */
  public MultipartUploadSession(String bucketName, String fileName, String uploadId) {
    this.bucketName = bucketName;
    this.fileName = fileName;
    this.uploadId = uploadId;
  }

  /**
   * 获取存储桶名称。
   *
   * @return 存储桶名称
   */
  public String getBucketName() {
    return bucketName;
  }

  /**
   * 获取文件名。
   *
   * @return 文件名
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * 获取分片上传任务 ID。
   *
   * @return 上传任务 ID
   */
  public String getUploadId() {
    return uploadId;
  }
}
