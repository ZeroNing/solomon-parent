package steven.handler;

import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.mqtt.model.MqttMessageModel;
import com.steven.solomon.mqtt.model.BrokerClientInfo;

@MessageListener(topics = "$SYS/brokers/+/clients/#",qos = 2)
public class BrokersConsumer extends AbstractConsumer<BrokerClientInfo,String> {
    @Override
    public String handleMessage(BrokerClientInfo body) throws Exception {
        if (null!=body.getConnectedAt() && null!=body.getDisconnectedAt()) {
            logger.info("设备ClientId:{}已离线！",body.getClientId());
        }
        if (null!=body.getConnectedAt() && null ==body.getDisconnectedAt()) {
            logger.info("设备ClientId:{}上线啦！",body.getClientId());
        }
        return "";
    }

    @Override
    public void saveLog(String result, Throwable throwable, MqttMessageModel<BrokerClientInfo> model) {

    }
}
