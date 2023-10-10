package com.steven.solomon.service;

import com.qcloud.cos.model.COSObject;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public class KODOService extends AbstractFileService {

  private UploadManager uploadManager;

  private Auth auth;

  private BucketManager bucketManager;

  private Configuration conf;

  public KODOService(FileChoiceProperties properties) {
    super(properties);
    this.conf = new Configuration(Region.autoRegion());
    this.auth = Auth.create(properties.getAccessKey(), properties.getSecretKey());
    this.uploadManager = new UploadManager(this.conf);
    this.bucketManager = new BucketManager(this.auth,this.conf);
  }

  @Override
  protected void upload(MultipartFile file, String bucketName, String filePath) throws Exception {
    String uploadToken = auth.uploadToken(bucketName);
    uploadManager.put(file.getInputStream(),file.getSize(),filePath,uploadToken,null,null,false);
  }

  @Override
  protected void delete(String bucketName, String filePath) throws Exception {
    bucketManager.delete(bucketName, filePath);
  }

  @Override
  protected String shareUrl(String bucketName, String filePath, long expiry, TimeUnit unit) throws Exception {
    String encodedFileName = URLEncoder.encode(getFilePath(filePath,properties), "utf-8").replace("+", "%20");
    String publicUrl = String.format("%s/%s", properties.getEndpoint(), encodedFileName);
    String accessKey = properties.getAccessKey();
    String secretKey = properties.getSecretKey();
    Auth auth = Auth.create(accessKey, secretKey);
    return auth.privateDownloadUrl(publicUrl, unit.toSeconds(expiry));
  }

  @Override
  protected InputStream getObject(String bucketName, String filePath) throws Exception {
    URL           url  = new URL(shareUrl(filePath,bucketName,3600L,TimeUnit.SECONDS));
    URLConnection conn = url.openConnection();
    return conn.getInputStream();
  }

  @Override
  protected void createBucket(String bucketName) throws Exception {
    bucketManager.createBucket(bucketName,properties.getRegionName());
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    List<String> bucketNames = new ArrayList<>(Arrays.asList(bucketManager.buckets()));
    return bucketNames.contains(bucketName);
  }

  @Override
  protected boolean checkObject(String bucketName, String objectName) throws Exception {
    FileInfo response = bucketManager.stat(bucketName,objectName);
    if(ValidateUtils.isEmpty(response) || ValidateUtils.isEmpty(response.key)){
      return false;
    } else {
      return true;
    }
  }

  @Override
  protected boolean copyFile(String sourceBucket, String targetBucket, String sourceObjectName, String targetObjectName)
      throws Exception {
    bucketManager.copy(sourceBucket,sourceObjectName,targetBucket,targetObjectName,true);
    return true;
  }
}
