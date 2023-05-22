package com.steven.solomon.config;

import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@DependsOn({"SpringUtil"})
public class MybatisConfig {
}
