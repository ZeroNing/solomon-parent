package com.steven.solomon.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.steven.solomon.code.BotNoticeErrorCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.pojo.entity.NoteRequestVO;
import com.steven.solomon.pojo.entity.SendBaseNoteMessage;
import com.steven.solomon.pojo.enums.BotClientEnum;
import com.steven.solomon.profile.BotNoteProfile;
import com.steven.solomon.verification.ValidateUtils;
import com.taobao.api.TaobaoResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@EnableConfigurationProperties(value = {BotNoteProfile.class})
public class BotNoteUtils {

    private final BotNoteProfile.WeComNotProfile weComProfile;

    private final BotNoteProfile.DingTalkNotProfile dingTalkProfile;

    public BotNoteUtils(BotNoteProfile profile) {
        this.weComProfile = profile.getWeCom();
        this.dingTalkProfile = profile.getDingTalk();
    }

    public NoteRequestVO sendBotNote(BotClientEnum client, SendBaseNoteMessage message) throws Exception {
        //获取机器人客户端链接
        String url = getUrl(client);
        NoteRequestVO noteRequestVO = null;
        if(StrUtil.equalsIgnoreCase(BotClientEnum.WECHAT.name(),client.name())){
            noteRequestVO = sendWeChatNote(message, url);
        } else if(StrUtil.equalsIgnoreCase(BotClientEnum.DING_TALK.name(),client.name())){
            noteRequestVO = sendDingTalkNote(message, url);
        } else {
            throw new BaseException(BotNoticeErrorCode.BOT_CLIENT_NOT_EXIST,client.name());
        }
        return noteRequestVO;
    }

    private String getUrl(BotClientEnum client) throws Exception {
        String url = null;
        if(StrUtil.equalsIgnoreCase(BotClientEnum.WECHAT.name(),client.name())){
            url = weComProfile.getUrl();
        } else if(StrUtil.equalsIgnoreCase(BotClientEnum.DING_TALK.name(),client.name())){
            url = dingTalkProfile.getWebUrl();
        } else {
            throw new BaseException(BotNoticeErrorCode.BOT_CLIENT_NOT_EXIST,client.name());
        }
        return url;
    }

    private NoteRequestVO sendWeChatNote(SendBaseNoteMessage message,String url) throws Exception {

        SendBaseNoteMessage.SendLinkMessage linkMessage = message.getLink();
        SendBaseNoteMessage.SendAtMessage sendAtMessage = message.getAt();
        SendBaseNoteMessage.SendTextMessage textMessage = message.getText();
        SendBaseNoteMessage.SendMarkdownMessage markdownMessage= message.getMarkdown();

        Map<String, Object> map = new HashMap<>();
        if(ValidateUtils.isNotEmpty(textMessage)){
            map.put("msgtype", "text");
            Map<String,Object> detailMap = new HashMap<>();
            detailMap.put("content", textMessage.getMessage());
            if(ValidateUtils.isNotEmpty(sendAtMessage)){
                List<String> metionedList = ValidateUtils.getOrDefault(sendAtMessage.getAtUserIds(),new ArrayList<>());
                List<String> metionedMobileList = ValidateUtils.getOrDefault(sendAtMessage.getAtMobiles(),new ArrayList<>());
                if(sendAtMessage.getAtAll()){
                    metionedList.add("@all");
                }
                if(ValidateUtils.isNotEmpty(metionedList)){
                    detailMap.put("mentioned_list", metionedList);
                }
                if(ValidateUtils.isNotEmpty(metionedMobileList)){
                    detailMap.put("mentioned_mobile_list", metionedMobileList);
                }
            }
            map.put("text", detailMap);
        }

        if(ValidateUtils.isNotEmpty(linkMessage)){
            map.put("msgtype", "news");

            Map<String,Object> detailMap = new HashMap<>();
            Map<String,Object> articlesMap = new HashMap<>();
            articlesMap.put("title", linkMessage.getTitle());
            articlesMap.put("description", linkMessage.getText());
            articlesMap.put("url", linkMessage.getMessageUrl());
            articlesMap.put("picurl", linkMessage.getPicUrl());
            detailMap.put("articles", List.of(articlesMap));
            map.put("news", detailMap);
        }

        if(ValidateUtils.isNotEmpty(markdownMessage)){
            map.put("msgtype", "markdown");

            Map<String,Object> detailMap = new HashMap<>();
            detailMap.put("content", markdownMessage.getTitle());
            map.put("markdown", detailMap);
        }

        HttpResponse response = HttpRequest.post(url).body(JackJsonUtils.formatJsonByFilter(map)).execute();
        if(ValidateUtils.isNotEmpty(response)){
            response.close();
        }
        return new NoteRequestVO(response.isOk(),response.body());
    }

    public NoteRequestVO sendDingTalkNote(SendBaseNoteMessage message,String url) throws Exception {
        DingTalkClient client = new DefaultDingTalkClient(url);
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
