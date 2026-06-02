package com.steven.solomon.model;

import java.io.InputStream;
import java.io.Serializable;

/**
 * 分片上传请求。
 *
 * <p>封装分片上传所需的参数，包含存储桶、文件名、输入流、
 * 文件大小和分片大小等信息。</p>
 */
public class MultipartUploadRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 存储桶名称。 */
  private String bucketName;

  /** 文件名。 */
  private String fileName;

  /** 文件输入流。 */
  private InputStream inputStream;

  /** 文件总大小（字节）。 */
  private long fileSize;

  /** 每个分片的大小（字节）。 */
  private long partSize;

  /**
   * 创建分片上传请求实例。
   *
   * @return 请求实例
   */
  public static MultipartUploadRequest create() {
    return new MultipartUploadRequest();
  }

  /**
   * 设置存储桶名称。
   *
   * @param bucketName 存储桶名称
   * @return 当前请求实例（链式调用）
   */
  public MultipartUploadRequest bucketName(String bucketName) {
    this.bucketName = bucketName;
    return this;
  }

  /**
   * 设置文件名。
   *
   * @param fileName 文件名
   * @return 当前请求实例（链式调用）
   */
  public MultipartUploadRequest fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  /**
   * 设置文件输入流。
   *
   * @param inputStream 文件输入流
   * @return 当前请求实例（链式调用）
   */
  public MultipartUploadRequest inputStream(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  /**
   * 设置文件总大小。
   *
   * @param fileSize 文件大小（字节）
   * @return 当前请求实例（链式调用）
   */
  public MultipartUploadRequest fileSize(long fileSize) {
    this.fileSize = fileSize;
    return this;
  }

  /**
   * 设置分片大小。
   *
   * @param partSize 分片大小（字节）
   * @return 当前请求实例（链式调用）
   */
  public MultipartUploadRequest partSize(long partSize) {
    this.partSize = partSize;
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
   * 获取文件名。
   *
   * @return 文件名
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * 获取文件输入流。
   *
   * @return 文件输入流
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  /**
   * 获取文件总大小。
   *
   * @return 文件大小（字节）
   */
  public long getFileSize() {
    return fileSize;
  }

  /**
   * 获取分片大小。
   *
   * @return 分片大小（字节）
   */
  public long getPartSize() {
    return partSize;
  }
}
