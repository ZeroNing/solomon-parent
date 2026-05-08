package com.steven.solomon.service;

import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;

/**
 * 通知服务接口
 */
public interface NoticeService {

    /**
     * 获取支持的渠道
     */
    NoticeChannelEnum getChannel();

    /**
     * 发送通知
     * @param message 消息内容
     * @return 是否发送成功
     */
    boolean send(NoticeMessage message) throws Exception;

    /**
     * 异步发送通知
     * @param message 消息内容
     */
    default void sendAsync(NoticeMessage message) {
        new Thread(() -> {
            try {
                send(message);
            } catch (Exception e) {
                // 异常处理
            }
        }).start();
    }
}
