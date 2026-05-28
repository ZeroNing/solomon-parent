package com.steven.solomon.service;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.config.NoticeProperties;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;
import com.steven.solomon.enums.NoticeMsgTypeEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 飞书机器人通知服务。
 *
 * <p>支持文本、Markdown、图片、文件、链接、交互卡片等多种消息类型，
 * 并实现飞书 Webhook 签名验证。</p>
 */
@Service
public class FeishuService extends AbstractRobotNoticeService {

    /**
     * 构造函数，注入通知配置属性。
     *
     * @param properties 通知配置属性
     */
    public FeishuService(NoticeProperties properties) {
        super(properties);
    }

    /**
     * 返回飞书通知渠道标识。
     *
     * @return 飞书渠道枚举
     */
    @Override
    public NoticeChannelEnum getChannel() {
        return NoticeChannelEnum.FEISHU;
    }

    /**
     * 构造飞书 Webhook 地址。
     *
     * @param message 通知消息
     * @return Webhook URL，未配置时返回 null
     */
    @Override
    protected String webhookUrl(NoticeMessage message) {
        NoticeProperties.Feishu config = properties.getFeishu();
        return config == null ? null : config.getWebhookUrl();
    }

    /**
     * 根据消息类型构造飞书请求体。
     *
     * @param message 通知消息
     * @return 飞书 API 请求体 Map
     */
    @Override
    protected Map<String, Object> buildRequestBody(NoticeMessage message) {
        NoticeProperties.Feishu config = properties.getFeishu();
        NoticeMsgTypeEnum msgType = message.getMsgType() == null ? NoticeMsgTypeEnum.MARKDOWN : message.getMsgType();
        Map<String, Object> body = NoticePayloadUtils.map().build();
        fillSign(body, config);

        Map<String, Object> content = NoticePayloadUtils.map().build();
        switch (msgType) {
            case TEXT:
                body.put("msg_type", "text");
                content.put("text", textContent(message));
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
                content.put("post", linkPost(message));
                break;
            case ACTION_CARD:
            case CARD:
                body.put("msg_type", "interactive");
                content.putAll(cardContent(message));
                break;
            case MARKDOWN:
                body.put("msg_type", "post");
                content.put("post", markdownPost(message));
                break;
            default:
                body.put("msg_type", "post");
                content.put("post", markdownPost(message));
                break;
        }

        body.put("content", content);
        return body;
    }

    private void fillSign(Map<String, Object> body, NoticeProperties.Feishu config) {
        if (config == null || StrUtil.isBlank(config.getSecret())) {
            return;
        }

        long timestamp = System.currentTimeMillis() / 1000;
        body.put("timestamp", String.valueOf(timestamp));
        body.put("sign", sign(timestamp, config.getSecret()));
    }

    private String textContent(NoticeMessage message) {
        StringBuilder content = new StringBuilder(StrUtil.nullToEmpty(message.getContent()));
        if (Boolean.TRUE.equals(message.getAtAll())) {
            content.append(" <at user_id=\"all\">所有人</at>");
        }
        if (NoticePayloadUtils.hasItems(message.getAtUsers())) {
            for (String userId : message.getAtUsers()) {
                content.append(" <at user_id=\"").append(userId).append("\"></at>");
            }
        }
        return signature(content.toString(), "\n");
    }

    private Map<String, Object> markdownPost(NoticeMessage message) {
        List<Map<String, Object>> line = new ArrayList<>();
        line.add(textNode(message.getContent() + "\n"));
        appendAtNodes(line, message);
        appendSignatureNode(line);
        return post(message.getTitle(), List.of(line));
    }

    private Map<String, Object> linkPost(NoticeMessage message) {
        List<List<Map<String, Object>>> content = new ArrayList<>();
        if (StrUtil.isNotBlank(message.getLinkPicUrl())) {
            content.add(List.of(NoticePayloadUtils.map()
                    .put("tag", "img")
                    .put("image_key", message.getLinkPicUrl())
                    .build()));
        }

        content.add(List.of(
                textNode(message.getContent() + "\n"),
                NoticePayloadUtils.map()
                        .put("tag", "a")
                        .put("text", "点击查看详情")
                        .put("href", message.getLinkUrl())
                        .build()
        ));
        return post(message.getTitle(), content);
    }

    private Map<String, Object> cardContent(NoticeMessage message) {
        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(NoticePayloadUtils.map()
                .put("tag", "div")
                .put("text", NoticePayloadUtils.map()
                        .put("tag", "lark_md")
                        .put("content", message.getContent())
                        .build())
                .build());

        if (NoticePayloadUtils.hasItems(message.getButtons())) {
            elements.add(actionElement(message.getButtons()));
        }

        Map<String, Object> card = NoticePayloadUtils.map()
                .put("config", NoticePayloadUtils.map("wide_screen_mode", true).build())
                .put("header", cardHeader(message))
                .put("elements", elements)
                .build();
        return NoticePayloadUtils.map("card", card).build();
    }

    private Map<String, Object> cardHeader(NoticeMessage message) {
        return NoticePayloadUtils.map()
                .put("title", NoticePayloadUtils.map()
                        .put("tag", "plain_text")
                        .put("content", message.getTitle())
                        .build())
                .put("template", "blue")
                .build();
    }

    private Map<String, Object> actionElement(List<NoticeMessage.Button> buttons) {
        List<Map<String, Object>> actions = new ArrayList<>();
        for (NoticeMessage.Button button : buttons) {
            actions.add(NoticePayloadUtils.map()
                    .put("tag", "button")
                    .put("text", NoticePayloadUtils.map()
                            .put("tag", "plain_text")
                            .put("content", button.getTitle())
                            .build())
                    .put("type", "primary")
                    .put("url", button.getActionUrl())
                    .build());
        }

        return NoticePayloadUtils.map()
                .put("tag", "action")
                .put("actions", actions)
                .build();
    }

    private Map<String, Object> post(String title, List<List<Map<String, Object>>> content) {
        return NoticePayloadUtils.map("zh_cn", NoticePayloadUtils.map()
                .put("title", title)
                .put("content", content)
                .build()).build();
    }

    private Map<String, Object> textNode(String text) {
        return NoticePayloadUtils.map()
                .put("tag", "text")
                .put("text", text)
                .build();
    }

    private void appendAtNodes(List<Map<String, Object>> line, NoticeMessage message) {
        if (Boolean.TRUE.equals(message.getAtAll())) {
            line.add(NoticePayloadUtils.map()
                    .put("tag", "at")
                    .put("user_id", "all")
                    .put("user_name", "所有人")
                    .build());
        }
        if (NoticePayloadUtils.hasItems(message.getAtUsers())) {
            for (String userId : message.getAtUsers()) {
                line.add(NoticePayloadUtils.map()
                        .put("tag", "at")
                        .put("user_id", userId)
                        .build());
            }
        }
    }

    private void appendSignatureNode(List<Map<String, Object>> line) {
        if (properties.isGlobalSignature() && StrUtil.isNotBlank(properties.getSignature())) {
            line.add(textNode("\n" + properties.getSignature()));
        }
    }

    private String signature(String content, String prefix) {
        return NoticePayloadUtils.appendSignature(
                content,
                properties.isGlobalSignature(),
                properties.getSignature(),
                prefix
        );
    }

    private String sign(long timestamp, String secret) {
        try {
            return NoticePayloadUtils.hmacSha256Base64("", timestamp + "\n" + secret);
        } catch (Exception e) {
            logger.error("生成飞书签名失败", e);
            return "";
        }
    }
}
