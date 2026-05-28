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

  Partition(int value, int companyPrefixDigits, int companyPrefixBits, int itemReferenceDigits, int itemReferenceBits) {
    this.value = value;
    this.companyPrefixDigits = companyPrefixDigits;
    this.companyPrefixBits = companyPrefixBits;
    this.itemReferenceDigits = itemReferenceDigits;
    this.itemReferenceBits = itemReferenceBits;
  }
}
