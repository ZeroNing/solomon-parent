package com.steven.solomon.service;

import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
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

public class KODOService implements FileServiceInterface {

  private UploadManager uploadManager;

  private Auth auth;

  private BucketManager bucketManager;

  private Configuration conf;

  private FileChoiceProperties properties;

  @Autowired
  private FileNamingRulesGenerationService fileNamingRulesGenerationService;

  public KODOService() {

  }

  public KODOService(FileChoiceProperties properties) {
    this.properties = properties;
    this.conf = new Configuration(Region.autoRegion());
    this.auth = Auth.create(properties.getAccessKey(), properties.getSecretKey());
    this.uploadManager = new UploadManager(this.conf);
    this.bucketManager = new BucketManager(this.auth,this.conf);
  }

  @Override
  public FileUpload upload(MultipartFile file, String bucketName) throws Exception {
    //创建桶
    makeBucket(bucketName);

    String       name     = fileNamingRulesGenerationService.getFileName(file);
    String       filePath = getFilePath(name,properties);
    String uploadToken = auth.uploadToken(bucketName);

    uploadManager.put(file.getInputStream(),file.getSize(),filePath,uploadToken,null,null,false);
    return new FileUpload(bucketName, filePath, file.getInputStream());
  }

  @Override
  public FileUpload upload(String bucketName, BufferedImage bi, String fileName) throws Exception {
    //创建桶
    makeBucket(bucketName);
    String    filePath  = getFilePath(fileName, properties);
    String uploadToken = auth.uploadToken(bucketName);

    ByteArrayOutputStream bs    = new ByteArrayOutputStream();
    ImageOutputStream     imOut = ImageIO.createImageOutputStream(bs);
    ImageIO.write(bi, "jpg", imOut);
    uploadManager.put(new ByteArrayInputStream(bs.toByteArray()),new ByteArrayInputStream(bs.toByteArray()).available(),filePath,uploadToken,null,null,false);
    return new FileUpload(bucketName, filePath, new ByteArrayInputStream(bs.toByteArray()));
  }

  @Override
  public void deleteFile(String fileName, String bucketName) throws Exception {
    boolean flag = bucketExists(bucketName);
    if (!flag || ValidateUtils.isEmpty(fileName)) {
      return;
    }
    bucketManager.delete(bucketName, fileName);
  }

  @Override
  public String share(String fileName, String bucketName, long expiry, TimeUnit unit) throws Exception {
    String encodedFileName = URLEncoder.encode(getFilePath(fileName,properties), "utf-8").replace("+", "%20");
    String publicUrl = String.format("%s/%s", properties.getEndpoint(), encodedFileName);
    String accessKey = properties.getAccessKey();
    String secretKey = properties.getSecretKey();
    Auth auth = Auth.create(accessKey, secretKey);

    return auth.privateDownloadUrl(publicUrl, unit.toSeconds(expiry));
  }

  @Override
  public InputStream download(String fileName, String bucketName) throws Exception {
    URL           url  = new URL(share(fileName,bucketName,3600L,TimeUnit.SECONDS));
    URLConnection conn = url.openConnection();
    return conn.getInputStream();
  }

  @Override
  public boolean bucketExists(String bucketName) throws Exception {
    List<String> bucketNames = new ArrayList<>(Arrays.asList(bucketManager.buckets()));
    return bucketNames.contains(bucketName);
  }

  @Override
  public void makeBucket(String bucketName) throws Exception {
    boolean flag = bucketExists(bucketName);
    if(flag){
      return;
    }
    bucketManager.createBucket(bucketName,properties.getRegionName());
  }


}
