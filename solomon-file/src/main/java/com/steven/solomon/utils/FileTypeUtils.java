package com.steven.solomon.utils;

import cn.hutool.core.io.FileTypeUtil;
import com.steven.solomon.verification.ValidateUtils;
import java.io.File;
import java.io.InputStream;

public class FileTypeUtils {

  public static String getFileType(InputStream inputStream) {
    return ValidateUtils.getOrDefault(FileTypeUtil.getType(inputStream), "application/octet-stream");
  }

  public static String getFileType(InputStream inputStream, String fileName) {
    return ValidateUtils.getOrDefault(FileTypeUtil.getType(inputStream, fileName), "application/octet-stream");
  }

  public static String getFileType(File file) {
    return ValidateUtils.getOrDefault(FileTypeUtil.getType(file), "application/octet-stream");
  }


}
