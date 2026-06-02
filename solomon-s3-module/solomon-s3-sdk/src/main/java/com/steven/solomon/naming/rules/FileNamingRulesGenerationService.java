package com.steven.solomon.naming.rules;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件命名规则生成器接口。
 *
 * <p>定义文件上传时文件名生成的策略接口，
 * 不同的实现类对应不同的命名规则（如按日期、UUID、雪花 ID 等）。
 * 提供默认方法用于获取文件扩展名。</p>
 */
public interface FileNamingRulesGenerationService {

  /**
   * 根据上传文件生成文件名。
   *
   * @param file 上传的文件
   * @return 生成的文件名（不含路径，可能包含扩展名）
   */
  String getFileName(MultipartFile file);

  /**
   * 获取文件扩展名，不包含点号（.）。
   *
   * <p>例如：传入 {@code "example.txt"} 返回 {@code "txt"}。</p>
   *
   * @param filename 文件名
   * @return 扩展名字符串，无扩展名时返回空字符串
   */
  default String getExtensionName(String filename) {
    return FileNameUtil.extName(filename);
  }
}
