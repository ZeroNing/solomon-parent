package com.steven.solomon.init;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.TypeUtil;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMessageLineRunner<T extends Annotation> implements CommandLineRunner {

    protected final Logger logger = LoggerUtils.logger(getClass());

    @Override
    public void run(String... args) throws Exception {
        Type type = TypeUtil.toParameterizedType(getClass());
        Type[] typeArguments = TypeUtil.getTypeArguments(type);
        Class<T> clazz = ClassUtil.loadClass(typeArguments[0].getTypeName());

        List<Object> clazzList = new ArrayList<>(SpringUtil.getBeansWithAnnotation(clazz).values());
        if(ValidateUtils.isEmpty(clazzList)){
            logger.error("AbstractMessageLineRunner:没有{}消费者",clazz.getSimpleName());
            return;
        }
        this.init(clazzList);
    }

    public abstract void init(List<Object> clazzList) throws Exception;
}
