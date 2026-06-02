package com.steven.solomon.naming.rules;

import org.springframework.web.multipart.MultipartFile;

/**
 * 使用上传时的原始文件名。
 *
 * <p>直接返回 {@link MultipartFile#getOriginalFilename()} 的值，
 * 不做任何修改或重命名。适用于需要保留用户原始文件名的场景。</p>
 */
public class OriginalNamingRulesGenerationService implements FileNamingRulesGenerationService {

  @Override
  public String getFileName(MultipartFile file) {
    return file.getOriginalFilename();
  }
}
