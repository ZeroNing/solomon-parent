package com.steven.solomon.utils;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.entity.JobAppVO;
import com.steven.solomon.entity.JobLoginVO;
import com.steven.solomon.entity.JobNamespace;
import com.steven.solomon.entity.SaveJobInfoRequest;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.pojo.vo.ResultVO;
import com.steven.solomon.properties.JobProperties;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.autoconfigure.PowerJobProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.steven.solomon.code.PowerJobErrorCode.*;

@Component
@Import(value = {JobProperties.class,PowerJobProperties.class})
public class PowerJobUtils {

    private final JobProperties jobProperties;

    private final PowerJobProperties powerJobProperties;

    private final String webUrl;

    public PowerJobUtils(JobProperties jobProperties, PowerJobProperties powerJobProperties) {
        this.jobProperties = jobProperties;
        this.powerJobProperties = powerJobProperties;
        this.webUrl = getUrl();
    }

    public void deleteTask(String cookie,String jobId) throws BaseException {
        if(ValidateUtils.isEmpty(jobId)){
            throw new BaseException(POWER_JOB_JOB_ID_IS_NULL);
        }
        executeGet(cookie,webUrl+"job/delete?jobId="+jobId);
    }

    /**
     * 启用任务
     */
    public void enableTask(String cookie,Integer appId,String processorInfo) throws BaseException {
        Map<String,SaveJobInfoRequest> taskMap = getAllTask(cookie,appId);
        SaveJobInfoRequest saveJobInfoRequest = taskMap.get(processorInfo);
        if(ValidateUtils.isEmpty(saveJobInfoRequest)){
            throw new BaseException(POWER_JOB_TASK_IS_NULL);
        }
        saveJobInfoRequest.setEnable(true);
        save(cookie,saveJobInfoRequest);
    }

    /**
     * 禁止启用任务
     */
    public void disableTask(String cookie,Integer appId,String processorInfo) throws BaseException {
        Map<String,SaveJobInfoRequest> taskMap = getAllTask(cookie,appId);
        SaveJobInfoRequest saveJobInfoRequest = taskMap.get(processorInfo);
        if(ValidateUtils.isEmpty(saveJobInfoRequest)){
            throw new BaseException(POWER_JOB_TASK_IS_NULL);
        }
        saveJobInfoRequest.setEnable(false);
        save(cookie,saveJobInfoRequest);
    }

    /**
     * 创建应用id
     */
    public Integer createAppId(String cookie,String appName,Integer namespacesId)throws BaseException {
        Map<String,Integer> appIdMap = getAllAppId(cookie);
        Integer appId = appIdMap.get(appName);
        if(ValidateUtils.isNotEmpty(appId)){
            return appId;
        }
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("appName",appName);
        paramMap.put("title",appName);
        paramMap.put("namespaceId",namespacesId);
        paramMap.put("password",jobProperties.getPassword());
        Map<String,Object> componentUserRoleInfoMap = new HashMap<>();
        componentUserRoleInfoMap.put("observer",new String[]{});
        componentUserRoleInfoMap.put("qa",new String[]{});
        componentUserRoleInfoMap.put("developer",new String[]{});
        componentUserRoleInfoMap.put("admin",new String[]{});
        paramMap.put("componentUserRoleInfo",componentUserRoleInfoMap);
        String body = execute(cookie,webUrl+"appInfo/save",paramMap);
        JobAppVO jobApp = JSONUtil.toBean(body,JobAppVO.class);
        return jobApp.getId();
    }

    /**
     * 获取全部命名空间 key为编码
     */
    public Map<String, JobNamespace> getAllNamespaces(String cookie) throws BaseException {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("index",0);
        paramMap.put("pageSize",1000);

        String body = execute(cookie,webUrl+"namespace/list",paramMap);
        List<JobNamespace> namespaceList = JSONUtil.toList(body,JobNamespace.class);
        return Lambda.toMap(namespaceList,JobNamespace::getCode);
    }
    /**
     * 创建命名空间
     */
    public Integer createNamespace(String code,String cookie) throws BaseException {
        Map<String, JobNamespace> namespaceMap = getAllNamespaces(cookie);
        JobNamespace jobNamespace =  namespaceMap.get(code);
        if(ValidateUtils.isNotEmpty(jobNamespace)){
            return jobNamespace.getId();
        }
        Map<String,Object> params = new HashMap<>();
        params.put("code", code);
        params.put("name", code);
        Map<String,Object> componentUserRoleInfoMap = new HashMap<>();
        componentUserRoleInfoMap.put("observer",new String[]{});
        componentUserRoleInfoMap.put("qa",new String[]{});
        componentUserRoleInfoMap.put("developer",new String[]{});
        componentUserRoleInfoMap.put("admin",new String[]{});
        params.put("componentUserRoleInfo",componentUserRoleInfoMap);
        String body = execute(cookie,webUrl + "/namespace/save",params);
        Map<String,Object> bodyMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
        return ValidateUtils.isEmpty(bodyMap) ? 0 : Integer.parseInt(bodyMap.get("id").toString());
    }

    /**
     * 登陆网页
     */
    public String login() throws BaseException {
        String userName = jobProperties.getUserName();
        String password = jobProperties.getPassword();
        if(ValidateUtils.isEmpty(webUrl)){
            throw new BaseException(POWER_JOB_URL_NULL);
        }
        if(ValidateUtils.isEmpty(userName)){
            throw new BaseException(POWER_JOB_USER_NAME_NULL);
        }
        if(ValidateUtils.isEmpty(password)){
            throw new BaseException(POWER_JOB_PASSWORD_NULL);
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

    /**
     * 获取全部应用id
     */
    public Map<String,Integer> getAllAppId(String cookie) throws BaseException {
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

    /**
     * 保存任务
     */
    public void save(String cookie, SaveJobInfoRequest saveRequest) throws BaseException {
        String url = webUrl + "job/save";
        execute(cookie,url,JSONUtil.toBean(JSONUtil.toJsonStr(saveRequest), new TypeReference<Map<String, Object>>() {},true));
    }

    /**
     * 获取全部任务
     */
    public Map<String,SaveJobInfoRequest> getAllTask(String cookie,Integer appId) throws BaseException {
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
    private HttpResponse executePostResponse(String cookie, String url, Map<String, Object> paramMap) throws BaseException {
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
            throw new BaseException(POWER_JOB_EXECUTE_POST_ERROR,url,JSONUtil.toJsonStr(paramMap),message);
        }
        return response;
    }

    /**
     * 调用接口
     */
    private HttpResponse executeGetResponse(String cookie, String url) throws BaseException {
        HttpRequest request = HttpUtil.createGet(url).contentType("application/json");
        if(ValidateUtils.isNotEmpty(cookie)){
            request = request.header("PowerJwt",cookie); // 需要替换为实际的认证信息
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
            throw new BaseException(POWER_JOB_EXECUTE_GET_ERROR,url,message);
        }
        return response;
    }

    /**
     * 获取接口返回的数据
     */
    private String execute(String cookie, String url, Map<String, Object> paramMap) throws BaseException {
        try (HttpResponse response = executePostResponse(cookie, url, paramMap)) {
            String body = response.body();
            Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
            return ValidateUtils.isEmpty(resultMap.get("data")) ? null : resultMap.get("data").toString();
        }
    }

    /**
     * 获取接口返回的数据
     */
    private String executeGet(String cookie, String url) throws BaseException {
        try (HttpResponse response = executeGetResponse(cookie, url)) {
            String body = response.body();
            Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
            return ValidateUtils.isEmpty(resultMap.get("data")) ? null : resultMap.get("data").toString();
        }
    }

    private String getUrl(){
        String adminAddresses = powerJobProperties.getWorker().getProtocol().name().toLowerCase() +"://"+ powerJobProperties.getWorker().getServerAddress();
        if(!adminAddresses.endsWith("/")){
            adminAddresses = adminAddresses + "/";
        }
        return adminAddresses;
    }
}
