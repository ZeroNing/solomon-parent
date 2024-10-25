package steven.controller;

import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.utils.MqttUtils;
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
        utils.send(new MqttModel<String>("test","top/test/123","123"));
        return null;
    }
}
