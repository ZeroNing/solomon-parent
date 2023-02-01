package com.steven.solomon.utils;

import com.steven.solomon.constant.code.BaseCode;
import com.steven.solomon.verification.ValidateUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

public class LocaleUtils {

    @Value("${i18n.language}")
    public Locale DEFAULT_LOCALE;

    public Locale getLocale(ServerRequest serverRequest){
        return getLocale(serverRequest.headers().firstHeader(BaseCode.HTTP_ACCEPT_LANGUAGE));
    }

    public Locale getLocale(ServerWebExchange serverWebExchange){
        return getLocale(serverWebExchange.getRequest().getHeaders().getFirst(BaseCode.HTTP_ACCEPT_LANGUAGE));
    }

    private Locale getLocale(String language){
        if(ValidateUtils.isEmpty(language)){
            return DEFAULT_LOCALE;
        }
        Locale resultLocale = Locale.forLanguageTag(language);
        List<Locale> defaultLocaleList = Arrays.asList(Locale.CHINA,Locale.ENGLISH,Locale.CHINESE);
        if(ValidateUtils.isEmpty(resultLocale) || !defaultLocaleList.contains(resultLocale)){
            resultLocale = DEFAULT_LOCALE;
        }
        return resultLocale;
    }
}
