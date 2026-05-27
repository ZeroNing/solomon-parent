package com.steven.solomon.model;

import java.io.Serializable;

/**
 * 单个分片上传结果。
 */
public class PartUploadResult implements Serializable {

  private static final long serialVersionUID = 1L;

  private int partNumber;

  private String eTag;

  public PartUploadResult(int partNumber, String eTag) {
    this.partNumber = partNumber;
    this.eTag = eTag;
  }

  public int getPartNumber() {
    return partNumber;
  }

  public String getETag() {
    return eTag;
  }
}
