package com.steven.solomon.model;

/**
 * EPC（电子产品编码）编解码结果。
 * <p>
 * 包含 EPC 编码或解码后的所有字段信息，
 * 支持 SGTIN-96、SGTIN-198、GIAI-96、GIAI-202 等类型的 EPC。
 * </p>
 *
 * @author 创建者
 */
public class EpcResult {

  /** EPC 类型，例如 SGTIN-96、SGTIN-198、GIAI-96、GIAI-202。 */
  private String type;

  /** EPC 的真实 bit 长度。 */
  private int bitLength;

  /** EPC 十六进制编码。 */
  private String hex;

  /** EPC 二进制编码。 */
  private String binary;

  /** EPC Tag URI。 */
  private String uri;

  /** 原始 GS1 条码内容。 */
  private String barcode;

  /** GS1 AI 01（GTIN）字段值。 */
  private String ai01;

  /** GS1 AI 21（序列号）字段值。 */
  private String ai21;

  /** GS1 AI 8004（GIAI 资产标识）字段值。 */
  private String ai8004;

  /** 14 位 GTIN。 */
  private String gtin14;

  /** EPC 序列号。 */
  private String serial;

  /** EPC 序列号兼容字段（与 serial 同步）。 */
  private String serialNumber;

  /** GS1 公司前缀。 */
  private String companyPrefix;

  /** SGTIN 的项目参考（Item Reference）。 */
  private String itemReference;

  /** GIAI 的资产参考。 */
  private String assetReference;

  /** EPC filter 值。 */
  private int filter;

  /** GS1 公司前缀长度。 */
  private int companyPrefixLength;

  /** 编解码结果是否有效。 */
  private boolean valid;

  /**
   * 获取 EPC 类型。
   *
   * @return EPC 类型字符串，如 "SGTIN-96"
   */
  public String getType() {
    return type;
  }

  /**
   * 设置 EPC 类型。
   *
   * @param type EPC 类型字符串
   * @return 当前结果对象
   */
  public EpcResult setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * 获取 EPC 真实 bit 长度。
   *
   * @return bit 长度
   */
  public int getBitLength() {
    return bitLength;
  }

  /**
   * 设置 EPC 真实 bit 长度。
   *
   * @param bitLength bit 长度
   * @return 当前结果对象
   */
  public EpcResult setBitLength(int bitLength) {
    this.bitLength = bitLength;
    return this;
  }

  /**
   * 获取 EPC 十六进制编码。
   *
   * @return 十六进制字符串
   */
  public String getHex() {
    return hex;
  }

  /**
   * 设置 EPC 十六进制编码。
   *
   * @param hex 十六进制字符串
   * @return 当前结果对象
   */
  public EpcResult setHex(String hex) {
    this.hex = hex;
    return this;
  }

  /**
   * 获取 EPC 二进制编码。
   *
   * @return 二进制字符串
   */
  public String getBinary() {
    return binary;
  }

  /**
   * 设置 EPC 二进制编码。
   *
   * @param binary 二进制字符串
   * @return 当前结果对象
   */
  public EpcResult setBinary(String binary) {
    this.binary = binary;
    return this;
  }

  /**
   * 获取 EPC Tag URI。
   *
   * @return URI 字符串，如 "urn:epc:tag:sgtin-96:0.690123.123456.ABC"
   */
  public String getUri() {
    return uri;
  }

  /**
   * 设置 EPC Tag URI。
   *
   * @param uri URI 字符串
   * @return 当前结果对象
   */
  public EpcResult setUri(String uri) {
    this.uri = uri;
    return this;
  }

  /**
   * 获取原始 GS1 条码内容。
   *
   * @return GS1 条码字符串
   */
  public String getBarcode() {
    return barcode;
  }

  /**
   * 设置原始 GS1 条码内容。
   *
   * @param barcode GS1 条码字符串
   * @return 当前结果对象
   */
  public EpcResult setBarcode(String barcode) {
    this.barcode = barcode;
    return this;
  }

  /**
   * 获取 AI 01（GTIN）值。
   *
   * @return GTIN 字符串
   */
  public String getAi01() {
    return ai01;
  }

  /**
   * 设置 AI 01（GTIN）值。
   *
   * @param ai01 GTIN 字符串
   * @return 当前结果对象
   */
  public EpcResult setAi01(String ai01) {
    this.ai01 = ai01;
    return this;
  }

  /**
   * 获取 AI 21（序列号）值。
   *
   * @return 序列号字符串
   */
  public String getAi21() {
    return ai21;
  }

  /**
   * 设置 AI 21（序列号）值。
   *
   * @param ai21 序列号字符串
   * @return 当前结果对象
   */
  public EpcResult setAi21(String ai21) {
    this.ai21 = ai21;
    return this;
  }

  /**
   * 获取 AI 8004（GIAI 资产标识）值。
   *
   * @return GIAI 资产标识字符串
   */
  public String getAi8004() {
    return ai8004;
  }

  /**
   * 设置 AI 8004（GIAI 资产标识）值。
   *
   * @param ai8004 GIAI 资产标识字符串
   * @return 当前结果对象
   */
  public EpcResult setAi8004(String ai8004) {
    this.ai8004 = ai8004;
    return this;
  }

  /**
   * 获取 GTIN-14。
   *
   * @return 14 位 GTIN 字符串
   */
  public String getGtin14() {
    return gtin14;
  }

  /**
   * 设置 GTIN-14。
   *
   * @param gtin14 14 位 GTIN 字符串
   * @return 当前结果对象
   */
  public EpcResult setGtin14(String gtin14) {
    this.gtin14 = gtin14;
    return this;
  }

  /**
   * 获取 EPC 序列号。
   *
   * @return 序列号字符串
   */
  public String getSerial() {
    return serial;
  }

  /**
   * 设置 EPC 序列号（同时同步到 serialNumber 字段）。
   *
   * @param serial 序列号字符串
   * @return 当前结果对象
   */
  public EpcResult setSerial(String serial) {
    this.serial = serial;
    this.serialNumber = serial;
    return this;
  }

  /**
   * 获取序列号兼容字段。
   *
   * @return 序列号字符串
   */
  public String getSerialNumber() {
    return serialNumber;
  }

  /**
   * 设置序列号兼容字段（同时同步到 serial 字段）。
   *
   * @param serialNumber 序列号字符串
   * @return 当前结果对象
   */
  public EpcResult setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
    this.serial = serialNumber;
    return this;
  }

  /**
   * 获取 GS1 公司前缀。
   *
   * @return 公司前缀字符串
   */
  public String getCompanyPrefix() {
    return companyPrefix;
  }

  /**
   * 设置 GS1 公司前缀。
   *
   * @param companyPrefix 公司前缀字符串
   * @return 当前结果对象
   */
  public EpcResult setCompanyPrefix(String companyPrefix) {
    this.companyPrefix = companyPrefix;
    return this;
  }

  /**
   * 获取 SGTIN 的项目参考。
   *
   * @return 项目参考字符串
   */
  public String getItemReference() {
    return itemReference;
  }

  /**
   * 设置 SGTIN 的项目参考。
   *
   * @param itemReference 项目参考字符串
   * @return 当前结果对象
   */
  public EpcResult setItemReference(String itemReference) {
    this.itemReference = itemReference;
    return this;
  }

  /**
   * 获取 GIAI 的资产参考。
   *
   * @return 资产参考字符串
   */
  public String getAssetReference() {
    return assetReference;
  }

  /**
   * 设置 GIAI 的资产参考。
   *
   * @param assetReference 资产参考字符串
   * @return 当前结果对象
   */
  public EpcResult setAssetReference(String assetReference) {
    this.assetReference = assetReference;
    return this;
  }

  /**
   * 获取 EPC filter 值。
   *
   * @return filter 值
   */
  public int getFilter() {
    return filter;
  }

  /**
   * 设置 EPC filter 值。
   *
   * @param filter filter 值（0-7）
   * @return 当前结果对象
   */
  public EpcResult setFilter(int filter) {
    this.filter = filter;
    return this;
  }

  /**
   * 获取公司前缀长度。
   *
   * @return 公司前缀长度
   */
  public int getCompanyPrefixLength() {
    return companyPrefixLength;
  }

  /**
   * 设置公司前缀长度。
   *
   * @param companyPrefixLength 公司前缀长度
   * @return 当前结果对象
   */
  public EpcResult setCompanyPrefixLength(int companyPrefixLength) {
    this.companyPrefixLength = companyPrefixLength;
    return this;
  }

  /**
   * 判断编解码结果是否有效。
   *
   * @return 如果有效则返回 true
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * 获取结果有效标记（兼容 Jackson 序列化）。
   *
   * @return 如果有效则返回 true
   */
  public boolean getValid() {
    return valid;
  }

  /**
   * 设置结果有效标记。
   *
   * @param valid 有效标记
   * @return 当前结果对象
   */
  public EpcResult setValid(boolean valid) {
    this.valid = valid;
    return this;
  }
}
