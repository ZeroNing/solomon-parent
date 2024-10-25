package steven.controller;

import com.steven.solomon.config.FileConfig;
import com.steven.solomon.file.MockMultipartFile;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.pojo.vo.ResultVO;
import com.steven.solomon.service.FileServiceInterface;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RestController
public class TestFileController {

    private final FileServiceInterface fileService;

    private final Logger logger = LoggerUtils.logger(TestFileController.class);

    public TestFileController(FileServiceInterface fileService) {
        this.fileService = fileService;
    }


    @PostMapping("/test")
    public ResultVO<String> test(@RequestPart(name = "file") MultipartFile file) throws Exception {
        String bucketName = "default";
        //判断桶是否存在
        boolean bucketExists = fileService.bucketExists(bucketName);
        logger.info("桶:{}{}",bucketExists,bucketExists ? "已存在" : "不存在");
        //上传文件
        FileUpload fileUpload = fileService.upload(file,bucketName);
        //分享URL
        String shareUrl = fileService.share(fileUpload.getFileName(),bucketName,3600L);
        //删除文件
        fileService.deleteFile(fileUpload.getFileName(),bucketName);
        //删除桶
        fileService.deleteBucket(bucketName);
        return new ResultVO<String>(shareUrl);
    }
}
