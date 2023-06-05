package com.steven.solomon.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.CannedAccessControlList;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public class COSService implements FileService {

  private FileChoiceProperties properties;

  private COSClient client;

  @Autowired
  private FileNamingRulesGenerationService fileNamingRulesGenerationService;

  public COSService() {

  }

  public COSService(FileChoiceProperties properties) {
    this.properties = properties;
    this.client = initClient(properties);
  }

  private static COSClient initClient(FileChoiceProperties properties){
    COSCredentials credentials  = new BasicCOSCredentials(properties.getAccessKey(), properties.getSecretKey());
    Region         region       = new Region(properties.getRegionName());
    ClientConfig   clientConfig = new ClientConfig(region);
    return new COSClient(credentials, clientConfig);
  }

  @Override
  public FileUpload upload(MultipartFile file, String bucketName) throws Exception {
    //创建桶
    makeBucket(bucketName);

    String name = fileNamingRulesGenerationService.getFileName(file);

    String       filePath = getFilePath(name,properties);
    client.putObject(new PutObjectRequest(bucketName,filePath,file.getInputStream(),null));
    client.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
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
    client.putObject(new PutObjectRequest(bucketName,filePath,new ByteArrayInputStream(bs.toByteArray()),null));
    client.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
    return new FileUpload(bucketName,filePath,new ByteArrayInputStream(bs.toByteArray()));
  }

  @Override
  public void deleteFile(String fileName, String bucketName) throws Exception {
    boolean flag = bucketExists(bucketName);
    if (!flag) {
      return;
    }
    String filePath = getFilePath(fileName,properties);
    client.deleteObject(bucketName,filePath);
  }

  @Override
  public String share(String fileName, String bucketName, long expiry, TimeUnit unit) throws Exception {
    String filePath = getFilePath(fileName,properties);
    return client.generatePresignedUrl(bucketName,filePath,new Date(System.currentTimeMillis()+unit.toMillis(expiry))).toString();
  }

  @Override
  public InputStream download(String fileName, String bucketName) throws Exception {
    String filePath = getFilePath(fileName,properties);
    return client.getObject(bucketName,filePath).getObjectContent();
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return client.doesBucketExist(bucketName);
  }

  @Override
  public void makeBucket(String bucketName) throws Exception {
    boolean flag = bucketExists(bucketName);
    if(flag){
      return;
    }
    client.createBucket(bucketName);
  }


}
