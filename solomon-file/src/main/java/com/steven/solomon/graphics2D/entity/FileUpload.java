package com.steven.solomon.graphics2D.entity;

import cn.hutool.core.io.IoUtil;
import com.steven.solomon.rsa.Md5Utils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class FileUpload implements Serializable {

  String bucket;

  String fileName;

  String md5;

  Long size;

  public FileUpload(){
    super();
  }

  public FileUpload(String bucket, String fileName, InputStream is) throws IOException {
    this.bucket = bucket;
    this.fileName = fileName;
    this.size = Long.valueOf(is.available());
    this.md5 = Md5Utils.getFileMd5(IoUtil.readBytes(is));
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
