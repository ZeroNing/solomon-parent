package steven.handler;

import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.entity.SysBrokersClients;

@MessageListener(topics = "$SYS/brokers/+/clients/#",qos = 2)
public class BrokersConsumer extends AbstractConsumer<SysBrokersClients,String> {
    @Override
    public String handleMessage(SysBrokersClients body) throws Exception {
        if(null!=body.getConnectedAt() && null!=body.getDisconnectedAt()){
            logger.info("设备ClientId:{}已离线！",body.getClientId());
        }
        if (null!=body.getConnectedAt() && null ==body.getDisconnectedAt()){
            logger.info("设备ClientId:{}上线啦！",body.getClientId());
        }
        return "";
    }

    @Override
    public void saveLog(String result, Throwable throwable, MqttModel<SysBrokersClients> model) {

    }
}
