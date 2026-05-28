package com.steven.solomon.service;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;
import com.steven.solomon.verification.ValidateUtils;

/**
 * GS1 AI 条码链式构建器。
 */
public class Gs1Builder {

  private final EpcService service;
  private String barcode;
  private String ai01;
  private String ai21;
  private String ai8004;
  private int companyPrefixLength = 6;
  private int tagSize = 96;
  private int filter = 0;

  Gs1Builder(EpcService service) { this.service = service; }

  public Gs1Builder barcode(String barcode) { this.barcode = barcode; return this; }
  public Gs1Builder ai01(String ai01) { this.ai01 = ai01; return this; }
  public Gs1Builder ai21(String ai21) { this.ai21 = ai21; return this; }
  public Gs1Builder ai8004(String ai8004) { this.ai8004 = ai8004; return this; }
  public Gs1Builder companyPrefixLength(int companyPrefixLength) { this.companyPrefixLength = companyPrefixLength; return this; }
  public Gs1Builder tagSize(int tagSize) { this.tagSize = tagSize; return this; }
  public Gs1Builder filter(int filter) { this.filter = filter; return this; }

  public EpcResult encode() throws BaseException {
    if (ValidateUtils.isNotEmpty(ai01) || ValidateUtils.isNotEmpty(ai21)) {
      return service.gtinSerialToEpc(ai01, ai21, companyPrefixLength, tagSize, filter).setBarcode("(01)" + ai01 + "(21)" + ai21);
    }
    if (ValidateUtils.isNotEmpty(ai8004)) {
      return service.gs1ToEpc("(8004)" + ai8004, companyPrefixLength, tagSize, filter);
    }
    return service.gs1ToEpc(barcode, companyPrefixLength, tagSize, filter);
  }
}
