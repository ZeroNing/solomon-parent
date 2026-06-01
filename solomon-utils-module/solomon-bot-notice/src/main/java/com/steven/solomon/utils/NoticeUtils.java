package com.steven.solomon.utils;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.config.NoticeProperties;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;
import com.steven.solomon.enums.NoticeLevelEnum;
import com.steven.solomon.service.NoticeService;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NoticeUtils implements DisposableBean {
    private static final int MIN_RETRY_COUNT = 1;
    private static final AtomicInteger THREAD_INDEX = new AtomicInteger();
    private final Logger logger = LoggerUtils.logger(getClass());
    private final NoticeProperties properties;
    private final Map<NoticeChannelEnum, NoticeService> serviceMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable); thread.setName("solomon-notice-sender-" + THREAD_INDEX.incrementAndGet()); thread.setDaemon(true); return thread;
    });

    public NoticeUtils(List<NoticeService> serviceList, NoticeProperties properties) { this.properties = properties; for (NoticeService service : serviceList) { serviceMap.put(service.getChannel(), service); } }

    public void send(NoticeChannelEnum channel, String title, String content, List<String> receivers) throws Exception { send(NoticeMessage.of(channel, title, content).receivers(receivers)); }
    public void send(NoticeChannelEnum channel, String title, String content, Boolean atAll, List<String> atUsers) throws Exception { send(NoticeMessage.of(channel, title, content).at(atAll, atUsers)); }

    public void send(NoticeMessage message) throws Exception {
        if (!properties.isEnabled()) { logger.debug("通知服务已关闭，跳过发送"); return; }
        if (message == null || ObjectUtil.isEmpty(message.getChannels())) { logger.warn("通知消息或通知渠道为空，跳过发送"); return; }
        resolveTemplate(message);
        for (NoticeChannelEnum channel : message.getChannels()) {
            NoticeService service = serviceMap.get(channel);
            if (service == null) { logger.warn("通知渠道 {} 未实现，跳过发送", channel.getName()); continue; }
            dispatch(service, message);
        }
    }

    public void sendAsync(NoticeChannelEnum channel, String title, String content, List<String> receivers) { silentSend(NoticeMessage.of(channel, title, content).receivers(receivers).async(true)); }
    public void sendAsync(NoticeChannelEnum channel, String title, String content, Boolean atAll, List<String> atUsers) { silentSend(NoticeMessage.of(channel, title, content).at(atAll, atUsers).async(true)); }
    public void sendError(String title, String content) throws Exception { send(NoticeMessage.create().channels(defaultChannels()).title(title).content(content).level(NoticeLevelEnum.ERROR)); }
    public void sendErrorAsync(String title, String content) { silentSend(NoticeMessage.create().channels(defaultChannels()).title(title).content(content).level(NoticeLevelEnum.ERROR).async(true)); }
    public NoticeService getService(NoticeChannelEnum channel) { return serviceMap.get(channel); }

    @Override public void destroy() { executorService.shutdown(); }

    private void dispatch(NoticeService service, NoticeMessage message) throws Exception {
        if (message.isAsync()) { executorService.execute(() -> sendWithRetry(service, message)); return; }
        boolean success = service.send(message);
        if (success) { logger.info("{} 同步发送成功", service.getChannel().getName()); }
    }

    private void silentSend(NoticeMessage message) { try { send(message); } catch (Exception e) { logger.error("异步通知提交失败", e); } }

    private void sendWithRetry(NoticeService service, NoticeMessage message) {
        int maxRetryCount = Math.max(message.getRetryCount(), MIN_RETRY_COUNT);
        for (int retryIndex = 1; retryIndex <= maxRetryCount; retryIndex++) {
            try { if (service.send(message)) { logger.info("{} 异步发送成功", service.getChannel().getName()); return; } }
            catch (Exception e) { logger.error("{} 异步发送失败，第 {} 次重试", service.getChannel().getName(), retryIndex, e); }
            if (!sleepBeforeNextRetry(retryIndex, maxRetryCount)) { return; }
        }
        logger.error("{} 异步发送最终失败，已重试 {} 次", service.getChannel().getName(), maxRetryCount);
    }

    private boolean sleepBeforeNextRetry(int retryIndex, int maxRetryCount) {
        if (retryIndex >= maxRetryCount) { return true; }
        try { TimeUnit.SECONDS.sleep(2L * retryIndex); return true; } catch (InterruptedException e) { Thread.currentThread().interrupt(); logger.warn("通知重试等待被中断，停止后续重试"); return false; }
    }

    private void resolveTemplate(NoticeMessage message) { if (ObjectUtil.isEmpty(message.getTemplateCode())) { return; } }
    private List<NoticeChannelEnum> defaultChannels() { return List.of(NoticeChannelEnum.WECHAT_WORK); }
}
