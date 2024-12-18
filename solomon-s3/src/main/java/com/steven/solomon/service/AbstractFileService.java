package com.steven.solomon.service;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.file.MockMultipartFile;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public abstract class AbstractFileService implements FileServiceInterface{

  protected static Logger logger = LoggerUtils.logger(AbstractFileService.class);

  protected FileNamingRulesGenerationService fileNamingRulesGenerationService;

  protected FileChoiceProperties properties;

  protected Long partSize;

  public AbstractFileService(FileChoiceProperties properties,FileNamingRulesGenerationService fileNamingRulesGenerationService){
    this.fileNamingRulesGenerationService = fileNamingRulesGenerationService;
    this.properties = properties;
    this.partSize = (long) (this.properties.getPartSize() * 1024 * 1024);
  }

  public AbstractFileService(FileNamingRulesGenerationService fileNamingRulesGenerationService){
      this.fileNamingRulesGenerationService = fileNamingRulesGenerationService;
      this.partSize = (long) (5 * 1024 * 1024);
  }

  public AbstractFileService(){
    super();
  }

  @Override
  public FileUpload upload(InputStream is, String bucketName,String fileName) throws Exception {
    return upload(is,bucketName,fileName,false);
  }

  @Override
  public FileUpload upload(InputStream is, String bucketName,String fileName,boolean isUseOriginalName) throws Exception {
    return upload(new MockMultipartFile(fileName,fileName, MediaType.MULTIPART_FORM_DATA_VALUE, is),bucketName,isUseOriginalName);
  }

  @Override
  public FileUpload upload(MultipartFile file, String bucketName) throws Exception {
    return upload(file,bucketName,false);
  }

  @Override
  public FileUpload upload(MultipartFile file,String bucketName,boolean isUseOriginalName) throws Exception{
    //创建桶
    makeBucket(bucketName);
    String       filePath = getFilePath(!isUseOriginalName ? fileNamingRulesGenerationService.getFileName(file): file.getOriginalFilename(),properties);
    long fileSize = file.getSize();
    if(fileSize >= partSize){
      return multipartUpload(file,bucketName,isUseOriginalName);
    } else {
      this.upload(file,bucketName,filePath);
      return new FileUpload(bucketName,filePath,file.getInputStream());
    }
  }

  @Override
  public FileUpload upload(String bucketName, BufferedImage bi, String fileName) throws Exception {
    //创建桶
    makeBucket(bucketName);

    String       filePath = getFilePath(fileName,properties);

    ByteArrayOutputStream bs    = new ByteArrayOutputStream();
    ImageOutputStream     imOut = ImageIO.createImageOutputStream(bs);
    ImageIO.write(bi, "jpg", imOut);

    InputStream inputStream = new ByteArrayInputStream(bs.toByteArray());
    MockMultipartFile file =  new MockMultipartFile(fileName,inputStream);

    this.upload(file,bucketName,filePath);
    return new FileUpload(bucketName,filePath,new ByteArrayInputStream(bs.toByteArray()));
  }

  @Override
  public void deleteFile(String fileName, String bucketName) throws Exception {
    if (!bucketExists(bucketName) || ValidateUtils.isEmpty(fileName)) {
      return;
    }
    delete(bucketName,getFilePath(fileName,properties));
  }

  @Override
  public String share(String fileName, String bucketName, long expiry) throws Exception {
    return shareUrl(bucketName,getFilePath(fileName,properties),expiry);
  }

  @Override
  public InputStream download(String fileName, String bucketName) throws Exception {
    String filePath = getFilePath(fileName,properties);
    if(!objectExist(bucketName,filePath)){
      throw new BaseException(BaseExceptionCode.FILE_IS_NOT_EXIST_EXCEPTION_CODE);
    }
    return getObject(bucketName,filePath);
  }

  @Override
  public void makeBucket(String bucketName) throws Exception {
    if(bucketExists(bucketName)){
      return;
    }
    this.createBucket(bucketName);
  }

  @Override
  public boolean copyObject(String sourceBucket,String targetBucket,String sourceObjectName,String targetObjectName) throws Exception{
    if(!objectExist(sourceBucket,sourceObjectName)){
      throw new BaseException(BaseExceptionCode.FILE_IS_NOT_EXIST_EXCEPTION_CODE);
    }
    copyFile(sourceBucket,getFilePath(sourceObjectName,properties),targetBucket,getFilePath(targetObjectName,properties));
    return true;
  }

  @Override
  public boolean objectExist(String bucketName,String objectName) throws Exception{
    try {
      return checkObjectExist(bucketName,getFilePath(objectName,properties));
    } catch (Throwable e){
      logger.error("检查文件出现异常",e);
      return false;
    }
  }

  @Override
  public InputStream generateThumbnail(String bucketName,String objectName,String filePath,boolean isUpload,int width,int height)throws Exception{
    makeBucket(bucketName);
    String extensionName = fileNamingRulesGenerationService.getExtensionName(objectName);
    objectName = objectName.substring(0,objectName.indexOf("."+extensionName));
    String thumbnailName = new StringBuilder(ValidateUtils.getOrDefault(filePath,ValidateUtils.getOrDefault(properties.getRootDirectory(), StrUtil.EMPTY))).append(objectName).append("_").append(width).append("_").append(height).append(".").append(extensionName).toString();
    if(!objectExist(bucketName,thumbnailName)){
      MockMultipartFile file = null;
      try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
        ImgUtil.scale(getObject(bucketName,objectName+"."+extensionName), baos, width, height, Color.decode("0xFFFFFF"));
        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
          bais.reset();
          try(InputStream is = new ByteArrayInputStream(bais.readAllBytes())){
            if(isUpload){
              file = new MockMultipartFile(thumbnailName,thumbnailName, MediaType.MULTIPART_FORM_DATA_VALUE, is);
              upload(file,bucketName,true);
              return file.getInputStream();
            } else {
              return is;
            }
          }
        }
      }
    } else {
      return getObject(bucketName,thumbnailName);
    }
  }

  public FileUpload multipartUpload(MultipartFile file, String bucketName, boolean isUseOriginalName) throws Exception {
    String filePath = getFilePath(!isUseOriginalName? fileNamingRulesGenerationService.getFileName(file): file.getName(),properties);
    String uploadId = initiateMultipartUploadTask(bucketName, filePath);

    long contentLength = file.getSize();
    int partCount = (int) (contentLength / partSize);
    if (contentLength % partSize != 0){
      partCount++;
    }
    try{
      multipartUpload(file,bucketName,contentLength,uploadId,filePath,partCount);
    }catch (Exception e){
      abortMultipartUpload(uploadId,bucketName,filePath);
      throw e;
    }
    return new FileUpload(bucketName,filePath,file.getInputStream());
  }

  /**
   * 分片上传
   * @param file 文件
   * @param bucketName 桶名
   * @param fileSize 文件大小
   * @param uploadId 上传id
   * @param filePath 文件名
   */
  protected abstract void multipartUpload(MultipartFile file, String bucketName,long fileSize,String uploadId,String filePath,int partCount) throws Exception;

  /**
   * 上传
   * @param file 文件
   * @param bucketName 桶名
   * @param filePath 文件名
   */
  protected abstract void upload(MultipartFile file, String bucketName,String filePath) throws Exception;

  /**
   * 删除
   * @param bucketName 桶名
   * @param filePath 文件名
   */
  protected abstract void delete(String bucketName,String filePath) throws Exception;

  /**
   * 分享url
   * @param bucketName 桶名
   * @param filePath 文件名
   * @param expiry 时间
   */
  protected abstract String shareUrl(String bucketName,String filePath,long expiry) throws Exception;

  /**
   * 获取文件流
   * @param bucketName 桶名
   * @param filePath 文件名
   */
  protected abstract InputStream getObject(String bucketName,String filePath) throws Exception;

  /**
   * 创建桶
   * @param bucketName 桶名
   */
  protected abstract void createBucket(String bucketName) throws Exception;

  /**
   * 判断文件是否存在
   * @param bucketName 桶名
   * @param objectName 文件名
   */
  protected abstract boolean checkObjectExist(String bucketName,String objectName) throws Exception;

  /**
   * 复制文件
   *
   * @param sourceBucket     来源桶
   * @param targetBucket     目标桶
   * @param sourceObjectName 来源文件名
   * @param targetObjectName 目标文件名
   */
  protected abstract void copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName) throws Exception;

  /**
   * 取消分片上传
   * @param uploadId 上传id
   * @param bucketName 桶名
   * @param filePath 文件名
   */
  protected abstract void abortMultipartUpload(String uploadId,String bucketName,String filePath) throws Exception;

  /**
   * 初始化分片任务
   * @param bucketName 桶名
   * @param objectName 文件名
   */
  protected abstract String initiateMultipartUploadTask(String bucketName,String objectName) throws Exception;
}
