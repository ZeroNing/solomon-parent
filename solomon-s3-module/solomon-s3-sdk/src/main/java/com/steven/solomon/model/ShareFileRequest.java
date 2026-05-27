package com.steven.solomon.model;

import java.io.Serializable;

/**
 * 文件分享请求。
 */
public class ShareFileRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 桶名。
   */
  private String bucketName;

  /**
   * 对象名。
   */
  private String fileName;

  /**
   * 过期秒数。
   */
  private long expirySeconds = 3600;

  /**
   * 下载时展示的文件名，部分供应商支持。
   */
  private String downloadFileName;

  /**
   * 响应 Content-Type，部分供应商支持。
   */
  private String contentType;

  public static ShareFileRequest create() {
    return new ShareFileRequest();
  }

  public static ShareFileRequest file(String bucketName, String fileName) {
    return create().bucketName(bucketName).fileName(fileName);
  }

  public ShareFileRequest bucketName(String bucketName) {
    this.bucketName = bucketName;
    return this;
  }

  public ShareFileRequest fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  public ShareFileRequest expirySeconds(long expirySeconds) {
    this.expirySeconds = expirySeconds;
    return this;
  }

  public ShareFileRequest downloadFileName(String downloadFileName) {
    this.downloadFileName = downloadFileName;
    return this;
  }

  public ShareFileRequest contentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getFileName() {
    return fileName;
  }

  public long getExpirySeconds() {
    return expirySeconds;
  }

  public String getDownloadFileName() {
    return downloadFileName;
  }

  public String getContentType() {
    return contentType;
  }
}
