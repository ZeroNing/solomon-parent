package com.steven.solomon.service;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;

/**
 * GIAI 链式构建器。
 */
public class GiaiBuilder {

  private final EpcService service;
  private String companyPrefix;
  private String assetReference;
  private int tagSize = 96;
  private int filter = 0;

  GiaiBuilder(EpcService service) { this.service = service; }

  public GiaiBuilder companyPrefix(String companyPrefix) { this.companyPrefix = companyPrefix; return this; }
  public GiaiBuilder assetReference(String assetReference) { this.assetReference = assetReference; return this; }
  public GiaiBuilder tagSize(int tagSize) { this.tagSize = tagSize; return this; }
  public GiaiBuilder filter(int filter) { this.filter = filter; return this; }

  public EpcResult encode() throws BaseException { return service.giaiToEpc(companyPrefix, assetReference, tagSize, filter); }
  public EpcResult generate() throws BaseException { return encode(); }
}
