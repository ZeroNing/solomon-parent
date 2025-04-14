//package com.steven.solomon.base.config;
//
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Properties;
//
//import cn.hutool.core.collection.ListUtil;
//import cn.hutool.core.util.ObjectUtil;
//import com.steven.solomon.base.profile.SwaggerProfile;
//import io.swagger.v3.oas.models.media.StringSchema;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//
//@Configuration
//@Enable
//@EnableConfigurationProperties(value={SwaggerProfile.class,})
//public class SwaggerConfig {
//
//  private final SwaggerProfile profile;
//
//  public SwaggerConfig(SwaggerProfile profile) {
//      this.profile = profile;
//  }
//
//  @Bean
//  @ConditionalOnMissingBean(Docket.class)
//  public Docket createRestApi() {
//    List<RequestParameter> requestParameters = new ArrayList<>();
//    if(ObjectUtil.isNotEmpty(profile.getGlobalRequestParameters())){
//      // 创建必要参数对象
//      for(SwaggerProfile.DocRequestParameter requestParameter: profile.getGlobalRequestParameters()){
//        requestParameters.add(new RequestParameterBuilder().name(requestParameter.getName())
//                .in(requestParameter.getIn())
//                .description(requestParameter.getDescription())
//                .required(requestParameter.getRequired())
//                .hidden(requestParameter.getHidden())
//                .build());
//      }
//    }
//    return new Docket(DocumentationType.SWAGGER_2)
//        .apiInfo(apiInfo())
//        //是否开启 (true 开启  false隐藏。生产环境建议隐藏)
//        .enable(profile.getEnabled())
//        .select()
//        //扫描的路径包,设置basePackage会将包下的所有被@Api标记类的所有方法作为api
//        .apis(RequestHandlerSelectors.any())
//        //指定路径处理PathSelectors.any()代表所有的路径
//        .paths(PathSelectors.any())
//        .build().globalRequestParameters(requestParameters);
//  }
//
//  private ApiInfo apiInfo() {
//    return new ApiInfoBuilder()
//        //设置文档标题(API名称)
//        .title(profile.getTitle())
//        //版本号
//        .version(getGitVersion())
//        .build();
//  }
//
//  private String getGitVersion() {
//    String version = "1.0.0";
//    try {
//      ClassLoader classLoader = getClass().getClassLoader();
//      URL         resource    = classLoader.getResource("git.properties");
//      if (resource != null) {
//        Properties props = new Properties();
//        props.load(resource.openStream());
//
//        String abbrev= props.getProperty("git.commit.id.abbrev");
//        String time=props.getProperty("git.build.time");
////        String user = props.getProperty("git.build.user.name");
//        version = time + "-" + abbrev ;
//      }
//    } catch (Throwable e) {
//      version = "1.0.0";
//    }
//    return version;
//  }
//}
