package com.steven.solomon.code;

import com.steven.solomon.code.BaseExceptionCode;

public interface EpcErrorCode extends BaseExceptionCode {

  String BARCODE_LENGTH_ERROR = "B0004";

  String EPC_INVALID_FORMAT = "B4001";

  String EPC_HEADER_MISMATCH = "B4002";

  String EPC_COMPANY_PREFIX_LENGTH_ERROR = "B4003";

  String EPC_PARTITION_ERROR = "B4004";

  String EPC_SERIAL_NUMBER_ERROR = "B4005";

  String EPC_SERIAL_NUMBER_RANGE_ERROR = "B4006";

  String EPC_TAG_SIZE_ERROR = "B4007";

  String EPC_AI_NOT_SUPPORTED = "B4008";

  String EPC_AI_REQUIRED = "B4009";

  String EPC_ASSET_REFERENCE_ERROR = "B4010";
}
