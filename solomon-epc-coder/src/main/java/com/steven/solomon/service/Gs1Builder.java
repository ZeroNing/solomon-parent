package com.steven.solomon.service;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;

/**
 * GS1 AI 条码链式构建器。
 */
public class Gs1Builder {

  private final EpcService service;

  private String barcode;

  private String ai01;

  private String ai21;

  private String ai8004;

  private int companyPrefixLength = 6;

  private int tagSize = 96;

  private int filter = 0;

  /**
   * 创建 GS1 构建器。
   *
   * @param service EPC 服务
   */
  Gs1Builder(EpcService service) {
    this.service = service;
  }

  /**
   * 设置完整 GS1 条码内容，例如 (01)06901234567892(21)ABC123。
   *
   * @param barcode GS1 条码内容
   * @return 当前构建器
   */
  public Gs1Builder barcode(String barcode) {
    this.barcode = barcode;
    return this;
  }

  /**
   * 设置 AI 01 的 GTIN 值。
   *
   * @param ai01 AI 01，GTIN，固定 14 位
   * @return 当前构建器
   */
  public Gs1Builder ai01(String ai01) {
    this.ai01 = ai01;
    return this;
  }

  /**
   * 设置 AI 21 的序列号值。
   *
   * @param ai21 AI 21，序列号
   * @return 当前构建器
   */
  public Gs1Builder ai21(String ai21) {
    this.ai21 = ai21;
    return this;
  }

  /**
   * 设置 AI 8004 的 GIAI 资产标识值。
   *
   * @param ai8004 AI 8004，GIAI
   * @return 当前构建器
   */
  public Gs1Builder ai8004(String ai8004) {
    this.ai8004 = ai8004;
    return this;
  }

  /**
   * 设置 GS1 公司前缀长度。
   *
   * @param companyPrefixLength 公司前缀长度，范围 6~12
   * @return 当前构建器
   */
  public Gs1Builder companyPrefixLength(int companyPrefixLength) {
    this.companyPrefixLength = companyPrefixLength;
    return this;
  }

  /**
   * 设置 EPC Tag Size。
   *
   * @param tagSize SGTIN 支持 96/198，GIAI 支持 96/202
   * @return 当前构建器
   */
  public Gs1Builder tagSize(int tagSize) {
    this.tagSize = tagSize;
    return this;
  }

  /**
   * 设置 EPC filter 值。
   *
   * @param filter filter 值，范围 0~7
   * @return 当前构建器
   */
  public Gs1Builder filter(int filter) {
    this.filter = filter;
    return this;
  }

  /**
   * 按当前 GS1 AI 字段生成 EPC。
   *
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult encode() throws BaseException {
    if (ai01 != null || ai21 != null) {
      return service.gtinSerialToEpc(ai01, ai21, companyPrefixLength, tagSize, filter)
          .setBarcode("(01)" + ai01 + "(21)" + ai21);
    }
    if (ai8004 != null) {
      return service.gs1ToEpc("(8004)" + ai8004, companyPrefixLength, tagSize, filter);
    }
    return service.gs1ToEpc(barcode, companyPrefixLength, tagSize, filter);
  }
}
