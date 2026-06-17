package com.steven.solomon.security.permission;

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
     * 执行权限扫描，返回扫描到的权限列表并保存到存储。
     *
     * @return 权限元数据列表
     */
    public List<PermissionInfo> scan() {
        Set<PermissionInfo> result = new LinkedHashSet<>();
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

    private String extractBasePath(Class<?> beanType) {
        RequestMapping classMapping = AnnotationUtils.findAnnotation(beanType, RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            return classMapping.value()[0];
        }
        return "";
    }

    private List<PathMethod> extractPathMappings(Method method, String basePath) {
        List<PathMethod> result = new ArrayList<>();
        RequestMapping reqMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (reqMapping != null) {
            String[] paths = reqMapping.value().length > 0 ? reqMapping.value() : new String[]{basePath};
            for (String path : paths) {
                if (reqMapping.method().length == 0) {
                    result.add(new PathMethod(joinPath(basePath, path), "GET"));
                } else {
                    for (RequestMethod rm : reqMapping.method()) {
                        result.add(new PathMethod(joinPath(basePath, path), rm.name()));
                    }
                }
            }
        }
        addSimpleMapping(method, GetMapping.class, "GET", basePath, result);
        addSimpleMapping(method, PostMapping.class, "POST", basePath, result);
        addSimpleMapping(method, PutMapping.class, "PUT", basePath, result);
        addSimpleMapping(method, DeleteMapping.class, "DELETE", basePath, result);
        addSimpleMapping(method, PatchMapping.class, "PATCH", basePath, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private void addSimpleMapping(Method method, Class<?> annotationType, String httpMethod,
                                  String basePath, List<PathMethod> result) {
        // 用 Java 原生反射获取注解，避免 Spring AnnotationUtils 的泛型约束问题
        Object annotation = null;
        for (java.lang.annotation.Annotation a : method.getAnnotations()) {
            if (a.annotationType() == annotationType) {
                annotation = a;
                break;
            }
        }
        if (annotation == null) {
            return;
        }
        // 通过反射调用注解的 value() 方法获取路径
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
            // 注解没有 value() 方法时，按空路径处理
            result.add(new PathMethod(joinPath(basePath, ""), httpMethod));
        }
    }

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

    private record PathMethod(String path, String method) {
    }
}
