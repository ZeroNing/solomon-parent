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
 * 企业微信通知服务实现
 */
@Service
public class WechatWorkServiceImpl implements NoticeService {

    private final Logger logger = LoggerUtils.logger(getClass());

    private final NoticeProperties properties;

    public WechatWorkServiceImpl(NoticeProperties properties) {
        this.properties = properties;
    }

    @Override
    public NoticeChannelEnum getChannel() {
        return NoticeChannelEnum.WECHAT_WORK;
    }

    @Override
    public boolean send(NoticeMessage message) throws Exception {
        NoticeProperties.WechatWork config = properties.getWechatWork();
        if (config == null || StrUtil.isEmpty(config.getWebhookUrl())) {
            logger.warn("企业微信未配置，跳过发送");
            return false;
        }

        // 构建请求URL（带签名）
        String url = buildUrl(config);

        // 构建请求体
        Map<String, Object> body = buildRequestBody(message, config);

        // 发送请求
        String response = HttpUtil.post(url, JSONUtil.toJsonStr(body));
        logger.info("企业微信发送报文:{} 结果: {}",JSONUtil.toJsonStr(body), response);

        return true;
    }

    /**
     * 构建带签名的URL
     */
    private String buildUrl(NoticeProperties.WechatWork config) throws Exception {
        String url = config.getWebhookUrl();
        if (StrUtil.isEmpty(config.getSecret())) {
            return url;
        }

        long timestamp = System.currentTimeMillis() / 1000;
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
    private Map<String, Object> buildRequestBody(NoticeMessage message, NoticeProperties.WechatWork config) {
        Map<String, Object> body = new HashMap<>();
        NoticeMsgTypeEnum msgType = message.getMsgType();
        body.put("msgtype", msgType.getCode());

        // 根据消息类型构建不同的内容
        switch (msgType) {
            case TEXT:
                body.put("text", buildTextContent(message));
                break;
            case MARKDOWN:
                Map<String, Object> markdown = new HashMap<>();
                markdown.put("content", buildMarkdownContent(message));
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
                body.put("msgtype", "news");
                body.put("news", buildNewsContent(message));
                break;
            case ACTION_CARD,CARD:
                body.put("msgtype", "template_card");
                body.put("template_card", buildTemplateCardContent(message));
                break;
            default:
                // 默认使用Markdown
                body.put("msgtype", "markdown");
                Map<String, Object> defaultMarkdown = new HashMap<>();
                defaultMarkdown.put("content", buildMarkdownContent(message));
                body.put("markdown", defaultMarkdown);
        }

        // 处理@用户（仅文本和Markdown支持@）
        if (msgType == NoticeMsgTypeEnum.TEXT || msgType == NoticeMsgTypeEnum.MARKDOWN) {
            // 完全使用消息中传入的atAll配置
            if (Boolean.TRUE.equals(message.getAtAll())) {
                body.put("mentioned_list", List.of("@all"));
            }
            // 完全使用消息中传入的atUsers配置（企业微信atUsers为用户ID列表）
            else if (message.getAtUsers() != null && !message.getAtUsers().isEmpty()) {
                body.put("mentioned_list", message.getAtUsers());
            }
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
            content.append("\n").append(properties.getSignature());
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
        return voice;
    }

    /**
     * 构建图文消息
     */
    private Map<String, Object> buildNewsContent(NoticeMessage message) {
        Map<String, Object> news = new HashMap<>();
        Map<String, Object> article = new HashMap<>();
        article.put("title", message.getTitle());
        article.put("description", message.getContent());
        article.put("url", message.getLinkUrl());
        article.put("picurl", message.getLinkPicUrl());
        news.put("articles", List.of(article));
        return news;
    }

    /**
     * 构建模板卡片消息
     * 严格按照官方文档实现，自动选择卡片类型：
     * - 有图片时用 news_notice（图文展示）
     * - 无图片时用 text_notice（文本通知）
     * 文档参考：https://developer.work.weixin.qq.com/document/path/99110
     */
    private Map<String, Object> buildTemplateCardContent(NoticeMessage message) {
        Map<String, Object> card = new HashMap<>();
        
        // 自动选择卡片类型
        boolean hasImage = StrUtil.isNotEmpty(message.getLinkPicUrl());
        String cardType = hasImage ? "news_notice" : "text_notice";
        card.put("card_type", cardType);
        
        // ========== 公共必填字段 ==========
        // 1. 主标题，必填
        Map<String, Object> mainTitle = new HashMap<>();
        mainTitle.put("title", message.getTitle());
        if (StrUtil.isNotEmpty(message.getContent()) && message.getContent().length() <= 30) {
            mainTitle.put("desc", message.getContent());
        }
        card.put("main_title", mainTitle);
        
        // 2. 卡片点击动作，必填
        String clickUrl = StrUtil.isEmpty(message.getLinkUrl()) ? "https://www.qq.com" : message.getLinkUrl();
        if (message.getButtons() != null && !message.getButtons().isEmpty()) {
            clickUrl = message.getButtons().get(0).getActionUrl();
        }
        card.put("card_action", Map.of(
                "type", 1, // 1：跳转链接，必填
                "url", clickUrl // 跳转链接，必填
        ));
        
        // ========== news_notice 专属必填字段 ==========
        if (hasImage) {
            // 卡片图片，必填
            card.put("card_image", Map.of(
                    "url", message.getLinkPicUrl(),
                    "aspect_ratio", 1.78 // 16:9 宽高比，可选
            ));
        }
        
        // ========== 公共可选字段 ==========
        // 可选：二级正文内容（text_notice 必填至少有main_title.title或sub_title_text）
        if (StrUtil.isNotEmpty(message.getContent()) && message.getContent().length() > 30) {
            card.put("sub_title_text", message.getContent());
        }
        
        // 可选：跳转按钮列表（最多3个）
        if (message.getButtons() != null && !message.getButtons().isEmpty()) {
            List<Map<String, Object>> jumpList = new ArrayList<>();
            for (NoticeMessage.Button button : message.getButtons()) {
                Map<String, Object> jump = new HashMap<>();
                jump.put("type", 1); // 1：跳转链接
                jump.put("title", button.getTitle()); // 按钮文字，必填
                jump.put("url", button.getActionUrl()); // 跳转链接，必填
                jumpList.add(jump);
                if (jumpList.size() >= 3) break; // 最多3个按钮
            }
            card.put("jump_list", jumpList);
        }
        
        // 可选：卡片来源信息（显示消息等级）
        card.put("source", Map.of(
                "desc", message.getLevel().getName(),
                "desc_color", getLevelColor(message.getLevel().getName())
        ));

        return card;
    }
    
    /**
     * 根据消息等级获取对应颜色
     * 0：灰色，1：黑色，2：红色，3：绿色
     */
    private int getLevelColor(String levelName) {
        return switch (levelName.toUpperCase()) {
            case "SUCCESS", "INFO" -> 3; // 成功/普通 → 绿色
            case "WARN", "WARNING" -> 1; // 警告 → 黑色
            case "ERROR", "FATAL" -> 2; // 错误 → 红色
            default -> 0; // 其他 → 灰色
        };
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
            sb.append(properties.getSignature());
        }

        return sb.toString();
    }
}
