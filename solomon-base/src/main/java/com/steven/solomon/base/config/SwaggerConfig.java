package com.steven.solomon.base.config;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration(proxyBeanMethods = false)
@EnableSwagger2WebMvc
public class SwaggerConfig {

  @Value("${knife4j.title:swagger文档}")
  private String title;

  @Value("${knife4j.open:false}")
  private boolean open;

  @Bean
  @ConditionalOnMissingBean(Docket.class)
  public Docket createRestApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(apiInfo())
        //是否开启 (true 开启  false隐藏。生产环境建议隐藏)
        .enable(open)
        .select()
        //扫描的路径包,设置basePackage会将包下的所有被@Api标记类的所有方法作为api
        .apis(RequestHandlerSelectors.any())
        //指定路径处理PathSelectors.any()代表所有的路径
        .paths(PathSelectors.any())
        .build();
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        //设置文档标题(API名称)
        .title(title)
        //版本号
        .version(getGitVersion())
        .build();
  }

  private String getGitVersion() {
    String version = "1.0.0";
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      URL         resource    = classLoader.getResource("git.properties");
      if (resource != null) {
        Properties props = new Properties();
        props.load(resource.openStream());

        String abbrev= props.getProperty("git.commit.id.abbrev");
        String time=props.getProperty("git.build.time");
//        String user = props.getProperty("git.build.user.name");
        version = time + "-" + abbrev ;
      }
    } catch (Throwable e) {
      version = "1.0.0";
    }
    return version;
  }
}
