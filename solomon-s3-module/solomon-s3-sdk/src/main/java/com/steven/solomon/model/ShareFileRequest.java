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

  /**
   * 设置存储桶名称。
   *
   * @param bucketName 存储桶名称
   * @return 当前请求实例（链式调用）
   */
  public ShareFileRequest bucketName(String bucketName) {
    this.bucketName = bucketName;
    return this;
  }

  /**
   * 设置对象名。
   *
   * @param fileName 对象名
   * @return 当前请求实例（链式调用）
   */
  public ShareFileRequest fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  /**
   * 设置分享过期秒数。
   *
   * @param expirySeconds 过期秒数
   * @return 当前请求实例（链式调用）
   */
  public ShareFileRequest expirySeconds(long expirySeconds) {
    this.expirySeconds = expirySeconds;
    return this;
  }

  /**
   * 设置下载时展示的文件名。
   *
   * @param downloadFileName 下载文件名
   * @return 当前请求实例（链式调用）
   */
  public ShareFileRequest downloadFileName(String downloadFileName) {
    this.downloadFileName = downloadFileName;
    return this;
  }

  /**
   * 设置响应 Content-Type。
   *
   * @param contentType 内容类型
   * @return 当前请求实例（链式调用）
   */
  public ShareFileRequest contentType(String contentType) {
    this.contentType = contentType;
    return this;
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
   * 获取对象名。
   *
   * @return 对象名
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * 获取分享过期秒数。
   *
   * @return 过期秒数
   */
  public long getExpirySeconds() {
    return expirySeconds;
  }

  /**
   * 获取下载文件名。
   *
   * @return 下载文件名
   */
  public String getDownloadFileName() {
    return downloadFileName;
  }

  /**
   * 获取响应内容类型。
   *
   * @return 内容类型
   */
  public String getContentType() {
    return contentType;
  }
}
