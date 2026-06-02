package com.steven.solomon.annotation;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.enums.JobPlatform;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 统一任务注解。
 *
 * <p>公共任务信息保留在当前注解，平台差异参数分别放入 PowerJobTask 和 XxlJobTask。</p>
 */
@Target(value = { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JobTask {

    /**
     * Bean 名称，同时作为 {@link Component} 的别名。
     */
    @AliasFor(annotation = Component.class)
    String value() default StrUtil.EMPTY;

    /**
     * 需要自动注册的平台。未配置时由实现模块按任务类型自动识别。
     */
    JobPlatform[] platforms() default {};

    /** 任务名称。未配置时使用当前类名。 */
    String taskName() default StrUtil.EMPTY;

    /** 任务描述。 */
    String taskDesc() default StrUtil.EMPTY;

    /** 任务参数。 */
    String taskParams() default StrUtil.EMPTY;

    /** PowerJob 专属任务参数。 */
    PowerJobTask powerJob() default @PowerJobTask;

    /** XXL-JOB 专属任务参数。 */
    XxlJobTask xxlJob() default @XxlJobTask;
}
