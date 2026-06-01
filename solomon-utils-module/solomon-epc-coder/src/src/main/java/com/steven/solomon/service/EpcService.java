package com.steven.solomon.service;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.code.EpcErrorCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;
import com.steven.solomon.model.Gs1BarcodeResult;

import java.math.BigInteger;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpcService {

  /** GS1 TDS 中 SGTIN-96 的 Header。 */
  private static final int HEADER_SGTIN_96 = 0x30;

  /** GS1 TDS 中 SGTIN-198 的 Header。 */
  private static final int HEADER_SGTIN_198 = 0x36;

  /** GS1 TDS 中 GIAI-96 的 Header。 */
  private static final int HEADER_GIAI_96 = 0x34;

  /** GS1 TDS 中 GIAI-202 的 Header。 */
  private static final int HEADER_GIAI_202 = 0x38;

  /** SGTIN-96 序列号固定为 38bit，可表达的最大无符号整数。 */
  private static final long SGTIN_96_MAX_SERIAL = 274877906943L;

  /** 支持括号格式 GS1 AI，例如：(01)06901234567892(21)ABC123。 */
  private static final Pattern BRACKET_AI_PATTERN = Pattern.compile("\\((\\d{2,4})\\)");

  /** GS1 EPC 分区表，按公司前缀长度映射公司前缀和对象字段的 bit 位数。 */
  private static final Partition[] PARTITIONS = {
      new Partition(0, 12, 40, 1, 4),
      new Partition(1, 11, 37, 2, 7),
      new Partition(2, 10, 34, 3, 10),
      new Partition(3, 9, 30, 4, 14),
      new Partition(4, 8, 27, 5, 17),
      new Partition(5, 7, 24, 6, 20),
      new Partition(6, 6, 20, 7, 24)
  };

  /**
   * 创建 SGTIN 链式构建器。
   *
   * @return SGTIN 构建器
   */
  public EpcBuilder builder() {
    return new EpcBuilder(this);
  }

  /**
   * 创建 GS1 AI 链式构建器。
   *
   * @return GS1 AI 构建器
   */
  public Gs1Builder gs1() {
    return new Gs1Builder(this);
  }

  /**
   * 创建默认 Tag Size 为 96 的 SGTIN 构建器。
   *
   * @return SGTIN-96 构建器
   */
  public EpcBuilder sgtin96() {
    return builder().tagSize(96);
  }

  /**
   * 创建默认 Tag Size 为 96 的 SSCC 构建器兼容入口。
   *
   * @return EPC 构建器
   */
  public EpcBuilder sscc96() {
    return builder().tagSize(96);
  }

  /**
   * 创建默认 Tag Size 为 198 的 SGTIN 构建器。
   *
   * @return SGTIN-198 构建器
   */
  public EpcBuilder sgtin198() {
    return builder().tagSize(198);
  }

  /**
   * 创建默认 Tag Size 为 96 的 GIAI 构建器。
   *
   * @return GIAI-96 构建器
   */
  public GiaiBuilder giai96() {
    return giai().tagSize(96);
  }

  /**
   * 创建默认 Tag Size 为 202 的 GIAI 构建器。
   *
   * @return GIAI-202 构建器
   */
  public GiaiBuilder giai202() {
    return giai().tagSize(202);
  }

  /**
   * 创建 GIAI 链式构建器。
   *
   * @return GIAI 构建器
   */
  public GiaiBuilder giai() {
    return new GiaiBuilder(this);
  }

  /**
   * 将 EAN-13 和序列号生成 SGTIN-96。
   *
   * @param ean13 EAN-13 条码
   * @param companyPrefixLength 公司前缀长度
   * @param serial 序列号
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult ean13ToSgtin96(String ean13, int companyPrefixLength, String serial) throws BaseException {
    if (ObjectUtil.isEmpty(ean13) || ean13.length() != 13) {
      throw new BaseException(EpcErrorCode.BARCODE_LENGTH_ERROR, ean13, 13);
    }
    return sgtin(ean13, companyPrefixLength, serial, 96, 0);
  }

  /**
   * 将 GTIN 和序列号生成 SGTIN EPC。
   *
   * @param gtin GTIN
   * @param serial 序列号
   * @param companyPrefixLength 公司前缀长度
   * @param tagSize Tag Size，支持 96/198
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult gtinSerialToEpc(String gtin, String serial, int companyPrefixLength, int tagSize)
      throws BaseException {
    return gtinSerialToEpc(gtin, serial, companyPrefixLength, tagSize, 0);
  }

  /**
   * 将 GTIN 和序列号按指定 filter 生成 SGTIN EPC。
   *
   * @param gtin GTIN
   * @param serial 序列号
   * @param companyPrefixLength 公司前缀长度
   * @param tagSize Tag Size，支持 96/198
   * @param filter EPC filter 值
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult gtinSerialToEpc(String gtin, String serial, int companyPrefixLength, int tagSize, int filter)
      throws BaseException {
    return sgtin(gtin, companyPrefixLength, serial, tagSize, filter);
  }

  /**
   * 将 GS1 AI 条码生成 EPC。
   *
   * @param barcode GS1 AI 条码
   * @param companyPrefixLength 公司前缀长度
   * @param tagSize Tag Size
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult gs1ToEpc(String barcode, int companyPrefixLength, int tagSize) throws BaseException {
    return gs1ToEpc(barcode, companyPrefixLength, tagSize, 0);
  }

  /**
   * 将 GS1 AI 条码按指定 filter 生成 EPC。
   *
   * @param barcode GS1 AI 条码
   * @param companyPrefixLength 公司前缀长度
   * @param tagSize Tag Size
   * @param filter EPC filter 值
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult gs1ToEpc(String barcode, int companyPrefixLength, int tagSize, int filter)
      throws BaseException {
    Gs1BarcodeResult gs1 = parseGs1Barcode(barcode);
    // AI 01 和 AI 21 必须分别解析、分别保存，不能合并成一个字段。
    if (gs1.hasGtinSerial()) {
      return gtinSerialToEpc(gs1.getAi01(), gs1.getAi21(), companyPrefixLength, tagSize, filter)
          .setBarcode(barcode);
    }
    if (gs1.hasGiai()) {
      GiaiParts parts = splitGiai(gs1.getAi8004(), companyPrefixLength);
      return giaiToEpc(parts.getCompanyPrefix(), parts.getAssetReference(), tagSize, filter).setBarcode(barcode);
    }
    throw new BaseException(EpcErrorCode.EPC_AI_REQUIRED, barcode);
  }

  /**
   * 将公司前缀和资产参考生成 GIAI EPC。
   *
   * @param companyPrefix 公司前缀
   * @param assetReference 资产参考
   * @param tagSize Tag Size，支持 96/202
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult giaiToEpc(String companyPrefix, String assetReference, int tagSize) throws BaseException {
    return giaiToEpc(companyPrefix, assetReference, tagSize, 0);
  }

  /**
   * 将公司前缀和资产参考按指定 filter 生成 GIAI EPC。
   *
   * @param companyPrefix 公司前缀
   * @param assetReference 资产参考
   * @param tagSize Tag Size，支持 96/202
   * @param filter EPC filter 值
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult giaiToEpc(String companyPrefix, String assetReference, int tagSize, int filter)
      throws BaseException {
    validateFilter(filter);
    Partition partition = partitionByCompanyPrefixLength(ObjectUtil.isEmpty(companyPrefix) ? 0 : companyPrefix.length());
    validateCompanyPrefix(companyPrefix, partition);
    requireNotBlank(assetReference, EpcErrorCode.EPC_ASSET_REFERENCE_ERROR);

    String type;
    String bits;
    if (tagSize == 96) {
      // GIAI-96 的资产参考是纯数字字段，剩余 bit 全部用于数值编码。
      if (!isNumeric(assetReference)) {
        throw new BaseException(EpcErrorCode.EPC_ASSET_REFERENCE_ERROR, assetReference);
      }
      int assetBits = 82 - partition.companyPrefixBits;
      if (new BigInteger(assetReference).bitLength() > assetBits) {
        throw new BaseException(EpcErrorCode.EPC_ASSET_REFERENCE_ERROR, assetReference);
      }
      bits = fixedBinary(HEADER_GIAI_96, 8)
          + fixedBinary(filter, 3)
          + fixedBinary(partition.value, 3)
          + fixedBinary(new BigInteger(companyPrefix), partition.companyPrefixBits)
          + fixedBinary(new BigInteger(assetReference), assetBits);
      type = "GIAI-96";
    } else if (tagSize == 202) {
      // GIAI-202 的资产参考使用 7bit 字符编码，适合资产、车辆等字母数字编号。
      int assetBits = 188 - partition.companyPrefixBits;
      validateSevenBit(assetReference, assetBits / 7, EpcErrorCode.EPC_ASSET_REFERENCE_ERROR);
      bits = fixedBinary(HEADER_GIAI_202, 8)
          + fixedBinary(filter, 3)
          + fixedBinary(partition.value, 3)
          + fixedBinary(new BigInteger(companyPrefix), partition.companyPrefixBits)
          + encodeString(assetReference, assetBits);
      type = "GIAI-202";
    } else {
      throw new BaseException(EpcErrorCode.EPC_TAG_SIZE_ERROR, tagSize);
    }
    String uri = "urn:epc:tag:giai-" + tagSize + ":" + filter + "." + companyPrefix + "." + assetReference;
    return baseResult(type, tagSize, bits, uri)
        .setCompanyPrefix(companyPrefix)
        .setCompanyPrefixLength(companyPrefix.length())
        .setAssetReference(assetReference)
        .setAi8004(companyPrefix + assetReference)
        .setFilter(filter);
  }

  /**
   * 将 SSCC 条码生成 SSCC-96 结果。
   *
   * @param sscc SSCC-18 条码
   * @param companyPrefixLength 公司前缀长度
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult ssccToSscc96(String sscc, int companyPrefixLength) throws BaseException {
    if (ObjectUtil.isEmpty(sscc) || sscc.length() != 18 || !isNumeric(sscc)) {
      throw new BaseException(EpcErrorCode.BARCODE_LENGTH_ERROR, sscc, 18);
    }
    String companyPrefix = sscc.substring(1, 1 + companyPrefixLength);
    String serial = sscc.substring(1 + companyPrefixLength, 17);
    return new EpcResult()
        .setType("SSCC-96")
        .setBitLength(96)
        .setHex("")
        .setUri("urn:epc:tag:sscc-96:0." + companyPrefix + "." + serial)
        .setCompanyPrefix(companyPrefix)
        .setCompanyPrefixLength(companyPrefixLength)
        .setSerial(serial)
        .setValid(true);
  }

  /**
   * 根据 EPC 十六进制编码自动识别类型并反译。
   *
   * @param hex EPC 十六进制编码
   * @return EPC 反译结果
   * @throws BaseException 参数无效或反译失败
   */
  public EpcResult decodeEpc(String hex) throws BaseException {
    if (ObjectUtil.isEmpty(hex) || !hex.matches("(?i)[0-9a-f]+")) {
      throw new BaseException(EpcErrorCode.EPC_INVALID_FORMAT, hex);
    }
    String bits = hexToBinary(hex.toUpperCase(Locale.ROOT));
    int header = Integer.parseInt(bits.substring(0, 8), 2);
    // 通过 Header 自动识别 EPC 类型，然后走对应的反译规则。
    if (header == HEADER_SGTIN_96) {
      return decodeSgtin(bits, hex, 96);
    }
    if (header == HEADER_SGTIN_198) {
      return decodeSgtin(bits, hex, 198);
    }
    if (header == HEADER_GIAI_96) {
      return decodeGiai(bits, hex, 96);
    }
    if (header == HEADER_GIAI_202) {
      return decodeGiai(bits, hex, 202);
    }
    throw new BaseException(EpcErrorCode.EPC_HEADER_MISMATCH, header);
  }

  /**
   * 解析 GS1 AI 条码，AI 01、AI 21、AI 8004 分别输出。
   *
   * @param barcode GS1 AI 条码
   * @return GS1 解析结果
   * @throws BaseException 参数无效或解析失败
   */
  public Gs1BarcodeResult parseGs1Barcode(String barcode) throws BaseException {
    requireNotBlank(barcode, EpcErrorCode.EPC_AI_REQUIRED);
    Gs1BarcodeResult result = new Gs1BarcodeResult();
    result.setContent(barcode);
    if (barcode.contains("(")) {
      parseBracketGs1(barcode, result);
    } else {
      parsePlainGs1(barcode, result);
    }
    return result;
  }

  /**
   * 生成 SGTIN-96 或 SGTIN-198。
   *
   * @param gtin GTIN
   * @param companyPrefixLength 公司前缀长度
   * @param serial 序列号
   * @param tagSize Tag Size
   * @param filter EPC filter 值
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  private EpcResult sgtin(String gtin, int companyPrefixLength, String serial, int tagSize, int filter)
      throws BaseException {
    validateFilter(filter);
    String gtin14 = normalizeGtin(gtin);
    Partition partition = partitionByCompanyPrefixLength(companyPrefixLength);
    // SGTIN 中 itemReference = indicator digit + item reference，不包含 GTIN 校验位。
    String companyPrefix = gtin14.substring(1, 1 + companyPrefixLength);
    String itemReference = gtin14.charAt(0) + gtin14.substring(1 + companyPrefixLength, 13);
    validateCompanyPrefix(companyPrefix, partition);
    requireNotBlank(serial, EpcErrorCode.EPC_SERIAL_NUMBER_ERROR);

    String type;
    String bits;
    if (tagSize == 96) {
      // SGTIN-96 的序列号为 38bit 纯数字，适合常规 RFID 单品和物流单元。
      if (!isNumeric(serial)) {
        throw new BaseException(EpcErrorCode.EPC_SERIAL_NUMBER_ERROR, serial);
      }
      long serialValue = Long.parseLong(serial);
      if (serialValue < 0 || serialValue > SGTIN_96_MAX_SERIAL) {
        throw new BaseException(EpcErrorCode.EPC_SERIAL_NUMBER_RANGE_ERROR, serial);
      }
      bits = fixedBinary(HEADER_SGTIN_96, 8)
          + fixedBinary(filter, 3)
          + fixedBinary(partition.value, 3)
          + fixedBinary(new BigInteger(companyPrefix), partition.companyPrefixBits)
          + fixedBinary(new BigInteger(itemReference), partition.itemReferenceBits)
          + fixedBinary(serialValue, 38);
      type = "SGTIN-96";
    } else if (tagSize == 198) {
      // SGTIN-198 的序列号为 20 个 7bit 字符，支持字母数字混合序号。
      validateSevenBit(serial, 20, EpcErrorCode.EPC_SERIAL_NUMBER_ERROR);
      bits = fixedBinary(HEADER_SGTIN_198, 8)
          + fixedBinary(filter, 3)
          + fixedBinary(partition.value, 3)
          + fixedBinary(new BigInteger(companyPrefix), partition.companyPrefixBits)
          + fixedBinary(new BigInteger(itemReference), partition.itemReferenceBits)
          + encodeString(serial, 140);
      type = "SGTIN-198";
    } else {
      throw new BaseException(EpcErrorCode.EPC_TAG_SIZE_ERROR, tagSize);
    }
    String uri = "urn:epc:tag:sgtin-" + tagSize + ":" + filter + "." + companyPrefix + "." + itemReference
        + "." + serial;
    return baseResult(type, tagSize, bits, uri)
        .setCompanyPrefix(companyPrefix)
        .setCompanyPrefixLength(companyPrefixLength)
        .setItemReference(itemReference)
        .setGtin14(gtin14)
        .setAi01(gtin14)
        .setAi21(serial)
        .setSerial(serial)
        .setFilter(filter);
  }

  /**
   * 反译 SGTIN-96 或 SGTIN-198。
   *
   * @param bits EPC 二进制编码
   * @param hex EPC 十六进制编码
   * @param tagSize Tag Size
   * @return EPC 反译结果
   * @throws BaseException 参数无效或反译失败
   */
  private EpcResult decodeSgtin(String bits, String hex, int tagSize) throws BaseException {
    if (bits.length() < tagSize) {
      throw new BaseException(EpcErrorCode.EPC_INVALID_FORMAT, hex);
    }
    bits = bits.substring(0, tagSize);
    int filter = readInt(bits, 8, 11);
    Partition partition = partitionByValue(readInt(bits, 11, 14));
    int cursor = 14;
    // 先按分区表还原公司前缀和 itemReference，再还原 AI 01 的 GTIN-14。
    String companyPrefix = readDecimal(bits, cursor, cursor + partition.companyPrefixBits, partition.companyPrefixDigits);
    cursor += partition.companyPrefixBits;
    String itemReference = readDecimal(bits, cursor, cursor + partition.itemReferenceBits, partition.itemReferenceDigits);
    cursor += partition.itemReferenceBits;
    String serial = tagSize == 96 ? new BigInteger(bits.substring(cursor), 2).toString()
        : decodeString(bits.substring(cursor));
    String gtin14 = itemReference.charAt(0) + companyPrefix + itemReference.substring(1);
    gtin14 = gtin14 + gs1CheckDigit(gtin14);
    String type = "SGTIN-" + tagSize;
    String uri = "urn:epc:tag:sgtin-" + tagSize + ":" + filter + "." + companyPrefix + "." + itemReference
        + "." + serial;
    return baseResult(type, tagSize, bits, uri)
        .setHex(hex.toUpperCase(Locale.ROOT))
        .setCompanyPrefix(companyPrefix)
        .setCompanyPrefixLength(partition.companyPrefixDigits)
        .setItemReference(itemReference)
        .setGtin14(gtin14)
        // 反译时 AI 01 和 AI 21 也保持独立字段输出。
        .setAi01(gtin14)
        .setAi21(serial)
        .setSerial(serial)
        .setFilter(filter);
  }

  /**
   * 反译 GIAI-96 或 GIAI-202。
   *
   * @param bits EPC 二进制编码
   * @param hex EPC 十六进制编码
   * @param tagSize Tag Size
   * @return EPC 反译结果
   * @throws BaseException 参数无效或反译失败
   */
  private EpcResult decodeGiai(String bits, String hex, int tagSize) throws BaseException {
    if (bits.length() < tagSize) {
      throw new BaseException(EpcErrorCode.EPC_INVALID_FORMAT, hex);
    }
    bits = bits.substring(0, tagSize);
    int filter = readInt(bits, 8, 11);
    Partition partition = partitionByValue(readInt(bits, 11, 14));
    int cursor = 14;
    // GIAI 的 AI 8004 = 公司前缀 + 资产参考，反译结果同时保留拆分字段。
    String companyPrefix = readDecimal(bits, cursor, cursor + partition.companyPrefixBits, partition.companyPrefixDigits);
    cursor += partition.companyPrefixBits;
    String assetReference = tagSize == 96 ? new BigInteger(bits.substring(cursor), 2).toString()
        : decodeString(bits.substring(cursor));
    String type = "GIAI-" + tagSize;
    String uri = "urn:epc:tag:giai-" + tagSize + ":" + filter + "." + companyPrefix + "." + assetReference;
    return baseResult(type, tagSize, bits, uri)
        .setHex(hex.toUpperCase(Locale.ROOT))
        .setCompanyPrefix(companyPrefix)
        .setCompanyPrefixLength(partition.companyPrefixDigits)
        .setAssetReference(assetReference)
        .setAi8004(companyPrefix + assetReference)
        .setFilter(filter);
  }

  /**
   * 解析带括号的 GS1 AI 格式。
   *
   * @param barcode GS1 AI 条码
   * @param result GS1 解析结果
   */
  private void parseBracketGs1(String barcode, Gs1BarcodeResult result) {
    Matcher matcher = BRACKET_AI_PATTERN.matcher(barcode);
    String ai = null;
    int valueStart = -1;
    while (matcher.find()) {
      if (ObjectUtil.isNotEmpty(ai)) {
        putAi(result, ai, barcode.substring(valueStart, matcher.start()));
      }
      ai = matcher.group(1);
      valueStart = matcher.end();
    }
    if (ObjectUtil.isNotEmpty(ai)) {
      putAi(result, ai, barcode.substring(valueStart));
    }
  }

  /**
   * 解析不带括号的 GS1 AI 格式。
   *
   * @param barcode GS1 AI 条码
   * @param result GS1 解析结果
   */
  private void parsePlainGs1(String barcode, Gs1BarcodeResult result) {
    int cursor = 0;
    while (cursor < barcode.length()) {
      if (barcode.startsWith("01", cursor) && barcode.length() >= cursor + 16) {
        // AI 01 固定 14 位 GTIN，必须和 AI 21 分开保存。
        result.setAi01(barcode.substring(cursor + 2, cursor + 16));
        cursor += 16;
      } else if (barcode.startsWith("21", cursor)) {
        // AI 21 是可变长序号，当前纯文本格式中默认读取到结尾。
        result.setAi21(barcode.substring(cursor + 2));
        break;
      } else if (barcode.startsWith("8004", cursor)) {
        result.setAi8004(barcode.substring(cursor + 4));
        break;
      } else {
        break;
      }
    }
  }

  /**
   * 根据 AI 编码写入对应的独立字段。
   *
   * @param result GS1 解析结果
   * @param ai AI 编码
   * @param value AI 值
   */
  private void putAi(Gs1BarcodeResult result, String ai, String value) {
    if ("01".equals(ai)) {
      result.setAi01(value);
    } else if ("21".equals(ai)) {
      result.setAi21(value);
    } else if ("8004".equals(ai)) {
      result.setAi8004(value);
    }
  }

  /**
   * 将 GTIN 补齐为 GTIN-14。
   *
   * @param gtin 原始 GTIN
   * @return GTIN-14
   * @throws BaseException GTIN 无效
   */
  private String normalizeGtin(String gtin) throws BaseException {
    requireNotBlank(gtin, EpcErrorCode.BARCODE_LENGTH_ERROR);
    if (!isNumeric(gtin) || gtin.length() < 8 || gtin.length() > 14) {
      throw new BaseException(EpcErrorCode.BARCODE_LENGTH_ERROR, gtin, "8~14");
    }
    return "0".repeat(14 - gtin.length()) + gtin;
  }

  /**
   * 将 AI 8004 按公司前缀长度拆分为公司前缀和资产参考。
   *
   * @param giai AI 8004 值
   * @param companyPrefixLength 公司前缀长度
   * @return GIAI 拆分结果
   * @throws BaseException AI 8004 无效
   */
  private GiaiParts splitGiai(String giai, int companyPrefixLength) throws BaseException {
    requireNotBlank(giai, EpcErrorCode.EPC_AI_REQUIRED);
    if (giai.length() <= companyPrefixLength) {
      throw new BaseException(EpcErrorCode.EPC_ASSET_REFERENCE_ERROR, giai);
    }
    return new GiaiParts(giai.substring(0, companyPrefixLength), giai.substring(companyPrefixLength));
  }

  /**
   * 构建通用 EPC 结果。
   *
   * @param type EPC 类型
   * @param tagSize Tag Size
   * @param bits EPC 二进制编码
   * @param uri EPC Tag URI
   * @return EPC 结果
   */
  private EpcResult baseResult(String type, int tagSize, String bits, String uri) {
    return new EpcResult()
        .setType(type)
        .setBitLength(tagSize)
        .setBinary(bits)
        .setHex(binaryToHex(bits))
        .setUri(uri)
        .setValid(true);
  }

  /**
   * 根据公司前缀长度查找分区。
   *
   * @param companyPrefixLength 公司前缀长度
   * @return 分区定义
   * @throws BaseException 公司前缀长度无效
   */
  private Partition partitionByCompanyPrefixLength(int companyPrefixLength) throws BaseException {
    for (Partition partition : PARTITIONS) {
      if (partition.companyPrefixDigits == companyPrefixLength) {
        return partition;
      }
    }
    throw new BaseException(EpcErrorCode.EPC_COMPANY_PREFIX_LENGTH_ERROR, companyPrefixLength);
  }

  /**
   * 根据分区值查找分区。
   *
   * @param value 分区值
   * @return 分区定义
   * @throws BaseException 分区值无效
   */
  private Partition partitionByValue(int value) throws BaseException {
    if (value < 0 || value >= PARTITIONS.length) {
      throw new BaseException(EpcErrorCode.EPC_PARTITION_ERROR, value);
    }
    return PARTITIONS[value];
  }

  /**
   * 校验 EPC filter 值。
   *
   * @param filter filter 值
   * @throws BaseException filter 无效
   */
  private void validateFilter(int filter) throws BaseException {
    if (filter < 0 || filter > 7) {
      throw new BaseException(EpcErrorCode.EPC_PARTITION_ERROR, filter);
    }
  }

  /**
   * 校验公司前缀。
   *
   * @param companyPrefix 公司前缀
   * @param partition 分区定义
   * @throws BaseException 公司前缀无效
   */
  private void validateCompanyPrefix(String companyPrefix, Partition partition) throws BaseException {
    if (ObjectUtil.isEmpty(companyPrefix) || companyPrefix.length() != partition.companyPrefixDigits || !isNumeric(companyPrefix)) {
      throw new BaseException(EpcErrorCode.EPC_COMPANY_PREFIX_LENGTH_ERROR, companyPrefix);
    }
  }

  /**
   * 校验字符串不能为空。
   *
   * @param value 字符串值
   * @param code 错误码
   * @throws BaseException 字符串为空
   */
  private void requireNotBlank(String value, String code) throws BaseException {
    if (ObjectUtil.isEmpty(value)) {
      throw new BaseException(code);
    }
  }

  /**
   * 校验字符串是否符合默认 7bit 字符编码要求。
   *
   * @param value 字符串值
   * @param code 错误码
   * @throws BaseException 字符串无效
   */
  private void validateSevenBit(String value, String code) throws BaseException {
    validateSevenBit(value, 20, code);
  }

  /**
   * 校验字符串是否符合指定长度的 7bit 字符编码要求。
   *
   * @param value 字符串值
   * @param maxLength 最大字符数
   * @param code 错误码
   * @throws BaseException 字符串无效
   */
  private void validateSevenBit(String value, int maxLength, String code) throws BaseException {
    if (value.length() > maxLength) {
      throw new BaseException(code, value);
    }
    for (int i = 0; i < value.length(); i++) {
      if (value.charAt(i) > 127) {
        throw new BaseException(code, value);
      }
    }
  }

  /**
   * 判断字符串是否为纯数字。
   *
   * @param value 字符串值
   * @return 是否为纯数字
   */
  private static boolean isNumeric(String value) {
    return ObjectUtil.isNotEmpty(value) && value.matches("\\d+");
  }

  /**
   * 将 long 数值转为固定位数二进制。
   *
   * @param value 数值
   * @param bits bit 位数
   * @return 二进制字符串
   */
  private static String fixedBinary(long value, int bits) {
    return fixedBinary(BigInteger.valueOf(value), bits);
  }

  /**
   * 将大整数转为固定位数二进制。
   *
   * @param value 数值
   * @param bits bit 位数
   * @return 二进制字符串
   */
  private static String fixedBinary(BigInteger value, int bits) {
    String binary = value.toString(2);
    if (binary.length() > bits) {
      return binary.substring(binary.length() - bits);
    }
    return "0".repeat(bits - binary.length()) + binary;
  }

  /**
   * 将字符串按 7bit 字符编码为二进制。
   *
   * @param value 字符串值
   * @param bits 总 bit 位数
   * @return 二进制字符串
   */
  private static String encodeString(String value, int bits) {
    StringBuilder builder = new StringBuilder(bits);
    for (int i = 0; i < value.length(); i++) {
      builder.append(fixedBinary(value.charAt(i), 7));
    }
    if (builder.length() > bits) {
      return builder.substring(0, bits);
    }
    return builder.append("0".repeat(bits - builder.length())).toString();
  }

  /**
   * 将 7bit 字符二进制解码为字符串。
   *
   * @param bits 二进制字符串
   * @return 解码后的字符串
   */
  private static String decodeString(String bits) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i + 7 <= bits.length(); i += 7) {
      int value = Integer.parseInt(bits.substring(i, i + 7), 2);
      if (value == 0) {
        break;
      }
      builder.append((char) value);
    }
    return builder.toString();
  }

  /**
   * 将二进制 EPC 转为十六进制 EPC。
   *
   * @param binary 二进制字符串
   * @return 十六进制字符串
   */
  private static String binaryToHex(String binary) {
    // 198/202 bit 不是 4 的整数倍，转十六进制时末尾补 0 到 nibble 边界。
    int pad = (4 - binary.length() % 4) % 4;
    String padded = binary + "0".repeat(pad);
    StringBuilder hex = new StringBuilder(padded.length() / 4);
    for (int i = 0; i < padded.length(); i += 4) {
      hex.append(Integer.toHexString(Integer.parseInt(padded.substring(i, i + 4), 2)));
    }
    return hex.toString().toUpperCase(Locale.ROOT);
  }

  /**
   * 将十六进制 EPC 转为二进制 EPC。
   *
   * @param hex 十六进制字符串
   * @return 二进制字符串
   */
  private static String hexToBinary(String hex) {
    return fixedBinary(new BigInteger(hex, 16), hex.length() * 4);
  }

  /**
   * 读取指定区间的二进制整数。
   *
   * @param bits 二进制字符串
   * @param start 起始位置
   * @param end 结束位置
   * @return 整数值
   */
  private static int readInt(String bits, int start, int end) {
    return Integer.parseInt(bits.substring(start, end), 2);
  }

  /**
   * 读取指定区间的二进制十进制字段。
   *
   * @param bits 二进制字符串
   * @param start 起始位置
   * @param end 结束位置
   * @param digits 输出十进制位数
   * @return 十进制字符串
   */
  private static String readDecimal(String bits, int start, int end, int digits) {
    String value = new BigInteger(bits.substring(start, end), 2).toString();
    return "0".repeat(Math.max(0, digits - value.length())) + value;
  }

  /**
   * 计算 GS1 校验位。
   *
   * @param withoutCheckDigit 不含校验位的数字字符串
   * @return 校验位
   */
  private static int gs1CheckDigit(String withoutCheckDigit) {
    int sum = 0;
    boolean triple = true;
    for (int i = withoutCheckDigit.length() - 1; i >= 0; i--) {
      int digit = withoutCheckDigit.charAt(i) - '0';
      sum += triple ? digit * 3 : digit;
      triple = !triple;
    }
    return (10 - (sum % 10)) % 10;
  }
}
