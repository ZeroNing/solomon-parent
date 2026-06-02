package com.steven.solomon.naming.rules;

import java.util.UUID;

import cn.hutool.core.util.StrUtil;
import org.springframework.web.multipart.MultipartFile;

/**
 * 使用 UUID 生成文件名。
 *
 * <p>生成 32 位无连字符的 UUID 作为文件名，
 * 保证全局唯一性。文件名格式为 {@code UUID + . + 扩展名}。</p>
 */
public class UUIDNamingRulesGenerationService implements FileNamingRulesGenerationService {

  @Override
  public String getFileName(MultipartFile file) {
    String contentType = getExtensionName(file.getOriginalFilename());
    return UUID.randomUUID().toString().replace("-", StrUtil.EMPTY) + "."+contentType;
  }
}
