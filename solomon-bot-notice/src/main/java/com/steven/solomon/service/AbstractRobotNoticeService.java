package com.steven.solomon.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.config.NoticeProperties;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;

import java.util.Map;

/**
 * 机器人通知服务基类。
 *
 * <p>统一处理 Webhook 校验、HTTP 投递和日志输出，子类只关注平台差异化报文。</p>
 */
abstract class AbstractRobotNoticeService implements NoticeService {

    protected final Logger logger = LoggerUtils.logger(getClass());

    protected final NoticeProperties properties;

    protected AbstractRobotNoticeService(NoticeProperties properties) {
        this.properties = properties;
    }

    @Override
    public final boolean send(NoticeMessage message) throws Exception {
        String webhookUrl = webhookUrl(message);
        if (StrUtil.isBlank(webhookUrl)) {
            logger.warn("{} 未配置 Webhook，跳过发送", getChannel().getName());
            return false;
        }

        Map<String, Object> requestBody = buildRequestBody(message);
        String response = HttpUtil.post(webhookUrl, JSONUtil.toJsonStr(requestBody));
        logger.info("{} 发送完成，响应：{}", getChannel().getName(), response);
        return true;
    }

    /**
     * 返回最终投递地址。需要签名的平台在这里完成 URL 签名。
     */
    protected abstract String webhookUrl(NoticeMessage message) throws Exception;

    /**
     * 构造平台请求体。
     */
    protected abstract Map<String, Object> buildRequestBody(NoticeMessage message) throws Exception;
}
