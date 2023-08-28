package com.steven.solomon.service;

import com.obs.services.ObsClient;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.web.multipart.MultipartFile;

public class OBSService extends AbstractFileService {

  private ObsClient client;

  public OBSService(FileChoiceProperties properties) {
    super(properties);
    this.client = client();
  }

  private ObsClient client() {
    return new ObsClient(properties.getAccessKey(), properties.getSecretKey(), properties.getEndpoint());
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return client.headBucket(bucketName);
  }

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception  {
    client.putObject(bucketName, filePath, file.getInputStream());
  }

  @Override
  protected void delete(String bucketName, String filePath) throws Exception  {
    client.deleteObject(bucketName, filePath);
  }

  @Override
  protected String shareUrl(String bucketName, String filePath, long expiry, TimeUnit unit) throws Exception  {
    TemporarySignatureRequest request = new TemporarySignatureRequest(HttpMethodEnum.GET, unit.toSeconds(expiry));
    request.setBucketName(bucketName);
    request.setObjectKey(filePath);
    TemporarySignatureResponse response = client.createTemporarySignature(request);
    return response.getSignedUrl();
  }

  @Override
  protected InputStream getObject(String bucketName, String filePath) throws Exception {
    return client.getObject(bucketName, filePath).getObjectContent();
  }

  @Override
  protected void createBucket(String bucketName) throws Exception  {
    client.createBucket(bucketName);
  }


}
