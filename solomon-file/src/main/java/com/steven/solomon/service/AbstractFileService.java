package com.steven.solomon.service;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.file.MockMultipartFile;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public abstract class AbstractFileService implements FileServiceInterface{

  @Autowired
  protected FileNamingRulesGenerationService fileNamingRulesGenerationService;

  protected FileChoiceProperties properties;

  public AbstractFileService(FileChoiceProperties properties){
    this.properties = properties;
  }

  @Override
  public FileUpload upload(MultipartFile file, String bucketName) throws Exception {
    //创建桶
    makeBucket(bucketName);

    String name = fileNamingRulesGenerationService.getFileName(file);

    String       filePath = getFilePath(name,properties);
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
    String filePath = getFilePath(fileName,properties);
    delete(bucketName,filePath);
  }

  @Override
  public String share(String fileName, String bucketName, long expiry, TimeUnit unit) throws Exception {
    String filePath = getFilePath(fileName,properties);
    return shareUrl(bucketName,filePath,expiry,unit);
  }

  @Override
  public InputStream download(String fileName, String bucketName) throws Exception {
    String filePath = getFilePath(fileName,properties);
    return getObject(bucketName,filePath);
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
    if(!checkObject(sourceBucket,sourceObjectName)){
      throw new BaseException(BaseExceptionCode.FILE_IS_NOT_EXIST_EXCEPTION_CODE);
    }
    copyFile(sourceBucket,getFilePath(sourceObjectName,properties),targetBucket,getFilePath(targetObjectName,properties));
    return true;
  }

  @Override
  public boolean checkObjectExist(String bucketName,String objectName) throws Exception{
    return checkObject(bucketName,getFilePath(objectName,properties));
  }

  protected abstract void upload(MultipartFile file, String bucketName,String filePath) throws Exception;

  protected abstract void delete(String bucketName,String filePath) throws Exception;

  protected abstract String shareUrl(String bucketName,String filePath,long expiry, TimeUnit unit) throws Exception;

  protected abstract InputStream getObject(String bucketName,String filePath) throws Exception;

  protected abstract void createBucket(String bucketName) throws Exception;

  protected abstract boolean checkObject(String bucketName,String objectName) throws Exception;

  protected abstract boolean copyFile(String sourceBucket,String targetBucket,String sourceObjectName,String targetObjectName) throws Exception;
}
