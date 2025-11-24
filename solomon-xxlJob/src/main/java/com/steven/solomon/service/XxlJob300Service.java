package com.steven.solomon.service;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.code.XxlJobErrorCode;
import com.steven.solomon.config.XxlJobCondition;
import com.steven.solomon.entity.XxlJobInfo;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.properties.XxlJobProperties;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("3.0.0")
@Conditional(XxlJobCondition.class)
@Import(XxlJobProperties.class)
public class XxlJob300Service extends CommonXxlJobService{

    protected XxlJob300Service(XxlJobProperties profile, ApplicationContext applicationContext) {
        super(profile, applicationContext);
    }

    @Override
    protected String getLoginUrl() {
        return adminAddresses + "login";
    }

}
