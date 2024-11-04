package com.steven.solomon.utils;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.code.XxlJobErrorCode;
import com.steven.solomon.entity.XxlJobInfo;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.properties.XxlJobProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class XxlJobUtils {

    private final XxlJobProperties profile;

    public XxlJobUtils(ApplicationContext applicationContext, XxlJobProperties profile) {
        this.profile = profile;
        SpringUtil.setContext(applicationContext);
    }

    /**
     * 登陆网页
     */
    public String login() throws BaseException {
        String adminAddresses = getUrl();;
        String userName = profile.getUserName();
        String password = profile.getPassword();
        if(ValidateUtils.isEmpty(adminAddresses)){
            throw new BaseException(XxlJobErrorCode.XXL_JOB_ADMIN_URL_IS_NULL);
        }
        if(ValidateUtils.isEmpty(userName)){
            throw new BaseException(XxlJobErrorCode.XXL_JOB_USERNAME_IS_NULL);
        }
        if(ValidateUtils.isEmpty(password)){
            throw new BaseException(XxlJobErrorCode.XXL_JOB_PASSWORD_IS_NULL);
        }

        String url = adminAddresses + "login";
        // 构建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userName", userName);
        paramMap.put("password", password);

        // 发送 POST 请求
        return getCookie(url,paramMap);
    }

    public void save(XxlJobInfo xxlJobInfo) throws BaseException {
        String cookie = login();

        List<XxlJobInfo> xxlJobInfoList = findByExecutorHandler(cookie,xxlJobInfo.getExecutorHandler());
        Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);

        String adminAddresses = getUrl();
        String url = adminAddresses + (ValidateUtils.isEmpty(xxlJobInfoMap.get(xxlJobInfo.getExecutorHandler()))? "jobinfo/add" : "jobinfo/update");
        // 发送 POST 请求
        execute(cookie,url,JSONUtil.toBean(JSONUtil.toJsonStr(xxlJobInfo), new TypeReference<Map<String,Object>>() {},true));
    }

    /**
     * 删除任务
     */
    public void remove(String cookie,String id) throws BaseException {
        String adminAddresses = getUrl();
        String url = adminAddresses + "jobinfo/remove";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        execute(cookie,url,paramMap);
    }

    /**
     * 调用xxl-job的任务页面查询
     */
    public List<XxlJobInfo> findByExecutorHandler(String cookie, String executorHandler) throws BaseException {
        String adminAddresses = getUrl();
        String url = adminAddresses + "jobinfo/pageList?jobGroup=1&triggerStatus=-1&start="+0+"&length="+1000+"&executorHandler="+executorHandler;

        String body = execute(cookie,url,null);
        Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
        Object obj = resultMap.get("data");
        return JSONUtil.toList(JSONUtil.toJsonStr(obj),XxlJobInfo.class);
    }

    /**
     * 启动任务
     */
    public void enabled(String cookie,String executorHandler,boolean isStart) throws BaseException {
        String adminAddresses = getUrl();
        String url = adminAddresses + (isStart ? "jobinfo/start" : "jobinfo/stop");
        List<XxlJobInfo> xxlJobInfoList = findByExecutorHandler(cookie,executorHandler);
        Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);
        XxlJobInfo xxlJobInfo = xxlJobInfoMap.get(executorHandler);
        if(ValidateUtils.isEmpty(xxlJobInfo)){
            throw new BaseException(XxlJobErrorCode.XXL_JOB_START_JOB_ERROR,executorHandler);
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", xxlJobInfo.getId());
        execute(cookie,url,paramMap);
    }

    /**
     * 获取登陆网页的cookie
     */
    private String getCookie(String url, Map<String, Object> paramMap) throws BaseException {
        try (HttpResponse response = executeResponse(null, url, paramMap)) {
            List<String> cookies = response.headerList("Set-Cookie");
            if(ValidateUtils.isEmpty(cookies)){
                throw new BaseException(XxlJobErrorCode.XXL_JOB_COOKIE_IS_NULL);
            }
            return cookies.get(0);
        }
    }

    private String execute(String cookie, String url, Map<String, Object> paramMap) throws BaseException {
        try (HttpResponse response = executeResponse(cookie, url, paramMap)) {
            return response.body();
        }
    }

    /**
     * 调用接口
     */
    private HttpResponse executeResponse(String cookie,String url,Map<String, Object> paramMap) throws BaseException {
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
            throw new BaseException(XxlJobErrorCode.XXL_JOB_EXECUTE_ERROR,url,JSONUtil.toJsonStr(paramMap),msg);
        }
        return response;
    }

    private String getUrl(){
        String adminAddresses = profile.getAdminAddresses();
        if(!adminAddresses.endsWith("/")){
            adminAddresses = adminAddresses + "/";
        }
        return adminAddresses;
    }
}
