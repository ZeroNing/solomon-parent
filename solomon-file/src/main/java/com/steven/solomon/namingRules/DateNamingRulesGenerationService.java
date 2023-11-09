package com.steven.solomon.namingRules;

import com.steven.solomon.utils.date.DateTimeUtils;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * 按照时间进行文件命名
 */
public class DateNamingRulesGenerationService implements FileNamingRulesGenerationService {

  @Override
  public String getFileName(MultipartFile file) {
    String contentType = getExtensionName(file.getOriginalFilename());
    return new StringBuilder(DateTimeUtils.getLocalDateTimeString("yyyy/MM/dd/yyyyMMddHHmmssSSS")).append(".").append(contentType).toString();
  }
}
