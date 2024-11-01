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
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
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

    private final JobProperties jobProperties;

    private final PowerJobProperties powerJobProperties;

    public PowerJobInit(ApplicationContext applicationContext, JobProperties jobProperties, PowerJobProperties powerJobProperties) {
        this.jobProperties = jobProperties;
        this.powerJobProperties = powerJobProperties;
        SpringUtil.setContext(applicationContext);
    }

    @Override
    public void init(List<Object> clazzList) throws Exception {
        if(!powerJobProperties.getWorker().isEnabled()){
            logger.error("powerJob不启用,不初始化定时任务");
            return;
        }
        String webUrl = powerJobProperties.getWorker().getProtocol().name().toLowerCase() +"://"+ powerJobProperties.getWorker().getServerAddress();
        if(!webUrl.endsWith("/")){
            webUrl = webUrl + "/";
        }
        //登陆获取cookie
        String cookie = login(webUrl);
        //获取全部AppId
        Map<String,Integer> appMap = getAllAppId(webUrl,cookie);
        //根据appName获取AppId
        Integer appId = appMap.get(powerJobProperties.getWorker().getAppName());
        if(ValidateUtils.isEmpty(appId)){
            throw new Exception("powerJob获取APPID失败,应用名:"+powerJobProperties.getWorker().getAppName());
        }
        //获取全部任务
        Map<String,SaveJobInfoRequest> taskMap = getAllTask(webUrl,cookie,appId);
        for(Object obj : clazzList){
            Class<?> clazz = obj.getClass();
            JobTask jobTask = AnnotationUtil.getAnnotation(clazz, JobTask.class);
            if(ValidateUtils.isEmpty(jobTask)){
                logger.error("{}没有JobTask注解,不进行初始化",obj.getClass().getSimpleName());
                continue;
            }
            String className = obj.getClass().getName();
            SaveJobInfoRequest saveRequest = taskMap.get(className);
            if(ValidateUtils.isEmpty(saveRequest)){
                saveRequest = new SaveJobInfoRequest(jobTask,appId,className);
            } else {
                saveRequest = saveRequest.update(jobTask,className);
            }
            save(webUrl,cookie,saveRequest);
        }
    }

    /**
     * 登陆网页
     */
    private String login(String webUrl) throws Exception {
        String userName = jobProperties.getUserName();
        String password = jobProperties.getPassword();
        if(ValidateUtils.isEmpty(webUrl)){
            throw new Exception("powerJob的网页url不允许为空");
        }
        if(ValidateUtils.isEmpty(userName)){
            throw new Exception("powerJob的账号不允许为空");
        }
        if(ValidateUtils.isEmpty(password)){
            throw new Exception("powerJob的密码不允许为空");
        }

        String url = webUrl + "auth/thirdPartyLoginDirect";
        // 构建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("loginType","PWJB");
        JSONObject originParams = new JSONObject();
        originParams.put("username", userName);
        originParams.put("password", password);
        originParams.put("encryption", "none");
        paramMap.put("originParams", originParams.toString());
        // 发送 POST 请求
        String body = execute(null,url,paramMap);
        JobLoginVO loginVO = JSONUtil.toBean(body, JobLoginVO.class);
        return loginVO.getJwtToken();
    }

    private Map<String,Integer> getAllAppId(String webUrl,String cookie) throws Exception {
        String url = webUrl + "appInfo/list";
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("showMyRelated",true);
        paramMap.put("index",0);
        paramMap.put("pageSize",1000);

        String body = execute(cookie,url,paramMap);
        Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
        JSONArray jsonArray = (JSONArray) resultMap.get("data");
        if(ValidateUtils.isEmpty(jsonArray)){
            return new HashMap<>();
        }
        List<JobAppVO> appVOList = JSONUtil.toList(jsonArray, JobAppVO.class);
        return Lambda.toMap(appVOList, JobAppVO::getAppName, JobAppVO::getId);
    }

    private void save(String webUrl,String cookie,SaveJobInfoRequest saveRequest) throws Exception {
        String url = webUrl + "job/save";
        execute(cookie,url,JSONUtil.toBean(JSONUtil.toJsonStr(saveRequest), new TypeReference<Map<String, Object>>() {},true));
    }

    private Map<String,SaveJobInfoRequest> getAllTask(String webUrl,String cookie,Integer appId) throws Exception {
        String url = webUrl + "job/list";
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("appId",appId);
        paramMap.put("index",0);
        paramMap.put("pageSize",100000);
        String body = execute(cookie,url,paramMap);
        ResultVO<String> data = JSONUtil.toBean(body, new TypeReference<ResultVO<String>>() {}, true);
        if(ValidateUtils.isEmpty(data) || ValidateUtils.isEmpty(data.getData())){
            return new HashMap<>();
        }
        List<SaveJobInfoRequest> list = JSONUtil.toList(data.getData(), SaveJobInfoRequest.class);
        return Lambda.toMap(list,SaveJobInfoRequest :: getProcessorInfo);
    }

    /**
     * 调用接口
     */
    private HttpResponse executeResponse(String cookie,String url,Map<String, Object> paramMap) throws Exception {
        HttpRequest request = HttpUtil.createPost(url).contentType("application/json");
        if(ValidateUtils.isNotEmpty(cookie)){
            request = request.header("PowerJwt",cookie); // 需要替换为实际的认证信息
        }
        if(ValidateUtils.isNotEmpty(paramMap)){
            request = request.body(JSONUtil.toJsonStr(paramMap));
        }
        HttpResponse response = request.execute();
        String body = response.body();
        Boolean success = null;
        String message = null;
        if(JSONUtil.isTypeJSON(body)){
            Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
            success = BooleanUtil.toBoolean(ValidateUtils.isEmpty(resultMap.get("success")) ? "false" : resultMap.get("success").toString());
            message = ValidateUtils.isEmpty(resultMap.get("message")) ? null : resultMap.get("message").toString();
        }

        if(!response.isOk() || Boolean.FALSE.equals(success)){
            throw new Exception("调用powerJob接口:["+url+"]失败,请求参数是:["+JSONUtil.toJsonStr(paramMap)+"],原因:"+message);
        }
        return response;
    }

    /**
     * 获取接口返回的数据
     */
    private String execute(String cookie, String url, Map<String, Object> paramMap) throws Exception {
        try (HttpResponse response = executeResponse(cookie, url, paramMap)) {
            String body = response.body();
            Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
            return ValidateUtils.isEmpty(resultMap.get("data")) ? null : resultMap.get("data").toString();
        }
    }
}
