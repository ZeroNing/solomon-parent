package steven.handler;

import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.mica.consumer.AbstractConsumer;
import com.steven.solomon.mqtt.model.MqttMessageModel;

@MessageListener(topics = "top1/#")
public class Test2Handler extends AbstractConsumer<String,String> {

    @Override
    public String handleMessage(String body) throws Exception {
        return "";
    }

    @Override
    public void saveLog(String result, Throwable throwable, MqttMessageModel<String> model) {

    }
}

