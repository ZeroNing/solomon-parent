package com.steven.solomon.model;

import cn.hutool.core.util.ObjectUtil;


public class Gs1BarcodeResult {

  /** 原始 GS1 条码内容。 */
  private String content;

  /** AI 01，GTIN，固定 14 位。 */
  private String ai01;

  /** AI 21，序列号，可变长。 */
  private String ai21;

  /** AI 8004，GIAI 资产标识。 */
  private String ai8004;

  /**
   * 获取原始 GS1 条码内容。
   *
   * @return 原始 GS1 条码内容
   */
  public String getContent() {
    return content;
  }

  /**
   * 设置原始 GS1 条码内容。
   *
   * @param content 原始 GS1 条码内容
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * 获取 AI 01。
   *
   * @return AI 01
   */
  public String getAi01() {
    return ai01;
  }

  /**
   * 设置 AI 01。
   *
   * @param ai01 AI 01
   */
  public void setAi01(String ai01) {
    this.ai01 = ai01;
  }

  /**
   * 获取 AI 21。
   *
   * @return AI 21
   */
  public String getAi21() {
    return ai21;
  }

  /**
   * 设置 AI 21。
   *
   * @param ai21 AI 21
   */
  public void setAi21(String ai21) {
    this.ai21 = ai21;
  }

  /**
   * 获取 AI 8004。
   *
   * @return AI 8004
   */
  public String getAi8004() {
    return ai8004;
  }

  /**
   * 设置 AI 8004。
   *
   * @param ai8004 AI 8004
   */
  public void setAi8004(String ai8004) {
    this.ai8004 = ai8004;
  }

  /**
   * 判断是否包含 AI 01 和 AI 21。
   *
   * @return 是否包含 GTIN 和序列号
   */
  public boolean hasGtinSerial() {
    return ObjectUtil.isNotEmpty(ai01) && ObjectUtil.isNotEmpty(ai21);
  }

  /**
   * 判断是否包含 AI 8004。
   *
   * @return 是否包含 GIAI
   */
  public boolean hasGiai() {
    return ObjectUtil.isNotEmpty(ai8004);
  }
}
