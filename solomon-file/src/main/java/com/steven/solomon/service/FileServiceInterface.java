package com.steven.solomon.service;

import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.rsa.Md5Utils;
import com.steven.solomon.verification.ValidateUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.web.multipart.MultipartFile;

public interface FileServiceInterface {

  /**
   * 上传
   * @param file 文件
   * @param bucketName 桶名
   * @param isUseOriginalName 是否使用原名,不使用原名则使用配置的文件名生成器生成
   * @return
   * @throws Exception
   */
  FileUpload upload(MultipartFile file,String bucketName,boolean isUseOriginalName) throws Exception;

  /**
   * 上传（默认使用文件名生成器生成）
   * @param file 文件
   * @param bucketName 桶名
   * @return
   * @throws Exception
   */
  FileUpload upload(MultipartFile file,String bucketName) throws Exception;

  /**
   * 上传
   * @param is 文件输入流
   * @param bucketName 桶名
   * @param fileName   文件名
   * @param isUseOriginalName 是否使用原名,不使用原名则使用配置的文件名生成器生成
   * @return
   * @throws Exception
   */
  FileUpload upload(InputStream is,String bucketName,String fileName,boolean isUseOriginalName) throws Exception;

  /**
   * 上传（默认使用文件名生成器生成）
   * @param is 文件输入流
   * @param bucketName 桶名
   * @param fileName   文件名
   * @return
   * @throws Exception
   */
  FileUpload upload(InputStream is,String bucketName,String fileName) throws Exception;

  /**
   * 上传（默认使用文件名生成器生成）
   * @param bucketName 桶名
   * @param bi         缓冲区图像类
   * @param fileName   文件名
   * @return
   * @throws Exception
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
   * 检查桶内文件是否存在
   * @param bucketName 桶名
   * @param objectName 文件名
   * @return
   * @throws Exception
   */
  boolean objectExist(String bucketName,String objectName) throws Exception;

  /**
   * 拷贝文件
   * @param sourceBucket 原桶名
   * @param targetBucket 目标桶名
   * @param sourceObjectName 原文件名
   * @param targetObjectName 目标文件名
   * @return
   * @throws Exception
   */
  boolean copyObject(String sourceBucket,String targetBucket,String sourceObjectName,String targetObjectName) throws Exception;

  /**
   * 生成缩略图
   * @param bucketName 桶名
   * @param objectName 文件名
   * @param filePath   存放路径（允许为空，为空则直接放入桶名下的目录，如果配置有根目录，则存放根目录）
   * @param isUpload   是否上传
   * @param width      宽
   * @param height     高
   */
  InputStream generateThumbnail(String bucketName,String objectName,String filePath,boolean isUpload,int width,int height) throws Exception;

  /**
   * 获取桶内文件名（文件名为空则返回所有文件名）
   * @param bucketName 桶名
   * @param key 文件名
   * @return
   */
  List<String> listObjects(String bucketName,String key) throws Exception;
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
