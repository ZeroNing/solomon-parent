package com.steven.solomon.naming.rules;

import org.springframework.web.multipart.MultipartFile;

/**
 * 用上传时候文件的原名
 */
public class OriginalNamingRulesGenerationService implements FileNamingRulesGenerationService {

  @Override
  public String getFileName(MultipartFile file) {
    return file.getOriginalFilename();
  }
}
