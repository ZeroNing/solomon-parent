package com.steven.solomon.service;

public interface SaasJobService<T,P> {

    /**
     * 登陆
     * @return 返回token
     */
    String login(P profile) throws Exception;

    /**
     * 保存任务
     */
    void saveJob(String cookie,T job,P profile) throws Exception;

    /**
     * 更新任务
     */
    void updateJob(String cookie,T job,P profile) throws Exception;

    /**
     * 删除任务
     */
    void deleteJob(String cookie,String executorHandler,P profile) throws Exception;

    /**
     * 开启任务
     */
    void startJob(String cookie,String executorHandler,P profile) throws Exception;

    /**
     * 停止任务
     */
    void stopJob(String cookie,String executorHandler,P profile) throws Exception;
}
