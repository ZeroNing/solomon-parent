package com.steven.solomon.service;

import com.baidubce.Protocol;
import com.baidubce.Region;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.*;
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

  private final BosClient client;

  private BosClient client() {
    BosClientConfiguration configuration = new BosClientConfiguration();
    configuration.setCredentials(new DefaultBceCredentials(properties.getAccessKey(), properties.getSecretKey()));
    configuration.setEndpoint(properties.getEndpoint());
    configuration.setConnectionTimeoutInMillis(properties.getConnectionTimeout());
    configuration.setSocketTimeoutInMillis(properties.getSocketTimeout());
    if(ValidateUtils.isNotEmpty(properties.getRegionName())){
      configuration.setRegion(Region.fromValue(properties.getRegionName()));
    }
    boolean             isHttps         = properties.getEndpoint().contains("https");
    configuration.setProtocol(isHttps ? Protocol.HTTPS : Protocol.HTTP);
    return new BosClient(configuration);
  }

  public BOSService(FileChoiceProperties properties) {
    super(properties);
    this.client = client();
  }

  @Override
  protected void multipartUpload(MultipartFile file, String bucketName, long fileSize, String uploadId, String filePath,int partCount)
      throws Exception {
    List<PartETag> partETags = new ArrayList<>();
    for(int i = 0; i < partCount; i++){
      InputStream inputStream = file.getInputStream();
      // 跳到每个分块的开头
      long skipBytes = partSize * i;
      inputStream.skip(skipBytes);

      // 计算每个分块的大小
      long size = partSize < fileSize - skipBytes ?
                  partSize : fileSize - skipBytes;
      UploadPartRequest uploadPartRequest = new UploadPartRequest();
      uploadPartRequest.setBucketName(bucketName);
      uploadPartRequest.setKey(filePath);
      uploadPartRequest.setUploadId(uploadId);
      uploadPartRequest.setInputStream(inputStream);
      uploadPartRequest.setPartSize(size);
      uploadPartRequest.setPartNumber(i + 1);
      UploadPartResponse uploadPartResponse = client.uploadPart(uploadPartRequest);
      // 将返回的PartETag保存到List中。
      partETags.add(uploadPartResponse.getPartETag());
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
  protected void copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName)
      throws Exception {
    client.copyObject(sourceBucket,sourceObjectName,targetBucket,targetObjectName);
  }

  @Override
  public List<String> listObjects(String bucketName,String key) throws Exception {
    if(ValidateUtils.isEmpty(bucketName) || !bucketExists(bucketName)){
     return new ArrayList<>();
    }
    ListObjectsResponse response = ValidateUtils.isEmpty(key) ? client.listObjects(bucketName) : client.listObjects(bucketName,key);
    return Lambda.toList(response.getContents(), BosObjectSummary::getKey);
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
