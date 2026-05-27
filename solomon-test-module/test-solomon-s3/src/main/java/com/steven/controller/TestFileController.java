package com.steven.controller;

import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.pojo.vo.ResultVO;
import com.steven.solomon.service.FileServiceInterface;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TestFileController {

    private final FileServiceInterface fileService;

    private final ClamAvUtils clamAvUtils;


    private final Logger logger = LoggerUtils.logger(TestFileController.class);

    public TestFileController(FileServiceInterface fileService, ClamAvUtils clamAvUtils) {
        this.fileService = fileService;
        this.clamAvUtils = clamAvUtils;
    }


    @PostMapping("/test")
    public ResultVO<String> test(@RequestPart(name = "file") MultipartFile file) throws Exception {
        clamAvUtils.scanFile(file.getInputStream(), BaseExceptionCode.FILE_HIGH_RISK);
        String bucketName = "default";
        //判断桶是否存在
        boolean bucketExists = fileService.bucketExists(bucketName);
        logger.info("桶:{}{}",bucketName,bucketExists ? "已存在" : "不存在");
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
