package com.steven.solomon.service;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.config.PowerJobCondition;
import com.steven.solomon.entity.JobAppVO;
import com.steven.solomon.entity.JobLoginVO;
import com.steven.solomon.entity.JobNamespace;
import com.steven.solomon.entity.SaveJobInfoRequest;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.http.HttpUtils;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.pojo.vo.ResultVO;
import com.steven.solomon.properties.JobProperties;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import tech.powerjob.worker.autoconfigure.PowerJobProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.steven.solomon.code.PowerJobErrorCode.*;
import static com.steven.solomon.code.PowerJobErrorCode.POWER_JOB_PASSWORD_NULL;

@Service
@Import(value = {JobProperties.class})
@Conditional(PowerJobCondition.class)
public class PowerJobService implements JobService<SaveJobInfoRequest> {

    private final JobProperties jobProperties;

    private final PowerJobProperties powerJobProperties;

    private final String adminAddresses;

    private final Integer appId;

    public PowerJobService(JobProperties jobProperties, PowerJobProperties powerJobProperties) throws Exception {
        this.jobProperties = jobProperties;
        this.powerJobProperties = powerJobProperties;
        this.adminAddresses = getUrl();
        String cookie = login();
        this.appId = createAppId(cookie,powerJobProperties.getWorker().getAppName(), createNamespace(jobProperties.getNamespace(), cookie));
    }

    @Override
    public String login() throws Exception {
        String userName = jobProperties.getUserName();
        String password = jobProperties.getPassword();

        if(ValidateUtils.isEmpty(adminAddresses)){
            throw new BaseException(POWER_JOB_URL_NULL);
        }
        if(ValidateUtils.isEmpty(userName)){
            throw new BaseException(POWER_JOB_USER_NAME_NULL);
        }
        if(ValidateUtils.isEmpty(password)){
            throw new BaseException(POWER_JOB_PASSWORD_NULL);
        }

        String url = adminAddresses + "auth/thirdPartyLoginDirect";
        // 构建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("loginType","PWJB");
        JSONObject originParams = new JSONObject();
        originParams.set("username", userName);
        originParams.set("password", password);
        originParams.set("encryption", "none");
        paramMap.put("originParams", originParams.toString());
        // 发送 POST 请求
        String body = execute(null,url,Method.POST,ContentType.JSON,paramMap);
        JobLoginVO loginVO = JSONUtil.toBean(body, JobLoginVO.class);
        return loginVO.getJwtToken();
    }

    @Override
    public void saveJob(String cookie, SaveJobInfoRequest job) throws Exception {
        if(ValidateUtils.isNotEmpty(job.getId())){
            throw new BaseException(POWER_JOB_ID_IS_NULL);
        }
        String url = adminAddresses + "job/save";
        cookie = ValidateUtils.getOrDefault(cookie, login());
        execute(cookie,url,Method.POST,ContentType.JSON,JSONUtil.toBean(JSONUtil.toJsonStr(job), new TypeReference<Map<String, Object>>() {},true));
    }

    @Override
    public void updateJob(String cookie, SaveJobInfoRequest job) throws Exception {
        if(ValidateUtils.isEmpty(job.getId())){
            throw new BaseException(POWER_JOB_ID_IS_NOT_NULL);
        }
        String url = adminAddresses + "job/save";
        cookie = ValidateUtils.getOrDefault(cookie, login());
        execute(cookie,url,Method.POST,ContentType.JSON,JSONUtil.toBean(JSONUtil.toJsonStr(job), new TypeReference<Map<String, Object>>() {},true));
    }

    @Override
    public void deleteJob(String cookie, String executorHandler) throws Exception {
        cookie = ValidateUtils.getOrDefault(cookie, login());
        Map<String,SaveJobInfoRequest> jobMap = findByExecutorHandler(cookie,appId);
        SaveJobInfoRequest jobInfoRequest = jobMap.get(executorHandler);
        if(ValidateUtils.isEmpty(jobInfoRequest)){
            throw new BaseException(POWER_JOB_TASK_IS_NULL,executorHandler);
        }
        if(ValidateUtils.isEmpty(jobInfoRequest.getId())){
            throw new BaseException(POWER_JOB_ID_IS_NULL);
        }
        execute(cookie,adminAddresses+"job/delete?jobId="+jobInfoRequest.getId(),Method.GET,null,null);
    }

    @Override
    public void startJob(String cookie, String executorHandler) throws Exception {
        cookie = ValidateUtils.getOrDefault(cookie, login());
        Map<String,SaveJobInfoRequest> taskMap = findByExecutorHandler(cookie,appId);
        SaveJobInfoRequest saveJobInfoRequest = taskMap.get(executorHandler);
        if(ValidateUtils.isEmpty(saveJobInfoRequest)){
            throw new BaseException(POWER_JOB_TASK_IS_NULL,executorHandler);
        }
        saveJobInfoRequest.setEnable(true);
        saveJob(cookie,saveJobInfoRequest);
    }

    @Override
    public void stopJob(String cookie, String executorHandler) throws Exception {
        cookie = ValidateUtils.getOrDefault(cookie, login());
        Map<String,SaveJobInfoRequest> taskMap = findByExecutorHandler(cookie,appId);
        SaveJobInfoRequest saveJobInfoRequest = taskMap.get(executorHandler);
        if(ValidateUtils.isEmpty(saveJobInfoRequest)){
            throw new BaseException(POWER_JOB_TASK_IS_NULL,executorHandler);
        }
        saveJobInfoRequest.setEnable(false);
        saveJob(cookie,saveJobInfoRequest);
    }

    /**
     * 获取全部任务
     */
    public Map<String,SaveJobInfoRequest> findByExecutorHandler(String cookie,Integer appId) throws BaseException {
        String url = adminAddresses + "job/list";
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("appId",appId);
        paramMap.put("index",0);
        paramMap.put("pageSize",100000);
        String body = execute(cookie,url,Method.POST,ContentType.JSON,paramMap);
        ResultVO<String> data = JSONUtil.toBean(body, new TypeReference<ResultVO<String>>() {}, true);
        if(ValidateUtils.isEmpty(data) || ValidateUtils.isEmpty(data.getData())){
            return new HashMap<>();
        }
        List<SaveJobInfoRequest> list = JSONUtil.toList(data.getData(), SaveJobInfoRequest.class);
        return Lambda.toMap(list,SaveJobInfoRequest :: getProcessorInfo);
    }

    /**
     * 获取全部应用id
     */
    public Map<String,Integer> getAllAppId(String cookie) throws BaseException {
        String url = adminAddresses + "appInfo/list";
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("showMyRelated",true);
        paramMap.put("index",0);
        paramMap.put("pageSize",1000);

        String body = execute(cookie,url,Method.POST,ContentType.JSON,paramMap);
        Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
        JSONArray jsonArray = (JSONArray) resultMap.get("data");
        if(ValidateUtils.isEmpty(jsonArray)){
            return new HashMap<>();
        }
        List<JobAppVO> appVOList = JSONUtil.toList(jsonArray, JobAppVO.class);
        return Lambda.toMap(appVOList, JobAppVO::getAppName, JobAppVO::getId);
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
        paramMap.put("componentUserRoleInfo",initUserRole());
        String body = execute(cookie,adminAddresses+"appInfo/save",Method.POST,ContentType.JSON,paramMap);
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

        String body = execute(cookie,adminAddresses+"namespace/list",Method.POST,ContentType.JSON,paramMap);
        Map<String,Object> bodyMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
        List<JobNamespace> namespaceList = JSONUtil.toList((JSONArray)bodyMap.get("data"),JobNamespace.class);
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
        params.put("componentUserRoleInfo",initUserRole());
        String body = execute(cookie,adminAddresses + "namespace/save",Method.POST,ContentType.JSON,params);
        Map<String,Object> bodyMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
        return ValidateUtils.isEmpty(bodyMap) ? 0 : Integer.parseInt(bodyMap.get("id").toString());
    }

    /**
     * 调用接口
     */
    private HttpResponse executeResponse(String cookie, String url, Method requestMethod, ContentType contentType, Map<String, Object> paramMap) throws BaseException {
        HttpRequest request = HttpUtils.initRequest(requestMethod, url,contentType);
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
     * 获取接口返回的数据
     */
    private String execute(String cookie, String url,Method requestMethod,ContentType contentType, Map<String, Object> paramMap) throws BaseException {
        try (HttpResponse response = executeResponse(cookie, url,requestMethod,contentType, paramMap)) {
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

    private Map<String,Object> initUserRole(){
        Map<String,Object> componentUserRoleInfoMap = new HashMap<>();
        componentUserRoleInfoMap.put("observer",new String[]{});
        componentUserRoleInfoMap.put("qa",new String[]{});
        componentUserRoleInfoMap.put("developer",new String[]{});
        componentUserRoleInfoMap.put("admin",new String[]{});
        return componentUserRoleInfoMap;
    }
}
