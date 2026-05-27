package com.steven.solomon.model;

import java.io.Serializable;

/**
 * 分片上传会话。
 */
public class MultipartUploadSession implements Serializable {

  private static final long serialVersionUID = 1L;

  private String bucketName;

  private String fileName;

  private String uploadId;

  public MultipartUploadSession(String bucketName, String fileName, String uploadId) {
    this.bucketName = bucketName;
    this.fileName = fileName;
    this.uploadId = uploadId;
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getFileName() {
    return fileName;
  }

  public String getUploadId() {
    return uploadId;
  }
}
