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
 * 解析图片 EXIF 属性工具类。
 *
 * <p>使用 Metadata Extractor 库读取图片的 EXIF 信息，
 * 返回键值对映射，key 为标签名，value 为目录名。
 * 参考：https://blog.csdn.net/itsonglin/article/details/46405313</p>
 */
public class ImageAnalysisUtils {

  /**
   * 根据文件路径解析图片 EXIF 信息。
   *
   * @param filePath 图片文件路径
   * @return EXIF 属性键值对映射
   * @throws Exception 读取或解析失败时抛出
   */
  public static Map<String,Object> analysis(String filePath) throws Exception {
    return analysis(ImageMetadataReader.readMetadata(new File(filePath)));
  }

  /**
   * 根据输入流解析图片 EXIF 信息。
   *
   * @param inputStream 图片输入流
   * @return EXIF 属性键值对映射
   * @throws Exception 读取或解析失败时抛出
   */
  public static Map<String,Object> analysis(InputStream inputStream) throws Exception {
    return analysis(ImageMetadataReader.readMetadata(inputStream));
  }

  /**
   * 解析 Metadata 对象为键值对映射。
   *
   * @param metadata 图片元数据对象
   * @return EXIF 属性键值对映射
   * @throws Exception 解析失败时抛出
   */
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
