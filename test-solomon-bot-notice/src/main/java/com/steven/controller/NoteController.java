package com.steven.controller;

import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.utils.NoticeUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class NoteController {

    private final NoticeUtils noticeUtils;

    private final Logger logger = LoggerUtils.logger(NoteController.class);

    public NoteController(NoticeUtils noticeUtils) {
        this.noticeUtils = noticeUtils;
    }


    @PostMapping("/test")
    public void test(@RequestBody NoticeMessage message) throws Exception {
        noticeUtils.send(message);
    }
}
