package com.steven.solomon.job.xxl.service;

import cn.hutool.core.util.StrUtil;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.job.xxl.code.XxlJobErrorCode;
import com.steven.solomon.job.xxl.entity.XxlJobInfo;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.job.xxl.properties.XxlJobProperties;
import com.steven.solomon.job.xxl.properties.XxlJobRegisterProperties;
import com.steven.solomon.service.JobService;
import com.steven.solomon.service.JobLogSanitizer;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XXL-JOB 管理端 API 抽象服务。
 *
 * <p>封装与 XXL-JOB Admin 的 HTTP 通信，提供登录、任务 CRUD、启停、
 * 执行器组解析等通用能力。子类只需实例化后即可使用。</p>
 */
public abstract class CommonXxlJobService implements JobService<XxlJobInfo>{

    /** XXL-JOB Executor 配置。 */
    protected final XxlJobProperties profile;

    /** 自动注册配置。 */
    protected final XxlJobRegisterProperties registerProperties;

    /** 管理端基础地址。 */
    protected final String adminAddresses;

    private final Logger logger = LoggerUtils.logger(CommonXxlJobService.class);

    protected CommonXxlJobService(XxlJobProperties profile, XxlJobRegisterProperties registerProperties, ApplicationContext applicationContext) {
        this.profile = profile;
        this.registerProperties = registerProperties;
        this.adminAddresses = getUrl();
        SpringUtil.setContext(applicationContext);
    }

    @Override
    public String login() throws Exception {
        String userName = registerProperties.getUserName();
        String password = registerProperties.getPassword();
        if (ObjectUtil.isEmpty(adminAddresses)) {
            throw new BaseException(XxlJobErrorCode.XXL_JOB_ADMIN_URL_IS_NULL);
        }
        if (ObjectUtil.isEmpty(userName)) {
            throw new BaseException(XxlJobErrorCode.XXL_JOB_USERNAME_IS_NULL);
        }
        if (ObjectUtil.isEmpty(password)) {
            throw new BaseException(XxlJobErrorCode.XXL_JOB_PASSWORD_IS_NULL);
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userName", userName);
        paramMap.put("username", userName);
        paramMap.put("password", password);
        return getCookie(Arrays.asList("login", "auth/doLogin"), paramMap);

    }

    @Override
    public void saveJob(String cookie,XxlJobInfo job) throws Exception {
        if (ObjectUtil.isEmpty(cookie)) {
            cookie = login();
        }

        List<XxlJobInfo> xxlJobInfoList = findByExecutorHandler(cookie, job.getExecutorHandler(), job.getJobGroup());
        Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);
        if (xxlJobInfoMap.containsKey(job.getExecutorHandler())) {
            throw new BaseException(XxlJobErrorCode.XXL_JOB_TASK_IS_NOT_NULL,job.getExecutorHandler());
        }
        executeFirst(cookie, Arrays.asList("jobinfo/add", "jobinfo/save"), toPayload(job));
    }

    @Override
    public void updateJob(String cookie,XxlJobInfo job) throws Exception {
        if (ObjectUtil.isEmpty(cookie)) {
            cookie = login();
        }

        List<XxlJobInfo> xxlJobInfoList = findByExecutorHandler(cookie, job.getExecutorHandler(), job.getJobGroup());
        Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);
        XxlJobInfo exitJob = xxlJobInfoMap.get(job.getExecutorHandler());
        if (ObjectUtil.isEmpty(exitJob)) {
            throw new BaseException(XxlJobErrorCode.XXL_JOB_TASK_IS_NULL,job.getExecutorHandler());
        }
        job.setId(exitJob.getId());
        executeFirst(cookie, Arrays.asList("jobinfo/update", "jobinfo/save"), toPayload(job));
    }

    @Override
    public void deleteJob(String cookie,String executorHandler) throws Exception {
        if (ObjectUtil.isEmpty(cookie)) {
            cookie = login();
        }

        List<XxlJobInfo> xxlJobInfoList = findByExecutorHandler(cookie, executorHandler);
        Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);
        XxlJobInfo exitJob = xxlJobInfoMap.get(executorHandler);
        if (ObjectUtil.isEmpty(exitJob)) {
            throw new BaseException(XxlJobErrorCode.XXL_JOB_TASK_IS_NULL,executorHandler);
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", exitJob.getId());
        executeFirst(cookie, Arrays.asList("jobinfo/remove", "jobinfo/delete"), paramMap);
    }

    @Override
    public void startJob(String cookie,String executorHandler) throws Exception {
        startJob(cookie, executorHandler, 1);
    }

    public void startJob(String cookie,String executorHandler, int jobGroup) throws Exception {
        if (ObjectUtil.isEmpty(cookie)) {
            cookie = login();
        }

        List<XxlJobInfo> xxlJobInfoList = findByExecutorHandler(cookie,executorHandler, jobGroup);
        Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);
        XxlJobInfo xxlJobInfo = xxlJobInfoMap.get(executorHandler);
        if (ObjectUtil.isEmpty(xxlJobInfo)) {
            throw new BaseException(XxlJobErrorCode.XXL_JOB_START_JOB_ERROR,executorHandler);
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", xxlJobInfo.getId());
        executeFirst(cookie, Arrays.asList("jobinfo/start", "jobinfo/triggerStatus/start"), paramMap);
    }

    @Override
    public void stopJob(String cookie,String executorHandler) throws Exception {
        stopJob(cookie, executorHandler, 1);
    }

    public void stopJob(String cookie,String executorHandler, int jobGroup) throws Exception {
        if (ObjectUtil.isEmpty(cookie)) {
            cookie = login();
        }

        List<XxlJobInfo> xxlJobInfoList = findByExecutorHandler(cookie,executorHandler, jobGroup);
        Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);
        XxlJobInfo xxlJobInfo = xxlJobInfoMap.get(executorHandler);
        if (ObjectUtil.isEmpty(xxlJobInfo)) {
            throw new BaseException(XxlJobErrorCode.XXL_JOB_STOP_JOB_ERROR,executorHandler);
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", xxlJobInfo.getId());
        executeFirst(cookie, Arrays.asList("jobinfo/stop", "jobinfo/triggerStatus/stop"), paramMap);
    }

    /**
     * 根据执行器 Handler 名称查询任务列表（默认执行器组 1）。
     *
     * @param cookie         登录 Cookie
     * @param executorHandler 执行器 Handler 名称
     * @return 任务列表
     * @throws Exception 请求失败时抛出
     */
    public List<XxlJobInfo> findByExecutorHandler(String cookie, String executorHandler) throws Exception {
        return findByExecutorHandler(cookie, executorHandler, 1);
    }

    /**
     * 根据执行器 Handler 和执行器组查询任务列表（分页，默认取 10000 条）。
     *
     * @param cookie         登录 Cookie
     * @param executorHandler 执行器 Handler 名称
     * @param jobGroup        执行器组 ID
     * @return 任务列表
     * @throws Exception 请求失败时抛出
     */
    public List<XxlJobInfo> findByExecutorHandler(String cookie, String executorHandler, int jobGroup) throws Exception {
        if (ObjectUtil.isEmpty(cookie)) {
            cookie = login();
        }
        Map<String,Object> paramMap = new  HashMap<>();
        paramMap.put("jobGroup", jobGroup <= 0 ? "" : String.valueOf(jobGroup));
        paramMap.put("triggerStatus", "-1");
        paramMap.put("start", "0");
        paramMap.put("length", "10000");
        paramMap.put("executorHandler", executorHandler);
        paramMap.put("author", "");
        paramMap.put("jobDesc", "");
        String body = executeFirst(cookie, Arrays.asList("jobinfo/pageList", "jobinfo/page"), paramMap);
        Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
        Object obj = firstNotEmpty(resultMap, "data", "rows", "records", "list");
        if (ObjectUtil.isEmpty(obj)) {
            return Collections.emptyList();
        }
        return JSONUtil.toList(JSONUtil.toJsonStr(obj),XxlJobInfo.class);
    }

    /**
     * 按执行器组和 Handler 查询任务，并转成 Map 便于自动注册判断新增或更新。
     */
    public Map<String,XxlJobInfo> findMapByExecutorHandler(String cookie, String executorHandler, int jobGroup) throws Exception {
        return Lambda.toMap(findByExecutorHandler(cookie, executorHandler, jobGroup), XxlJobInfo::getExecutorHandler);
    }

    /**
     * 判断任务核心参数是否一致，一致时跳过无意义更新。
     */
    public boolean sameJob(XxlJobInfo source, XxlJobInfo target) {
        return toPayload(source).equals(toPayload(target));
    }

    /**
     * 根据执行器 appName 自动解析执行器组 ID，减少业务侧手写 jobGroup 的配置成本。
     */
    public int resolveJobGroup(String cookie, int defaultJobGroup) {
        if (!registerProperties.getAutoResolveJobGroup() || ObjectUtil.isEmpty(profile.getAppName())) {
            return defaultJobGroup;
        }
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("start", "0");
            paramMap.put("length", "10000");
            paramMap.put("appname", profile.getAppName());
            paramMap.put("title", "");
            String body = executeFirst(cookie, Arrays.asList("jobgroup/pageList", "jobgroup/page"), paramMap);
            Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
            Object data = firstNotEmpty(resultMap, "data", "rows", "records", "list");
            List<Map> groups = JSONUtil.toList(JSONUtil.toJsonStr(data), Map.class);
            for (Map group : groups) {
                Object appName = firstNotEmpty(group, "appname", "appName");
                if (StrUtil.equalsIgnoreCase(profile.getAppName(), ObjectUtil.isEmpty(appName) ? null : appName.toString())) {
                    Object id = group.get("id");
                    return ObjectUtil.isEmpty(id) ? defaultJobGroup : Integer.parseInt(id.toString());
                }
            }
        } catch (Exception exception) {
            logger.warn("按appName:{}解析XXL-JOB执行器组失败，回退注解jobGroup:{}", profile.getAppName(), defaultJobGroup, exception);
        }
        return defaultJobGroup;
    }

    /**
     * 获取登陆网页的cookie
     */
    protected String getCookie(List<String> paths, Map<String, Object> paramMap) throws BaseException {
        BaseException lastException = null;
        for (String path : paths) {
            String url = adminAddresses + path;
            try (HttpResponse response = executeResponse(null, url, paramMap)) {
                List<String> cookies = response.headerList("Set-Cookie");
                if (ObjectUtil.isEmpty(cookies)) {
                    throw new BaseException(XxlJobErrorCode.XXL_JOB_COOKIE_IS_NULL);
                }
                String cookie = cookies.getFirst();
                String[] parts = cookie.split(";");
                return parts.length > 0 ? parts[0].trim() : cookie;
            } catch (BaseException exception) {
                lastException = exception;
                logger.warn("请求XXL-JOB登录接口:{}失败,尝试下一个候选接口", url);
            }
        }
        throw ObjectUtil.isEmpty(lastException) ? new BaseException(XxlJobErrorCode.XXL_JOB_COOKIE_IS_NULL) : lastException;
    }

    /**
     * 按候选接口顺序调用，兼容不同 XXL-JOB 管理端版本路径。
     */
    protected String executeFirst(String cookie, List<String> paths, Map<String, Object> paramMap) throws BaseException {
        BaseException lastException = null;
        for (String path : paths) {
            try {
                return execute(cookie, adminAddresses + path, paramMap);
            } catch (BaseException exception) {
                lastException = exception;
            }
        }
        throw ObjectUtil.isEmpty(lastException) ? new BaseException(XxlJobErrorCode.XXL_JOB_EXECUTE_ERROR, adminAddresses, JobLogSanitizer.sanitize(paramMap), "无可用接口路径") : lastException;
    }

    /**
     * 发送 POST 请求并返回响应体字符串。
     *
     * @param cookie   登录 Cookie
     * @param url      请求地址
     * @param paramMap 表单参数
     * @return 响应体字符串
     * @throws BaseException 请求失败时抛出
     */
    protected String execute(String cookie, String url, Map<String, Object> paramMap) throws BaseException {
        try (HttpResponse response = executeResponse(cookie, url, paramMap)) {
            return response.body();
        }
    }

    /**
     * 发送 POST 请求并执行。
     *
     * @param cookie   登录 Cookie
     * @param url      请求地址
     * @param paramMap 表单参数
     * @return HTTP 响应
     * @throws BaseException 请求失败或返回状态异常时抛出
     */
    protected HttpResponse executeResponse(String cookie, String url, Map<String, Object> paramMap) throws BaseException {
        HttpRequest request = HttpUtil.createPost(url);
        if (ObjectUtil.isNotEmpty(cookie)) {
            request = request.header("Cookie", cookie);
        }
        if (ObjectUtil.isNotEmpty(profile.getAccessToken())) {
            request = request.header("XXL-JOB-ACCESS-TOKEN", profile.getAccessToken());
        }
        if (ObjectUtil.isNotEmpty(paramMap)) {
            request = request.form(paramMap);
        }
        HttpResponse response = request.execute();
        String body = response.body();
        String code = null;
        String msg = null;
        if (JSONUtil.isTypeJSON(body)) {
            Map<String,Object> resultMap = JSONUtil.toBean(body, new TypeReference<Map<String, Object>>() {},true);
            Object codeValue = firstNotEmpty(resultMap, "code", "status");
            code = ObjectUtil.isEmpty(codeValue) ? null : codeValue.toString();
            Object msgValue = firstNotEmpty(resultMap, "msg", "message");
            msg = ObjectUtil.isEmpty(msgValue) ? null : msgValue.toString();
        }

        if (!response.isOk() || (ObjectUtil.isNotEmpty(code)
                && !StrUtil.equalsIgnoreCase(code, "200")
                && !StrUtil.equalsIgnoreCase(code, "0"))) {
            throw new BaseException(XxlJobErrorCode.XXL_JOB_EXECUTE_ERROR,url, JobLogSanitizer.sanitize(paramMap),msg);
        }
        return response;
    }

    /**
     * 组装管理端基础地址，确保以 / 结尾。
     */
    protected String getUrl() {
        String adminAddresses = profile.getAdminAddresses();
        if (!adminAddresses.endsWith("/")) {
            adminAddresses = adminAddresses + "/";
        }
        return adminAddresses;
    }

    /**
     * 将任务对象转换为管理端表单参数，保留字符串枚举值，适配 XXL-JOB Admin。
     */
    private Map<String,Object> toPayload(XxlJobInfo job) {
        return JSONUtil.toBean(JSONUtil.toJsonStr(job), new TypeReference<Map<String,Object>>() {},true);
    }

    /**
     * 从返回 Map 中读取第一个非空字段，兼容不同版本命名。
     */
    private Object firstNotEmpty(Map resultMap, String... keys) {
        for (String key : keys) {
            Object value = resultMap.get(key);
            if (ObjectUtil.isNotEmpty(value)) {
                return value;
            }
        }
        return null;
    }
}
