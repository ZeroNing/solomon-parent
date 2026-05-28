package com.steven.solomon.service;

/**
 * AI 8004 拆分后的 GIAI 字段。
 */
class GiaiParts {

  private final String companyPrefix;
  private final String assetReference;

  GiaiParts(String companyPrefix, String assetReference) {
    this.companyPrefix = companyPrefix;
    this.assetReference = assetReference;
  }

  String getCompanyPrefix() { return companyPrefix; }
  String getAssetReference() { return assetReference; }
}
