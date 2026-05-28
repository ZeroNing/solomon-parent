package com.steven.solomon.base.exception;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.verification.ValidateUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * 全局异常处理基础工具类。
 *
 * <p>提供统一的异常捕获与响应封装能力，将各类异常翻译为包含
 * 错误码、国际化消息、请求ID和服务标识的标准 {@link BaseExceptionVO} 结构。</p>
 *
 * <p>本类为工具类，仅提供静态方法，不注册为 Spring Bean；
 * 具体的异常分发逻辑由业务侧的 ControllerAdvice 或 GatewayFilter 调用。</p>
 */
public class BaseGlobalExceptionHandler {

  /** 私有构造函数，防止实例化。 */
  private BaseGlobalExceptionHandler() {
  }

  /**
   * 异常处理核心逻辑：将异常翻译为标准响应体。
   *
   * @param ex      捕获的异常
   * @param serverId 服务标识，用于追踪异常来源
   * @param locale   国际化语言区域
   * @return 包含错误码、消息、请求ID的异常响应体
   */
  private static BaseExceptionVO handler(Throwable ex, String serverId, Locale locale) {
    // 获取异常类的简单名称，用于匹配对应的异常处理器
    String exceptionSimpleName = ex.getClass().getSimpleName();
    // 通过异常名称查找注册的处理器，生成初始响应体
    BaseExceptionVO baseExceptionVO = ExceptionUtil.getBaseExceptionVO(exceptionSimpleName, ex);
    // 从线程上下文中获取请求ID，若无则默认为"0"
    String requestId = ValidateUtils.getOrDefault(ExceptionUtil.requestId.get(), "0");

    // 设置服务标识、语言区域和请求ID
    baseExceptionVO.setServerId(serverId);
    baseExceptionVO.setLocale(locale);
    baseExceptionVO.setRequestId(requestId);
    // 若响应体中无消息，则通过国际化工具解析
    if (ValidateUtils.isEmpty(baseExceptionVO.getMessage())) {
      baseExceptionVO.setMessage(resolveMessage(baseExceptionVO, locale));
    }
    // 清除线程变量中的请求ID，防止内存泄漏
    ExceptionUtil.requestId.remove();
    return baseExceptionVO;
  }

  /**
   * 异常处理结果封装为 Map。
   *
   * <p>将异常响应体转换为包含 HTTP状态码、错误码、消息、服务标识和请求ID的 Map 结构，
   * 适合直接写入 HTTP 响应体。</p>
   *
   * @param ex      捕获的异常
   * @param serverId 服务标识
   * @param locale   国际化语言区域
   * @return 包含异常信息的 Map
   */
  public static Map<String, Object> handlerMap(Throwable ex, String serverId, Locale locale) {
    Map<String, Object> result = new HashMap<>(5);
    BaseExceptionVO baseExceptionVO = handler(ex, serverId, locale);
    // 将异常响应体的关键字段写入 Map
    result.put(BaseCode.HTTP_STATUS,
        ValidateUtils.getOrDefault(baseExceptionVO.getStatusCode(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()));
    result.put(BaseCode.ERROR_CODE, baseExceptionVO.getCode());
    result.put(BaseCode.MESSAGE, baseExceptionVO.getMessage());
    result.put(BaseCode.SERVER_ID, serverId);
    result.put(BaseCode.REQUEST_ID, baseExceptionVO.getRequestId());
    return result;
  }

  /**
   * 异常处理结果封装为 Map，并设置 HTTP 响应状态码。
   *
   * @param ex        捕获的异常
   * @param serverId  服务标识
   * @param locale    国际化语言区域
   * @param response  HTTP 响应对象，用于设置状态码
   * @return 包含异常信息的 Map
   */
  public static Map<String, Object> handlerMap(Throwable ex, String serverId, Locale locale,
      HttpServletResponse response) {
    Map<String, Object> map = handlerMap(ex, serverId, locale);
    // 从 Map 中提取 HTTP 状态码并设置到响应中
    response.setStatus(
        (Integer) map.getOrDefault(BaseCode.HTTP_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    return map;
  }

  /**
   * 通过国际化工具解析异常消息。
   *
   * @param baseExceptionVO 异常响应体，提供错误码和参数
   * @param locale          国际化语言区域
   * @return 国际化后的异常消息
   */
  private static String resolveMessage(BaseExceptionVO baseExceptionVO, Locale locale) {
    if (ValidateUtils.isNotEmpty(locale)) {
      // 指定语言区域时，使用带 locale 的国际化消息解析
      return I18nUtils.getErrorMessage(baseExceptionVO.getCode(), locale, baseExceptionVO.getArg());
    }
    // 未指定语言区域时，使用默认语言的消息解析
    return I18nUtils.getErrorMessage(baseExceptionVO.getCode(), baseExceptionVO.getArg());
  }
}
