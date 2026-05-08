package com.steven.solomon.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.http.HttpUtil;
import com.steven.solomon.config.NoticeProperties;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;
import com.steven.solomon.enums.NoticeMsgTypeEnum;
import com.steven.solomon.service.NoticeService;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 钉钉通知服务实现
 */
@Service
public class DingTalkServiceImpl implements NoticeService {

    private final Logger logger = LoggerUtils.logger(getClass());

    private final NoticeProperties properties;

    public DingTalkServiceImpl(NoticeProperties properties) {
        this.properties = properties;
    }

    @Override
    public NoticeChannelEnum getChannel() {
        return NoticeChannelEnum.DING_TALK;
    }

    @Override
    public boolean send(NoticeMessage message) throws Exception {
        NoticeProperties.DingTalk config = properties.getDingTalk();
        if (config == null || StrUtil.isEmpty(config.getWebhookUrl())) {
            logger.warn("钉钉未配置，跳过发送");
            return false;
        }

        // 构建签名URL
        String url = buildSignedUrl(config);

        // 构建请求体
        Map<String, Object> body = buildRequestBody(message, config);

        // 发送请求
        String response = HttpUtil.post(url, JSONUtil.toJsonStr(body));
        logger.info("钉钉发送结果: {}", response);

        return true;
    }

    /**
     * 构建签名URL
     */
    private String buildSignedUrl(NoticeProperties.DingTalk config) throws Exception {
        String url = config.getWebhookUrl();
        if (StrUtil.isEmpty(config.getSecret())) {
            return url;
        }

        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + config.getSecret();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(config.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);

        return url + "&timestamp=" + timestamp + "&sign=" + sign;
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(NoticeMessage message, NoticeProperties.DingTalk config) {
        Map<String, Object> body = new HashMap<>();
        NoticeMsgTypeEnum msgType = message.getMsgType();
        body.put("msgtype", StrUtil.equalsAnyIgnoreCase(msgType.getCode(),NoticeMsgTypeEnum.CARD.getCode()) ? NoticeMsgTypeEnum.ACTION_CARD.getCode() : msgType.getCode());

        // 根据消息类型构建不同的内容
        switch (msgType) {
            case TEXT:
                body.put("text", buildTextContent(message));
                break;
            case MARKDOWN:
                Map<String, Object> markdown = new HashMap<>();
                markdown.put("title", message.getTitle());
                markdown.put("text", buildMarkdownContent(message));
                body.put("markdown", markdown);
                break;
            case IMAGE:
                body.put("image", buildImageContent(message));
                break;
            case FILE:
                body.put("file", buildFileContent(message));
                break;
            case VOICE:
                body.put("voice", buildVoiceContent(message));
                break;
            case LINK:
                body.put("link", buildLinkContent(message));
                break;
            case ACTION_CARD, CARD:
                body.put("actionCard", buildActionCardContent(message));
                break;
            case FEED_CARD:
                body.put("feedCard", buildFeedCardContent(message));
                break;
            default:
                // 默认使用Markdown
                body.put("msgtype", "markdown");
                Map<String, Object> defaultMarkdown = new HashMap<>();
                defaultMarkdown.put("title", message.getTitle());
                defaultMarkdown.put("text", buildMarkdownContent(message));
                body.put("markdown", defaultMarkdown);
        }

        // 处理@用户（仅文本和Markdown支持@）
        if (msgType == NoticeMsgTypeEnum.TEXT || msgType == NoticeMsgTypeEnum.MARKDOWN) {
            Map<String, Object> at = new HashMap<>();
            // 完全使用消息中传入的atAll配置
            if (Boolean.TRUE.equals(message.getAtAll())) {
                at.put("isAtAll", true);
            }
            // 完全使用消息中传入的atUsers配置（钉钉atUsers为手机号列表）
            if (message.getAtUsers() != null && !message.getAtUsers().isEmpty()) {
                at.put("atMobiles", message.getAtUsers());
            }
            body.put("at", at);
        }

        return body;
    }

    /**
     * 构建文本消息
     */
    private Map<String, Object> buildTextContent(NoticeMessage message) {
        Map<String, Object> text = new HashMap<>();
        StringBuilder content = new StringBuilder(message.getContent());
        // 添加全局签名
        if (properties.isGlobalSignature()) {
            content.append("\n> ").append(properties.getSignature());
        }
        text.put("content", content.toString());
        return text;
    }

    /**
     * 构建图片消息
     */
    private Map<String, Object> buildImageContent(NoticeMessage message) {
        Map<String, Object> image = new HashMap<>();
        image.put("media_id", message.getMediaId());
        return image;
    }

    /**
     * 构建文件消息
     */
    private Map<String, Object> buildFileContent(NoticeMessage message) {
        Map<String, Object> file = new HashMap<>();
        file.put("media_id", message.getMediaId());
        return file;
    }

    /**
     * 构建语音消息
     */
    private Map<String, Object> buildVoiceContent(NoticeMessage message) {
        Map<String, Object> voice = new HashMap<>();
        voice.put("media_id", message.getMediaId());
        voice.put("duration", 10); // 默认10秒，可扩展
        return voice;
    }

    /**
     * 构建链接消息
     */
    private Map<String, Object> buildLinkContent(NoticeMessage message) {
        Map<String, Object> link = new HashMap<>();
        link.put("title", message.getTitle());
        link.put("text", message.getContent());
        link.put("messageUrl", message.getLinkUrl());
        link.put("picUrl", message.getLinkPicUrl());
        return link;
    }

    /**
     * 构建交互卡片消息
     */
    private Map<String, Object> buildActionCardContent(NoticeMessage message) {
        Map<String, Object> card = new HashMap<>();
        card.put("title", message.getTitle());
        card.put("text", buildMarkdownContent(message));
        card.put("btn_orientation", "0"); // 0-按钮垂直排列 1-横向

        // 添加按钮
        if (message.getButtons() != null && !message.getButtons().isEmpty()) {
            if (message.getButtons().size() == 1) {
                // 单个按钮
                NoticeMessage.Button button = message.getButtons().get(0);
                card.put("singleTitle", button.getTitle());
                card.put("singleURL", button.getActionUrl());
            } else {
                // 多个按钮
                List<Map<String, Object>> btns = new ArrayList<>();
                for (NoticeMessage.Button button : message.getButtons()) {
                    Map<String, Object> btn = new HashMap<>();
                    btn.put("title", button.getTitle());
                    btn.put("actionURL", button.getActionUrl());
                    btns.add(btn);
                }
                card.put("btns", btns);
            }
        }

        return card;
    }

    /**
     * 构建Feed流卡片消息
     */
    private Map<String, Object> buildFeedCardContent(NoticeMessage message) {
        Map<String, Object> feedCard = new HashMap<>();
        if (message.getFeedItems() != null && !message.getFeedItems().isEmpty()) {
            List<Map<String, Object>> links = new ArrayList<>();
            for (NoticeMessage.FeedItem item : message.getFeedItems()) {
                Map<String, Object> link = new HashMap<>();
                link.put("title", item.getTitle());
                link.put("messageURL", item.getMessageUrl());
                link.put("picURL", item.getPicUrl());
                links.add(link);
            }
            feedCard.put("links", links);
        }
        return feedCard;
    }

    /**
     * 构建Markdown内容
     */
    private String buildMarkdownContent(NoticeMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append(message.getLevel().getEmoji()).append(" **").append(message.getTitle()).append("**\n\n");
        sb.append(message.getContent()).append("\n\n");

        // 添加全局签名
        if (properties.isGlobalSignature()) {
            sb.append("\n> ").append(properties.getSignature());
        }

        return sb.toString();
    }
}
