package com.steven.solomon.utils;

import cn.hutool.core.io.FileTypeUtil;
import com.steven.solomon.verification.ValidateUtils;
import java.io.File;
import java.io.InputStream;

public class FileTypeUtils {

  public static String getFileType(InputStream inputStream) {
    String fileType = ValidateUtils.getOrDefault(FileTypeUtil.getType(inputStream), "application/octet-stream");
    if(!fileType.contains("application")){
      return "application/octet-stream";
    } else {
      return fileType;
    }
  }

  public static String getFileType(InputStream inputStream, String fileName) {
    String fileType = ValidateUtils.getOrDefault(FileTypeUtil.getType(inputStream, fileName), "application/octet-stream");
    if(!fileType.contains("application")){
      return "application/octet-stream";
    } else {
      return fileType;
    }
  }

  public static String getFileType(File file) {
    String fileType = ValidateUtils.getOrDefault(FileTypeUtil.getType(file), "application/octet-stream");
    if(!fileType.contains("application")){
      return "application/octet-stream";
    } else {
      return fileType;
    }
  }


}
