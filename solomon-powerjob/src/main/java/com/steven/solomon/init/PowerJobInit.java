package com.steven.solomon.init;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.entity.JobAppVO;
import com.steven.solomon.entity.JobLoginVO;
import com.steven.solomon.entity.SaveJobInfoRequest;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.pojo.vo.ResultVO;
import com.steven.solomon.properties.JobProperties;
import com.steven.solomon.service.PowerJobService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import tech.powerjob.worker.autoconfigure.PowerJobProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Import(value = {JobProperties.class})
@Order(1)
public class PowerJobInit extends AbstractMessageLineRunner<JobTask> {

    private final PowerJobProperties powerJobProperties;

    private final PowerJobService service;

    public PowerJobInit(ApplicationContext applicationContext, PowerJobProperties powerJobProperties, PowerJobService service) {
        this.powerJobProperties = powerJobProperties;
        this.service = service;
        SpringUtil.setContext(applicationContext);
    }

    @Override
    public void init(List<Object> clazzList) throws Exception {
        if(!powerJobProperties.getWorker().isEnabled()){
            logger.error("powerJob不启用,不初始化定时任务");
            return;
        }
        //登陆获取cookie
        String cookie = service.login();
        //创建命名空间
        Integer namespaceId = service.createNamespace(powerJobProperties.getWorker().getAppName(),cookie);
        //根据appName创建namespace并返回namespaceId
        Integer appId = service.createAppId(cookie,powerJobProperties.getWorker().getAppName(),namespaceId);
        //获取全部任务
        Map<String,SaveJobInfoRequest> taskMap = service.findByExecutorHandler(cookie,appId);
        for(Object obj : clazzList){
            Class<?> clazz = AopUtils.getTargetClass(obj);
            JobTask jobTask = AnnotationUtil.getAnnotation(clazz, JobTask.class);
            if(ValidateUtils.isEmpty(jobTask)){
                logger.error("{}没有JobTask注解,不进行初始化",clazz.getSimpleName());
                continue;
            }
            String className = clazz.getName();
            SaveJobInfoRequest saveRequest = taskMap.get(className);
            if(ValidateUtils.isEmpty(saveRequest)){
                saveRequest = new SaveJobInfoRequest(jobTask,appId,className);
                service.saveJob(cookie,saveRequest);
            } else {
                saveRequest = saveRequest.update(jobTask,className);
                service.updateJob(cookie,saveRequest);
            }
        }
    }

}
