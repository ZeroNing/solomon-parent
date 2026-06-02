package com.steven.solomon.service;

/**
 * AI 8004 拆分后的 GIAI（全球单个资产标识）字段。
 * <p>
 * 将 AI 8004 的完整值按公司前缀长度拆分为公司前缀和资产参考两部分。
 * </p>
 *
 * @author 创建者
 */
class GiaiParts {

  /** 公司前缀部分。 */
  private final String companyPrefix;

  /** 资产参考部分。 */
  private final String assetReference;

  /**
   * 创建一个 GIAI 拆分结果。
   *
   * @param companyPrefix  公司前缀
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
  String getCompanyPrefix() { return companyPrefix; }

  /**
   * 获取资产参考。
   *
   * @return 资产参考
   */
  String getAssetReference() { return assetReference; }
}
