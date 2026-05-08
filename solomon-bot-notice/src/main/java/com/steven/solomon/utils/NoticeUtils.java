package com.steven.solomon.utils;

import com.steven.solomon.config.NoticeProperties;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;
import com.steven.solomon.service.NoticeService;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 通知工具类（统一入口）
 */
@Component
public class NoticeUtils {

    private final Logger logger = LoggerUtils.logger(getClass());

    private final NoticeProperties properties;

    /**
     * 服务缓存
     */
    private final Map<NoticeChannelEnum, NoticeService> serviceMap = new ConcurrentHashMap<>();

    public NoticeUtils(List<NoticeService> serviceList, NoticeProperties properties) {
        this.properties = properties;
        for (NoticeService service : serviceList) {
            serviceMap.put(service.getChannel(), service);
        }
    }

    /**
     * 快速发送（单渠道）
     */
    public void send(NoticeChannelEnum channel, String title, String content, List<String> receivers) throws Exception {
        NoticeMessage message = new NoticeMessage(channel, title, content, receivers);
        send(message);
    }

    /**
     * 发送通知（支持多渠道）
     */
    public void send(NoticeMessage message) throws Exception {
        if (!properties.isEnabled()) {
            logger.debug("通知服务已关闭，跳过发送");
            return;
        }

        if (ValidateUtils.isEmpty(message.getChannels())) {
            logger.warn("通知渠道为空，跳过发送");
            return;
        }

        // 处理模板
        if (ValidateUtils.isNotEmpty(message.getTemplateCode())) {
            resolveTemplate(message);
        }

        // 按渠道发送
        for (NoticeChannelEnum channel : message.getChannels()) {
            NoticeService service = serviceMap.get(channel);
            if (service == null) {
                logger.warn("渠道 {} 未实现，跳过发送", channel.getName());
                continue;
            }

            try {
                if (message.isAsync()) {
                    // 异步发送，带重试
                    asyncSendWithRetry(service, message, message.getRetryCount());
                } else {
                    // 同步发送
                    service.send(message);
                    logger.info("{} 发送成功", channel.getName());
                }
            } catch (Exception e) {
                logger.error("{} 发送失败", channel.getName(), e);
                if (!message.isAsync()) {
                    throw e;
                }
            }
        }
    }

    /**
     * 快速发送带@功能（单渠道）
     * @param atAll 是否@所有人
     * @param atUsers @用户列表（企业微信userid/钉钉手机号）
     */
    public void send(NoticeChannelEnum channel, String title, String content, Boolean atAll, List<String> atUsers) throws Exception {
        NoticeMessage message = new NoticeMessage(channel, title, content, atAll, atUsers);
        send(message);
    }

    /**
     * 异步快速发送
     */
    public void sendAsync(NoticeChannelEnum channel, String title, String content, List<String> receivers) {
        NoticeMessage message = new NoticeMessage(channel, title, content, receivers);
        message.setAsync(true);
        try {
            send(message);
        } catch (Exception e) {
            logger.error("异步发送失败", e);
        }
    }

    /**
     * 异步快速发送带@功能
     */
    public void sendAsync(NoticeChannelEnum channel, String title, String content, Boolean atAll, List<String> atUsers) {
        NoticeMessage message = new NoticeMessage(channel, title, content, atAll, atUsers);
        message.setAsync(true);
        try {
            send(message);
        } catch (Exception e) {
            logger.error("异步发送失败", e);
        }
    }

    /**
     * 发送错误告警
     */
    public void sendError(String title, String content) throws Exception {
        NoticeMessage message = new NoticeMessage();
        message.setChannels(getDefaultChannels());
        message.setTitle(title);
        message.setContent(content);
        message.setLevel(com.steven.solomon.enums.NoticeLevelEnum.ERROR);
        send(message);
    }

    /**
     * 异步发送错误告警
     */
    public void sendErrorAsync(String title, String content) {
        try {
            sendError(title, content);
        } catch (Exception e) {
            logger.error("发送错误告警失败", e);
        }
    }

    /**
     * 异步发送带重试
     */
    private void asyncSendWithRetry(NoticeService service, NoticeMessage message, int retryCount) {
        new Thread(() -> {
            int count = 0;
            boolean success = false;
            while (count < retryCount && !success) {
                try {
                    success = service.send(message);
                    if (success) {
                        logger.info("{} 异步发送成功", service.getChannel().getName());
                        break;
                    }
                } catch (Exception e) {
                    count++;
                    logger.error("{} 异步发送失败，第{}次重试", service.getChannel().getName(), count, e);
                    if (count < retryCount) {
                        try {
                            TimeUnit.SECONDS.sleep(2 * count); // 指数退避
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            if (!success) {
                logger.error("{} 异步发送最终失败，重试{}次全部失败", service.getChannel().getName(), retryCount);
            }
        }).start();
    }

    /**
     * 解析模板
     */
    private void resolveTemplate(NoticeMessage message) {
        // TODO 模板解析逻辑，使用FreeMarker渲染
        // String template = templateManager.getTemplate(message.getTemplateCode());
        // String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, message.getTemplateParams());
        // message.setContent(content);
    }

    /**
     * 获取默认渠道
     */
    private List<NoticeChannelEnum> getDefaultChannels() {
        // 可配置默认渠道，这里默认企业微信+邮件
        return List.of(NoticeChannelEnum.WECHAT_WORK, NoticeChannelEnum.WECHAT_WORK);
    }

    /**
     * 获取指定渠道的服务
     */
    public NoticeService getService(NoticeChannelEnum channel) {
        return serviceMap.get(channel);
    }
}
