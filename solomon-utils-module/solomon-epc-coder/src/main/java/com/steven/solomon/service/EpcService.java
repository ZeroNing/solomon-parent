package com.steven.solomon.service;

import com.steven.solomon.code.EpcErrorCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;
import com.steven.solomon.model.Gs1BarcodeResult;
import com.steven.solomon.verification.ValidateUtils;

import java.math.BigInteger;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpcService {

  private static final int HEADER_SGTIN_96 = 0x30;
  private static final int HEADER_SGTIN_198 = 0x36;
  private static final int HEADER_GIAI_96 = 0x34;
  private static final int HEADER_GIAI_202 = 0x38;
  private static final long SGTIN_96_MAX_SERIAL = 274877906943L;
  private static final Pattern BRACKET_AI_PATTERN = Pattern.compile("\\((\\d{2,4})\\)");
  private static final Partition[] PARTITIONS = {
      new Partition(0, 12, 40, 1, 4),
      new Partition(1, 11, 37, 2, 7),
      new Partition(2, 10, 34, 3, 10),
      new Partition(3, 9, 30, 4, 14),
      new Partition(4, 8, 27, 5, 17),
      new Partition(5, 7, 24, 6, 20),
      new Partition(6, 6, 20, 7, 24)
  };

  public EpcBuilder builder() { return new EpcBuilder(this); }
  public Gs1Builder gs1() { return new Gs1Builder(this); }
  public EpcBuilder sgtin96() { return builder().tagSize(96); }
  public EpcBuilder sscc96() { return builder().tagSize(96); }
  public EpcBuilder sgtin198() { return builder().tagSize(198); }
  public GiaiBuilder giai96() { return giai().tagSize(96); }
  public GiaiBuilder giai202() { return giai().tagSize(202); }
  public GiaiBuilder giai() { return new GiaiBuilder(this); }

  public EpcResult ean13ToSgtin96(String ean13, int companyPrefixLength, String serial) throws BaseException {
    if (ValidateUtils.isEmpty(ean13) || ean13.length() != 13) {
      throw new BaseException(EpcErrorCode.BARCODE_LENGTH_ERROR, ean13, 13);
    }
    return sgtin(ean13, companyPrefixLength, serial, 96, 0);
  }

  public EpcResult gtinSerialToEpc(String gtin, String serial, int companyPrefixLength, int tagSize) throws BaseException {
    return gtinSerialToEpc(gtin, serial, companyPrefixLength, tagSize, 0);
  }

  public EpcResult gtinSerialToEpc(String gtin, String serial, int companyPrefixLength, int tagSize, int filter) throws BaseException {
    return sgtin(gtin, companyPrefixLength, serial, tagSize, filter);
  }

  public EpcResult gs1ToEpc(String barcode, int companyPrefixLength, int tagSize) throws BaseException {
    return gs1ToEpc(barcode, companyPrefixLength, tagSize, 0);
  }

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

  public EpcResult giaiToEpc(String companyPrefix, String assetReference, int tagSize) throws BaseException {
    return giaiToEpc(companyPrefix, assetReference, tagSize, 0);
  }

  public EpcResult giaiToEpc(String companyPrefix, String assetReference, int tagSize, int filter) throws BaseException {
    validateFilter(filter);
    Partition partition = partitionByCompanyPrefixLength(ValidateUtils.isEmpty(companyPrefix) ? 0 : companyPrefix.length());
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

  public EpcResult ssccToSscc96(String sscc, int companyPrefixLength) throws BaseException {
    if (ValidateUtils.isEmpty(sscc) || sscc.length() != 18 || !isNumeric(sscc)) { throw new BaseException(EpcErrorCode.BARCODE_LENGTH_ERROR, sscc, 18); }
    String companyPrefix = sscc.substring(1, 1 + companyPrefixLength);
    String serial = sscc.substring(1 + companyPrefixLength, 17);
    return new EpcResult().setType("SSCC-96").setBitLength(96).setHex("").setUri("urn:epc:tag:sscc-96:0." + companyPrefix + "." + serial)
        .setCompanyPrefix(companyPrefix).setCompanyPrefixLength(companyPrefixLength).setSerial(serial).setValid(true);
  }

  public EpcResult decodeEpc(String hex) throws BaseException {
    if (ValidateUtils.isEmpty(hex) || !hex.matches("(?i)[0-9a-f]+")) { throw new BaseException(EpcErrorCode.EPC_INVALID_FORMAT, hex); }
    String bits = hexToBinary(hex.toUpperCase(Locale.ROOT));
    int header = Integer.parseInt(bits.substring(0, 8), 2);
    if (header == HEADER_SGTIN_96) { return decodeSgtin(bits, hex, 96); }
    if (header == HEADER_SGTIN_198) { return decodeSgtin(bits, hex, 198); }
    if (header == HEADER_GIAI_96) { return decodeGiai(bits, hex, 96); }
    if (header == HEADER_GIAI_202) { return decodeGiai(bits, hex, 202); }
    throw new BaseException(EpcErrorCode.EPC_HEADER_MISMATCH, header);
  }

  public Gs1BarcodeResult parseGs1Barcode(String barcode) throws BaseException {
    requireNotBlank(barcode, EpcErrorCode.EPC_AI_REQUIRED);
    Gs1BarcodeResult result = new Gs1BarcodeResult();
    result.setContent(barcode);
    if (barcode.contains("(")) { parseBracketGs1(barcode, result); } else { parsePlainGs1(barcode, result); }
    return result;
  }

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

  private void parseBracketGs1(String barcode, Gs1BarcodeResult result) {
    Matcher matcher = BRACKET_AI_PATTERN.matcher(barcode);
    String ai = null; int valueStart = -1;
    while (matcher.find()) {
      if (ValidateUtils.isNotEmpty(ai)) { putAi(result, ai, barcode.substring(valueStart, matcher.start())); }
      ai = matcher.group(1); valueStart = matcher.end();
    }
    if (ValidateUtils.isNotEmpty(ai)) { putAi(result, ai, barcode.substring(valueStart)); }
  }

  private void parsePlainGs1(String barcode, Gs1BarcodeResult result) {
    int cursor = 0;
    while (cursor < barcode.length()) {
      if (barcode.startsWith("01", cursor) && barcode.length() >= cursor + 16) { result.setAi01(barcode.substring(cursor + 2, cursor + 16)); cursor += 16; }
      else if (barcode.startsWith("21", cursor)) { result.setAi21(barcode.substring(cursor + 2)); break; }
      else if (barcode.startsWith("8004", cursor)) { result.setAi8004(barcode.substring(cursor + 4)); break; }
      else { break; }
    }
  }

  private void putAi(Gs1BarcodeResult result, String ai, String value) {
    if ("01".equals(ai)) { result.setAi01(value); } else if ("21".equals(ai)) { result.setAi21(value); } else if ("8004".equals(ai)) { result.setAi8004(value); }
  }

  private String normalizeGtin(String gtin) throws BaseException {
    requireNotBlank(gtin, EpcErrorCode.BARCODE_LENGTH_ERROR);
    if (!isNumeric(gtin) || gtin.length() < 8 || gtin.length() > 14) { throw new BaseException(EpcErrorCode.BARCODE_LENGTH_ERROR, gtin, "8~14"); }
    return "0".repeat(14 - gtin.length()) + gtin;
  }

  private GiaiParts splitGiai(String giai, int companyPrefixLength) throws BaseException {
    requireNotBlank(giai, EpcErrorCode.EPC_AI_REQUIRED);
    if (giai.length() <= companyPrefixLength) { throw new BaseException(EpcErrorCode.EPC_ASSET_REFERENCE_ERROR, giai); }
    return new GiaiParts(giai.substring(0, companyPrefixLength), giai.substring(companyPrefixLength));
  }

  private EpcResult baseResult(String type, int tagSize, String bits, String uri) {
    return new EpcResult().setType(type).setBitLength(tagSize).setBinary(bits).setHex(binaryToHex(bits)).setUri(uri).setValid(true);
  }

  private Partition partitionByCompanyPrefixLength(int companyPrefixLength) throws BaseException {
    for (Partition partition : PARTITIONS) { if (partition.companyPrefixDigits == companyPrefixLength) { return partition; } }
    throw new BaseException(EpcErrorCode.EPC_COMPANY_PREFIX_LENGTH_ERROR, companyPrefixLength);
  }

  private Partition partitionByValue(int value) throws BaseException {
    if (value < 0 || value >= PARTITIONS.length) { throw new BaseException(EpcErrorCode.EPC_PARTITION_ERROR, value); }
    return PARTITIONS[value];
  }

  private void validateFilter(int filter) throws BaseException { if (filter < 0 || filter > 7) { throw new BaseException(EpcErrorCode.EPC_PARTITION_ERROR, filter); } }

  private void validateCompanyPrefix(String companyPrefix, Partition partition) throws BaseException {
    if (ValidateUtils.isEmpty(companyPrefix) || companyPrefix.length() != partition.companyPrefixDigits || !isNumeric(companyPrefix)) { throw new BaseException(EpcErrorCode.EPC_COMPANY_PREFIX_LENGTH_ERROR, companyPrefix); }
  }

  private void requireNotBlank(String value, String code) throws BaseException { if (ValidateUtils.isEmpty(value)) { throw new BaseException(code); } }

  private void validateSevenBit(String value, String code) throws BaseException { validateSevenBit(value, 20, code); }

  private void validateSevenBit(String value, int maxLength, String code) throws BaseException {
    if (value.length() > maxLength) { throw new BaseException(code, value); }
    for (int i = 0; i < value.length(); i++) { if (value.charAt(i) > 127) { throw new BaseException(code, value); } }
  }

  private static boolean isNumeric(String value) { return ValidateUtils.isNotEmpty(value) && value.matches("\\d+"); }

  private static String fixedBinary(long value, int bits) { return fixedBinary(BigInteger.valueOf(value), bits); }

  private static String fixedBinary(BigInteger value, int bits) {
    String binary = value.toString(2);
    if (binary.length() > bits) { return binary.substring(binary.length() - bits); }
    return "0".repeat(bits - binary.length()) + binary;
  }

  private static String encodeString(String value, int bits) {
    StringBuilder builder = new StringBuilder(bits);
    for (int i = 0; i < value.length(); i++) { builder.append(fixedBinary(value.charAt(i), 7)); }
    if (builder.length() > bits) { return builder.substring(0, bits); }
    return builder.append("0".repeat(bits - builder.length())).toString();
  }

  private static String decodeString(String bits) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i + 7 <= bits.length(); i += 7) { int value = Integer.parseInt(bits.substring(i, i + 7), 2); if (value == 0) { break; } builder.append((char) value); }
    return builder.toString();
  }

  private static String binaryToHex(String binary) {
    int pad = (4 - binary.length() % 4) % 4;
    String padded = binary + "0".repeat(pad);
    StringBuilder hex = new StringBuilder(padded.length() / 4);
    for (int i = 0; i < padded.length(); i += 4) { hex.append(Integer.toHexString(Integer.parseInt(padded.substring(i, i + 4), 2))); }
    return hex.toString().toUpperCase(Locale.ROOT);
  }

  private static String hexToBinary(String hex) { return fixedBinary(new BigInteger(hex, 16), hex.length() * 4); }
  private static int readInt(String bits, int start, int end) { return Integer.parseInt(bits.substring(start, end), 2); }
  private static String readDecimal(String bits, int start, int end, int digits) { String value = new BigInteger(bits.substring(start, end), 2).toString(); return "0".repeat(Math.max(0, digits - value.length())) + value; }

  private static int gs1CheckDigit(String withoutCheckDigit) {
    int sum = 0; boolean triple = true;
    for (int i = withoutCheckDigit.length() - 1; i >= 0; i--) { int digit = withoutCheckDigit.charAt(i) - '0'; sum += triple ? digit * 3 : digit; triple = !triple; }
    return (10 - (sum % 10)) % 10;
  }
}
