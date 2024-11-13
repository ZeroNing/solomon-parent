package com.steven.solomon.http;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.steven.solomon.verification.ValidateUtils;

public class HttpUtils extends HttpUtil {

    public static HttpRequest initRequest(Method method, String url, ContentType contentType) {
        HttpRequest request = createRequest(method,url);
        if(ValidateUtils.isNotEmpty(contentType)){
            request.contentType(contentType.getValue());
        }
        return request;
    }

    public static HttpRequest initRequest(Method method, String url) {
        return initRequest(method,url,null);
    }
}
