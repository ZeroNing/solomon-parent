package com.steven.solomon.code;

import com.steven.solomon.code.BaseExceptionCode;

/**
 * GS1 EPC 编解码错误码定义。
 * <p>
 * 定义了 EPC（电子产品编码）编解码过程中可能出现的各类错误码，
 * 包括条码长度、格式、Header 匹配、公司前缀、分区值、序列号、Tag Size、
 * AI（应用标识符）支持和资产参考等方面的错误。
 * </p>
 *
 * @see com.steven.solomon.code.BaseExceptionCode
 */
public interface EpcErrorCode extends BaseExceptionCode {

  /** GS1 条码长度错误（B0004）。 */
  String BARCODE_LENGTH_ERROR = "B0004";

  /** EPC 十六进制格式无效（B4001）。 */
  String EPC_INVALID_FORMAT = "B4001";

  /** EPC Header（头部）不匹配（B4002）。 */
  String EPC_HEADER_MISMATCH = "B4002";

  /** EPC 公司前缀长度错误（B4003）。 */
  String EPC_COMPANY_PREFIX_LENGTH_ERROR = "B4003";

  /** EPC 分区（Partition）值错误（B4004）。 */
  String EPC_PARTITION_ERROR = "B4004";

  /** EPC 序列号为空或无效（B4005）。 */
  String EPC_SERIAL_NUMBER_ERROR = "B4005";

  /** EPC 序列号超出范围（B4006）。 */
  String EPC_SERIAL_NUMBER_RANGE_ERROR = "B4006";

  /** EPC 标签尺寸（Tag Size）不支持（B4007）。 */
  String EPC_TAG_SIZE_ERROR = "B4007";

  /** EPC 不支持的 AI（应用标识符）（B4008）。 */
  String EPC_AI_NOT_SUPPORTED = "B4008";

  /** EPC 缺少必需的 AI（应用标识符）（B4009）。 */
  String EPC_AI_REQUIRED = "B4009";

  /** EPC 资产参考（Asset Reference）无效（B4010）。 */
  String EPC_ASSET_REFERENCE_ERROR = "B4010";
}
