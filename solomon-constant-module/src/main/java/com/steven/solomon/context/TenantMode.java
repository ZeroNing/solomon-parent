package com.steven.solomon.context;

/** 租户运行模式。 */
public enum TenantMode {

  /** 单租户模式，未传租户编码时使用默认租户。 */
  SINGLE,

  /** 多租户模式，请求需要携带明确租户编码。 */
  MULTI
}
