package com.steven.solomon.naming.rules;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.lang.generator.SnowflakeGenerator;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * 使用雪花算法生成文件名。
 *
 * <p>利用 Hutool 的 {@link Snowflake} 生成全局唯一的数字 ID，
 * 适用于分布式环境下需要唯一文件名的场景。
 * 文件名格式为 {@code 雪花ID + . + 扩展名}。</p>
 */
public class SnowflakeNamingRulesGenerationService implements FileNamingRulesGenerationService {

  /** 雪花算法 ID 生成器。 */
  private final Snowflake generator;

  public SnowflakeNamingRulesGenerationService() {
    this.generator = new Snowflake();
  }

  @Override
  public String getFileName(MultipartFile file) {
    String contentType = getExtensionName(file.getOriginalFilename());
    return generator.nextIdStr() + "." + contentType;
  }
}
