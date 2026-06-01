package com.steven.solomon.utils;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.code.BaseCode;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * 国际化语言工具类
 * 用于从请求头中解析用户语言，提供多语言支持
 * 支持WebFlux环境下的ServerRequest和ServerWebExchange两种请求对象
 *
 * @author steven
 * @since 1.0.0
 */
public class LocaleUtils {

    /**
     * 默认语言，从配置文件i18n.language中读取
     */
    @Value("${i18n.language}")
    public Locale DEFAULT_LOCALE;

    /**
     * 从ServerRequest中解析语言
     *
     * @param serverRequest WebFlux请求对象
     * @return 解析后的Locale对象
     */
    public Locale getLocale(ServerRequest serverRequest) {
        // 从请求头中获取Accept-Language
        String acceptLanguage = serverRequest.headers().firstHeader(BaseCode.HTTP_ACCEPT_LANGUAGE);
        return getLocale(acceptLanguage);
    }

    /**
     * 从ServerWebExchange中解析语言
     *
     * @param serverWebExchange WebFlux请求上下文对象
     * @return 解析后的Locale对象
     */
    public Locale getLocale(ServerWebExchange serverWebExchange) {
        // 从请求头中获取Accept-Language
        String acceptLanguage = serverWebExchange.getRequest().getHeaders().getFirst(BaseCode.HTTP_ACCEPT_LANGUAGE);
        return getLocale(acceptLanguage);
    }

    /**
     * 根据Accept-Language头解析Locale
     * 仅支持中文、英文两种语言，不支持的语言使用默认语言
     *
     * @param language Accept-Language头内容
     * @return 解析后的Locale对象
     */
    private Locale getLocale(String language) {
        // 如果语言为空，返回默认语言
        if (ObjectUtil.isEmpty(language)) {
            return DEFAULT_LOCALE;
        }
        
        // 根据语言标签解析Locale
        Locale resultLocale = Locale.forLanguageTag(language);
        
        // 支持的语言列表：中文、英文
        List<Locale> supportedLocales = Arrays.asList(Locale.CHINA, Locale.ENGLISH, Locale.CHINESE);
        
        // 如果解析失败或者语言不支持，返回默认语言
        if (ObjectUtil.isEmpty(resultLocale) || !supportedLocales.contains(resultLocale)) {
            resultLocale = DEFAULT_LOCALE;
        }
        return resultLocale;
    }
}
