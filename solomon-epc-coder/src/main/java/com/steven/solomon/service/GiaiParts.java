package com.steven.solomon.service;

/**
 * AI 8004 拆分后的 GIAI 字段。
 */
class GiaiParts {

  private final String companyPrefix;

  private final String assetReference;

  /**
   * 创建 GIAI 字段拆分结果。
   *
   * @param companyPrefix 公司前缀
   * @param assetReference 资产参考
   */
  GiaiParts(String companyPrefix, String assetReference) {
    this.companyPrefix = companyPrefix;
    this.assetReference = assetReference;
  }

  /**
   * 获取公司前缀。
   *
   * @return 公司前缀
   */
  String getCompanyPrefix() {
    return companyPrefix;
  }

  /**
   * 获取资产参考。
   *
   * @return 资产参考
   */
  String getAssetReference() {
    return assetReference;
  }
}
