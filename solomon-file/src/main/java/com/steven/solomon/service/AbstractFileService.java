package com.steven.solomon.service;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.IoUtil;
import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.file.MockMultipartFile;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.utils.ImageUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public abstract class AbstractFileService implements FileServiceInterface{

  private static Logger logger = LoggerUtils.logger(AbstractFileService.class);

  @Autowired
  protected FileNamingRulesGenerationService fileNamingRulesGenerationService;

  protected FileChoiceProperties properties;

  public AbstractFileService(FileChoiceProperties properties){
    this.properties = properties;
  }

  @Override
  public FileUpload upload(InputStream is, String bucketName,String fileName) throws Exception {
    return upload(is,bucketName,fileName,false);
  }

  @Override
  public FileUpload upload(InputStream is, String bucketName,String fileName,boolean isUseOriginalName) throws Exception {
    return upload(new MockMultipartFile(fileName,fileName, MediaType.MULTIPART_FORM_DATA_VALUE, is),bucketName,false);
  }

  @Override
  public FileUpload upload(MultipartFile file, String bucketName) throws Exception {
    return upload(file,bucketName,false);
  }

  @Override
  public FileUpload upload(MultipartFile file,String bucketName,boolean isUseOriginalName) throws Exception{
    //创建桶
    makeBucket(bucketName);
    String       filePath = getFilePath(!isUseOriginalName? fileNamingRulesGenerationService.getFileName(file): file.getName(),properties);
    this.upload(file,bucketName,filePath);
    return new FileUpload(bucketName,filePath,file.getInputStream());
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
    boolean flag = bucketExists(bucketName);
    if (!flag || ValidateUtils.isEmpty(fileName)) {
      return;
    }
    delete(bucketName,getFilePath(fileName,properties));
  }

  @Override
  public String share(String fileName, String bucketName, long expiry, TimeUnit unit) throws Exception {
    return shareUrl(bucketName,getFilePath(fileName,properties),expiry,unit);
  }

  @Override
  public InputStream download(String fileName, String bucketName) throws Exception {
    return getObject(bucketName,getFilePath(fileName,properties));
  }

  @Override
  public void makeBucket(String bucketName) throws Exception {
    boolean flag = bucketExists(bucketName);
    if(flag){
      return;
    }
    this.createBucket(bucketName);
  }

  @Override
  public boolean copyObject(String sourceBucket,String targetBucket,String sourceObjectName,String targetObjectName) throws Exception{
    if(!checkObjectExist(sourceBucket,sourceObjectName)){
      throw new BaseException(BaseExceptionCode.FILE_IS_NOT_EXIST_EXCEPTION_CODE);
    }
    copyFile(sourceBucket,getFilePath(sourceObjectName,properties),targetBucket,getFilePath(targetObjectName,properties));
    return true;
  }

  @Override
  public boolean checkObjectExist(String bucketName,String objectName) throws Exception{
    try {
      return checkObject(bucketName,getFilePath(objectName,properties));
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
    String thumbnailName = new StringBuilder(ValidateUtils.isEmpty(filePath) ? "":filePath).append(objectName).append("_").append(width).append("_").append(height).append(".").append(extensionName).toString();
    if(!checkObjectExist(bucketName,thumbnailName)){
      MockMultipartFile file = null;
      try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
        ImgUtil.scale(getObject(bucketName,objectName+"."+extensionName), baos, width, height, Color.decode("0xFFFFFF"));
        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
          bais.reset();
          if(isUpload){
            try(InputStream is = new ByteArrayInputStream(bais.readAllBytes());){
              file = new MockMultipartFile(thumbnailName,thumbnailName, MediaType.MULTIPART_FORM_DATA_VALUE, is);
              upload(file,bucketName,true);
            }
          }
        }
      }
      return file.getInputStream();
    } else {
      return getObject(bucketName,getFilePath(thumbnailName,properties));
    }
  }

  protected abstract void upload(MultipartFile file, String bucketName,String filePath) throws Exception;

  protected abstract void delete(String bucketName,String filePath) throws Exception;

  protected abstract String shareUrl(String bucketName,String filePath,long expiry, TimeUnit unit) throws Exception;

  protected abstract InputStream getObject(String bucketName,String filePath) throws Exception;

  protected abstract void createBucket(String bucketName) throws Exception;

  protected abstract boolean checkObject(String bucketName,String objectName) throws Exception;

  protected abstract boolean copyFile(String sourceBucket,String targetBucket,String sourceObjectName,String targetObjectName) throws Exception;
}
