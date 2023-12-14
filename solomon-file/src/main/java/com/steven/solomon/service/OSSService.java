package com.steven.solomon.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.AbortMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
  public FileUpload multipartUpload(MultipartFile file, String bucketName, boolean isUseOriginalName) throws Exception {
    String filePath = getFilePath(!isUseOriginalName? fileNamingRulesGenerationService.getFileName(file): file.getName(),properties);

    // 初始化分片上传
    InitiateMultipartUploadRequest initRequest  = new InitiateMultipartUploadRequest(bucketName, filePath);
    InitiateMultipartUploadResult  initResponse = client.initiateMultipartUpload(initRequest);
    String uploadId = initResponse.getUploadId();
    long contentLength = file.getSize();
    try{
      // 分割文件并上传分片
      List<PartETag> partETags    = new ArrayList<>();
      for (int i = 0; contentLength > partSize * i; i++) {
        // 计算每个分片的大小
        long size = Math.min(partSize, contentLength - partSize * i);
        // 创建上传分片请求
        UploadPartRequest uploadPartRequest = new UploadPartRequest();
        uploadPartRequest.setBucketName(bucketName);
        uploadPartRequest.setKey(filePath);
        uploadPartRequest.setUploadId(uploadId);
        uploadPartRequest.setInputStream(file.getInputStream());
        uploadPartRequest.setPartSize(size);
        uploadPartRequest.setPartNumber(i + 1);
        // 上传分片
        UploadPartResult uploadPartResult = client.uploadPart(uploadPartRequest);
        partETags.add(uploadPartResult.getPartETag());
      }
      // 完成分片上传
      CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
          bucketName, filePath, initResponse.getUploadId(), partETags);

      client.completeMultipartUpload(compRequest);
    } catch (Exception e) {
      abortMultipartUpload(initResponse.getUploadId(),bucketName,filePath);
      throw e;
    }
    return new FileUpload(bucketName,filePath,file.getInputStream());
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return client.doesBucketExist(bucketName);
  }

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception {
    client.putObject(new PutObjectRequest(bucketName, filePath, file.getInputStream()));
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
    return client.doesObjectExist(bucketName,objectName);
  }

  @Override
  protected boolean copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName)
      throws Exception {
    client.copyObject(sourceBucket,sourceObjectName,targetBucket,targetObjectName);
    return true;
  }

  @Override
  public List<String> listObjects(String bucketName,String key) throws Exception {
    if(ValidateUtils.isEmpty(bucketName) || !bucketExists(bucketName)){
      return new ArrayList<>();
    }
    ObjectListing response = ValidateUtils.isEmpty(key) ? client.listObjects(bucketName) : client.listObjects(bucketName,key);
    return Lambda.toList(response.getObjectSummaries(),data->data.getKey());
  }

  @Override
  public String initiateMultipartUploadTask(String bucketName, String objectName) throws Exception {
    InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(bucketName,objectName);
    InitiateMultipartUploadResult                       initiateMultipartUploadResult  = client.initiateMultipartUpload(initiateMultipartUploadRequest);
    return initiateMultipartUploadResult.getUploadId();
  }

  @Override
  protected void abortMultipartUpload(String uploadId, String bucketName, String filePath) throws Exception {
    client.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName,filePath,uploadId));
  }
}
