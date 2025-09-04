package com.steven.solomon.clamav.config;

import com.steven.solomon.clamav.properties.ClamAvProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.Platform;

@Configuration
@ConditionalOnProperty(name = "clamav.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(value = {ClamAvProperties.class})
public class ClamAvConfig {

    private final ClamAvProperties clamAVProperties;

    public ClamAvConfig(ClamAvProperties clamAVProperties) {
        this.clamAVProperties = clamAVProperties;
    }

    @Bean("clamavClient")
    @ConditionalOnMissingBean(ClamavClient.class)
    @Conditional(ClamAvCondition.class)
    public ClamavClient clamavClient() {
        return new ClamavClient(clamAVProperties.getHost(), clamAVProperties.getPort(), Platform.UNIX);
    }
}
