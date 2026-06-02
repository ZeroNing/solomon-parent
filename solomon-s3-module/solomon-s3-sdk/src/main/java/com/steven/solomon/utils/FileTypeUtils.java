package com.steven.solomon.utils;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.io.FileTypeUtil;
import java.io.File;
import java.io.InputStream;

/**
 * 文件类型检测工具类。
 *
 * <p>基于 Hutool 的 {@link FileTypeUtil} 获取文件的 MIME 类型，
 * 当无法识别时默认返回 {@code application/octet-stream}。</p>
 */
public class FileTypeUtils {

  /**
   * 根据输入流检测文件类型。
   *
   * @param inputStream 文件输入流
   * @return MIME 类型字符串，无法识别时返回 {@code application/octet-stream}
   */
  public static String getFileType(InputStream inputStream) {
    String fileType = ObjectUtil.defaultIfNull(FileTypeUtil.getType(inputStream), "application/octet-stream");
    if (!fileType.contains("application")) {
      return "application/octet-stream";
    } else {
      return fileType;
    }
  }

  /**
   * 根据输入流和文件名检测文件类型。
   *
   * @param inputStream 文件输入流
   * @param fileName    文件名（用于辅助判断）
   * @return MIME 类型字符串，无法识别时返回 {@code application/octet-stream}
   */
  public static String getFileType(InputStream inputStream, String fileName) {
    String fileType = ObjectUtil.defaultIfNull(FileTypeUtil.getType(inputStream, fileName), "application/octet-stream");
    if (!fileType.contains("application")) {
      return "application/octet-stream";
    } else {
      return fileType;
    }
  }

  /**
   * 根据文件对象检测文件类型。
   *
   * @param file 文件对象
   * @return MIME 类型字符串，无法识别时返回 {@code application/octet-stream}
   */
  public static String getFileType(File file) {
    String fileType = ObjectUtil.defaultIfNull(FileTypeUtil.getType(file), "application/octet-stream");
    if (!fileType.contains("application")) {
      return "application/octet-stream";
    } else {
      return fileType;
    }
  }


}
