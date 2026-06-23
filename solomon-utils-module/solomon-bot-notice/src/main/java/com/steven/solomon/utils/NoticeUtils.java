package com.steven.solomon.utils;

import cn.hutool.core.util.ObjectUtil;
import com.steven.solomon.config.NoticeProperties;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;
import com.steven.solomon.enums.NoticeLevelEnum;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.service.NoticeService;
import com.steven.solomon.utils.logger.LoggerUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NoticeUtils implements DisposableBean {

    private static final int MIN_RETRY_COUNT = 1;
    private static final AtomicInteger THREAD_INDEX = new AtomicInteger();

    private final Logger logger = LoggerUtils.logger(getClass());
    private final NoticeProperties properties;
    private final Map<NoticeChannelEnum, NoticeService> serviceMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService;
    private final MeterRegistry meterRegistry;

    public NoticeUtils(List<NoticeService> serviceList, NoticeProperties properties) {
        this(serviceList, properties, (MeterRegistry) null);
    }

    public NoticeUtils(List<NoticeService> serviceList, NoticeProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.executorService = createExecutor(properties);
        this.meterRegistry = meterRegistry;
        for (NoticeService service : serviceList) {
            serviceMap.put(service.getChannel(), service);
        }
    }

    @Autowired
    public NoticeUtils(
            List<NoticeService> serviceList,
            NoticeProperties properties,
            ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.properties = properties;
        this.executorService = createExecutor(properties);
        this.meterRegistry = meterRegistryProvider == null ? null : meterRegistryProvider.getIfAvailable();
        for (NoticeService service : serviceList) {
            serviceMap.put(service.getChannel(), service);
        }
    }

    public void send(NoticeChannelEnum channel, String title, String content, List<String> receivers) throws Exception {
        send(NoticeMessage.of(channel, title, content).receivers(receivers));
    }

    public void send(NoticeChannelEnum channel, String title, String content, Boolean atAll, List<String> atUsers)
            throws Exception {
        send(NoticeMessage.of(channel, title, content).at(atAll, atUsers));
    }

    public void send(NoticeMessage message) throws Exception {
        if (!properties.isEnabled()) {
            logger.debug("Notice service disabled, skip send.");
            return;
        }
        if (message == null || ObjectUtil.isEmpty(message.getChannels())) {
            logger.warn("Notice message or channels are empty, skip send.");
            return;
        }
        resolveTemplate(message);
        for (NoticeChannelEnum channel : message.getChannels()) {
            NoticeService service = serviceMap.get(channel);
            if (service == null) {
                logger.warn("Notice channel {} has no implementation, skip send.", channel.getName());
                continue;
            }
            dispatch(service, message);
        }
    }

    public void sendAsync(NoticeChannelEnum channel, String title, String content, List<String> receivers) {
        silentSend(NoticeMessage.of(channel, title, content).receivers(receivers).async(true));
    }

    public void sendAsync(NoticeChannelEnum channel, String title, String content, Boolean atAll, List<String> atUsers) {
        silentSend(NoticeMessage.of(channel, title, content).at(atAll, atUsers).async(true));
    }

    public void sendError(String title, String content) throws Exception {
        send(NoticeMessage.create().channels(defaultChannels()).title(title).content(content).level(NoticeLevelEnum.ERROR));
    }

    public void sendErrorAsync(String title, String content) {
        silentSend(NoticeMessage.create().channels(defaultChannels()).title(title).content(content)
                .level(NoticeLevelEnum.ERROR).async(true));
    }

    public NoticeService getService(NoticeChannelEnum channel) {
        return serviceMap.get(channel);
    }

    @Override
    public void destroy() {
        executorService.shutdown();
    }

    private void dispatch(NoticeService service, NoticeMessage message) throws Exception {
        if (message.isAsync()) {
            executorService.execute(RequestHeaderHolder.wrap(() -> sendWithRetry(service, message)));
            return;
        }
        boolean success = sendAndRecord(service, message, false);
        if (success) {
            logger.info("{} sync send succeeded.", service.getChannel().getName());
        }
    }

    private void silentSend(NoticeMessage message) {
        try {
            send(message);
        } catch (Exception e) {
            logger.error("Async notice submit failed.", e);
        }
    }

    private void sendWithRetry(NoticeService service, NoticeMessage message) {
        int maxRetryCount = Math.max(message.getRetryCount(), MIN_RETRY_COUNT);
        for (int retryIndex = 1; retryIndex <= maxRetryCount; retryIndex++) {
            try {
                if (sendAndRecord(service, message, true)) {
                    logger.info("{} async send succeeded.", service.getChannel().getName());
                    return;
                }
            } catch (Exception e) {
                logger.error("{} async send failed on attempt {}.", service.getChannel().getName(), retryIndex, e);
            }
            if (!sleepBeforeNextRetry(retryIndex, maxRetryCount)) {
                return;
            }
        }
        logger.error("{} async send finally failed after {} attempt(s).", service.getChannel().getName(), maxRetryCount);
    }

    private boolean sleepBeforeNextRetry(int retryIndex, int maxRetryCount) {
        if (retryIndex >= maxRetryCount) {
            return true;
        }
        long backoffMillis = (long) properties.getRetryBackoffMillis() * retryIndex;
        if (backoffMillis <= 0) {
            return true;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(backoffMillis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Notice retry wait interrupted, stop retrying.");
            return false;
        }
    }

    private boolean sendAndRecord(NoticeService service, NoticeMessage message, boolean async) throws Exception {
        Timer.Sample sample = meterRegistry == null ? null : Timer.start(meterRegistry);
        String outcome = "success";
        try {
            boolean success = service.send(message);
            outcome = success ? "success" : "failure";
            return success;
        } catch (Exception e) {
            outcome = "error";
            throw e;
        } finally {
            recordMetrics(service, async, outcome, sample);
        }
    }

    private void recordMetrics(NoticeService service, boolean async, String outcome, Timer.Sample sample) {
        if (meterRegistry == null) {
            return;
        }
        Tags tags = Tags.of(
                "channel", service.getChannel().name(),
                "async", Boolean.toString(async),
                "outcome", outcome);
        meterRegistry.counter("solomon.notice.send.total", tags).increment();
        if (sample != null) {
            sample.stop(Timer.builder("solomon.notice.send.duration").tags(tags).register(meterRegistry));
        }
    }

    private ExecutorService createExecutor(NoticeProperties properties) {
        int corePoolSize = Math.max(1, properties.getAsyncCorePoolSize());
        int maxPoolSize = Math.max(corePoolSize, properties.getAsyncMaxPoolSize());
        int queueCapacity = Math.max(1, properties.getAsyncQueueCapacity());
        long keepAliveSeconds = Math.max(1, properties.getAsyncKeepAliveSeconds());
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("solomon-notice-sender-" + THREAD_INDEX.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private void resolveTemplate(NoticeMessage message) {
        if (ObjectUtil.isEmpty(message.getTemplateCode())) {
            return;
        }
    }

    private List<NoticeChannelEnum> defaultChannels() {
        return List.of(NoticeChannelEnum.WECHAT_WORK);
    }
}
