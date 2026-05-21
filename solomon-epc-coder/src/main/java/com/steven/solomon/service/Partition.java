package com.steven.solomon.service;

/**
 * GS1 EPC 分区定义。
 */
class Partition {

  final int value;

  final int companyPrefixDigits;

  final int companyPrefixBits;

  final int itemReferenceDigits;

  final int itemReferenceBits;

  /**
   * 创建 EPC 分区定义。
   *
   * @param value 分区值
   * @param companyPrefixDigits 公司前缀位数
   * @param companyPrefixBits 公司前缀 bit 位数
   * @param itemReferenceDigits itemReference 位数
   * @param itemReferenceBits itemReference bit 位数
   */
  Partition(int value, int companyPrefixDigits, int companyPrefixBits, int itemReferenceDigits,
      int itemReferenceBits) {
    this.value = value;
    this.companyPrefixDigits = companyPrefixDigits;
    this.companyPrefixBits = companyPrefixBits;
    this.itemReferenceDigits = itemReferenceDigits;
    this.itemReferenceBits = itemReferenceBits;
  }
}
