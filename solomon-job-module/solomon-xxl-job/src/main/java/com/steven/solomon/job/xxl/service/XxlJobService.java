package com.steven.solomon.job.xxl.service;

import com.steven.solomon.job.xxl.config.XxlJobCondition;
import com.steven.solomon.job.xxl.properties.XxlJobProperties;
import com.steven.solomon.job.xxl.properties.XxlJobRegisterProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

/**
 * XXL-JOB 管理端 API 服务实现。
 *
 * <p>继承 {@link CommonXxlJobService} 提供 XXL-JOB Admin 的 HTTP API 调用，
 * 通过条件注解 {@link XxlJobCondition} 控制是否启用。</p>
 */
@Service
@Conditional(XxlJobCondition.class)
@Import({XxlJobProperties.class, XxlJobRegisterProperties.class})
public class XxlJobService extends CommonXxlJobService{

    protected XxlJobService(XxlJobProperties profile, XxlJobRegisterProperties registerProperties, ApplicationContext applicationContext) {
        super(profile, registerProperties, applicationContext);
    }

}
