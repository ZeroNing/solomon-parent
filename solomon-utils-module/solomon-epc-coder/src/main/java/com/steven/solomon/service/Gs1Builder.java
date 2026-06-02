package com.steven.solomon.service;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;

/**
 * GS1 AI（应用标识符）条码 EPC 链式构建器。
 * <p>
 * 提供链式调用方式将 GS1 AI 条码（如 (01)GTIN(21)Serial 或 (8004)GIAI）
 * 编码为 EPC（电子产品编码）。
 * 使用示例：
 * <pre>{@code
 *   EpcResult result = epcService.gs1()
 *       .ai01("06901234567892")
 *       .ai21("ABC123")
 *       .companyPrefixLength(6)
 *       .tagSize(96)
 *       .encode();
 * }</pre>
 * </p>
 *
 * @author 创建者
 */
public class Gs1Builder {

  /** EPC 服务实例。 */
  private final EpcService service;

  /** 原始 GS1 条码字符串。 */
  private String barcode;

  /** GS1 AI 01（GTIN）值。 */
  private String ai01;

  /** GS1 AI 21（序列号）值。 */
  private String ai21;

  /** GS1 AI 8004（GIAI 资产标识）值。 */
  private String ai8004;

  /** 公司前缀长度，默认 6 位。 */
  private int companyPrefixLength = 6;

  /** EPC 标签尺寸，默认 96 位。 */
  private int tagSize = 96;

  /** EPC filter 值，默认 0。 */
  private int filter = 0;

  /**
   * 创建一个 GS1 构建器。
   *
   * @param service EPC 服务实例
   */
  Gs1Builder(EpcService service) { this.service = service; }

  /**
   * 设置原始 GS1 条码。
   *
   * @param barcode GS1 条码字符串
   * @return 当前构建器
   */
  public Gs1Builder barcode(String barcode) { this.barcode = barcode; return this; }

  /**
   * 设置 AI 01（GTIN）值。
   *
   * @param ai01 GTIN 字符串
   * @return 当前构建器
   */
  public Gs1Builder ai01(String ai01) { this.ai01 = ai01; return this; }

  /**
   * 设置 AI 21（序列号）值。
   *
   * @param ai21 序列号字符串
   * @return 当前构建器
   */
  public Gs1Builder ai21(String ai21) { this.ai21 = ai21; return this; }

  /**
   * 设置 AI 8004（GIAI 资产标识）值。
   *
   * @param ai8004 GIAI 资产标识字符串
   * @return 当前构建器
   */
  public Gs1Builder ai8004(String ai8004) { this.ai8004 = ai8004; return this; }

  /**
   * 设置公司前缀长度。
   *
   * @param companyPrefixLength 公司前缀长度
   * @return 当前构建器
   */
  public Gs1Builder companyPrefixLength(int companyPrefixLength) { this.companyPrefixLength = companyPrefixLength; return this; }

  /**
   * 设置 EPC 标签尺寸。
   *
   * @param tagSize 标签尺寸（96 或 198）
   * @return 当前构建器
   */
  public Gs1Builder tagSize(int tagSize) { this.tagSize = tagSize; return this; }

  /**
   * 设置 filter 值。
   *
   * @param filter filter 值（0-7）
   * @return 当前构建器
   */
  public Gs1Builder filter(int filter) { this.filter = filter; return this; }

  /**
   * 执行 EPC 编码。
   * <p>
   * 优先级规则：
   * <ol>
   *   <li>如果设置了 AI 01 或 AI 21，优先使用 GTIN+序列号编码</li>
   *   <li>如果设置了 AI 8004，使用 GIAI 编码</li>
   *   <li>否则使用原始 GS1 条码进行解析后编码</li>
   * </ol>
   * </p>
   *
   * @return EPC 编码结果
   * @throws BaseException 如果编码参数无效则抛出异常
   */
  public EpcResult encode() throws BaseException {
    if (ObjectUtil.isNotEmpty(ai01) || ObjectUtil.isNotEmpty(ai21)) {
      return service.gtinSerialToEpc(ai01, ai21, companyPrefixLength, tagSize, filter).setBarcode("(01)" + ai01 + "(21)" + ai21);
    }
    if (ObjectUtil.isNotEmpty(ai8004)) {
      return service.gs1ToEpc("(8004)" + ai8004, companyPrefixLength, tagSize, filter);
    }
    return service.gs1ToEpc(barcode, companyPrefixLength, tagSize, filter);
  }
}
