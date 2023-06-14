package com.steven.solomon.service;

import com.obs.services.ObsClient;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
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

public class OBSService implements FileServiceInterface {

  private FileChoiceProperties properties;

  private ObsClient obsClient;

  @Autowired
  private FileNamingRulesGenerationService fileNamingRulesGenerationService;


  public OBSService() {

  }

  public OBSService(FileChoiceProperties properties) {
    this.properties = properties;
    this.obsClient  = client();
  }

  private ObsClient client() {
    return new ObsClient(properties.getAccessKey(), properties.getSecretKey(), properties.getEndpoint());
  }

  @Override
  public FileUpload upload(MultipartFile file, String bucketName) throws Exception {
    //创建桶
    makeBucket(bucketName);

    String    fileName  = fileNamingRulesGenerationService.getFileName(file);
    String    filePath  = getFilePath(fileName, properties);
    obsClient.putObject(bucketName, filePath, file.getInputStream());
    return new FileUpload(bucketName, filePath, file.getInputStream());
  }

  @Override
  public FileUpload upload(String bucketName, BufferedImage bi, String fileName) throws Exception {
    //创建桶
    makeBucket(bucketName);

    String    filePath  = getFilePath(fileName, properties);

    ByteArrayOutputStream bs    = new ByteArrayOutputStream();
    ImageOutputStream     imOut = ImageIO.createImageOutputStream(bs);
    ImageIO.write(bi, "jpg", imOut);

    obsClient.putObject(bucketName, filePath, new ByteArrayInputStream(bs.toByteArray()));
    return new FileUpload(bucketName, filePath, new ByteArrayInputStream(bs.toByteArray()));
  }

  @Override
  public void deleteFile(String fileName, String bucketName) throws Exception {
    boolean flag = bucketExists(bucketName);
    if (!flag) {
      return;
    }
    String    filePath  = getFilePath(fileName, properties);
    obsClient.deleteObject(bucketName, filePath);
  }

  @Override
  public String share(String fileName, String bucketName, long expiry, TimeUnit unit) throws Exception {
    String filePath = ValidateUtils.isEmpty(properties.getRootDirectory()) ? fileName :
                      properties.getRootDirectory() + fileName;
    TemporarySignatureRequest request = new TemporarySignatureRequest(HttpMethodEnum.GET, unit.toSeconds(expiry));
    request.setBucketName(bucketName);
    request.setObjectKey(filePath);
    TemporarySignatureResponse response = obsClient.createTemporarySignature(request);
    return response.getSignedUrl();
  }

  @Override
  public InputStream download(String fileName, String bucketName) throws Exception {
    String    filePath  = getFilePath(fileName, properties);
    return obsClient.getObject(bucketName, filePath).getObjectContent();
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return obsClient.headBucket(bucketName);
  }

  @Override
  public void makeBucket(String bucketName) throws Exception {
    if (bucketExists(bucketName)) {
      return;
    }
    obsClient.createBucket(bucketName);
  }


}
