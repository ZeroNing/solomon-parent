package com.steven.solomon.service;

import com.steven.solomon.config.XxlJobCondition;
import com.steven.solomon.properties.XxlJobProperties;
import com.steven.solomon.properties.XxlJobRegisterProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

@Service
@Conditional(XxlJobCondition.class)
@Import({XxlJobProperties.class, XxlJobRegisterProperties.class})
public class XxlJobService extends CommonXxlJobService{

    protected XxlJobService(XxlJobProperties profile, XxlJobRegisterProperties registerProperties, ApplicationContext applicationContext) {
        super(profile, registerProperties, applicationContext);
    }

}
