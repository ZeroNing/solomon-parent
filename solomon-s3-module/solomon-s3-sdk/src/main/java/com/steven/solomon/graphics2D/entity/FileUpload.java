package com.steven.solomon.graphics2D.entity;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.digest.DigestUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * 文件上传结果实体。
 *
 * <p>存储文件上传成功后的基本信息，包括存储桶名称、文件名、
 * 文件 MD5 校验值和文件大小。</p>
 */
public class FileUpload implements Serializable {

  /** 存储桶名称。 */
  String bucket;

  /** 文件名（含路径）。 */
  String fileName;

  /** 文件 MD5 校验值，用于完整性验证。 */
  String md5;

  /** 文件大小（字节数）。 */
  Long size;

  public FileUpload() {
    super();
  }

  /**
   * 构造上传结果。
   *
   * @param bucket   存储桶名称
   * @param fileName 文件名
   * @param is       文件输入流，用于计算 MD5 和大小
   * @throws IOException 读取输入流时可能抛出
   */
  public FileUpload(String bucket, String fileName, InputStream is) throws IOException {
    this.bucket = bucket;
    this.fileName = fileName;
    this.size = (long) is.available();
    this.md5 = DigestUtil.md5Hex(IoUtil.readBytes(is));
  }

  /**
   * 获取存储桶名称。
   *
   * @return 存储桶名称
   */
  public String getBucket() {
    return bucket;
  }

  /**
   * 设置存储桶名称。
   *
   * @param bucket 存储桶名称
   */
  public void setBucket(String bucket) {
    this.bucket = bucket;
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
   * 设置文件名。
   *
   * @param fileName 文件名
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * 获取文件 MD5 值。
   *
   * @return MD5 校验值
   */
  public String getMd5() {
    return md5;
  }

  /**
   * 设置文件 MD5 值。
   *
   * @param md5 MD5 校验值
   */
  public void setMd5(String md5) {
    this.md5 = md5;
  }

  /**
   * 获取文件大小。
   *
   * @return 文件大小（字节）
   */
  public Long getSize() {
    return size;
  }

  /**
   * 设置文件大小。
   *
   * @param size 文件大小（字节）
   */
  public void setSize(Long size) {
    this.size = size;
  }
}
