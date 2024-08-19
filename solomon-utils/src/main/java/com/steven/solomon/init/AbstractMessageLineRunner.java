package com.steven.solomon.init;

import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMessageLineRunner<T extends Annotation> implements CommandLineRunner {

    protected final Logger logger = LoggerUtils.logger(getClass());

    @Override
    public void run(String... args) throws Exception {
        ResolvableType resolvableType = ResolvableType.forClass(getClass());
        String clazzName = resolvableType.getSuperType().getSuperType().getGeneric(0).getType().getTypeName();
        Class<T> clazz = (Class<T>) Class.forName(clazzName);
        List<Object> clazzList = new ArrayList<>(SpringUtil.getBeansWithAnnotation(clazz).values());
        if(ValidateUtils.isEmpty(clazzList)){
            logger.debug("AbstractMessageLineRunner:没有{}消费者",clazz.getSimpleName());
            return;
        }
        this.init(clazzList);
    }

    public abstract void init(List<Object> clazzList) throws Exception;
}
