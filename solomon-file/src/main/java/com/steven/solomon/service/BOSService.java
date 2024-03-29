package com.steven.solomon.service;

import com.amazonaws.services.s3.model.Bucket;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.AbortMultipartUploadRequest;
import com.baidubce.services.bos.model.BucketSummary;
import com.baidubce.services.bos.model.CompleteMultipartUploadRequest;
import com.baidubce.services.bos.model.InitiateMultipartUploadRequest;
import com.baidubce.services.bos.model.InitiateMultipartUploadResponse;
import com.baidubce.services.bos.model.ListBucketsResponse;
import com.baidubce.services.bos.model.ListObjectsResponse;
import com.baidubce.services.bos.model.PartETag;
import com.baidubce.services.bos.model.PutObjectRequest;
import com.baidubce.services.bos.model.UploadPartRequest;
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
 * 百度云文件实现类
 */
public class BOSService extends AbstractFileService {

  private BosClient client;

  private BosClient client() {
    BosClientConfiguration config = new BosClientConfiguration();
    config.setCredentials(new DefaultBceCredentials(properties.getAccessKey(), properties.getSecretKey()));
    config.setEndpoint(properties.getEndpoint());
    return new BosClient(config);
  }

  public BOSService(FileChoiceProperties properties) {
    super(properties);
    this.client = client();
  }

  @Override
  protected void multipartUpload(MultipartFile file, String bucketName, long fileSize, String uploadId, String filePath)
      throws Exception {
    long           filePosition = 0;
    List<PartETag> partETags    = new ArrayList<>();
    for (int i = 1; filePosition < fileSize; i++) {
      // 计算当前分片大小
      partSize = Math.min(partSize, (fileSize - filePosition));

      // 创建上传请求
      UploadPartRequest uploadRequest = new UploadPartRequest()
          .withBucketName(bucketName).withKey(filePath)
          .withUploadId(uploadId).withPartNumber(i)
          .withInputStream(file.getInputStream())
          .withPartSize(partSize);

      // 上传分片并添加到列表
      partETags.add(client.uploadPart(uploadRequest).getPartETag());

      filePosition += partSize;
    }
    // 完成分片上传
    CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
        bucketName, filePath, uploadId, partETags);

    client.completeMultipartUpload(compRequest);
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    return client.doesBucketExist(bucketName);
  }

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception  {
    client.putObject(new PutObjectRequest(bucketName,filePath,file.getInputStream()));
  }

  @Override
  protected void delete(String bucketName, String filePath) throws Exception  {
    client.deleteObject(bucketName, filePath);
  }

  @Override
  protected String shareUrl(String bucketName, String filePath, long expiry, TimeUnit unit) throws Exception  {
    return client.generatePresignedUrl(bucketName,filePath,new Date(System.currentTimeMillis()+unit.toMillis(expiry)).getSeconds()).toString();
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
    ListObjectsResponse response = ValidateUtils.isEmpty(key) ? client.listObjects(bucketName) : client.listObjects(bucketName,key);
    return Lambda.toList(response.getContents(),data->data.getKey());
  }

  @Override
  public String initiateMultipartUploadTask(String bucketName, String objectName) throws Exception {
    InitiateMultipartUploadRequest  initRequest  = new InitiateMultipartUploadRequest(bucketName,objectName);
    InitiateMultipartUploadResponse initResponse = client.initiateMultipartUpload(initRequest);
    return initResponse.getUploadId();
  }

  @Override
  protected void abortMultipartUpload(String uploadId, String bucketName, String filePath) throws Exception {
    client.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName,filePath,uploadId));
  }

  @Override
  public void deleteBucket(String bucketName) throws Exception {
    if(ValidateUtils.isEmpty(bucketName)){
      logger.error("deleteBucket方法中,请求参数为空,删除桶失败");
    }
    client.deleteBucket(bucketName);
  }

  @Override
  public List<String> getBucketList() throws Exception {
    ListBucketsResponse listBucketsResponse     = client.listBuckets();
    List<String>        bucketNameList = new ArrayList<>();
    if(ValidateUtils.isNotEmpty(listBucketsResponse)){
      for(BucketSummary bucket : listBucketsResponse.getBuckets()){
        bucketNameList.add(bucket.getName());
      }
    }
    return bucketNameList;
  }
}
