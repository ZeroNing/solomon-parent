package com.steven.solomon.service;

/**
 * GS1 EPC 分区（Partition）定义。
 * <p>
 * 根据 GS1 EPC Tag Data Standard 标准定义的分区表，
 * 用于将公司前缀（Company Prefix）和项目参考（Item Reference）/ 资产参考（Asset Reference）
 * 映射到 EPC 二进制编码中的不同位宽。
 * </p>
 *
 * @author 创建者
 */
class Partition {

  /** 分区值（0-6）。 */
  final int value;

  /** 公司前缀的十进制位数。 */
  final int companyPrefixDigits;

  /** 公司前缀的二进制位数。 */
  final int companyPrefixBits;

  /** 项目参考的十进制位数。 */
  final int itemReferenceDigits;

  /** 项目参考的二进制位数。 */
  final int itemReferenceBits;

  /**
   * 创建一个分区定义。
   *
   * @param value               分区值
   * @param companyPrefixDigits 公司前缀十进制位数
   * @param companyPrefixBits   公司前缀二进制位数
   * @param itemReferenceDigits 项目参考十进制位数
   * @param itemReferenceBits   项目参考二进制位数
   */
  Partition(int value, int companyPrefixDigits, int companyPrefixBits, int itemReferenceDigits, int itemReferenceBits) {
    this.value = value;
    this.companyPrefixDigits = companyPrefixDigits;
    this.companyPrefixBits = companyPrefixBits;
    this.itemReferenceDigits = itemReferenceDigits;
    this.itemReferenceBits = itemReferenceBits;
  }
}
