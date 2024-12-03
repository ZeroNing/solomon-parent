package com.steven.solomon.config;

import com.steven.solomon.config.component.AutoConfigurationExclusionListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MongoDbConfigurationExclusionListener extends AutoConfigurationExclusionListener {

    @Override
    public String getEnabledKey() {
        return "spring.data.mongodb.enabled";
    }

    @Override
    public String getComponentName() {
        return "MongoDb";
    }

    @Override
    public Map<String, Object> getExclude() {
        Map<String, Object> map = new HashMap<>();
        map.put("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration");
        return map;
    }
}
