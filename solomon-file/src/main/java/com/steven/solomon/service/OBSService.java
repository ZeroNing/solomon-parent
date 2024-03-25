package com.steven.solomon.service;

import com.obs.services.ObsClient;
import com.obs.services.model.AbortMultipartUploadRequest;
import com.obs.services.model.CompleteMultipartUploadRequest;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.InitiateMultipartUploadRequest;
import com.obs.services.model.InitiateMultipartUploadResult;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.PartEtag;
import com.obs.services.model.S3Bucket;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.obs.services.model.UploadPartRequest;
import com.obs.services.model.UploadPartResult;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
import io.minio.messages.Bucket;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.web.multipart.MultipartFile;
/**
 * 华为云文件实现类
 */
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
  protected void multipartUpload(MultipartFile file, String bucketName, long fileSize, String uploadId, String filePath)
      throws Exception {
    // 分割文件并上传分片
    long           filePosition = 0;
    List<PartEtag> partETags    = new ArrayList<>();
    int            partNumber   = 1;
    while (fileSize > filePosition) {
      // 计算每个分片的大小
      long currentPartSize = Math.min(partSize, fileSize - filePosition);
      byte[] partBuffer = new byte[(int) currentPartSize];
      file.getInputStream().read(partBuffer, 0, (int) currentPartSize);

      UploadPartRequest uploadPartRequest = new UploadPartRequest();
      uploadPartRequest.setBucketName(bucketName);
      uploadPartRequest.setObjectKey(filePath);
      uploadPartRequest.setUploadId(uploadId);
      uploadPartRequest.setPartSize(currentPartSize);
      uploadPartRequest.setPartNumber(partNumber++);
      uploadPartRequest.setInput(new ByteArrayInputStream(partBuffer));

      UploadPartResult uploadPartResult = client.uploadPart(uploadPartRequest);
      filePosition += currentPartSize;
      partETags.add(new PartEtag(uploadPartResult.getEtag(),uploadPartResult.getPartNumber()));

    }
    // 完成分片上传
    CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
        bucketName, filePath,uploadId, partETags);

    client.completeMultipartUpload(compRequest);
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
    ListObjectsRequest request = new ListObjectsRequest(bucketName);
    request.setPrefix(key);
    ObjectListing      response = client.listObjects(request);
    return Lambda.toList(response.getObjects(),data->data.getObjectKey());
  }

  @Override
  public String initiateMultipartUploadTask(String bucketName, String objectName) throws Exception {
    InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(bucketName,objectName);
    InitiateMultipartUploadResult  initiateMultipartUploadResult  = client.initiateMultipartUpload(initiateMultipartUploadRequest);
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
    List<S3Bucket> listBucketsResponse = client.listBuckets();
    List<String>   bucketNameList      = new ArrayList<>();
    if(ValidateUtils.isNotEmpty(listBucketsResponse)){
      for(S3Bucket bucket : listBucketsResponse){
        bucketNameList.add(bucket.getBucketName());
      }
    }
    return bucketNameList;
  }
}
