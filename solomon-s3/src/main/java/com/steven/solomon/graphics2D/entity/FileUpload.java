package com.steven.solomon.graphics2D.entity;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.digest.DigestUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class FileUpload implements Serializable {

  /**
   * 桶名
   */
  String bucket;
  /**
   * 文件名
   */
  String fileName;
  /**
   * 文件加密
   */
  String md5;
  /**
   * 文件大小
   */
  Long size;

  public FileUpload(){
    super();
  }

  public FileUpload(String bucket, String fileName, InputStream is) throws IOException {
    this.bucket = bucket;
    this.fileName = fileName;
    this.size = (long) is.available();
    this.md5 = DigestUtil.md5Hex(IoUtil.readBytes(is));
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }
}
