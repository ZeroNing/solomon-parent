package com.steven.solomon.service;

import com.obs.services.ObsClient;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties.OBSProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.web.multipart.MultipartFile;

public class OBSService implements FileServiceInterface{

  private final OBSProperties properties;

  @Resource
  private FileNamingRulesGenerationService fileNamingRulesGenerationService;

  public OBSService(OBSProperties properties) {
    this.properties = properties;
  }

  private ObsClient client(){
    return new ObsClient(properties.getAccessKey(), properties.getSecretKey(), properties.getEndpoint());
  }

  @Override
  public FileUpload upload(MultipartFile file, String bucketName) throws Exception {
    ObsClient obsClient = null;
    try {
      obsClient = client();
      String fileName = fileNamingRulesGenerationService.getFileName(file);
      String       filePath = getFilePath(fileName,properties);
      obsClient.putObject(bucketName,filePath,file.getInputStream());
      return new FileUpload(bucketName,filePath,file.getInputStream());
    } finally {
      if(obsClient != null){
        obsClient.close();
      }
    }
  }

  @Override
  public FileUpload upload(String bucketName, BufferedImage bi, String fileName) throws Exception {
    ObsClient obsClient = null;
    try {
      obsClient = client();
      String       filePath = getFilePath(fileName,properties);

      ByteArrayOutputStream bs    = new ByteArrayOutputStream();
      ImageOutputStream     imOut = ImageIO.createImageOutputStream(bs);
      ImageIO.write(bi, "jpg", imOut);

      obsClient.putObject(bucketName,filePath,new ByteArrayInputStream(bs.toByteArray()));
      return new FileUpload(bucketName,filePath,new ByteArrayInputStream(bs.toByteArray()));
    } finally {
      if(obsClient != null){
        obsClient.close();
      }
    }
  }

  @Override
  public void deleteFile(String fileName, String bucketName) throws Exception {
    ObsClient obsClient = null;
    try {
      obsClient = client();
      String filePath = getFilePath(fileName,properties);
      obsClient.deleteObject(bucketName,filePath);
    } finally {
      if(obsClient != null){
        obsClient.close();
      }
    }
  }

  @Override
  public String share(String fileName, String bucketName, long expiry, TimeUnit unit) throws Exception {
    ObsClient obsClient = null;
    try {
      obsClient = client();
      String                    filePath = ValidateUtils.isEmpty(properties.getRootDirectory()) ? fileName : properties.getRootDirectory() + fileName;
      TemporarySignatureRequest request  = new TemporarySignatureRequest(HttpMethodEnum.GET, unit.toSeconds(expiry));
      request.setBucketName(bucketName);
      request.setObjectKey(filePath);
      TemporarySignatureResponse response = obsClient.createTemporarySignature(request);
      return response.getSignedUrl();
    } finally {
      if(obsClient != null){
        obsClient.close();
      }
    }
  }

  @Override
  public InputStream download(String fileName, String bucketName) throws Exception {
    ObsClient obsClient = null;
    try {
      obsClient = client();
      String filePath = getFilePath(fileName,properties);
      return obsClient.getObject(bucketName,filePath).getObjectContent();
    } finally {
      if(obsClient != null){
        obsClient.close();
      }
    }
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    ObsClient obsClient = null;
    try {
      obsClient = client();
      return obsClient.headBucket(bucketName);
    } finally {
      if(obsClient != null){
        obsClient.close();
      }
    }
  }

  @Override
  public void makeBucket(String bucketName) throws Exception {
    ObsClient obsClient = null;
    try {
      if(bucketExists(bucketName)){
        return;
      }
      obsClient = client();
      obsClient.createBucket(bucketName);
    } finally {
      if(obsClient != null){
        obsClient.close();
      }
    }
  }


}
