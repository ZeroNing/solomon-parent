package com.steven.solomon.service;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;

/**
 * GIAI（全球单个资产标识）EPC 链式构建器。
 * <p>
 * 提供链式调用方式构建 GIAI-96 或 GIAI-202 类型的 EPC 编码。
 * 使用示例：
 * <pre>{@code
 *   EpcResult result = epcService.giai()
 *       .companyPrefix("690123")
 *       .assetReference("ASSET001")
 *       .tagSize(96)
 *       .encode();
 * }</pre>
 * </p>
 *
 * @author 创建者
 */
public class GiaiBuilder {

  /** EPC 服务实例。 */
  private final EpcService service;

  /** 公司前缀。 */
  private String companyPrefix;

  /** 资产参考。 */
  private String assetReference;

  /** EPC 标签尺寸（96 或 202）。 */
  private int tagSize = 96;

  /** EPC filter 值（0-7）。 */
  private int filter = 0;

  /**
   * 创建一个 GIAI 构建器。
   *
   * @param service EPC 服务实例
   */
  GiaiBuilder(EpcService service) { this.service = service; }

  /**
   * 设置公司前缀。
   *
   * @param companyPrefix 公司前缀字符串
   * @return 当前构建器
   */
  public GiaiBuilder companyPrefix(String companyPrefix) { this.companyPrefix = companyPrefix; return this; }

  /**
   * 设置资产参考。
   *
   * @param assetReference 资产参考字符串
   * @return 当前构建器
   */
  public GiaiBuilder assetReference(String assetReference) { this.assetReference = assetReference; return this; }

  /**
   * 设置 EPC 标签尺寸。
   *
   * @param tagSize 标签尺寸（96 或 202）
   * @return 当前构建器
   */
  public GiaiBuilder tagSize(int tagSize) { this.tagSize = tagSize; return this; }

  /**
   * 设置 filter 值。
   *
   * @param filter filter 值（0-7）
   * @return 当前构建器
   */
  public GiaiBuilder filter(int filter) { this.filter = filter; return this; }

  /**
   * 执行 EPC 编码。
   *
   * @return EPC 编码结果
   * @throws BaseException 如果编码参数无效则抛出异常
   */
  public EpcResult encode() throws BaseException { return service.giaiToEpc(companyPrefix, assetReference, tagSize, filter); }

  /**
   * 执行 EPC 编码（{@link #encode()} 的别名方法）。
   *
   * @return EPC 编码结果
   * @throws BaseException 如果编码参数无效则抛出异常
   */
  public EpcResult generate() throws BaseException { return encode(); }
}
