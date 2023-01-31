package com.steven.solomon.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析图片exif属性
 * key解释参考https://blog.csdn.net/itsonglin/article/details/46405313
 */
public class ImageAnalysisUtils {

  public static Map<String,Object> analysis(String filePath) throws Exception {
    return analysis(ImageMetadataReader.readMetadata(new File(filePath)));
  }

  public static Map<String,Object> analysis(InputStream inputStream) throws Exception {
    return analysis(ImageMetadataReader.readMetadata(inputStream));
  }

  private static Map<String,Object> analysis(Metadata metadata) throws Exception {
    Iterable<Directory> directories = metadata.getDirectories();
    Map<String, Object> map = new HashMap<>();
    for (Directory directory : directories) {
      for (Tag tag : directory.getTags()) {
        map.put(tag.getTagName(),tag.getDirectoryName());
      }
    }
    return map;
  }

}
