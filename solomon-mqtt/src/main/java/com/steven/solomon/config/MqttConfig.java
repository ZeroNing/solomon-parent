package com.steven.solomon.config;

import com.steven.solomon.annotation.Mqtt;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
@IntegrationComponentScan
@Order(2)
@DependsOn("springUtil")
public class MqttConfig {

  private final MqttProfile profile;

  public MqttConfig(MqttProfile profile) {this.profile = profile;}

  @Bean
  public MqttConnectOptions getMqttConnectOptions() throws UnsupportedEncodingException, MqttException {
    MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
    mqttConnectOptions.setUserName(profile.getUserName());
    mqttConnectOptions.setPassword(profile.getPassword().toCharArray());
    mqttConnectOptions.setServerURIs(new String[]{profile.getUrl()});
    //设置超时时间
    mqttConnectOptions.setConnectionTimeout(profile.getCompletionTimeout());
    //设置自动重连
    mqttConnectOptions.setAutomaticReconnect(profile.getAutomaticReconnect());
    //cleanSession 设为 true;当客户端掉线时;服务器端会清除 客户端session;重连后 客户端会有一个新的session,cleanSession
    // 设为false，客户端掉线后 服务器端不会清除session，当重连后可以接收之前订阅主题的消息。当客户端上线后会接受到它离线的这段时间的消息
    mqttConnectOptions.setCleanSession(profile.getCleanSession());
    // 设置会话心跳时间 单位为秒   设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送心跳判断客户端是否在线，但这个方法并没有重连的机制
    mqttConnectOptions.setKeepAliveInterval(profile.getKeepAliveInterval());
    return mqttConnectOptions;
  }

  @Bean
  public MqttClient client(MqttConnectOptions options) throws MqttException {
    MqttClient mqttClient = new MqttClient(profile.getUrl(),profile.getClientId());
    List<Object> clazzList = new ArrayList<>(SpringUtil.getBeansWithAnnotation(Mqtt.class).values());
    mqttClient.connect(options);

    if(ValidateUtils.isNotEmpty(clazzList)){
      for(Object abstractConsumer : clazzList){
        Mqtt mqtt = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), Mqtt.class);

        if(ValidateUtils.isEmpty(mqtt)){
          continue;
        }
        mqttClient.subscribe(mqtt.topics(),mqtt.qos(),(AbstractConsumer)abstractConsumer);
      }
    }
    return mqttClient;
  }


  @Bean
  public MessageChannel mqttInputChannel() {
    return new DirectChannel();
  }

}
