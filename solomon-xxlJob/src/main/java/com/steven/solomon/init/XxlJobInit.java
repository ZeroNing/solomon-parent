package com.steven.solomon.init;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.entity.XxlJobInfo;
import com.steven.solomon.properties.XxlJobProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Import(XxlJobProperties.class)
public class XxlJobInit extends AbstractMessageLineRunner<JobTask> {

    private final XxlJobProperties profile;

    public XxlJobInit(ApplicationContext applicationContext, XxlJobProperties profile) {
        this.profile = profile;
        SpringUtil.setContext(applicationContext);
    }

    @Override
    public void init(List<Object> clazzList) throws Exception {
        if(!profile.getEnabled()){
            logger.error("xxl-Job不启用,不初始化定时任务");
            return;
        }
        String cookie = login();
        String adminAddresses = profile.getAdminAddresses();
        if(!adminAddresses.endsWith("/")){
            adminAddresses = adminAddresses + "/";
        }
        String url = adminAddresses + "jobinfo/add";
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        for(Object obj : clazzList){
            Class<?> clazz = obj.getClass();
            JobTask jobTask = AnnotationUtil.getAnnotation(clazz, JobTask.class);
            if(ValidateUtils.isEmpty(jobTask)){
                logger.error("{}没有JobTask注解,不进行初始化",obj.getClass().getSimpleName());
                continue;
            }
            XxlJobInfo xxlJobInfo = new XxlJobInfo(jobTask);
            // 发送 POST 请求
            HttpResponse response = HttpUtil.createPost(url)
                    .form(JSONUtil.toBean(JSONUtil.toJsonStr(xxlJobInfo), new TypeReference<Map<String,Object>>() {},true))
                    .cookie(cookie) // 需要替换为实际的认证信息
                    .execute();
            XxlJobSpringExecutor.registJobHandler(jobTask.executorHandler(), (IJobHandler) obj);
            if(!response.isOk()){
                throw new Exception("xxl-job初始化任务失败");
            }
        }
        xxlJobSpringExecutor.start();
    }

    private String login() throws Exception {
        String adminAddresses = profile.getAdminAddresses();
        String userName = profile.getUserName();
        String password = profile.getPassword();
        if(ValidateUtils.isEmpty(adminAddresses)){
            throw new Exception("xxl-job的网页url不允许为空");
        }
        if(ValidateUtils.isEmpty(userName)){
            throw new Exception("xxl-job的账号不允许为空");
        }
        if(ValidateUtils.isEmpty(password)){
            throw new Exception("xxl-job的密码不允许为空");
        }

        if(!adminAddresses.endsWith("/")){
            adminAddresses = adminAddresses + "/";
        }
        String url = adminAddresses + "login";
        // 构建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userName", userName);
        paramMap.put("password", password);

        // 发送 POST 请求
        HttpResponse response = HttpUtil.createPost(url).form(paramMap).execute();
        if(!response.isOk()){
            throw new Exception("xxl-job登陆失败");
        }
        List<String> cookies = response.headerList("Set-Cookie");
        if(ValidateUtils.isEmpty(cookies)){
            throw new Exception("获取xxl-job的cookie失败");
        }
        return cookies.get(0);
    }
}
