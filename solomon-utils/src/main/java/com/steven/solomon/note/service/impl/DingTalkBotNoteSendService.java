package com.steven.solomon.note.service.impl;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.steven.solomon.note.entity.NoteRequestVO;
import com.steven.solomon.note.entity.SendBaseNoteMessage;
import com.steven.solomon.note.profile.NoteProfile;
import com.steven.solomon.note.service.BotNoteSendService;
import com.steven.solomon.verification.ValidateUtils;
import com.taobao.api.TaobaoResponse;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
@EnableConfigurationProperties(value={NoteProfile.class})
public class DingTalkBotNoteSendService implements BotNoteSendService {

    private final NoteProfile.DingTalkNotProfile profile;

    public DingTalkBotNoteSendService(NoteProfile profile) {
        this.profile = profile.getDingTalk();
    }

    @Override
    public String getUrl() throws Exception {
        String secret = profile.getSecret();
        long timestamp = Instant.now().toEpochMilli();
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        return profile.getUrl() + "&timestamp=" + timestamp + "&sign=" + sign;
    }

    @Override
    public NoteRequestVO sendNote(SendBaseNoteMessage message) throws Exception {
        DingTalkClient client = new DefaultDingTalkClient(getUrl());
        OapiRobotSendRequest request = new OapiRobotSendRequest();

        SendBaseNoteMessage.SendLinkMessage linkMessage = message.getLink();
        SendBaseNoteMessage.SendAtMessage sendAtMessage = message.getAt();
        SendBaseNoteMessage.SendTextMessage textMessage = message.getText();
        SendBaseNoteMessage.SendMarkdownMessage markdownMessage= message.getMarkdown();
        //设置需要@的人处理
        if(ValidateUtils.isNotEmpty(sendAtMessage)){
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setIsAtAll(sendAtMessage.getAtAll());
            at.setAtMobiles(sendAtMessage.getAtMobiles());
            at.setAtUserIds(sendAtMessage.getAtUserIds());
            request.setAt(at);
        }
        if(ValidateUtils.isNotEmpty(textMessage)){
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            text.setContent(textMessage.getMessage());
            request.setText(text);
            request.setMsgtype("text");
        }
        if(ValidateUtils.isNotEmpty(linkMessage)){
            OapiRobotSendRequest.Link link = new OapiRobotSendRequest.Link();
            link.setText(linkMessage.getText());
            link.setMessageUrl(linkMessage.getMessageUrl());
            link.setTitle(linkMessage.getTitle());
            link.setPicUrl(linkMessage.getPicUrl());
            request.setLink(link);
            request.setMsgtype("link");
        }
        if(ValidateUtils.isNotEmpty(markdownMessage)){
            OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
            markdown.setText(markdownMessage.getText());
            markdown.setTitle(markdownMessage.getTitle());
            request.setMarkdown(markdown);
            request.setMsgtype("markdown");
        }
        TaobaoResponse response = client.execute(request);
        return new NoteRequestVO(response.isSuccess(),response.getBody());
    }
}
