package com.steven.controller;

import com.steven.solomon.pojo.entity.NoteRequestVO;
import com.steven.solomon.pojo.entity.SendBaseNoteMessage;
import com.steven.solomon.pojo.enums.BotClientEnum;
import com.steven.solomon.pojo.vo.ResultVO;
import com.steven.solomon.utils.BotNoteUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class NoteController {

    private final BotNoteUtils botNoteUtils;

    private final Logger logger = LoggerUtils.logger(NoteController.class);

    public NoteController(BotNoteUtils botNoteUtils) {
        this.botNoteUtils = botNoteUtils;
    }


    @PostMapping("/test")
    public ResultVO<NoteRequestVO> test(@RequestBody SendBaseNoteMessage noteMessage) throws Exception {
        return new ResultVO<NoteRequestVO>(botNoteUtils.sendBotNote(BotClientEnum.DING_TALK,noteMessage));
    }
}
