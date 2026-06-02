package com.steven.solomon.service;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;

/**
 * SGTIN（系列化全球贸易项目代码）EPC 链式构建器。
 * <p>
 * 提供链式调用方式构建 SGTIN-96 或 SGTIN-198 类型的 EPC 编码。
 * 支持通过 GS1 条码或 GTIN+序列号两种方式输入。
 * 使用示例：
 * <pre>{@code
 *   // 方式一：通过 GS1 条码
 *   EpcResult result = epcService.builder()
 *       .barcode("(01)06901234567892(21)ABC123")
 *       .companyPrefixLength(6)
 *       .tagSize(96)
 *       .encode();
 *
 *   // 方式二：通过 GTIN + 序列号
 *   EpcResult result2 = epcService.builder()
 *       .gtin("06901234567892")
 *       .serial("ABC123")
 *       .companyPrefixLength(6)
 *       .tagSize(96)
 *       .encode();
 * }</pre>
 * </p>
 *
 * @author 创建者
 */
public class EpcBuilder {

  /** EPC 服务实例。 */
  private final EpcService service;

  /** 原始 GS1 条码或 GTIN 字符串。 */
  private String barcode;

  /** 序列号。 */
  private String serialNumber;

  /** 公司前缀长度，默认 6 位。 */
  private int companyPrefixLength = 6;

  /** EPC 标签尺寸，默认 96 位。 */
  private int tagSize = 96;

  /** EPC filter 值，默认 0。 */
  private int filter = 0;

  /**
   * 创建一个 EPC 构建器。
   *
   * @param service EPC 服务实例
   */
  EpcBuilder(EpcService service) { this.service = service; }

  /**
   * 设置原始 GS1 条码或 GTIN。
   *
   * @param barcode GS1 条码或 GTIN 字符串
   * @return 当前构建器
   */
  public EpcBuilder barcode(String barcode) { this.barcode = barcode; return this; }

  /**
   * 设置 GTIN（{@link #barcode(String)} 的别名方法）。
   *
   * @param gtin GTIN 字符串
   * @return 当前构建器
   */
  public EpcBuilder gtin(String gtin) { this.barcode = gtin; return this; }

  /**
   * 设置序列号。
   *
   * @param serialNumber 序列号字符串
   * @return 当前构建器
   */
  public EpcBuilder serial(String serialNumber) { this.serialNumber = serialNumber; return this; }

  /**
   * 设置序列号（{@link #serial(String)} 的别名方法）。
   *
   * @param serialNumber 序列号字符串
   * @return 当前构建器
   */
  public EpcBuilder serialNumber(String serialNumber) { this.serialNumber = serialNumber; return this; }

  /**
   * 设置公司前缀长度。
   *
   * @param companyPrefixLength 公司前缀长度
   * @return 当前构建器
   */
  public EpcBuilder companyPrefixLength(int companyPrefixLength) { this.companyPrefixLength = companyPrefixLength; return this; }

  /**
   * 设置 EPC 标签尺寸。
   *
   * @param tagSize 标签尺寸（96 或 198）
   * @return 当前构建器
   */
  public EpcBuilder tagSize(int tagSize) { this.tagSize = tagSize; return this; }

  /**
   * 设置 filter 值。
   *
   * @param filter filter 值（0-7）
   * @return 当前构建器
   */
  public EpcBuilder filter(int filter) { this.filter = filter; return this; }

  /**
   * 执行 EPC 编码。
   * <p>
   * 编码逻辑：
   * <ul>
   *   <li>如果未设置序列号但设置了条码且以 '(' 开头，则按 GS1 条码方式解析编码</li>
   *   <li>否则按 GTIN + 序列号方式编码</li>
   * </ul>
   * </p>
   *
   * @return EPC 编码结果
   * @throws BaseException 如果编码参数无效则抛出异常
   */
  public EpcResult encode() throws BaseException {
    if (ObjectUtil.isEmpty(serialNumber) && ObjectUtil.isNotEmpty(barcode) && barcode.startsWith("(")) {
      return service.gs1ToEpc(barcode, companyPrefixLength, tagSize, filter);
    }
    return service.gtinSerialToEpc(barcode, serialNumber, companyPrefixLength, tagSize, filter);
  }

  /**
   * 执行 EPC 编码（{@link #encode()} 的别名方法）。
   *
   * @return EPC 编码结果
   * @throws BaseException 如果编码参数无效则抛出异常
   */
  public EpcResult generate() throws BaseException { return encode(); }
}
