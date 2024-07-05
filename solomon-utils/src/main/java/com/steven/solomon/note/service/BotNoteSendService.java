package com.steven.solomon.note.service;

import com.steven.solomon.note.entity.NoteRequestVO;
import com.steven.solomon.note.entity.SendBaseNoteMessage;

public interface BotNoteSendService {
    /**
     * 初始化机器人通知配置
     */
    String initProfile() throws Exception;

    /**
     * 发送机器人通知
     */
    NoteRequestVO sendNote(SendBaseNoteMessage message) throws Exception;
}
