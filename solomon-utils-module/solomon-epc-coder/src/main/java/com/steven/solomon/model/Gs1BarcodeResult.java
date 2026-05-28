package com.steven.solomon.model;

import com.steven.solomon.verification.ValidateUtils;

public class Gs1BarcodeResult {

  /** 原始 GS1 条码内容。 */
  private String content;

  /** AI 01，GTIN，固定 14 位。 */
  private String ai01;

  /** AI 21，序列号，可变长。 */
  private String ai21;

  /** AI 8004，GIAI 资产标识。 */
  private String ai8004;

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getAi01() { return ai01; }
  public void setAi01(String ai01) { this.ai01 = ai01; }
  public String getAi21() { return ai21; }
  public void setAi21(String ai21) { this.ai21 = ai21; }
  public String getAi8004() { return ai8004; }
  public void setAi8004(String ai8004) { this.ai8004 = ai8004; }
  public boolean hasGtinSerial() { return ValidateUtils.isNotEmpty(ai01) && ValidateUtils.isNotEmpty(ai21); }
  public boolean hasGiai() { return ValidateUtils.isNotEmpty(ai8004); }
}
