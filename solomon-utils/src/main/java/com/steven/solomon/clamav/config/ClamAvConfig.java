package com.steven.solomon.clamav.config;

import com.steven.solomon.clamav.properties.ClamAvProperties;
import com.steven.solomon.clamav.utils.ClamAvUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.Platform;

@Configuration
@EnableConfigurationProperties(value = {ClamAvProperties.class})
public class ClamAvConfig {

    private final ClamAvProperties clamAVProperties;

    public ClamAvConfig(ClamAvProperties clamAVProperties) {
        this.clamAVProperties = clamAVProperties;
    }

    @Bean
    public ClamAvUtils clamAvUtils() {
        ClamavClient client = null;
        if(clamAVProperties.getEnabled()){
            client = new ClamavClient(clamAVProperties.getHost(), clamAVProperties.getPort(), clamAVProperties.getPlatform());
        }
        return new ClamAvUtils(client,clamAVProperties);
    }
}
