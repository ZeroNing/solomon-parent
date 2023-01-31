package com.steven.solomon.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.PutObjectRequest;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties.OSSProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.web.multipart.MultipartFile;

public class OSSService implements FileServiceInterface{

  private final OSSProperties properties;

  @Resource
  private FileNamingRulesGenerationService fileNamingRulesGenerationService;

  public OSSService(OSSProperties properties) {this.properties = properties;}

  public OSS client(){
    return new OSSClientBuilder().build(properties.getEndpoint(), properties.getAccessKey(), properties.getSecretKey());
  }

  @Override
  public FileUpload upload(MultipartFile file, String bucketName) throws Exception {
    OSS oss = client();
    try {
      //创建桶
      makeBucket(bucketName);

      String name = fileNamingRulesGenerationService.getFileName(file);

      String       filePath = getFilePath(name,properties);
      oss.putObject(new PutObjectRequest(bucketName,filePath,file.getInputStream()));
      oss.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
      return new FileUpload(bucketName,filePath,file.getInputStream());
    } finally {
      if(ValidateUtils.isNotEmpty(oss)){
        oss.shutdown();
      }
    }
  }

  @Override
  public FileUpload upload(String bucketName, BufferedImage bi, String fileName) throws Exception {
    OSS oss = client();
    try {
      //创建桶
      makeBucket(bucketName);

      String       filePath = getFilePath(fileName,properties);

      ByteArrayOutputStream bs    = new ByteArrayOutputStream();
      ImageOutputStream     imOut = ImageIO.createImageOutputStream(bs);
      ImageIO.write(bi, "jpg", imOut);
      oss.putObject(new PutObjectRequest(bucketName,filePath,new ByteArrayInputStream(bs.toByteArray())));
      oss.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
      return new FileUpload(bucketName,filePath,new ByteArrayInputStream(bs.toByteArray()));
    } finally {
      if(ValidateUtils.isNotEmpty(oss)){
        oss.shutdown();
      }
    }
  }

  @Override
  public void deleteFile(String fileName, String bucketName) throws Exception {
    boolean flag = bucketExists(bucketName);
    if (!flag) {
      return;
    }
    OSS oss = client();
    try {
      String filePath = getFilePath(fileName,properties);
      oss.deleteObject(bucketName,filePath);
    } finally {
      if(ValidateUtils.isNotEmpty(oss)){
        oss.shutdown();
      }
    }
  }

  @Override
  public String share(String fileName, String bucketName, long expiry, TimeUnit unit) throws Exception {
    OSS oss = client();
    try {
      String filePath = getFilePath(fileName,properties);

      return oss.generatePresignedUrl(bucketName,filePath,new Date(System.currentTimeMillis()+unit.toMillis(expiry))).toString();
    } finally {
      if(ValidateUtils.isNotEmpty(oss)){
        oss.shutdown();
      }
    }
  }

  @Override
  public InputStream download(String fileName, String bucketName) throws Exception {
    OSS oss = client();
    try {
      String filePath = getFilePath(fileName,properties);
      return oss.getObject(bucketName,filePath).getObjectContent();
    } finally {
      if(ValidateUtils.isNotEmpty(oss)){
        oss.shutdown();
      }
    }
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    OSS oss = client();
    try {
      return oss.doesBucketExist(bucketName);
    } finally {
      if(ValidateUtils.isNotEmpty(oss)){
        oss.shutdown();
      }
    }
  }

  @Override
  public void makeBucket(String bucketName) throws Exception {
    OSS oss = client();
    try {
      boolean flag = bucketExists(bucketName);
      if(flag){
        return;
      }
      oss.createBucket(bucketName);
    } finally {
      if(ValidateUtils.isNotEmpty(oss)){
        oss.shutdown();
      }
    }
  }


}
