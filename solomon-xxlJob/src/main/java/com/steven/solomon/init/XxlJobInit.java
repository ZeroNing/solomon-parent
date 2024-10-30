package com.steven.solomon.init;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.entity.XxlJobInfo;
import com.steven.solomon.enums.ScheduleTypeEnum;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.properties.XxlJobProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.handler.IJobHandler;
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
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        for(Object obj : clazzList){
            Class<?> clazz = obj.getClass();
            JobTask jobTask = AnnotationUtil.getAnnotation(clazz, JobTask.class);
            if(ValidateUtils.isEmpty(jobTask)){
                logger.error("{}没有JobTask注解,不进行初始化",obj.getClass().getSimpleName());
                continue;
            }
            String className = obj.getClass().getSimpleName();
            String executorHandler = ValidateUtils.getOrDefault(jobTask.executorHandler(),className);

            List<XxlJobInfo> xxlJobInfoList = findByExecutorHandler(cookie,executorHandler);
            Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);
            XxlJobInfo xxlJobInfo = xxlJobInfoMap.get(executorHandler);

            String url = adminAddresses + (ValidateUtils.isEmpty(xxlJobInfo)? "jobinfo/add" : "jobinfo/update");
            xxlJobInfo = ValidateUtils.isEmpty(xxlJobInfo) ? new XxlJobInfo(jobTask,className) : xxlJobInfo.update(jobTask,className);
            xxlJobInfo.setExecutorHandler(executorHandler);
            // 发送 POST 请求
            execute(cookie,url,JSONUtil.toBean(JSONUtil.toJsonStr(xxlJobInfo), new TypeReference<Map<String,Object>>() {},true));

            //启用或禁止任务，调度类型必须不是不调度才可以
            if(!ValidateUtils.equalsIgnoreCase(jobTask.scheduleType().name(), ScheduleTypeEnum.NONE.name())){
                url = adminAddresses + (jobTask.start() ? "jobinfo/start" : "jobinfo/stop");
                enabled(cookie,executorHandler,url);
            } else {
                logger.info("{}类的调度类型为不调度,不允许启用或者禁止任务",className);
            }
            XxlJobSpringExecutor.registJobHandler(executorHandler, (IJobHandler) obj);
        }
        xxlJobSpringExecutor.start();
    }

    /**
     * 登陆网页
     */
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
        return getCookie(url,paramMap);
    }

    /**
     * 启动任务
     */
    private void enabled(String cookie,String executorHandler,String url) throws Exception {
        List<XxlJobInfo> xxlJobInfoList = findByExecutorHandler(cookie,executorHandler);
        Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);
        XxlJobInfo xxlJobInfo = xxlJobInfoMap.get(executorHandler);
        if(ValidateUtils.isEmpty(xxlJobInfo)){
            throw new Exception("xxl-job启动"+executorHandler+"任务失败");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", xxlJobInfo.getId());
        execute(cookie,url,paramMap);
    }

    /**
     * 调用xxl-job的任务页面查询
     */
    private List<XxlJobInfo> findByExecutorHandler(String cookie,String executorHandler) throws Exception {
        String adminAddresses = profile.getAdminAddresses();
        if(!adminAddresses.endsWith("/")){
            adminAddresses = adminAddresses + "/";
        }
        String url = adminAddresses + "jobinfo/pageList?jobGroup=1&triggerStatus=-1&start="+0+"&length="+10+"&executorHandler="+executorHandler;

        String body = execute(cookie,url,null);
        Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
        Object obj = resultMap.get("data");
        return JSONUtil.toList(JSONUtil.toJsonStr(obj),XxlJobInfo.class);
    }

    /**
     * 调用接口
     */
    private HttpResponse executeResponse(String cookie,String url,Map<String, Object> paramMap) throws Exception {
        HttpRequest request = HttpUtil.createPost(url);
        if(ValidateUtils.isNotEmpty(cookie)){
            request = request.cookie(cookie); // 需要替换为实际的认证信息
        }
        if(ValidateUtils.isNotEmpty(paramMap)){
            request = request.form(paramMap);
        }
        HttpResponse response = request.execute();
        String body = response.body();
        String code = null;
        String msg = null;
        if(JSONUtil.isTypeJSON(body)){
            Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
            code = ValidateUtils.isEmpty(resultMap.get("code")) ? null : resultMap.get("code").toString();
            msg = ValidateUtils.isEmpty(resultMap.get("msg")) ? null : resultMap.get("msg").toString();
        }

        if(!response.isOk() || (ValidateUtils.isNotEmpty(code) && ValidateUtils.notEqualsIgnoreCase(code,"200"))){
            throw new Exception("调用xxl-job接口:["+url+"]失败,请求参数是:["+JSONUtil.toJsonStr(paramMap)+"],原因:"+msg);
        }
        return response;
    }

    /**
     * 获取登陆网页的cookie
     */
    private String getCookie(String url, Map<String, Object> paramMap) throws Exception {
        try (HttpResponse response = executeResponse(null, url, paramMap)) {
            List<String> cookies = response.headerList("Set-Cookie");
            if(ValidateUtils.isEmpty(cookies)){
                throw new Exception("获取xxl-job的cookie失败");
            }
            return cookies.get(0);
        }
    }

    /**
     * 获取接口返回的数据
     */
    private String execute(String cookie, String url, Map<String, Object> paramMap) throws Exception {
        try (HttpResponse response = executeResponse(cookie, url, paramMap)) {
            return response.body();
        }
    }
}
