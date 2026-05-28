package com.steven.solomon.service;

import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;

public interface NoticeService {
    NoticeChannelEnum getChannel();
    boolean send(NoticeMessage message) throws Exception;
}
