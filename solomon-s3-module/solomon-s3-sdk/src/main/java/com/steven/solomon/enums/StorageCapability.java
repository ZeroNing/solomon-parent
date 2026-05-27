package com.steven.solomon.enums;

/**
 * 对象存储能力点。
 */
public enum StorageCapability {

  /**
   * 普通上传。
   */
  UPLOAD,

  /**
   * 文件下载。
   */
  DOWNLOAD,

  /**
   * 删除对象。
   */
  DELETE,

  /**
   * 预签名分享链接。
   */
  SHARE_URL,

  /**
   * 分片上传。
   */
  MULTIPART_UPLOAD,

  /**
   * 桶创建。
   */
  CREATE_BUCKET,

  /**
   * 桶删除。
   */
  DELETE_BUCKET,

  /**
   * 对象复制。
   */
  COPY_OBJECT,

  /**
   * 对象列表。
   */
  LIST_OBJECTS,

  /**
   * 缩略图生成。
   */
  THUMBNAIL
}
