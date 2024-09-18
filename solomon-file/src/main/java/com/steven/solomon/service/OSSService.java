package com.steven.solomon.service;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.comm.Protocol;
import com.aliyun.oss.model.*;
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

  private final OSS client;

  public OSSService(FileChoiceProperties properties) {
    super(properties);
    this.client     = client();
  }

  public OSS client() {
    ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
    configuration.setConnectionTimeout(properties.getConnectionTimeout());
    configuration.setSocketTimeout(properties.getSocketTimeout());
    boolean             isHttps         = properties.getEndpoint().contains("https");
    configuration.setProtocol(isHttps ? Protocol.HTTPS : Protocol.HTTP);
    return new OSSClientBuilder().build(properties.getEndpoint(), properties.getAccessKey(), properties.getSecretKey(),configuration);
  }

  @Override
  protected void multipartUpload(MultipartFile file, String bucketName, long fileSize, String uploadId, String filePath,int partCount)
      throws Exception {
    List<PartETag> partETags = new ArrayList<>();
    for (int i = 0; i < partCount; i++) {
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
      // 设置上传的分片流。
      uploadPartRequest.setInputStream(inputStream);
      // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
      uploadPartRequest.setPartSize(size);
      // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
      uploadPartRequest.setPartNumber(i + 1);
      // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
      UploadPartResult uploadPartResult = client.uploadPart(uploadPartRequest);
      // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
      partETags.add(uploadPartResult.getPartETag());
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
  protected void copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName)
      throws Exception {
    client.copyObject(sourceBucket,sourceObjectName,targetBucket,targetObjectName);
  }

  @Override
  public List<String> listObjects(String bucketName,String key) throws Exception {
    if(ValidateUtils.isEmpty(bucketName) || !bucketExists(bucketName)){
      return new ArrayList<>();
    }
    ObjectListing response = ValidateUtils.isEmpty(key) ? client.listObjects(bucketName) : client.listObjects(bucketName,key);
    return Lambda.toList(response.getObjectSummaries(), OSSObjectSummary::getKey);
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

  @Override
  public void deleteBucket(String bucketName) throws Exception {
    if(ValidateUtils.isEmpty(bucketName)){
      logger.error("deleteBucket方法中,请求参数为空,删除桶失败");
    }
    client.deleteBucket(bucketName);
  }

  @Override
  public List<String> getBucketList() throws Exception {
    List<Bucket> listBucketsResponse = client.listBuckets();
    List<String> bucketNameList      = new ArrayList<>();
    if(ValidateUtils.isNotEmpty(listBucketsResponse)){
      for(Bucket bucket : listBucketsResponse){
        bucketNameList.add(bucket.getName());
      }
    }
    return bucketNameList;
  }
}
