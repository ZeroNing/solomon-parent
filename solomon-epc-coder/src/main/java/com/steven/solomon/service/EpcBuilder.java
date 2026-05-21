package com.steven.solomon.service;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;

/**
 * SGTIN 链式构建器。
 */
public class EpcBuilder {

  private final EpcService service;

  private String barcode;

  private String serialNumber;

  private int companyPrefixLength = 6;

  private int tagSize = 96;

  private int filter = 0;

  /**
   * 创建 SGTIN 构建器。
   *
   * @param service EPC 服务
   */
  EpcBuilder(EpcService service) {
    this.service = service;
  }

  /**
   * 设置 GTIN 或完整 GS1 条码内容。
   *
   * @param barcode GTIN 或 GS1 条码
   * @return 当前构建器
   */
  public EpcBuilder barcode(String barcode) {
    this.barcode = barcode;
    return this;
  }

  /**
   * 设置 GTIN。
   *
   * @param gtin GTIN
   * @return 当前构建器
   */
  public EpcBuilder gtin(String gtin) {
    this.barcode = gtin;
    return this;
  }

  /**
   * 设置序列号。
   *
   * @param serialNumber 序列号
   * @return 当前构建器
   */
  public EpcBuilder serial(String serialNumber) {
    this.serialNumber = serialNumber;
    return this;
  }

  /**
   * 设置序列号。
   *
   * @param serialNumber 序列号
   * @return 当前构建器
   */
  public EpcBuilder serialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
    return this;
  }

  /**
   * 设置 GS1 公司前缀长度。
   *
   * @param companyPrefixLength 公司前缀长度，范围 6~12
   * @return 当前构建器
   */
  public EpcBuilder companyPrefixLength(int companyPrefixLength) {
    this.companyPrefixLength = companyPrefixLength;
    return this;
  }

  /**
   * 设置 EPC Tag Size。
   *
   * @param tagSize SGTIN 支持 96/198
   * @return 当前构建器
   */
  public EpcBuilder tagSize(int tagSize) {
    this.tagSize = tagSize;
    return this;
  }

  /**
   * 设置 EPC filter 值。
   *
   * @param filter filter 值，范围 0~7
   * @return 当前构建器
   */
  public EpcBuilder filter(int filter) {
    this.filter = filter;
    return this;
  }

  /**
   * 生成 EPC。
   *
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult encode() throws BaseException {
    if (serialNumber == null && barcode != null && barcode.startsWith("(")) {
      return service.gs1ToEpc(barcode, companyPrefixLength, tagSize, filter);
    }
    return service.gtinSerialToEpc(barcode, serialNumber, companyPrefixLength, tagSize, filter);
  }

  /**
   * 生成 EPC，兼容 generate 命名。
   *
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult generate() throws BaseException {
    return encode();
  }
}
