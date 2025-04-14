package com.steven.solomon.base.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
// 显式指定扫描工具包路径
@ComponentScan("com.steven.solomon")
public class ToolPackageAutoConfiguration {
}
