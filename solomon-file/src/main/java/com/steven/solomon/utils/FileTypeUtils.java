package com.steven.solomon.utils;

import cn.hutool.core.io.FileTypeUtil;
import com.steven.solomon.verification.ValidateUtils;
import java.io.File;
import java.io.InputStream;

public class FileTypeUtils {

  private final static String IMAGE_TYPE = "image/";
  private final static String AUDIO_TYPE = "audio/";
  private final static String VIDEO_TYPE = "video/";
  private final static String APPLICATION_TYPE = "application/";
  private final static String TXT_TYPE = "text/";

  public static String getFileType(InputStream inputStream) {
    return getFileType(FileTypeUtil.getType(inputStream));
  }

  public static String getFileType(File file){
    return getFileType(FileTypeUtil.getType(file));
  }


  public static String getFileType(String type){
    if(ValidateUtils.isEmpty(type)){
      return null;
    }
    if (type.equalsIgnoreCase("JPG") || type.equalsIgnoreCase("JPEG")
        || type.equalsIgnoreCase("GIF") || type.equalsIgnoreCase("PNG")
        || type.equalsIgnoreCase("BMP") || type.equalsIgnoreCase("PCX")
        || type.equalsIgnoreCase("TGA") || type.equalsIgnoreCase("PSD")
        || type.equalsIgnoreCase("TIFF")) {
      return IMAGE_TYPE+type;
    }
    if (type.equalsIgnoreCase("mp3") || type.equalsIgnoreCase("OGG")
        || type.equalsIgnoreCase("WAV") || type.equalsIgnoreCase("REAL")
        || type.equalsIgnoreCase("APE") || type.equalsIgnoreCase("MODULE")
        || type.equalsIgnoreCase("MIDI") || type.equalsIgnoreCase("VQF")
        || type.equalsIgnoreCase("CD")) {
      return AUDIO_TYPE+type;
    }
    if (type.equalsIgnoreCase("mp4") || type.equalsIgnoreCase("avi")
        || type.equalsIgnoreCase("MPEG-1") || type.equalsIgnoreCase("RM")
        || type.equalsIgnoreCase("ASF") || type.equalsIgnoreCase("WMV")
        || type.equalsIgnoreCase("qlv") || type.equalsIgnoreCase("MPEG-2")
        || type.equalsIgnoreCase("MPEG4") || type.equalsIgnoreCase("mov")
        || type.equalsIgnoreCase("3gp")) {
      return VIDEO_TYPE+type;
    }
    if (type.equalsIgnoreCase("doc") || type.equalsIgnoreCase("docx")
        || type.equalsIgnoreCase("ppt") || type.equalsIgnoreCase("pptx")
        || type.equalsIgnoreCase("xls") || type.equalsIgnoreCase("xlsx")
        || type.equalsIgnoreCase("zip")||type.equalsIgnoreCase("jar")) {
      return APPLICATION_TYPE+type;
    }
    if (type.equalsIgnoreCase("txt")) {
      return TXT_TYPE+type;
    }
    if(ValidateUtils.isEmpty(type)){
      return APPLICATION_TYPE+"file";
    }
    return null;
  }
}
