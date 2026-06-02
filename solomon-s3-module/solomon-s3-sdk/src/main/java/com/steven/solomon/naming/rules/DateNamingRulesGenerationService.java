package com.steven.solomon.naming.rules;

import com.steven.solomon.utils.date.DateTimeUtils;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * 按照日期时间进行文件命名。
 *
 * <p>文件名格式为 {@code yyyy/MM/dd/HHmmssSSS + 扩展名}，
 * 例如：{@code 2024/01/15/143021123.jpg}。</p>
 *
 * <p>注意：路径中包含年/月/日的层级目录结构，
 * 有利于文件在存储桶中按日期组织和管理。</p>
 */
public class DateNamingRulesGenerationService implements FileNamingRulesGenerationService {

  @Override
  public String getFileName(MultipartFile file) {
    String contentType = getExtensionName(file.getOriginalFilename());
    return DateTimeUtils.getLocalDateTimeString("yyyy/MM/dd/HHmmssSSS") + "."+contentType;
  }
}
