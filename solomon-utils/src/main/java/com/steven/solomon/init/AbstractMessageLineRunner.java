package com.steven.solomon.init;

import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

public abstract class AbstractMessageLineRunner implements CommandLineRunner {

    protected final Logger logger = LoggerUtils.logger(getClass());

    @Override
    public void run(String... args) throws Exception {
        this.init(getQueueClazzList());
    }

    public abstract void init(List<Object> clazzList) throws Exception;

    public abstract List<Object> getQueueClazzList();
}
