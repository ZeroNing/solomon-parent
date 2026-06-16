package com.steven.solomon.cloud.nacos.service;

import cn.hutool.core.util.StrUtil;

import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Nacos 服务注册状态监听器。
 *
 * <p>监听 Spring Cloud 服务注册事件，在服务成功注册到 Nacos 后打印服务名、实例地址，
 * 便于排查注册失败或地址错误的问题。动态配置变更（{@code @RefreshScope}）由 Nacos 自动处理，
 * 这里不重复实现。</p>
 *
 * @author steven
 */
public class NacosServiceSubscriber {

    private static final Logger logger = LoggerUtils.logger(NacosServiceSubscriber.class);

    /**
     * 监听服务注册完成事件，输出注册信息。
     *
     * <p>使用 {@code @EventListener(Registration.class)} 的形式依赖具体注册事件类型，
     * 这里改为注入 Registration 延迟打印，避免事件类型耦合。</p>
     */
    public void onRegistered(Registration registration) {
        String serviceId = registration.getServiceId();
        String host = registration.getHost();
        int port = registration.getPort();
        logger.info("服务已注册到 Nacos: 服务名={}, 实例={}:{}", serviceId, host, port);
    }
}
