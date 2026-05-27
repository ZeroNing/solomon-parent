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
import com.steven.solomon.entity.JobNamespace;
import com.steven.solomon.entity.PowerJobRequestFactory;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.http.HttpUtils;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.properties.JobProperties;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import tech.powerjob.common.OpenAPIConstant;
import tech.powerjob.common.request.http.SaveJobInfoRequest;
import tech.powerjob.common.response.JobInfoDTO;
import tech.powerjob.worker.autoconfigure.PowerJobProperties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.steven.solomon.code.PowerJobErrorCode.*;

@Service
@Import(value = {JobProperties.class})
@Conditional(PowerJobCondition.class)
public class PowerJobService implements JobService<SaveJobInfoRequest> {

    private final JobProperties jobProperties;

    private final PowerJobProperties powerJobProperties;

    private final String adminAddresses;

    private Integer appId;

    public PowerJobService(JobProperties jobProperties, PowerJobProperties powerJobProperties) {
        this.jobProperties = jobProperties;
        this.powerJobProperties = powerJobProperties;
        this.adminAddresses = getUrl();
    }

    @Override
    public String login() throws Exception {
        String userName = jobProperties.getUserName();
        String password = jobProperties.getPassword();

        if (ValidateUtils.isEmpty(adminAddresses)) {
            throw new BaseException(POWER_JOB_URL_NULL);
        }
        if (ValidateUtils.isEmpty(userName)) {
            throw new BaseException(POWER_JOB_USER_NAME_NULL);
        }
        if (ValidateUtils.isEmpty(password)) {
            throw new BaseException(POWER_JOB_PASSWORD_NULL);
        }

        Map<String, Object> thirdPartyParam = new HashMap<>();
        thirdPartyParam.put("loginType","PWJB");
        JSONObject originParams = new JSONObject();
        originParams.set("username", userName);
        originParams.set("password", password);
        originParams.set("encryption", "none");
        thirdPartyParam.put("originParams", originParams.toString());

        Map<String, Object> passwordParam = new HashMap<>();
        passwordParam.put("username", userName);
        passwordParam.put("userName", userName);
        passwordParam.put("password", password);

        String body = executeCandidates(null, Arrays.asList(
                ApiRequest.postJson("auth/thirdPartyLoginDirect", thirdPartyParam),
                ApiRequest.postJson("auth/login", passwordParam),
                ApiRequest.postJson("user/login", passwordParam)
        ));
        String token = resolveToken(body);
        if (ValidateUtils.isEmpty(token)) {
            throw new BaseException(POWER_JOB_EXECUTE_POST_ERROR, adminAddresses, JSONUtil.toJsonStr(passwordParam), "登录成功但未返回token");
        }
        return token;
    }

    @Override
    public void saveJob(String cookie, SaveJobInfoRequest job) throws Exception {
        if (ValidateUtils.isNotEmpty(job.getId())) {
            throw new BaseException(POWER_JOB_ID_IS_NULL);
        }
        cookie = ValidateUtils.getOrDefault(cookie, login());
        executeFirst(cookie, Arrays.asList(openApi(OpenAPIConstant.SAVE_JOB), "job/save"), Method.POST, ContentType.JSON, PowerJobRequestFactory.toPayload(job));
    }

    @Override
    public void updateJob(String cookie, SaveJobInfoRequest job) throws Exception {
        if (ValidateUtils.isEmpty(job.getId())) {
            throw new BaseException(POWER_JOB_ID_IS_NOT_NULL);
        }
        cookie = ValidateUtils.getOrDefault(cookie, login());
        executeFirst(cookie, Arrays.asList(openApi(OpenAPIConstant.SAVE_JOB), "job/save"), Method.POST, ContentType.JSON, PowerJobRequestFactory.toPayload(job));
    }

    @Override
    public void deleteJob(String cookie, String executorHandler) throws Exception {
        cookie = ValidateUtils.getOrDefault(cookie, login());
        Map<String,SaveJobInfoRequest> jobMap = findByExecutorHandler(cookie,getAppId(cookie));
        SaveJobInfoRequest jobInfoRequest = jobMap.get(executorHandler);
        if (ValidateUtils.isEmpty(jobInfoRequest)) {
            throw new BaseException(POWER_JOB_TASK_IS_NULL,executorHandler);
        }
        if (ValidateUtils.isEmpty(jobInfoRequest.getId())) {
            throw new BaseException(POWER_JOB_ID_IS_NULL);
        }
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("jobId", jobInfoRequest.getId());
        paramMap.put("id", jobInfoRequest.getId());
        executeCandidates(cookie, Arrays.asList(
                new ApiRequest(openApi(OpenAPIConstant.DELETE_JOB), Method.POST, ContentType.JSON, paramMap),
                new ApiRequest("job/delete?jobId=" + jobInfoRequest.getId(), Method.GET, null, null)
        ));
    }

    @Override
    public void startJob(String cookie, String executorHandler) throws Exception {
        cookie = ValidateUtils.getOrDefault(cookie, login());
        Map<String,SaveJobInfoRequest> taskMap = findByExecutorHandler(cookie,getAppId(cookie));
        SaveJobInfoRequest saveJobInfoRequest = taskMap.get(executorHandler);
        if (ValidateUtils.isEmpty(saveJobInfoRequest)) {
            throw new BaseException(POWER_JOB_TASK_IS_NULL,executorHandler);
        }
        updateStatus(cookie, saveJobInfoRequest, true);
    }

    @Override
    public void stopJob(String cookie, String executorHandler) throws Exception {
        cookie = ValidateUtils.getOrDefault(cookie, login());
        Map<String,SaveJobInfoRequest> taskMap = findByExecutorHandler(cookie,getAppId(cookie));
        SaveJobInfoRequest saveJobInfoRequest = taskMap.get(executorHandler);
        if (ValidateUtils.isEmpty(saveJobInfoRequest)) {
            throw new BaseException(POWER_JOB_TASK_IS_NULL,executorHandler);
        }
        updateStatus(cookie, saveJobInfoRequest, false);
    }

    /**
     * 获取全部任务
     */
    public Map<String,SaveJobInfoRequest> findByExecutorHandler(String cookie,Integer appId) throws BaseException {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("appId",appId);
        paramMap.put("index",0);
        paramMap.put("pageSize",100000);
        String body = executeFirst(cookie, Arrays.asList(openApi(OpenAPIConstant.FETCH_ALL_JOB), "job/list"), Method.POST, ContentType.JSON,paramMap);
        JSONArray data = readArray(body);
        if (ValidateUtils.isEmpty(data)) {
            return new HashMap<>();
        }
        List<JobInfoDTO> list = JSONUtil.toList(data, JobInfoDTO.class);
        return Lambda.toMap(list.stream().map(PowerJobRequestFactory::from).toList(),SaveJobInfoRequest :: getProcessorInfo);
    }

    /**
     * 获取全部应用id
     */
    public Map<String,Integer> getAllAppId(String cookie) throws BaseException {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("showMyRelated",true);
        paramMap.put("index",0);
        paramMap.put("pageSize",1000);

        String body = executeFirst(cookie, Arrays.asList("appInfo/list", "app/list"), Method.POST, ContentType.JSON, paramMap);
        JSONArray jsonArray = readArray(body);
        if (ValidateUtils.isEmpty(jsonArray)) {
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
        if (ValidateUtils.isNotEmpty(appId)) {
            return appId;
        }
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("appName",appName);
        paramMap.put("title",appName);
        paramMap.put("namespaceId",namespacesId);
        paramMap.put("password",jobProperties.getPassword());
        paramMap.put("componentUserRoleInfo",initUserRole());
        String body = executeFirst(cookie, Arrays.asList("appInfo/save", "app/save"), Method.POST, ContentType.JSON, paramMap);
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
        List<JobNamespace> namespaceList = JSONUtil.toList(readArray(body),JobNamespace.class);
        return Lambda.toMap(namespaceList,JobNamespace::getCode);
    }
    /**
     * 创建命名空间
     */
    public Integer createNamespace(String code,String cookie) throws BaseException {
        Map<String, JobNamespace> namespaceMap = getAllNamespaces(cookie);
        JobNamespace jobNamespace =  namespaceMap.get(code);
        if (ValidateUtils.isNotEmpty(jobNamespace)) {
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
     * 历史版本 PowerJob 管理端接口路径存在差异，按候选路径顺序尝试，优先使用新版接口。
     */
    private String executeFirst(String cookie, List<String> paths, Method requestMethod, ContentType contentType, Map<String, Object> paramMap) throws BaseException {
        return executeCandidates(cookie, paths.stream()
                .map(path -> new ApiRequest(path, requestMethod, contentType, paramMap))
                .toList());
    }

    /**
     * 使用 PowerJob common jar 暴露的 OpenAPI 常量拼接官方接口路径。
     */
    private String openApi(String api) {
        return trimStartSlash(OpenAPIConstant.WEB_PATH) + "/" + trimStartSlash(api);
    }

    /**
     * PowerJob OpenAPI 常量自带前导斜杠，内部统一转换为相对路径。
     */
    private String trimStartSlash(String value) {
        return value.startsWith("/") ? value.substring(1) : value;
    }

    /**
     * 优先使用官方 OpenAPI 启停任务，失败后退回保存任务状态。
     */
    private void updateStatus(String cookie, SaveJobInfoRequest job, boolean enable) throws BaseException {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("jobId", job.getId());
        paramMap.put("id", job.getId());
        try {
            execute(cookie, adminAddresses + openApi(enable ? OpenAPIConstant.ENABLE_JOB : OpenAPIConstant.DISABLE_JOB), Method.POST, ContentType.JSON, paramMap);
            return;
        } catch (BaseException ignored) {
            // 历史版本可能没有 OpenAPI 启停接口，退回保存任务状态。
        }
        job.setEnable(enable);
        execute(cookie, adminAddresses + "job/save", Method.POST, ContentType.JSON, PowerJobRequestFactory.toPayload(job));
    }

    /**
     * 按候选接口顺序执行，兼容新旧版本路径和参数差异。
     */
    private String executeCandidates(String cookie, List<ApiRequest> candidates) throws BaseException {
        BaseException lastException = null;
        for (ApiRequest candidate : candidates) {
            try {
                return execute(cookie, adminAddresses + candidate.path, candidate.method, candidate.contentType, candidate.paramMap);
            } catch (BaseException exception) {
                lastException = exception;
            }
        }
        throw ValidateUtils.isEmpty(lastException) ? new BaseException(POWER_JOB_EXECUTE_POST_ERROR, adminAddresses, "{}", "无可用接口路径") : lastException;
    }

    /**
     * 兼容不同 PowerJob 版本的分页结构，统一提取列表数据。
     */
    private JSONArray readArray(String body) {
        if (ValidateUtils.isEmpty(body)) {
            return new JSONArray();
        }
        Object data = JSONUtil.parse(body);
        while (data instanceof JSONObject) {
            JSONObject json = (JSONObject) data;
            data = firstNotEmpty(json, "data", "rows", "records", "list", "content");
        }
        if (data instanceof JSONArray) {
            return (JSONArray) data;
        }
        return new JSONArray();
    }

    /**
     * 从 JSON 对象中按顺序获取第一个非空字段。
     */
    private Object firstNotEmpty(JSONObject json, String... keys) {
        for (String key : keys) {
            Object value = json.get(key);
            if (ValidateUtils.isNotEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 兼容不同登录接口的 token 返回字段。
     */
    private String resolveToken(String body) {
        if (ValidateUtils.isEmpty(body)) {
            return null;
        }
        if (!JSONUtil.isTypeJSON(body) || !body.trim().startsWith("{")) {
            return body;
        }
        JSONObject json = JSONUtil.parseObj(body);
        Object token = firstNotEmpty(json, "jwtToken", "token", "accessToken");
        return ValidateUtils.isEmpty(token) ? null : token.toString();
    }

    /**
     * 延迟创建应用，避免服务 Bean 初始化时强依赖 PowerJob 管理端可用。
     */
    private Integer getAppId(String cookie) throws BaseException {
        if (ValidateUtils.isEmpty(appId)) {
            appId = createAppId(cookie, powerJobProperties.getWorker().getAppName(), createNamespace(jobProperties.getNamespace(), cookie));
        }
        return appId;
    }

    /**
     * 调用接口
     */
    private HttpResponse executeResponse(String cookie, String url, Method requestMethod, ContentType contentType, Map<String, Object> paramMap) throws BaseException {
        HttpRequest request = HttpUtils.initRequest(requestMethod, url,contentType);
        if (ValidateUtils.isNotEmpty(cookie)) {
            // 新版本使用 PowerJwt，部分历史版本仍兼容 Cookie 鉴权。
            request = request.header("PowerJwt",cookie)
                    .header("Cookie", cookie)
                    .header(OpenAPIConstant.REQUEST_HEADER_ACCESS_TOKEN, cookie);
        }
        if (ValidateUtils.isNotEmpty(appId)) {
            request = request.header(OpenAPIConstant.REQUEST_HEADER_APP_ID, appId.toString());
        }
        if (ValidateUtils.isNotEmpty(paramMap)) {
            request = request.body(JSONUtil.toJsonStr(paramMap));
        }
        HttpResponse response = request.execute();
        String body = response.body();
        Boolean success = null;
        String message = null;
        if (JSONUtil.isTypeJSON(body)) {
            Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
            Object successValue = resultMap.get("success");
            Object codeValue = resultMap.get("code");
            success = ValidateUtils.isEmpty(successValue) ? null : BooleanUtil.toBoolean(successValue.toString());
            message = ValidateUtils.isEmpty(resultMap.get("message")) ? null : resultMap.get("message").toString();
            if (ValidateUtils.isEmpty(message)) {
                message = ValidateUtils.isEmpty(resultMap.get("msg")) ? null : resultMap.get("msg").toString();
            }
            if (ValidateUtils.isNotEmpty(codeValue) && !ValidateUtils.equalsIgnoreCase(codeValue.toString(), "200") && !ValidateUtils.equalsIgnoreCase(codeValue.toString(), "0")) {
                success = false;
            }
        }

        if (!response.isOk() || Boolean.FALSE.equals(success)) {
            throw new BaseException(POWER_JOB_EXECUTE_POST_ERROR,url,JSONUtil.toJsonStr(paramMap),message);
        }
        return response;
    }

    /**
     * 管理端接口候选项。
     */
    private static class ApiRequest {

        private final String path;

        private final Method method;

        private final ContentType contentType;

        private final Map<String, Object> paramMap;

        private ApiRequest(String path, Method method, ContentType contentType, Map<String, Object> paramMap) {
            this.path = path;
            this.method = method;
            this.contentType = contentType;
            this.paramMap = paramMap;
        }

        private static ApiRequest postJson(String path, Map<String, Object> paramMap) {
            return new ApiRequest(path, Method.POST, ContentType.JSON, paramMap);
        }
    }

    /**
     * 获取接口返回的数据
     */
    private String execute(String cookie, String url,Method requestMethod,ContentType contentType, Map<String, Object> paramMap) throws BaseException {
        try (HttpResponse response = executeResponse(cookie, url,requestMethod,contentType, paramMap)) {
            String body = response.body();
            if (!JSONUtil.isTypeJSON(body) || !body.trim().startsWith("{")) {
                return body;
            }
            Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
            return ValidateUtils.isEmpty(resultMap.get("data")) ? body : resultMap.get("data").toString();
        }
    }

    private String getUrl() {
        String adminAddresses = powerJobProperties.getWorker().getProtocol().name().toLowerCase() +"://"+ powerJobProperties.getWorker().getServerAddress();
        if (!adminAddresses.endsWith("/")) {
            adminAddresses = adminAddresses + "/";
        }
        return adminAddresses;
    }

    private Map<String,Object> initUserRole() {
        Map<String,Object> componentUserRoleInfoMap = new HashMap<>();
        componentUserRoleInfoMap.put("observer",new String[]{});
        componentUserRoleInfoMap.put("qa",new String[]{});
        componentUserRoleInfoMap.put("developer",new String[]{});
        componentUserRoleInfoMap.put("admin",new String[]{});
        return componentUserRoleInfoMap;
    }
}
