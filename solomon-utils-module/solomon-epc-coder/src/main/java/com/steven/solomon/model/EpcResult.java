package com.steven.solomon.model;

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

  /** GS1 AI 01，GTIN 字段，和 AI 21 分开保存。 */
  private String ai01;

  /** GS1 AI 21，序列号字段，和 AI 01 分开保存。 */
  private String ai21;

  /** GS1 AI 8004，GIAI 资产标识。 */
  private String ai8004;

  /** 14 位 GTIN。 */
  private String gtin14;

  /** EPC 序列号。 */
  private String serial;

  /** EPC 序列号兼容字段。 */
  private String serialNumber;

  /** GS1 公司前缀。 */
  private String companyPrefix;

  /** SGTIN 的 item reference。 */
  private String itemReference;

  /** GIAI 的资产参考。 */
  private String assetReference;

  /** EPC filter 值。 */
  private int filter;

  /** GS1 公司前缀长度。 */
  private int companyPrefixLength;

  /** 编解码是否有效。 */
  private boolean valid;

  /** 获取 EPC 类型。 */
  public String getType() {
    return type;
  }

  /** 设置 EPC 类型。 */
  public EpcResult setType(String type) {
    this.type = type;
    return this;
  }

  /** 获取真实 bit 长度。 */
  public int getBitLength() {
    return bitLength;
  }

  /** 设置真实 bit 长度。 */
  public EpcResult setBitLength(int bitLength) {
    this.bitLength = bitLength;
    return this;
  }

  /** 获取 EPC 十六进制编码。 */
  public String getHex() {
    return hex;
  }

  /** 设置 EPC 十六进制编码。 */
  public EpcResult setHex(String hex) {
    this.hex = hex;
    return this;
  }

  /** 获取 EPC 二进制编码。 */
  public String getBinary() {
    return binary;
  }

  /** 设置 EPC 二进制编码。 */
  public EpcResult setBinary(String binary) {
    this.binary = binary;
    return this;
  }

  /** 获取 EPC Tag URI。 */
  public String getUri() {
    return uri;
  }

  /** 设置 EPC Tag URI。 */
  public EpcResult setUri(String uri) {
    this.uri = uri;
    return this;
  }

  /** 获取原始 GS1 条码内容。 */
  public String getBarcode() {
    return barcode;
  }

  /** 设置原始 GS1 条码内容。 */
  public EpcResult setBarcode(String barcode) {
    this.barcode = barcode;
    return this;
  }

  /** 获取 AI 01。 */
  public String getAi01() {
    return ai01;
  }

  /** 设置 AI 01。 */
  public EpcResult setAi01(String ai01) {
    this.ai01 = ai01;
    return this;
  }

  /** 获取 AI 21。 */
  public String getAi21() {
    return ai21;
  }

  /** 设置 AI 21。 */
  public EpcResult setAi21(String ai21) {
    this.ai21 = ai21;
    return this;
  }

  /** 获取 AI 8004。 */
  public String getAi8004() {
    return ai8004;
  }

  /** 设置 AI 8004。 */
  public EpcResult setAi8004(String ai8004) {
    this.ai8004 = ai8004;
    return this;
  }

  /** 获取 GTIN-14。 */
  public String getGtin14() {
    return gtin14;
  }

  /** 设置 GTIN-14。 */
  public EpcResult setGtin14(String gtin14) {
    this.gtin14 = gtin14;
    return this;
  }

  /** 获取序列号。 */
  public String getSerial() {
    return serial;
  }

  /** 设置序列号。 */
  public EpcResult setSerial(String serial) {
    this.serial = serial;
    this.serialNumber = serial;
    return this;
  }

  /** 获取序列号兼容字段。 */
  public String getSerialNumber() {
    return serialNumber;
  }

  /** 设置序列号兼容字段。 */
  public EpcResult setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
    this.serial = serialNumber;
    return this;
  }

  /** 获取公司前缀。 */
  public String getCompanyPrefix() {
    return companyPrefix;
  }

  /** 设置公司前缀。 */
  public EpcResult setCompanyPrefix(String companyPrefix) {
    this.companyPrefix = companyPrefix;
    return this;
  }

  /** 获取 itemReference。 */
  public String getItemReference() {
    return itemReference;
  }

  /** 设置 itemReference。 */
  public EpcResult setItemReference(String itemReference) {
    this.itemReference = itemReference;
    return this;
  }

  /** 获取资产参考。 */
  public String getAssetReference() {
    return assetReference;
  }

  /** 设置资产参考。 */
  public EpcResult setAssetReference(String assetReference) {
    this.assetReference = assetReference;
    return this;
  }

  /** 获取 filter 值。 */
  public int getFilter() {
    return filter;
  }

  /** 设置 filter 值。 */
  public EpcResult setFilter(int filter) {
    this.filter = filter;
    return this;
  }

  /** 获取公司前缀长度。 */
  public int getCompanyPrefixLength() {
    return companyPrefixLength;
  }

  /** 设置公司前缀长度。 */
  public EpcResult setCompanyPrefixLength(int companyPrefixLength) {
    this.companyPrefixLength = companyPrefixLength;
    return this;
  }

  /** 判断结果是否有效。 */
  public boolean isValid() {
    return valid;
  }

  /** 获取结果有效标记。 */
  public boolean getValid() {
    return valid;
  }

  /** 设置结果有效标记。 */
  public EpcResult setValid(boolean valid) {
    this.valid = valid;
    return this;
  }
}
