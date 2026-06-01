package com.steven.solomon.service;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.crypto.digest.DigestUtil;
import com.steven.solomon.enums.StorageCapability;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.model.FileUploadRequest;
import com.steven.solomon.model.ShareFileRequest;
import com.steven.solomon.properties.FileChoiceProperties;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务统一接口。
 *
 * <p>业务侧面向该接口完成上传、下载、分享、删除、缩略图和桶管理；具体实现由配置选择。</p>
 */
public interface FileServiceInterface {

  /**
   * 使用统一请求对象上传文件，适合链式调用。
   */
  FileUpload upload(FileUploadRequest request) throws Exception;

  /**
   * 上传(支持大文件上传)
   * @param file 文件
   * @param bucketName 桶名
   * @param isUseOriginalName 是否使用原名,不使用原名则使用配置的文件名生成器生成
   */
  FileUpload upload(MultipartFile file,String bucketName,boolean isUseOriginalName) throws Exception;

  /**
   * 上传（默认使用文件名生成器生成,支持大文件上传）
   * @param file 文件
   * @param bucketName 桶名
   */
  FileUpload upload(MultipartFile file,String bucketName) throws Exception;

  /**
   * 上传(支持大文件上传)
   * @param is 文件输入流
   * @param bucketName 桶名
   * @param fileName   文件名
   * @param isUseOriginalName 是否使用原名,不使用原名则使用配置的文件名生成器生成
   */
  FileUpload upload(InputStream is,String bucketName,String fileName,boolean isUseOriginalName) throws Exception;

  /**
   * 上传（默认使用文件名生成器生成,支持大文件上传）
   * @param is 文件输入流
   * @param bucketName 桶名
   * @param fileName   文件名
   */
  FileUpload upload(InputStream is,String bucketName,String fileName) throws Exception;

  /**
   * 上传（默认使用文件名生成器生成,支持大文件上传）
   * @param bucketName 桶名
   * @param bi         缓冲区图像类
   * @param fileName   文件名
   */
  FileUpload upload(String bucketName, BufferedImage bi, String fileName) throws Exception;

  /**
   * 删除文件
   */
  void deleteFile(String fileName,String bucketName) throws Exception;

  /**
   * 分享
   * @param expiry 默认秒
   */
  String share(String fileName,String bucketName,long expiry) throws Exception;

  /**
   * 使用统一请求对象生成分享链接。
   */
  default String share(ShareFileRequest request) throws Exception {
    return share(request.getFileName(), request.getBucketName(), request.getExpirySeconds());
  }

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
   */
  boolean objectExist(String bucketName,String objectName) throws Exception;

  /**
   * 拷贝文件
   * @param sourceBucket 原桶名
   * @param targetBucket 目标桶名
   * @param sourceObjectName 原文件名
   * @param targetObjectName 目标文件名
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
   */
  List<String> listObjects(String bucketName,String key) throws Exception;

  /**
   * 删除桶名
   * @param bucketName 桶名
   */
  void deleteBucket(String bucketName) throws Exception;

  /**
   * 获取所有桶名
   */
  List<String> getBucketList() throws Exception;

  /**
   * 当前供应商支持的能力。
   */
  default Set<StorageCapability> capabilities() {
    return EnumSet.of(
        StorageCapability.UPLOAD,
        StorageCapability.DOWNLOAD,
        StorageCapability.DELETE,
        StorageCapability.SHARE_URL,
        StorageCapability.CREATE_BUCKET,
        StorageCapability.DELETE_BUCKET,
        StorageCapability.COPY_OBJECT,
        StorageCapability.LIST_OBJECTS,
        StorageCapability.THUMBNAIL
    );
  }

  /**
   * 获取文件MD5
   */
  default String getMd5(MultipartFile file) throws IOException {
    return DigestUtil.md5Hex(file.getInputStream());
  }

  default String getFilePath(String fileName, FileChoiceProperties properties) {
    return ObjectUtil.isEmpty(properties.getRootDirectory()) ? fileName : properties.getRootDirectory() + fileName;
  }
}
