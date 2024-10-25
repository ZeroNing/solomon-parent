package com.steven.controller;

import com.steven.solomon.entity.RabbitMqModel;
import com.steven.solomon.utils.RabbitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TestSendMqController {

    private final RabbitUtils utils;

    public TestSendMqController(RabbitUtils utils) {
        this.utils = utils;
    }

    @GetMapping("/test")
    public Object test() throws Exception {
        //发送消息到A交换器 直连模式
        utils.send(new RabbitMqModel<String>("A","A"));
        //发送消息到B交换器 广播模式
        utils.send(new RabbitMqModel<String>("B","B"));
        //发送消息到C交换器 主题模式
        utils.send(new RabbitMqModel<String>("C","TEST.USER","C"));
        //发送消息到C交换器 主题模式
        utils.send(new RabbitMqModel<String>("C","TEST.TEST","C"));
        //发送消息到D交换器 直连队列 死信队列
        utils.send(new RabbitMqModel<String>("D","D"));
        //发送消息到E交换器 请求回应
        RabbitMqModel<String> rabbitMqModel = new RabbitMqModel<String>("E","G","G");
        rabbitMqModel.setReplyTo("G");
        return utils.convertSendAndReceive(rabbitMqModel);
    }
}
