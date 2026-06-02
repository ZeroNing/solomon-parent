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

/**
 * EPC（电子产品编码）编解码服务。
 * <p>
 * 提供 GS1 条码解析、EPC 编码生成和 EPC 反译解码等功能，
 * 支持 SGTIN-96、SGTIN-198、GIAI-96、GIAI-202 等 EPC 类型。
 * </p>
 *
 * <h3>功能概述</h3>
 * <ul>
 *   <li>GS1 条码解析（支持括号格式和纯文本格式）</li>
 *   <li>GTIN + 序列号 → EPC 编码</li>
 *   <li>GS1 条码 → EPC 编码</li>
 *   <li>GIAI 公司前缀 + 资产参考 → EPC 编码</li>
 *   <li>EPC 十六进制 → 反译解码</li>
 * </ul>
 *
 * @author 创建者
 */
public class EpcService {

  /** SGTIN-96 的 Header（0x30 = 00110000）。 */
  private static final int HEADER_SGTIN_96 = 0x30;

  /** SGTIN-198 的 Header（0x36 = 00110110）。 */
  private static final int HEADER_SGTIN_198 = 0x36;

  /** GIAI-96 的 Header（0x34 = 00110100）。 */
  private static final int HEADER_GIAI_96 = 0x34;

  /** GIAI-202 的 Header（0x38 = 00111000）。 */
  private static final int HEADER_GIAI_202 = 0x38;

  /** SGTIN-96 序列号最大值（2^38 - 1）。 */
  private static final long SGTIN_96_MAX_SERIAL = 274877906943L;

  /** GS1 括号 AI（应用标识符）正则匹配模式，匹配 (01)、(21)、(8004) 等。 */
  private static final Pattern BRACKET_AI_PATTERN = Pattern.compile("\\((\\d{2,4})\\)");

  /** GS1 EPC 分区表，用于公司前缀和项目参考的位数映射。 */
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
   * 创建一个 SGTIN EPC 构建器。
   *
   * @return EPC 构建器
   */
  public EpcBuilder builder() { return new EpcBuilder(this); }

  /**
   * 创建一个 GS1 AI 条码构建器。
   *
   * @return GS1 构建器
   */
  public Gs1Builder gs1() { return new Gs1Builder(this); }

  /**
   * 创建一个默认标签尺寸为 96 的 SGTIN 构建器。
   *
   * @return EPC 构建器
   */
  public EpcBuilder sgtin96() { return builder().tagSize(96); }

  /**
   * 创建一个默认标签尺寸为 96 的 SSCC 构建器。
   *
   * @return EPC 构建器
   */
  public EpcBuilder sscc96() { return builder().tagSize(96); }

  /**
   * 创建一个默认标签尺寸为 198 的 SGTIN 构建器。
   *
   * @return EPC 构建器
   */
  public EpcBuilder sgtin198() { return builder().tagSize(198); }

  /**
   * 创建一个默认标签尺寸为 96 的 GIAI 构建器。
   *
   * @return GIAI 构建器
   */
  public GiaiBuilder giai96() { return giai().tagSize(96); }

  /**
   * 创建一个默认标签尺寸为 202 的 GIAI 构建器。
   *
   * @return GIAI 构建器
   */
  public GiaiBuilder giai202() { return giai().tagSize(202); }

  /**
   * 创建一个 GIAI 构建器。
   *
   * @return GIAI 构建器
   */
  public GiaiBuilder giai() { return new GiaiBuilder(this); }

  /**
   * 将 EAN-13 条码编码为 SGTIN-96。
   *
   * @param ean13              EAN-13 条码（13 位数字）
   * @param companyPrefixLength 公司前缀长度
   * @param serial              序列号
   * @return EPC 编码结果
   * @throws BaseException 如果条码长度无效则抛出异常
   */
  public EpcResult ean13ToSgtin96(String ean13, int companyPrefixLength, String serial) throws BaseException {
    if (ObjectUtil.isEmpty(ean13) || ean13.length() != 13) {
      throw new BaseException(EpcErrorCode.BARCODE_LENGTH_ERROR, ean13, 13);
    }
    return sgtin(ean13, companyPrefixLength, serial, 96, 0);
  }

  /**
   * 将 GTIN + 序列号编码为 EPC（默认 filter=0）。
   *
   * @param gtin                GTIN 字符串（8-14 位数字）
   * @param serial              序列号
   * @param companyPrefixLength 公司前缀长度
   * @param tagSize             标签尺寸（96 或 198）
   * @return EPC 编码结果
   * @throws BaseException 如果参数无效则抛出异常
   */
  public EpcResult gtinSerialToEpc(String gtin, String serial, int companyPrefixLength, int tagSize) throws BaseException {
    return gtinSerialToEpc(gtin, serial, companyPrefixLength, tagSize, 0);
  }

  /**
   * 将 GTIN + 序列号编码为 EPC（支持 filter 参数）。
   *
   * @param gtin                GTIN 字符串（8-14 位数字）
   * @param serial              序列号
   * @param companyPrefixLength 公司前缀长度
   * @param tagSize             标签尺寸（96 或 198）
   * @param filter              filter 值（0-7）
   * @return EPC 编码结果
   * @throws BaseException 如果参数无效则抛出异常
   */
  public EpcResult gtinSerialToEpc(String gtin, String serial, int companyPrefixLength, int tagSize, int filter) throws BaseException {
    return sgtin(gtin, companyPrefixLength, serial, tagSize, filter);
  }

  /**
   * 将 GS1 条码编码为 EPC（默认 filter=0）。
   *
   * @param barcode             GS1 条码字符串
   * @param companyPrefixLength 公司前缀长度
   * @param tagSize             标签尺寸
   * @return EPC 编码结果
   * @throws BaseException 如果参数无效则抛出异常
   */
  public EpcResult gs1ToEpc(String barcode, int companyPrefixLength, int tagSize) throws BaseException {
    return gs1ToEpc(barcode, companyPrefixLength, tagSize, 0);
  }

  /**
   * 将 GS1 条码编码为 EPC（支持 filter 参数）。
   * <p>
   * 先解析 GS1 条码，根据 AI 类型自动选择 SGTIN 或 GIAI 编码方式。
   * </p>
   *
   * @param barcode             GS1 条码字符串
   * @param companyPrefixLength 公司前缀长度
   * @param tagSize             标签尺寸
   * @param filter              filter 值（0-7）
   * @return EPC 编码结果
   * @throws BaseException 如果参数无效或 AI 类型不支持则抛出异常
   */
  public EpcResult gs1ToEpc(String barcode, int companyPrefixLength, int tagSize, int filter) throws BaseException {
    Gs1BarcodeResult gs1 = parseGs1Barcode(barcode);
    if (gs1.hasGtinSerial()) {
      return gtinSerialToEpc(gs1.getAi01(), gs1.getAi21(), companyPrefixLength, tagSize, filter).setBarcode(barcode);
    }
    if (gs1.hasGiai()) {
      GiaiParts parts = splitGiai(gs1.getAi8004(), companyPrefixLength);
      return giaiToEpc(parts.getCompanyPrefix(), parts.getAssetReference(), tagSize, filter).setBarcode(barcode);
    }
    throw new BaseException(EpcErrorCode.EPC_AI_REQUIRED, barcode);
  }

  /**
   * 将 GIAI 公司前缀 + 资产参考编码为 EPC（默认 filter=0）。
   *
   * @param companyPrefix  公司前缀
   * @param assetReference 资产参考
   * @param tagSize        标签尺寸（96 或 202）
   * @return EPC 编码结果
   * @throws BaseException 如果参数无效则抛出异常
   */
  public EpcResult giaiToEpc(String companyPrefix, String assetReference, int tagSize) throws BaseException {
    return giaiToEpc(companyPrefix, assetReference, tagSize, 0);
  }

  /**
   * 将 GIAI 公司前缀 + 资产参考编码为 EPC（支持 filter 参数）。
   * <p>
   * 支持 GIAI-96（资产参考必须为纯数字）和 GIAI-202（资产参考支持 7 位 ASCII 字符）两种标签尺寸。
   * </p>
   *
   * @param companyPrefix  公司前缀
   * @param assetReference 资产参考
   * @param tagSize        标签尺寸（96 或 202）
   * @param filter         filter 值（0-7）
   * @return EPC 编码结果
   * @throws BaseException 如果参数无效则抛出异常
   */
  public EpcResult giaiToEpc(String companyPrefix, String assetReference, int tagSize, int filter) throws BaseException {
    validateFilter(filter);
    Partition partition = partitionByCompanyPrefixLength(ObjectUtil.isEmpty(companyPrefix) ? 0 : companyPrefix.length());
    validateCompanyPrefix(companyPrefix, partition);
    requireNotBlank(assetReference, EpcErrorCode.EPC_ASSET_REFERENCE_ERROR);
    String type; String bits;
    if (tagSize == 96) {
      if (!isNumeric(assetReference)) { throw new BaseException(EpcErrorCode.EPC_ASSET_REFERENCE_ERROR, assetReference); }
      int assetBits = 82 - partition.companyPrefixBits;
      if (new BigInteger(assetReference).bitLength() > assetBits) { throw new BaseException(EpcErrorCode.EPC_ASSET_REFERENCE_ERROR, assetReference); }
      bits = fixedBinary(HEADER_GIAI_96, 8) + fixedBinary(filter, 3) + fixedBinary(partition.value, 3)
          + fixedBinary(new BigInteger(companyPrefix), partition.companyPrefixBits) + fixedBinary(new BigInteger(assetReference), assetBits);
      type = "GIAI-96";
    } else if (tagSize == 202) {
      int assetBits = 188 - partition.companyPrefixBits;
      validateSevenBit(assetReference, assetBits / 7, EpcErrorCode.EPC_ASSET_REFERENCE_ERROR);
      bits = fixedBinary(HEADER_GIAI_202, 8) + fixedBinary(filter, 3) + fixedBinary(partition.value, 3)
          + fixedBinary(new BigInteger(companyPrefix), partition.companyPrefixBits) + encodeString(assetReference, assetBits);
      type = "GIAI-202";
    } else { throw new BaseException(EpcErrorCode.EPC_TAG_SIZE_ERROR, tagSize); }
    String uri = "urn:epc:tag:giai-" + tagSize + ":" + filter + "." + companyPrefix + "." + assetReference;
    return baseResult(type, tagSize, bits, uri).setCompanyPrefix(companyPrefix).setCompanyPrefixLength(companyPrefix.length())
        .setAssetReference(assetReference).setAi8004(companyPrefix + assetReference).setFilter(filter);
  }

  /**
   * 将 SSCC（系列货运容器代码）编码为 SSCC-96。
   *
   * @param sscc                SSCC 条码（18 位数字）
   * @param companyPrefixLength 公司前缀长度
   * @return EPC 编码结果
   * @throws BaseException 如果条码格式无效则抛出异常
   */
  public EpcResult ssccToSscc96(String sscc, int companyPrefixLength) throws BaseException {
    if (ObjectUtil.isEmpty(sscc) || sscc.length() != 18 || !isNumeric(sscc)) { throw new BaseException(EpcErrorCode.BARCODE_LENGTH_ERROR, sscc, 18); }
    String companyPrefix = sscc.substring(1, 1 + companyPrefixLength);
    String serial = sscc.substring(1 + companyPrefixLength, 17);
    return new EpcResult().setType("SSCC-96").setBitLength(96).setHex("").setUri("urn:epc:tag:sscc-96:0." + companyPrefix + "." + serial)
        .setCompanyPrefix(companyPrefix).setCompanyPrefixLength(companyPrefixLength).setSerial(serial).setValid(true);
  }

  /**
   * 解码 EPC 十六进制字符串。
   * <p>
   * 根据 EPC Header 自动识别 EPC 类型（SGTIN-96、SGTIN-198、GIAI-96、GIAI-202）并进行反译。
   * </p>
   *
   * @param hex EPC 十六进制字符串
   * @return EPC 解码结果
   * @throws BaseException 如果十六进制格式无效或 Header 不匹配则抛出异常
   */
  public EpcResult decodeEpc(String hex) throws BaseException {
    if (ObjectUtil.isEmpty(hex) || !hex.matches("(?i)[0-9a-f]+")) { throw new BaseException(EpcErrorCode.EPC_INVALID_FORMAT, hex); }
    String bits = hexToBinary(hex.toUpperCase(Locale.ROOT));
    int header = Integer.parseInt(bits.substring(0, 8), 2);
    if (header == HEADER_SGTIN_96) { return decodeSgtin(bits, hex, 96); }
    if (header == HEADER_SGTIN_198) { return decodeSgtin(bits, hex, 198); }
    if (header == HEADER_GIAI_96) { return decodeGiai(bits, hex, 96); }
    if (header == HEADER_GIAI_202) { return decodeGiai(bits, hex, 202); }
    throw new BaseException(EpcErrorCode.EPC_HEADER_MISMATCH, header);
  }

  /**
   * 解析 GS1 条码。
   * <p>
   * 支持两种格式：
   * <ul>
   *   <li>括号格式：如 "(01)06901234567892(21)ABC123"</li>
   *   <li>纯文本格式：如 "010690123456789221ABC123"</li>
   * </ul>
   * </p>
   *
   * @param barcode GS1 条码字符串
   * @return GS1 条码解析结果
   * @throws BaseException 如果条码为空则抛出异常
   */
  public Gs1BarcodeResult parseGs1Barcode(String barcode) throws BaseException {
    requireNotBlank(barcode, EpcErrorCode.EPC_AI_REQUIRED);
    Gs1BarcodeResult result = new Gs1BarcodeResult();
    result.setContent(barcode);
    if (barcode.contains("(")) { parseBracketGs1(barcode, result); } else { parsePlainGs1(barcode, result); }
    return result;
  }

  /**
   * 执行 SGTIN 编码。
   *
   * @param gtin                GTIN 字符串
   * @param companyPrefixLength 公司前缀长度
   * @param serial              序列号
   * @param tagSize             标签尺寸（96 或 198）
   * @param filter              filter 值
   * @return EPC 编码结果
   * @throws BaseException 如果参数无效则抛出异常
   */
  private EpcResult sgtin(String gtin, int companyPrefixLength, String serial, int tagSize, int filter) throws BaseException {
    validateFilter(filter);
    String gtin14 = normalizeGtin(gtin);
    Partition partition = partitionByCompanyPrefixLength(companyPrefixLength);
    String companyPrefix = gtin14.substring(1, 1 + companyPrefixLength);
    String itemReference = gtin14.charAt(0) + gtin14.substring(1 + companyPrefixLength, 13);
    validateCompanyPrefix(companyPrefix, partition);
    requireNotBlank(serial, EpcErrorCode.EPC_SERIAL_NUMBER_ERROR);
    String type; String bits;
    if (tagSize == 96) {
      if (!isNumeric(serial)) { throw new BaseException(EpcErrorCode.EPC_SERIAL_NUMBER_ERROR, serial); }
      long serialValue = Long.parseLong(serial);
      if (serialValue < 0 || serialValue > SGTIN_96_MAX_SERIAL) { throw new BaseException(EpcErrorCode.EPC_SERIAL_NUMBER_RANGE_ERROR, serial); }
      bits = fixedBinary(HEADER_SGTIN_96, 8) + fixedBinary(filter, 3) + fixedBinary(partition.value, 3)
          + fixedBinary(new BigInteger(companyPrefix), partition.companyPrefixBits)
          + fixedBinary(new BigInteger(itemReference), partition.itemReferenceBits) + fixedBinary(serialValue, 38);
      type = "SGTIN-96";
    } else if (tagSize == 198) {
      validateSevenBit(serial, 20, EpcErrorCode.EPC_SERIAL_NUMBER_ERROR);
      bits = fixedBinary(HEADER_SGTIN_198, 8) + fixedBinary(filter, 3) + fixedBinary(partition.value, 3)
          + fixedBinary(new BigInteger(companyPrefix), partition.companyPrefixBits)
          + fixedBinary(new BigInteger(itemReference), partition.itemReferenceBits) + encodeString(serial, 140);
      type = "SGTIN-198";
    } else { throw new BaseException(EpcErrorCode.EPC_TAG_SIZE_ERROR, tagSize); }
    String uri = "urn:epc:tag:sgtin-" + tagSize + ":" + filter + "." + companyPrefix + "." + itemReference + "." + serial;
    return baseResult(type, tagSize, bits, uri).setCompanyPrefix(companyPrefix).setCompanyPrefixLength(companyPrefixLength)
        .setItemReference(itemReference).setGtin14(gtin14).setAi01(gtin14).setAi21(serial).setSerial(serial).setFilter(filter);
  }

  /**
   * 解码 SGTIN 二进制位。
   *
   * @param bits    EPC 二进制字符串
   * @param hex     原始十六进制字符串
   * @param tagSize 标签尺寸（96 或 198）
   * @return EPC 解码结果
   * @throws BaseException 如果格式无效则抛出异常
   */
  private EpcResult decodeSgtin(String bits, String hex, int tagSize) throws BaseException {
    if (bits.length() < tagSize) { throw new BaseException(EpcErrorCode.EPC_INVALID_FORMAT, hex); }
    bits = bits.substring(0, tagSize);
    int filter = readInt(bits, 8, 11);
    Partition partition = partitionByValue(readInt(bits, 11, 14));
    int cursor = 14;
    String companyPrefix = readDecimal(bits, cursor, cursor + partition.companyPrefixBits, partition.companyPrefixDigits);
    cursor += partition.companyPrefixBits;
    String itemReference = readDecimal(bits, cursor, cursor + partition.itemReferenceBits, partition.itemReferenceDigits);
    cursor += partition.itemReferenceBits;
    String serial = tagSize == 96 ? new BigInteger(bits.substring(cursor), 2).toString() : decodeString(bits.substring(cursor));
    String gtin14 = itemReference.charAt(0) + companyPrefix + itemReference.substring(1);
    gtin14 = gtin14 + gs1CheckDigit(gtin14);
    String type = "SGTIN-" + tagSize;
    String uri = "urn:epc:tag:sgtin-" + tagSize + ":" + filter + "." + companyPrefix + "." + itemReference + "." + serial;
    return baseResult(type, tagSize, bits, uri).setHex(hex.toUpperCase(Locale.ROOT)).setCompanyPrefix(companyPrefix)
        .setCompanyPrefixLength(partition.companyPrefixDigits).setItemReference(itemReference).setGtin14(gtin14)
        .setAi01(gtin14).setAi21(serial).setSerial(serial).setFilter(filter);
  }

  /**
   * 解码 GIAI 二进制位。
   *
   * @param bits    EPC 二进制字符串
   * @param hex     原始十六进制字符串
   * @param tagSize 标签尺寸（96 或 202）
   * @return EPC 解码结果
   * @throws BaseException 如果格式无效则抛出异常
   */
  private EpcResult decodeGiai(String bits, String hex, int tagSize) throws BaseException {
    if (bits.length() < tagSize) { throw new BaseException(EpcErrorCode.EPC_INVALID_FORMAT, hex); }
    bits = bits.substring(0, tagSize);
    int filter = readInt(bits, 8, 11);
    Partition partition = partitionByValue(readInt(bits, 11, 14));
    int cursor = 14;
    String companyPrefix = readDecimal(bits, cursor, cursor + partition.companyPrefixBits, partition.companyPrefixDigits);
    cursor += partition.companyPrefixBits;
    String assetReference = tagSize == 96 ? new BigInteger(bits.substring(cursor), 2).toString() : decodeString(bits.substring(cursor));
    String type = "GIAI-" + tagSize;
    String uri = "urn:epc:tag:giai-" + tagSize + ":" + filter + "." + companyPrefix + "." + assetReference;
    return baseResult(type, tagSize, bits, uri).setHex(hex.toUpperCase(Locale.ROOT)).setCompanyPrefix(companyPrefix)
        .setCompanyPrefixLength(partition.companyPrefixDigits).setAssetReference(assetReference).setAi8004(companyPrefix + assetReference).setFilter(filter);
  }

  /**
   * 解析括号格式的 GS1 条码（如 "(01)GTIN(21)Serial"）。
   *
   * @param barcode GS1 条码字符串
   * @param result  解析结果对象
   */
  private void parseBracketGs1(String barcode, Gs1BarcodeResult result) {
    Matcher matcher = BRACKET_AI_PATTERN.matcher(barcode);
    String ai = null; int valueStart = -1;
    while (matcher.find()) {
      if (ObjectUtil.isNotEmpty(ai)) { putAi(result, ai, barcode.substring(valueStart, matcher.start())); }
      ai = matcher.group(1); valueStart = matcher.end();
    }
    if (ObjectUtil.isNotEmpty(ai)) { putAi(result, ai, barcode.substring(valueStart)); }
  }

  /**
   * 解析纯文本格式的 GS1 条码（如 "01GTIN21Serial"）。
   *
   * @param barcode GS1 条码字符串
   * @param result  解析结果对象
   */
  private void parsePlainGs1(String barcode, Gs1BarcodeResult result) {
    int cursor = 0;
    while (cursor < barcode.length()) {
      if (barcode.startsWith("01", cursor) && barcode.length() >= cursor + 16) { result.setAi01(barcode.substring(cursor + 2, cursor + 16)); cursor += 16; }
      else if (barcode.startsWith("21", cursor)) { result.setAi21(barcode.substring(cursor + 2)); break; }
      else if (barcode.startsWith("8004", cursor)) { result.setAi8004(barcode.substring(cursor + 4)); break; }
      else { break; }
    }
  }

  /**
   * 将解析出的 AI 值设置到结果对象中。
   *
   * @param result 解析结果对象
   * @param ai     AI 标识符（如 "01"、"21"、"8004"）
   * @param value  AI 对应的值
   */
  private void putAi(Gs1BarcodeResult result, String ai, String value) {
    if ("01".equals(ai)) { result.setAi01(value); } else if ("21".equals(ai)) { result.setAi21(value); } else if ("8004".equals(ai)) { result.setAi8004(value); }
  }

  /**
   * 标准化 GTIN 为 14 位，不足时前面补零。
   *
   * @param gtin 原始 GTIN 字符串
   * @return 14 位 GTIN 字符串
   * @throws BaseException 如果 GTIN 格式无效则抛出异常
   */
  private String normalizeGtin(String gtin) throws BaseException {
    requireNotBlank(gtin, EpcErrorCode.BARCODE_LENGTH_ERROR);
    if (!isNumeric(gtin) || gtin.length() < 8 || gtin.length() > 14) { throw new BaseException(EpcErrorCode.BARCODE_LENGTH_ERROR, gtin, "8~14"); }
    return "0".repeat(14 - gtin.length()) + gtin;
  }

  /**
   * 拆分 GIAI 为公司前缀和资产参考。
   *
   * @param giai                GIAI 完整值
   * @param companyPrefixLength 公司前缀长度
   * @return 拆分后的 GIAI 字段
   * @throws BaseException 如果 GIAI 值无效则抛出异常
   */
  private GiaiParts splitGiai(String giai, int companyPrefixLength) throws BaseException {
    requireNotBlank(giai, EpcErrorCode.EPC_AI_REQUIRED);
    if (giai.length() <= companyPrefixLength) { throw new BaseException(EpcErrorCode.EPC_ASSET_REFERENCE_ERROR, giai); }
    return new GiaiParts(giai.substring(0, companyPrefixLength), giai.substring(companyPrefixLength));
  }

  /**
   * 创建基础的 EPC 结果对象。
   *
   * @param type   EPC 类型
   * @param tagSize 标签尺寸
   * @param bits   二进制字符串
   * @param uri    EPC URI
   * @return 基础结果对象
   */
  private EpcResult baseResult(String type, int tagSize, String bits, String uri) {
    return new EpcResult().setType(type).setBitLength(tagSize).setBinary(bits).setHex(binaryToHex(bits)).setUri(uri).setValid(true);
  }

  /**
   * 根据公司前缀长度查找对应的分区定义。
   *
   * @param companyPrefixLength 公司前缀长度
   * @return 分区定义
   * @throws BaseException 如果未找到匹配的分区则抛出异常
   */
  private Partition partitionByCompanyPrefixLength(int companyPrefixLength) throws BaseException {
    for (Partition partition : PARTITIONS) { if (partition.companyPrefixDigits == companyPrefixLength) { return partition; } }
    throw new BaseException(EpcErrorCode.EPC_COMPANY_PREFIX_LENGTH_ERROR, companyPrefixLength);
  }

  /**
   * 根据分区值查找对应的分区定义。
   *
   * @param value 分区值（0-6）
   * @return 分区定义
   * @throws BaseException 如果分区值无效则抛出异常
   */
  private Partition partitionByValue(int value) throws BaseException {
    if (value < 0 || value >= PARTITIONS.length) { throw new BaseException(EpcErrorCode.EPC_PARTITION_ERROR, value); }
    return PARTITIONS[value];
  }

  /**
   * 校验 filter 值是否有效。
   *
   * @param filter filter 值
   * @throws BaseException 如果 filter 超出 0-7 范围则抛出异常
   */
  private void validateFilter(int filter) throws BaseException { if (filter < 0 || filter > 7) { throw new BaseException(EpcErrorCode.EPC_PARTITION_ERROR, filter); } }

  /**
   * 校验公司前缀是否与分区定义匹配。
   *
   * @param companyPrefix 公司前缀
   * @param partition     分区定义
   * @throws BaseException 如果公司前缀无效则抛出异常
   */
  private void validateCompanyPrefix(String companyPrefix, Partition partition) throws BaseException {
    if (ObjectUtil.isEmpty(companyPrefix) || companyPrefix.length() != partition.companyPrefixDigits || !isNumeric(companyPrefix)) { throw new BaseException(EpcErrorCode.EPC_COMPANY_PREFIX_LENGTH_ERROR, companyPrefix); }
  }

  /**
   * 校验参数非空。
   *
   * @param value 待校验的值
   * @param code  错误码
   * @throws BaseException 如果值为空则抛出异常
   */
  private void requireNotBlank(String value, String code) throws BaseException { if (ObjectUtil.isEmpty(value)) { throw new BaseException(code); } }

  /**
   * 校验 7 位 ASCII 字符串的有效性（默认最大长度 20）。
   *
   * @param value 待校验的字符串
   * @param code  错误码
   * @throws BaseException 如果字符串无效则抛出异常
   */
  private void validateSevenBit(String value, String code) throws BaseException { validateSevenBit(value, 20, code); }

  /**
   * 校验 7 位 ASCII 字符串的有效性和最大长度。
   *
   * @param value     待校验的字符串
   * @param maxLength 最大允许长度
   * @param code      错误码
   * @throws BaseException 如果字符串超长或包含非 ASCII 字符则抛出异常
   */
  private void validateSevenBit(String value, int maxLength, String code) throws BaseException {
    if (value.length() > maxLength) { throw new BaseException(code, value); }
    for (int i = 0; i < value.length(); i++) { if (value.charAt(i) > 127) { throw new BaseException(code, value); } }
  }

  /**
   * 判断字符串是否为纯数字。
   *
   * @param value 待判断的字符串
   * @return 如果为纯数字则返回 true
   */
  private static boolean isNumeric(String value) { return ObjectUtil.isNotEmpty(value) && value.matches("\\d+"); }

  /**
   * 将 long 值转换为固定位宽的二进制字符串。
   *
   * @param value 整数值
   * @param bits  目标位宽
   * @return 固定位宽的二进制字符串
   */
  private static String fixedBinary(long value, int bits) { return fixedBinary(BigInteger.valueOf(value), bits); }

  /**
   * 将 BigInteger 值转换为固定位宽的二进制字符串。
   * 如果值的二进制长度超出指定位宽，则截取低位；不足则高位补零。
   *
   * @param value BigInteger 值
   * @param bits  目标位宽
   * @return 固定位宽的二进制字符串
   */
  private static String fixedBinary(BigInteger value, int bits) {
    String binary = value.toString(2);
    if (binary.length() > bits) { return binary.substring(binary.length() - bits); }
    return "0".repeat(bits - binary.length()) + binary;
  }

  /**
   * 将字符串编码为 7 位 ASCII 二进制位流。
   *
   * @param value 待编码的字符串
   * @param bits  目标位宽
   * @return 二进制字符串
   */
  private static String encodeString(String value, int bits) {
    StringBuilder builder = new StringBuilder(bits);
    for (int i = 0; i < value.length(); i++) { builder.append(fixedBinary(value.charAt(i), 7)); }
    if (builder.length() > bits) { return builder.substring(0, bits); }
    return builder.append("0".repeat(bits - builder.length())).toString();
  }

  /**
   * 从 7 位 ASCII 二进制位流解码为字符串。
   * 遇到值为 0 的字节则停止解码。
   *
   * @param bits 二进制字符串
   * @return 解码后的字符串
   */
  private static String decodeString(String bits) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i + 7 <= bits.length(); i += 7) { int value = Integer.parseInt(bits.substring(i, i + 7), 2); if (value == 0) { break; } builder.append((char) value); }
    return builder.toString();
  }

  /**
   * 将二进制字符串转换为十六进制字符串。
   *
   * @param binary 二进制字符串
   * @return 十六进制字符串（大写）
   */
  private static String binaryToHex(String binary) {
    int pad = (4 - binary.length() % 4) % 4;
    String padded = binary + "0".repeat(pad);
    StringBuilder hex = new StringBuilder(padded.length() / 4);
    for (int i = 0; i < padded.length(); i += 4) { hex.append(Integer.toHexString(Integer.parseInt(padded.substring(i, i + 4), 2))); }
    return hex.toString().toUpperCase(Locale.ROOT);
  }

  /**
   * 将十六进制字符串转换为二进制字符串。
   *
   * @param hex 十六进制字符串
   * @return 二进制字符串
   */
  private static String hexToBinary(String hex) { return fixedBinary(new BigInteger(hex, 16), hex.length() * 4); }
  /**
   * 从二进制字符串中读取指定范围的整数值。
   *
   * @param bits  二进制字符串
   * @param start 起始位置（含）
   * @param end   结束位置（不含）
   * @return 整数值
   */
  private static int readInt(String bits, int start, int end) { return Integer.parseInt(bits.substring(start, end), 2); }
  /**
   * 从二进制字符串中读取指定位宽的十进制数字符串（自动补齐前导零）。
   *
   * @param bits  二进制字符串
   * @param start 起始位置（含）
   * @param end   结束位置（不含）
   * @param digits 目标十进制位数
   * @return 十进制数字符串
   */
  private static String readDecimal(String bits, int start, int end, int digits) { String value = new BigInteger(bits.substring(start, end), 2).toString(); return "0".repeat(Math.max(0, digits - value.length())) + value; }

  /**
   * 计算 GS1 校验位（Check Digit）。
   * <p>
   * 采用 GS1 标准算法：从右向左，奇数位乘以 3，偶数位乘以 1，
   * 求和后取 10 的补数作为校验位。
   * </p>
   *
   * @param withoutCheckDigit 不含校验位的数字字符串
   * @return 校验位（0-9）
   */
  private static int gs1CheckDigit(String withoutCheckDigit) {
    int sum = 0; boolean triple = true;
    for (int i = withoutCheckDigit.length() - 1; i >= 0; i--) { int digit = withoutCheckDigit.charAt(i) - '0'; sum += triple ? digit * 3 : digit; triple = !triple; }
    return (10 - (sum % 10)) % 10;
  }
}
