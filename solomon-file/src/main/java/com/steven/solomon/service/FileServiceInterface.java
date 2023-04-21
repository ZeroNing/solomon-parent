package com.steven.solomon.service;

import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.rsa.Md5Utils;
import com.steven.solomon.verification.ValidateUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.springframework.web.multipart.MultipartFile;

public interface FileServiceInterface {
  /**
   * 上传
   * @return
   */
  FileUpload upload(MultipartFile file,String bucketName) throws Exception;

  /**
   * 上传
   * @return
   */
  FileUpload upload(String bucketName, BufferedImage bi, String fileName) throws Exception;

  /**
   * 删除文件
   */
  void deleteFile(String fileName,String bucketName) throws Exception;

  /**
   * 分享
   */
  String share(String fileName,String bucketName,long expiry, TimeUnit unit) throws Exception;

  /**
   * 下载
   */
  InputStream download(String fileName,String bucketName) throws Exception;

  /**
   * 检查桶是否存在
   */
  boolean bucketExists(String bucketName) throws Exception;

  /**
   * 创建桶
   */
  void makeBucket(String bucketName) throws Exception;

  /**
   * 获取文件MD5
   */
  default String getMd5(MultipartFile file) throws IOException {
    return Md5Utils.getFileMd5Code(file);
  }

  default String getFilePath(String fileName, FileChoiceProperties properties){
    return ValidateUtils.isEmpty(properties.getRootDirectory()) ? fileName : properties.getRootDirectory() + fileName;
  }
}
