package steven.handler;

import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.entity.MqttModel;

@MessageListener(topics = "top/+/123")
public class TestHandler extends AbstractConsumer<String,String> {

    @Override
    public String handleMessage(String body) throws Exception {
        return "";
    }

    @Override
    public void saveLog(String result, Throwable throwable, MqttModel<String> model) {

    }
}

