package com.steven.solomon.namingRules;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用uuid用作文件名
 */
public class UUIDNamingRulesGenerationService implements FileNamingRulesGenerationService {

  @Override
  public String getFileName(MultipartFile file) {
    String contentType = getExtensionName(file.getOriginalFilename());
    return UUID.randomUUID().toString().replace("-","") + "."+contentType;
  }
}
