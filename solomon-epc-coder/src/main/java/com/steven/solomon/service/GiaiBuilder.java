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

  /**
   * 创建 GIAI 构建器。
   *
   * @param service EPC 服务
   */
  GiaiBuilder(EpcService service) {
    this.service = service;
  }

  /**
   * 设置 GS1 公司前缀。
   *
   * @param companyPrefix 公司前缀
   * @return 当前构建器
   */
  public GiaiBuilder companyPrefix(String companyPrefix) {
    this.companyPrefix = companyPrefix;
    return this;
  }

  /**
   * 设置资产参考值。
   *
   * @param assetReference 资产参考值
   * @return 当前构建器
   */
  public GiaiBuilder assetReference(String assetReference) {
    this.assetReference = assetReference;
    return this;
  }

  /**
   * 设置 EPC Tag Size。
   *
   * @param tagSize GIAI 支持 96/202
   * @return 当前构建器
   */
  public GiaiBuilder tagSize(int tagSize) {
    this.tagSize = tagSize;
    return this;
  }

  /**
   * 设置 EPC filter 值。
   *
   * @param filter filter 值，范围 0~7
   * @return 当前构建器
   */
  public GiaiBuilder filter(int filter) {
    this.filter = filter;
    return this;
  }

  /**
   * 生成 GIAI EPC。
   *
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult encode() throws BaseException {
    return service.giaiToEpc(companyPrefix, assetReference, tagSize, filter);
  }

  /**
   * 生成 GIAI EPC，兼容 generate 命名。
   *
   * @return EPC 结果
   * @throws BaseException 参数无效或编码失败
   */
  public EpcResult generate() throws BaseException {
    return encode();
  }
}
