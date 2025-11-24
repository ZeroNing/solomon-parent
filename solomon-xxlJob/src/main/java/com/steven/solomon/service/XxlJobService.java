package com.steven.solomon.service;

import com.steven.solomon.config.XxlJobCondition;
import com.steven.solomon.properties.XxlJobProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

@Service
@Conditional(XxlJobCondition.class)
@Import(XxlJobProperties.class)
public class XxlJobService extends CommonXxlJobService{

    protected XxlJobService(XxlJobProperties profile, ApplicationContext applicationContext) {
        super(profile, applicationContext);
    }

}
