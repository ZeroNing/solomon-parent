package com.steven.solomon.service;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.config.NoticeProperties;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.enums.NoticeChannelEnum;
import com.steven.solomon.enums.NoticeMsgTypeEnum;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 企业微信机器人通知服务。
 */
@Service
public class WechatWorkServiceImpl extends AbstractRobotNoticeService {

    public WechatWorkServiceImpl(NoticeProperties properties) {
        super(properties);
    }

    @Override
    public NoticeChannelEnum getChannel() {
        return NoticeChannelEnum.WECHAT_WORK;
    }

    @Override
    protected String webhookUrl(NoticeMessage message) throws Exception {
        NoticeProperties.WechatWork config = properties.getWechatWork();
        if (config == null || StrUtil.isBlank(config.getWebhookUrl())) {
            return null;
        }
        if (StrUtil.isBlank(config.getSecret())) {
            return config.getWebhookUrl();
        }

        long timestamp = System.currentTimeMillis() / 1000;
        String content = timestamp + "\n" + config.getSecret();
        String sign = URLEncoder.encode(
                NoticePayloadUtils.hmacSha256Base64(content, config.getSecret()),
                StandardCharsets.UTF_8
        );
        return config.getWebhookUrl() + "&timestamp=" + timestamp + "&sign=" + sign;
    }

    @Override
    protected Map<String, Object> buildRequestBody(NoticeMessage message) {
        NoticeMsgTypeEnum msgType = message.getMsgType() == null ? NoticeMsgTypeEnum.MARKDOWN : message.getMsgType();
        Map<String, Object> body = NoticePayloadUtils.map("msgtype", msgType.getCode()).build();

        switch (msgType) {
            case TEXT:
                body.put("text", textContent(message));
                break;
            case IMAGE:
            case FILE:
                body.put(msgType.getCode(), NoticePayloadUtils.mediaIdBody(message.getMediaId()));
                break;
            case VOICE:
                body.put("voice", NoticePayloadUtils.mediaIdBody(message.getMediaId()));
                break;
            case LINK:
                body.put("msgtype", "news");
                body.put("news", newsContent(message));
                break;
            case ACTION_CARD:
            case CARD:
                body.put("msgtype", "template_card");
                body.put("template_card", templateCardContent(message));
                break;
            case MARKDOWN:
                body.put("markdown", markdownContent(message));
                break;
            default:
                body.put("msgtype", "markdown");
                body.put("markdown", markdownContent(message));
                break;
        }

        if (msgType == NoticeMsgTypeEnum.TEXT || msgType == NoticeMsgTypeEnum.MARKDOWN) {
            fillMention(body, message);
        }
        return body;
    }

    private Map<String, Object> textContent(NoticeMessage message) {
        return NoticePayloadUtils.map("content", signature(message.getContent(), "\n")).build();
    }

    private Map<String, Object> markdownContent(NoticeMessage message) {
        return NoticePayloadUtils.map("content", buildMarkdown(message)).build();
    }

    private Map<String, Object> newsContent(NoticeMessage message) {
        Map<String, Object> article = NoticePayloadUtils.map()
                .put("title", message.getTitle())
                .put("description", message.getContent())
                .put("url", message.getLinkUrl())
                .put("picurl", message.getLinkPicUrl())
                .build();
        return NoticePayloadUtils.map("articles", List.of(article)).build();
    }

    private Map<String, Object> templateCardContent(NoticeMessage message) {
        boolean hasImage = StrUtil.isNotBlank(message.getLinkPicUrl());
        Map<String, Object> card = NoticePayloadUtils.map()
                .put("card_type", hasImage ? "news_notice" : "text_notice")
                .put("main_title", mainTitle(message))
                .put("card_action", cardAction(message))
                .put("source", source(message))
                .putIf(hasImage, "card_image", cardImage(message))
                .putIf(StrUtil.isNotBlank(message.getContent()) && message.getContent().length() > 30,
                        "sub_title_text", message.getContent())
                .build();

        List<Map<String, Object>> jumpList = jumpList(message);
        if (!jumpList.isEmpty()) {
            card.put("jump_list", jumpList);
        }
        return card;
    }

    private Map<String, Object> mainTitle(NoticeMessage message) {
        return NoticePayloadUtils.map()
                .put("title", message.getTitle())
                .putIf(StrUtil.isNotBlank(message.getContent()) && message.getContent().length() <= 30,
                        "desc", message.getContent())
                .build();
    }

    private Map<String, Object> cardAction(NoticeMessage message) {
        String defaultUrl = StrUtil.blankToDefault(message.getLinkUrl(), "https://www.qq.com");
        return NoticePayloadUtils.map()
                .put("type", 1)
                .put("url", NoticePayloadUtils.firstButtonUrlOrDefault(message, defaultUrl))
                .build();
    }

    private Map<String, Object> cardImage(NoticeMessage message) {
        return NoticePayloadUtils.map()
                .put("url", message.getLinkPicUrl())
                .put("aspect_ratio", 1.78)
                .build();
    }

    private Map<String, Object> source(NoticeMessage message) {
        return NoticePayloadUtils.map()
                .put("desc", message.getLevel().getName())
                .put("desc_color", levelColor(message.getLevel().getName()))
                .build();
    }

    private List<Map<String, Object>> jumpList(NoticeMessage message) {
        List<Map<String, Object>> jumps = new ArrayList<>();
        if (!NoticePayloadUtils.hasItems(message.getButtons())) {
            return jumps;
        }

        for (NoticeMessage.Button button : message.getButtons()) {
            jumps.add(NoticePayloadUtils.map()
                    .put("type", 1)
                    .put("title", button.getTitle())
                    .put("url", button.getActionUrl())
                    .build());
            if (jumps.size() >= 3) {
                break;
            }
        }
        return jumps;
    }

    private void fillMention(Map<String, Object> body, NoticeMessage message) {
        if (Boolean.TRUE.equals(message.getAtAll())) {
            body.put("mentioned_list", List.of("@all"));
            return;
        }
        if (NoticePayloadUtils.hasItems(message.getAtUsers())) {
            body.put("mentioned_list", message.getAtUsers());
        }
    }

    /**
     * 企业微信卡片色值：0 灰色，1 黑色，2 红色，3 绿色。
     */
    private int levelColor(String levelName) {
        String level = StrUtil.nullToEmpty(levelName).toUpperCase();
        switch (level) {
            case "SUCCESS":
            case "INFO":
                return 3;
            case "WARN":
            case "WARNING":
                return 1;
            case "ERROR":
            case "FATAL":
            case "CRITICAL":
                return 2;
            default:
                return 0;
        }
    }

    private String buildMarkdown(NoticeMessage message) {
        String content = message.getLevel().getEmoji() + " **" + message.getTitle() + "**\n\n"
                + message.getContent() + "\n\n";
        return signature(content, "");
    }

    private String signature(String content, String prefix) {
        return NoticePayloadUtils.appendSignature(
                content,
                properties.isGlobalSignature(),
                properties.getSignature(),
                prefix
        );
    }
}
