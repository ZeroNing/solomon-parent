package com.steven.solomon.service;

import com.baidubce.services.bos.model.BosObject;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.CannedAccessControlList;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
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

public class COSService extends AbstractFileService {

  private COSClient client;

  public COSService(FileChoiceProperties properties) {
    super(properties);
    this.client = initClient(properties);
  }

  private static COSClient initClient(FileChoiceProperties properties){
    COSCredentials credentials  = new BasicCOSCredentials(properties.getAccessKey(), properties.getSecretKey());
    Region         region       = new Region(properties.getRegionName());
    ClientConfig   clientConfig = new ClientConfig(region);
    return new COSClient(credentials, clientConfig);
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return client.doesBucketExist(bucketName);
  }

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception  {
    client.putObject(new PutObjectRequest(bucketName,filePath,file.getInputStream(),null));
    client.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
  }

  @Override
  protected void delete(String bucketName, String filePath) throws Exception  {
    client.deleteObject(bucketName, filePath);
  }

  @Override
  protected String shareUrl(String bucketName, String filePath, long expiry, TimeUnit unit) throws Exception  {
    return client.generatePresignedUrl(bucketName,filePath,new Date(System.currentTimeMillis()+unit.toMillis(expiry))).toString();
  }

  @Override
  protected InputStream getObject(String bucketName, String filePath) throws Exception  {
    return client.getObject(bucketName, filePath).getObjectContent();
  }

  @Override
  protected void createBucket(String bucketName) throws Exception  {
    client.createBucket(bucketName);
  }

  @Override
  protected boolean checkObject(String bucketName, String objectName) throws Exception {
    COSObject response = client.getObject(bucketName,objectName);
    if(ValidateUtils.isEmpty(response) || ValidateUtils.isEmpty(response.getKey())){
      return false;
    } else {
      return true;
    }
  }

  @Override
  protected boolean copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName)
      throws Exception {
    client.copyObject(sourceBucket,sourceObjectName,targetBucket,targetObjectName);
    return true;
  }
}
