package com.steven.solomon.init;

import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

public abstract class AbstractMessageLineRunner implements CommandLineRunner {

    protected final Logger logger = LoggerUtils.logger(getClass());

    @Override
    public void run(String... args) throws Exception {
        List<Object> clazzList = getQueueClazzList();
        if(ValidateUtils.isEmpty(clazzList)){
            logger.debug("AbstractMessageLineRunner:没有{}消费者",getName());
            return;
        }
        this.init(clazzList);
    }

    public abstract void init(List<Object> clazzList) throws Exception;

    public abstract List<Object> getQueueClazzList();

    public abstract String getName();
}
