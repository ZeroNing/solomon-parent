package com.steven.solomon.service;

import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;

/**
 * 通知服务接口。
 */
public interface NoticeService {

    /**
     * 返回当前服务支持的通知渠道。
     */
    NoticeChannelEnum getChannel();

    /**
     * 发送通知。
     *
     * @param message 通知消息
     * @return 是否发送成功
     */
    boolean send(NoticeMessage message) throws Exception;
}
