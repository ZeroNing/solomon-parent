package com.steven.solomon.service;

/**
 * 任务调度服务接口，定义任务生命周期管理的基本操作。
 *
 * <p>包含登录认证、任务创建/更新/删除以及启停控制等能力。
 * 典型实现为 XxlJob 或 PowerJob 的适配器。</p>
 *
 * @param <T> 任务配置实体类型
 */
public interface JobService<T> {

    /**
     * 登录认证，获取调度平台的访问凭证。
     *
     * @return 登录成功后返回的 cookie/token
     * @throws Exception 远程调用异常
     */
    String login() throws Exception;

    /**
     * 保存任务。
     *
     * @param cookie  登录凭证
     * @param job     任务配置实体
     * @throws Exception 远程调用异常
     */
    void saveJob(String cookie,T job) throws Exception;

    /**
     * 更新任务。
     *
     * @param cookie  登录凭证
     * @param job     任务配置实体
     * @throws Exception 远程调用异常
     */
    void updateJob(String cookie,T job) throws Exception;

    /**
     * 删除任务。
     *
     * @param cookie           登录凭证
     * @param executorHandler  任务执行器标识
     * @throws Exception 远程调用异常
     */
    void deleteJob(String cookie,String executorHandler) throws Exception;

    /**
     * 开启任务。
     *
     * @param cookie           登录凭证
     * @param executorHandler  任务执行器标识
     * @throws Exception 远程调用异常
     */
    void startJob(String cookie,String executorHandler) throws Exception;

    /**
     * 停止任务。
     *
     * @param cookie           登录凭证
     * @param executorHandler  任务执行器标识
     * @throws Exception 远程调用异常
     */
    void stopJob(String cookie,String executorHandler) throws Exception;
}
