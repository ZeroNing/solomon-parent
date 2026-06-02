package com.steven.solomon.model;

import java.io.Serializable;

/**
 * 单个分片上传结果。
 *
 * <p>记录每个分片上传成功后的分片编号和 ETag，
 * 用于完成分片上传时提交给服务端进行校验和合并。</p>
 */
public class PartUploadResult implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 分片编号，从 1 开始。 */
  private int partNumber;

  /** 分片的 ETag 标识。 */
  private String eTag;

  /**
   * 构造分片上传结果。
   *
   * @param partNumber 分片编号
   * @param eTag       分片 ETag
   */
  public PartUploadResult(int partNumber, String eTag) {
    this.partNumber = partNumber;
    this.eTag = eTag;
  }

  /**
   * 获取分片编号。
   *
   * @return 分片编号
   */
  public int getPartNumber() {
    return partNumber;
  }

  /**
   * 获取分片 ETag。
   *
   * @return ETag 字符串
   */
  public String getETag() {
    return eTag;
  }
}
