package com.steven.solomon.service;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;
import com.steven.solomon.verification.ValidateUtils;

/**
 * SGTIN 链式构建器。
 */
public class EpcBuilder {

  private final EpcService service;
  private String barcode;
  private String serialNumber;
  private int companyPrefixLength = 6;
  private int tagSize = 96;
  private int filter = 0;

  EpcBuilder(EpcService service) { this.service = service; }

  public EpcBuilder barcode(String barcode) { this.barcode = barcode; return this; }
  public EpcBuilder gtin(String gtin) { this.barcode = gtin; return this; }
  public EpcBuilder serial(String serialNumber) { this.serialNumber = serialNumber; return this; }
  public EpcBuilder serialNumber(String serialNumber) { this.serialNumber = serialNumber; return this; }
  public EpcBuilder companyPrefixLength(int companyPrefixLength) { this.companyPrefixLength = companyPrefixLength; return this; }
  public EpcBuilder tagSize(int tagSize) { this.tagSize = tagSize; return this; }
  public EpcBuilder filter(int filter) { this.filter = filter; return this; }

  public EpcResult encode() throws BaseException {
    if (ValidateUtils.isEmpty(serialNumber) && ValidateUtils.isNotEmpty(barcode) && barcode.startsWith("(")) {
      return service.gs1ToEpc(barcode, companyPrefixLength, tagSize, filter);
    }
    return service.gtinSerialToEpc(barcode, serialNumber, companyPrefixLength, tagSize, filter);
  }

  public EpcResult generate() throws BaseException { return encode(); }
}
