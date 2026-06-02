package com.steven.solomon.model;

import cn.hutool.core.util.ObjectUtil;


/**
 * GS1 条码解析结果。
 * <p>
 * 用于存储 GS1 条码解析后的各应用标识符（AI）字段值，
 * 支持 AI 01（GTIN）、AI 21（序列号）和 AI 8004（GIAI 资产标识）。
 * </p>
 *
 * @author 创建者
 */
public class Gs1BarcodeResult {

  /** 原始 GS1 条码内容。 */
  private String content;

  /** AI 01，GTIN（全球贸易项目代码），固定 14 位。 */
  private String ai01;

  /** AI 21，序列号，可变长。 */
  private String ai21;

  /** AI 8004，GIAI（全球单个资产标识）。 */
  private String ai8004;

  /**
   * 获取原始条码内容。
   *
   * @return 原始条码字符串
   */
  public String getContent() { return content; }

  /**
   * 设置原始条码内容。
   *
   * @param content 原始条码字符串
   */
  public void setContent(String content) { this.content = content; }

  /**
   * 获取 AI 01（GTIN）值。
   *
   * @return GTIN 字符串
   */
  public String getAi01() { return ai01; }

  /**
   * 设置 AI 01（GTIN）值。
   *
   * @param ai01 GTIN 字符串
   */
  public void setAi01(String ai01) { this.ai01 = ai01; }

  /**
   * 获取 AI 21（序列号）值。
   *
   * @return 序列号字符串
   */
  public String getAi21() { return ai21; }

  /**
   * 设置 AI 21（序列号）值。
   *
   * @param ai21 序列号字符串
   */
  public void setAi21(String ai21) { this.ai21 = ai21; }

  /**
   * 获取 AI 8004（GIAI 资产标识）值。
   *
   * @return GIAI 资产标识字符串
   */
  public String getAi8004() { return ai8004; }

  /**
   * 设置 AI 8004（GIAI 资产标识）值。
   *
   * @param ai8004 GIAI 资产标识字符串
   */
  public void setAi8004(String ai8004) { this.ai8004 = ai8004; }

  /**
   * 判断是否包含 GTIN 和序列号（AI 01 + AI 21）。
   *
   * @return 如果 AI 01 和 AI 21 均不为空则返回 true
   */
  public boolean hasGtinSerial() { return ObjectUtil.isNotEmpty(ai01) && ObjectUtil.isNotEmpty(ai21); }

  /**
   * 判断是否包含 GIAI 资产标识（AI 8004）。
   *
   * @return 如果 AI 8004 不为空则返回 true
   */
  public boolean hasGiai() { return ObjectUtil.isNotEmpty(ai8004); }
}
