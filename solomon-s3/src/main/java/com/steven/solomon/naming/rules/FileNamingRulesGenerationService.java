package com.steven.solomon.naming.rules;

import cn.hutool.core.io.file.FileNameUtil;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件命名规则扩展点。
 *
 * <p>上传文件在进入具体存储服务前，会通过该接口生成最终对象名。业务方可注入自定义实现，
 * 覆盖默认的原文件名、日期、UUID、雪花 ID 等命名策略。</p>
 */
public interface FileNamingRulesGenerationService {

  /**
   * 根据上传文件生成存储对象名。
   *
   * @param file 上传文件
   * @return 存储对象名，通常包含文件扩展名
   */
  String getFileName(MultipartFile file);

  /**
   * 获取文件扩展名，不带 .
   */
  default String getExtensionName(String filename) {
    return FileNameUtil.extName(filename);
  }
}
