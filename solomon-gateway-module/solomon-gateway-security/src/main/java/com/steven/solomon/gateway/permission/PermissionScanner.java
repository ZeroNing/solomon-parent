package com.steven.solomon.gateway.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.steven.solomon.utils.logger.LoggerUtils;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 权限扫描器。
 *
 * <p>启动时扫描所有 Controller 方法上的 {@link RequirePermission} 注解，
 * 结合 {@code @RequestMapping}/{@code @GetMapping} 等映射注解和 Swagger {@code @Operation}
 * 生成 {@link PermissionInfo}，同步到 {@link PermissionStore}。</p>
 *
 * <p>权限编码：注解 {@link RequirePermission#value()} 显式指定优先；未指定时由
 * {@link PermissionCodeGenerator#fromPath(String)} 按路径自动生成。</p>
 *
 * <p>权限名称：注解 {@link RequirePermission#name()} 优先；其次 Swagger
 * {@code @Operation.summary()}；最后回退方法名。</p>
 *
 * @author steven
 */
public class PermissionScanner {

    private static final Logger logger = LoggerUtils.logger(PermissionScanner.class);

    private final ApplicationContext applicationContext;
    private final PermissionStore permissionStore;

    public PermissionScanner(ApplicationContext applicationContext, PermissionStore permissionStore) {
        this.applicationContext = applicationContext;
        this.permissionStore = permissionStore;
    }

    /**
     * 执行权限扫描。
     *
     * <p>遍历容器中所有 Controller Bean 的方法，提取 {@link RequirePermission} 注解信息，
     * 生成权限元数据并保存到 {@link PermissionStore}。</p>
     *
     * @return 扫描到的权限列表
     */
    public List<PermissionInfo> scan() {
        Set<PermissionInfo> result = new LinkedHashSet<>();
        // 遍历所有 Bean，找带 @RequirePermission 的方法
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Class<?> beanType;
            try {
                beanType = applicationContext.getType(beanName);
            } catch (Exception e) {
                continue;
            }
            if (beanType == null) {
                continue;
            }
            // 获取类级别的 @RequestMapping 作为基础路径
            String basePath = extractBasePath(beanType);
            for (Method method : beanType.getDeclaredMethods()) {
                RequirePermission annotation = AnnotationUtils.findAnnotation(method, RequirePermission.class);
                if (annotation == null) {
                    continue;
                }
                List<PathMethod> pathMethods = extractPathMappings(method, basePath);
                if (CollUtil.isEmpty(pathMethods)) {
                    logger.warn("跳过无路径映射的权限注解: {}.{}", beanType.getSimpleName(), method.getName());
                    continue;
                }
                String displayName = resolveDisplayName(annotation, method);
                for (PathMethod pm : pathMethods) {
                    String code = StrUtil.isNotBlank(annotation.value())
                            ? annotation.value()
                            : PermissionCodeGenerator.fromPath(pm.path);
                    result.add(new PermissionInfo(code, displayName, pm.path, pm.method, annotation.anonymous()));
                }
            }
        }
        List<PermissionInfo> list = new ArrayList<>(result);
        permissionStore.saveAll(list);
        logger.info("权限扫描完成, 共发现 {} 个权限, 其中 {} 个匿名接口",
                list.size(), list.stream().filter(PermissionInfo::anonymous).count());
        return list;
    }

    /**
     * 提取类级别 @RequestMapping 的基础路径。
     */
    private String extractBasePath(Class<?> beanType) {
        RequestMapping classMapping = AnnotationUtils.findAnnotation(beanType, RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            return classMapping.value()[0];
        }
        return "";
    }

    /**
     * 提取方法级别的路径和 HTTP 方法映射。
     */
    private List<PathMethod> extractPathMappings(Method method, String basePath) {
        List<PathMethod> result = new ArrayList<>();

        // @RequestMapping
        RequestMapping reqMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (reqMapping != null) {
            String[] paths = reqMapping.value().length > 0 ? reqMapping.value() : new String[]{basePath};
            for (String path : paths) {
                for (RequestMethod rm : reqMapping.method()) {
                    result.add(new PathMethod(joinPath(basePath, path), rm.name()));
                }
            }
            // 如果没有指定 method，默认 GET
            if (reqMapping.method().length == 0) {
                for (String path : paths) {
                    result.add(new PathMethod(joinPath(basePath, path), "GET"));
                }
            }
        }
        // 快捷映射注解
        addSimpleMapping(method, GetMapping.class, "GET", basePath, result);
        addSimpleMapping(method, PostMapping.class, "POST", basePath, result);
        addSimpleMapping(method, PutMapping.class, "PUT", basePath, result);
        addSimpleMapping(method, DeleteMapping.class, "DELETE", basePath, result);
        addSimpleMapping(method, PatchMapping.class, "PATCH", basePath, result);
        return result;
    }

    /**
     * 处理 @GetMapping/@PostMapping 等快捷注解。
     */
    private void addSimpleMapping(Method method, Class<?> annotationType, String httpMethod,
                                  String basePath, List<PathMethod> result) {
        Object annotation = null;
        for (java.lang.annotation.Annotation a : method.getAnnotations()) {
            if (a.annotationType() == annotationType) { annotation = a; break; }
        }
        if (annotation == null) { return; }
        try {
            java.lang.reflect.Method valueMethod = annotation.getClass().getMethod("value");
            String[] values = (String[]) valueMethod.invoke(annotation);
            if (values == null || values.length == 0) {
                result.add(new PathMethod(joinPath(basePath, ""), httpMethod));
            } else {
                for (String path : values) {
                    result.add(new PathMethod(joinPath(basePath, path), httpMethod));
                }
            }
        } catch (Exception e) {
            result.add(new PathMethod(joinPath(basePath, ""), httpMethod));
        }
    }

    /**
     * 拼接基础路径和方法路径。
     */
    private String joinPath(String basePath, String methodPath) {
        if (StrUtil.isBlank(methodPath)) {
            return StrUtil.isBlank(basePath) ? "/" : basePath;
        }
        if (StrUtil.isBlank(basePath)) {
            return methodPath.startsWith("/") ? methodPath : "/" + methodPath;
        }
        String left = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        String right = methodPath.startsWith("/") ? methodPath : "/" + methodPath;
        return left + right;
    }

    /**
     * 解析权限展示名称：注解 > Swagger summary > 方法名。
     */
    private String resolveDisplayName(RequirePermission annotation, Method method) {
        if (StrUtil.isNotBlank(annotation.name())) {
            return annotation.name();
        }
        Operation operation = AnnotationUtils.findAnnotation(method, Operation.class);
        if (operation != null && StrUtil.isNotBlank(operation.summary())) {
            return operation.summary();
        }
        return method.getName();
    }

    /** 路径与HTTP方法的内部组合。 */
    private record PathMethod(String path, String method) {
    }
}
