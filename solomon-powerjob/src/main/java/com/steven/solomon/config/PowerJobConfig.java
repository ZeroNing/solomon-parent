package com.steven.solomon.config;

import com.steven.solomon.properties.JobProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.powerjob.worker.PowerJobSpringWorker;
import tech.powerjob.worker.autoconfigure.PowerJobAutoConfiguration;
import tech.powerjob.worker.autoconfigure.PowerJobProperties;

@Configuration
@Import(value={PowerJobProperties.class, JobProperties.class})
public class PowerJobConfig {

    private final PowerJobProperties powerJobProperties;

    public PowerJobConfig(PowerJobProperties powerJobProperties) {
        this.powerJobProperties = powerJobProperties;
    }

    @Bean
    @Conditional(value = PowerJobCondition.class)
    @ConditionalOnMissingBean(PowerJobSpringWorker.class)
    public PowerJobSpringWorker initPowerJob(){
        PowerJobAutoConfiguration powerJobAutoConfiguration = new PowerJobAutoConfiguration();
        return powerJobAutoConfiguration.initPowerJob(powerJobProperties);
    }
}
