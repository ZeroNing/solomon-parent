package com.steven.solomon.service;

public interface JobService<T> {

    /**
     * 登陆
     * @return 返回token
     */
    String login() throws Exception;

    /**
     * 保存任务
     */
    void saveJob(String cookie,T job) throws Exception;

    /**
     * 更新任务
     */
    void updateJob(String cookie,T job) throws Exception;

    /**
     * 删除任务
     */
    void deleteJob(String cookie,String executorHandler) throws Exception;

    /**
     * 开启任务
     */
    void startJob(String cookie,String executorHandler) throws Exception;

    /**
     * 停止任务
     */
    void stopJob(String cookie,String executorHandler) throws Exception;
}
