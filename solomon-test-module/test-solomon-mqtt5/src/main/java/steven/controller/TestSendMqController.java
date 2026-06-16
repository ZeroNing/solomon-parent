package steven.controller;

import com.steven.solomon.mqtt.model.MqttMessageModel;
import com.steven.solomon.mqtt.v5.utils.MqttUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSendMqController {

    private final MqttUtils utils;

    public TestSendMqController(MqttUtils utils) {
        this.utils = utils;
    }

    @GetMapping("/test")
    public Object test() throws Exception {
        utils.send(new MqttMessageModel<String>("test","top/test/123","123"));
        return null;
    }
}
