package com.steven.solomon.model;

import java.io.InputStream;
import java.io.Serializable;

/**
 * 分片上传请求。
 */
public class MultipartUploadRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String bucketName;

  private String fileName;

  private InputStream inputStream;

  private long fileSize;

  private long partSize;

  public static MultipartUploadRequest create() {
    return new MultipartUploadRequest();
  }

  public MultipartUploadRequest bucketName(String bucketName) {
    this.bucketName = bucketName;
    return this;
  }

  public MultipartUploadRequest fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  public MultipartUploadRequest inputStream(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  public MultipartUploadRequest fileSize(long fileSize) {
    this.fileSize = fileSize;
    return this;
  }

  public MultipartUploadRequest partSize(long partSize) {
    this.partSize = partSize;
    return this;
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getFileName() {
    return fileName;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public long getFileSize() {
    return fileSize;
  }

  public long getPartSize() {
    return partSize;
  }
}
