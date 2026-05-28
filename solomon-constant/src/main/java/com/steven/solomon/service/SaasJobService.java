package com.steven.solomon.service;

/**
 * SaaS多租户任务调度服务接口，在 {@link JobService} 基础上增加租户配置参数。
 *
 * <p>每个方法都接受租户配置参数 {@code P}，用于实现多租户场景下的任务隔离。</p>
 *
 * @param <T> 任务配置实体类型
 * @param <P> 租户配置属性类型
 */
public interface SaasJobService<T,P> {

    /**
     * 登录认证，使用租户配置获取调度平台的访问凭证。
     *
     * @param profile 租户配置属性
     * @return 登录成功后返回的 cookie/token
     * @throws Exception 远程调用异常
     */
    String login(P profile) throws Exception;

    /**
     * 保存任务。
     *
     * @param cookie   登录凭证
     * @param job      任务配置实体
     * @param profile  租户配置属性
     * @throws Exception 远程调用异常
     */
    void saveJob(String cookie,T job,P profile) throws Exception;

    /**
     * 更新任务。
     *
     * @param cookie   登录凭证
     * @param job      任务配置实体
     * @param profile  租户配置属性
     * @throws Exception 远程调用异常
     */
    void updateJob(String cookie,T job,P profile) throws Exception;

    /**
     * 删除任务。
     *
     * @param cookie           登录凭证
     * @param executorHandler  任务执行器标识
     * @param profile          租户配置属性
     * @throws Exception 远程调用异常
     */
    void deleteJob(String cookie,String executorHandler,P profile) throws Exception;

    /**
     * 开启任务。
     *
     * @param cookie           登录凭证
     * @param executorHandler  任务执行器标识
     * @param profile          租户配置属性
     * @throws Exception 远程调用异常
     */
    void startJob(String cookie,String executorHandler,P profile) throws Exception;

    /**
     * 停止任务。
     *
     * @param cookie           登录凭证
     * @param executorHandler  任务执行器标识
     * @param profile          租户配置属性
     * @throws Exception 远程调用异常
     */
    void stopJob(String cookie,String executorHandler,P profile) throws Exception;
}
