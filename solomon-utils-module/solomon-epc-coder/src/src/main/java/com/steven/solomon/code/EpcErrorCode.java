package com.steven.solomon.code;

import com.steven.solomon.code.BaseExceptionCode;

/**
 * GS1 EPC 编解码错误码。
 */
public interface EpcErrorCode extends BaseExceptionCode {

  /** GS1 条码长度错误。 */
  String BARCODE_LENGTH_ERROR = "B0004";

  /** EPC 十六进制格式无效。 */
  String EPC_INVALID_FORMAT = "B4001";

  /** EPC Header 不匹配。 */
  String EPC_HEADER_MISMATCH = "B4002";

  /** EPC 公司前缀长度错误。 */
  String EPC_COMPANY_PREFIX_LENGTH_ERROR = "B4003";

  /** EPC 分区值错误。 */
  String EPC_PARTITION_ERROR = "B4004";

  /** EPC 序列号为空或无效。 */
  String EPC_SERIAL_NUMBER_ERROR = "B4005";

  /** EPC 序列号超出范围。 */
  String EPC_SERIAL_NUMBER_RANGE_ERROR = "B4006";

  /** EPC Tag Size 不支持。 */
  String EPC_TAG_SIZE_ERROR = "B4007";

  /** EPC 不支持的 AI。 */
  String EPC_AI_NOT_SUPPORTED = "B4008";

  /** EPC 缺少必需的 AI。 */
  String EPC_AI_REQUIRED = "B4009";

  /** EPC 资产参考无效。 */
  String EPC_ASSET_REFERENCE_ERROR = "B4010";
}
