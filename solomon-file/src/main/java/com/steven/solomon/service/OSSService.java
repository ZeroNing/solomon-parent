package com.steven.solomon.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.web.multipart.MultipartFile;
/**
 * 阿里云文件实现类
 */
public class OSSService extends AbstractFileService {

  private OSS client;

  public OSSService(FileChoiceProperties properties) {
    super(properties);
    this.client     = client();
  }

  public OSS client() {
    return new OSSClientBuilder().build(properties.getEndpoint(), properties.getAccessKey(), properties.getSecretKey());
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return client.doesBucketExist(bucketName);
  }

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception {
    client.putObject(new PutObjectRequest(bucketName, filePath, file.getInputStream()));
    client.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
  }

  @Override
  protected void delete(String bucketName, String filePath) throws Exception {
    client.deleteObject(bucketName, filePath);
  }

  @Override
  protected String shareUrl(String bucketName, String filePath, long expiry, TimeUnit unit) throws Exception {
    return client.generatePresignedUrl(bucketName, filePath, new Date(System.currentTimeMillis() + unit.toMillis(expiry)))
        .toString();
  }

  @Override
  protected InputStream getObject(String bucketName, String filePath) throws Exception {
    return client.getObject(bucketName, filePath).getObjectContent();
  }

  @Override
  protected void createBucket(String bucketName) throws Exception {
    client.createBucket(bucketName);
  }
  @Override
  protected boolean checkObjectExist(String bucketName, String objectName) throws Exception {
    OSSObject response = client.getObject(bucketName,objectName);
    return (ValidateUtils.isEmpty(response) || ValidateUtils.isEmpty(response.getKey()));
  }

  @Override
  protected boolean copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName)
      throws Exception {
    client.copyObject(sourceBucket,sourceObjectName,targetBucket,targetObjectName);
    return true;
  }
}
