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
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 飞书通知服务实现
 */
@Service
public class FeishuServiceImpl implements NoticeService {

    private final Logger logger = LoggerUtils.logger(getClass());

    private final NoticeProperties properties;

    public FeishuServiceImpl(NoticeProperties properties) {
        this.properties = properties;
    }

    @Override
    public NoticeChannelEnum getChannel() {
        return NoticeChannelEnum.FEISHU;
    }

    @Override
    public boolean send(NoticeMessage message) throws Exception {
        NoticeProperties.Feishu config = properties.getFeishu();
        if (ValidateUtils.isEmpty(config) || StrUtil.isBlank(config.getWebhookUrl())) {
            logger.warn("飞书未配置，跳过发送");
            return false;
        }

        // 构建请求体
        Map<String, Object> body = buildRequestBody(message, config);

        // 发送请求
        String response = HttpUtil.post(config.getWebhookUrl(), JSONUtil.toJsonStr(body));
        logger.info("飞书发送结果: {}", response);

        return true;
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(NoticeMessage message, NoticeProperties.Feishu config) {
        Map<String, Object> body = new HashMap<>();
        NoticeMsgTypeEnum msgType = message.getMsgType();

        // 处理签名（如果配置了secret）
        if (StrUtil.isNotBlank(config.getSecret())) {
            long timestamp = System.currentTimeMillis() / 1000;
            body.put("timestamp", String.valueOf(timestamp));
            body.put("sign", generateSign(timestamp, config.getSecret()));
        }

        // 根据消息类型构建不同的内容
        Map<String, Object> content = new HashMap<>();
        switch (msgType) {
            case TEXT:
                body.put("msg_type", "text");
                content.put("text", buildTextContent(message, config));
                break;
            case MARKDOWN:
                body.put("msg_type", "post");
                content.put("post", buildMarkdownContent(message, config));
                break;
            case IMAGE:
                body.put("msg_type", "image");
                content.put("image_key", message.getMediaId());
                break;
            case FILE:
                body.put("msg_type", "file");
                content.put("file_key", message.getMediaId());
                break;
            case LINK:
                body.put("msg_type", "post");
                content.put("post", buildLinkContent(message, config));
                break;
            case ACTION_CARD:
            case CARD:
                body.put("msg_type", "interactive");
                content.putAll(buildCardContent(message));
                break;
            default:
                // 默认使用Markdown
                body.put("msg_type", "post");
                content.put("post", buildMarkdownContent(message, config));
        }

        body.put("content", content);
        return body;
    }

    /**
     * 构建文本消息
     */
    private String buildTextContent(NoticeMessage message, NoticeProperties.Feishu config) {
        StringBuilder content = new StringBuilder(message.getContent());
        
        // 完全使用消息中传入的@配置
        if (Boolean.TRUE.equals(message.getAtAll())) {
            content.append(" <at user_id=\"all\">所有人</at>");
        }
        if (NoticePayloadUtils.hasItems(message.getAtUsers())) {
            for (String userId : message.getAtUsers()) {
                content.append(" <at user_id=\"").append(userId).append("\"></at>");
            }
        }
        
        return NoticePayloadUtils.appendSignature(
                content.toString(), properties.isGlobalSignature(), properties.getSignature(), "\n"
        );
    }

    /**
     * 构建Markdown（富文本）消息
     */
    private Map<String, Object> buildMarkdownContent(NoticeMessage message, NoticeProperties.Feishu config) {
        Map<String, Object> post = new HashMap<>();
        Map<String, Object> zhCn = new HashMap<>();
        zhCn.put("title", message.getTitle());

        // 构建内容
        List<List<Map<String, Object>>> contentList = new ArrayList<>();
        List<Map<String, Object>> line = new ArrayList<>();

        // 内容文本
        Map<String, Object> text = new HashMap<>();
        text.put("tag", "text");
        text.put("text", message.getContent() + "\n");
        line.add(text);

        // 完全使用消息中传入的@配置
        if (Boolean.TRUE.equals(message.getAtAll())) {
            Map<String, Object> atAll = new HashMap<>();
            atAll.put("tag", "at");
            atAll.put("user_id", "all");
            atAll.put("user_name", "所有人");
            line.add(atAll);
        }
        if (NoticePayloadUtils.hasItems(message.getAtUsers())) {
            for (String userId : message.getAtUsers()) {
                Map<String, Object> at = new HashMap<>();
                at.put("tag", "at");
                at.put("user_id", userId);
                line.add(at);
            }
        }

        // 富文本签名必须作为独立 text 节点，避免破坏飞书 post 消息结构。
        if (properties.isGlobalSignature() && StrUtil.isNotBlank(properties.getSignature())) {
            Map<String, Object> sign = new HashMap<>();
            sign.put("tag", "text");
            sign.put("text", "\n" + properties.getSignature());
            line.add(sign);
        }

        contentList.add(line);
        zhCn.put("content", contentList);
        post.put("zh_cn", zhCn);
        return post;
    }

    /**
     * 构建链接消息
     */
    private Map<String, Object> buildLinkContent(NoticeMessage message, NoticeProperties.Feishu config) {
        Map<String, Object> post = new HashMap<>();
        Map<String, Object> zhCn = new HashMap<>();
        zhCn.put("title", message.getTitle());

        List<List<Map<String, Object>>> contentList = new ArrayList<>();
        List<Map<String, Object>> line = new ArrayList<>();

        // 内容
        Map<String, Object> text = new HashMap<>();
        text.put("tag", "text");
        text.put("text", message.getContent() + "\n");
        line.add(text);

        // 链接
        Map<String, Object> link = new HashMap<>();
        link.put("tag", "a");
        link.put("text", "点击查看详情");
        link.put("href", message.getLinkUrl());
        line.add(link);

        // 图片（如果有）
        if (StrUtil.isNotEmpty(message.getLinkPicUrl())) {
            List<Map<String, Object>> imgLine = new ArrayList<>();
            Map<String, Object> img = new HashMap<>();
            img.put("tag", "img");
            img.put("image_key", message.getLinkPicUrl());
            imgLine.add(img);
            contentList.add(imgLine);
        }

        contentList.add(line);
        zhCn.put("content", contentList);
        post.put("zh_cn", zhCn);
        return post;
    }

    /**
     * 构建交互卡片消息
     */
    private Map<String, Object> buildCardContent(NoticeMessage message) {
        Map<String, Object> card = new HashMap<>();

        // 卡片配置
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("wide_screen_mode", true);
        card.put("config", configMap);

        // 卡片头部
        Map<String, Object> header = new HashMap<>();
        header.put("title", Map.of(
                "tag", "plain_text",
                "content", message.getTitle()
        ));
        header.put("template", "blue"); // 蓝色主题
        card.put("header", header);

        // 卡片内容
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        content.put("tag", "div");
        content.put("text", Map.of(
                "tag", "lark_md",
                "content", message.getContent()
        ));
        elements.add(content);

        // 添加按钮
        if (NoticePayloadUtils.hasItems(message.getButtons())) {
            Map<String, Object> action = new HashMap<>();
            action.put("tag", "action");
            List<Map<String, Object>> actions = new ArrayList<>();

            for (NoticeMessage.Button button : message.getButtons()) {
                Map<String, Object> btn = new HashMap<>();
                btn.put("tag", "button");
                btn.put("text", Map.of(
                        "tag", "plain_text",
                        "content", button.getTitle()
                ));
                btn.put("type", "primary");
                btn.put("url", button.getActionUrl());
                actions.add(btn);
            }

            action.put("actions", actions);
            elements.add(action);
        }

        card.put("elements", elements);
        return Map.of("card", card);
    }

    /**
     * 生成飞书签名
     */
    private String generateSign(long timestamp, String secret) {
        try {
            String stringToSign = timestamp + "\n" + secret;
            return NoticePayloadUtils.hmacSha256Base64("", stringToSign);
        } catch (Exception e) {
            logger.error("生成飞书签名失败", e);
            return "";
        }
    }
}
