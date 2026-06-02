package com.steven.solomon.cache.aspect;

import com.steven.solomon.cache.util.CacheDigestUtils;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.context.expression.MethodBasedEvaluationContext;

/**
 * 缓存注解 SpEL 表达式解析器。
 *
 * <p>解析 {@link com.steven.solomon.cache.annotation.CacheResult} 等注解中的
 * SpEL 表达式，支持从方法参数和返回值中动态提取缓存 key 和条件判断。
 * 当表达式为空时自动根据类名、方法名和参数生成默认 key（SHA-256）。</p>
 */
public class CacheExpressionResolver {

  /** SpEL 表达式解析器实例。 */
  private final ExpressionParser expressionParser = new SpelExpressionParser();

  /** 参数名发现器，用于获取方法参数的变量名。 */
  private final ParameterNameDiscoverer parameterNameDiscoverer =
      new DefaultParameterNameDiscoverer();

  /**
   * 解析缓存 key，表达式为空时使用默认 key。
   *
   * @param joinPoint 连接点
   * @param expression SpEL key 表达式
   * @return 解析后的 key 字符串
   */
  public String resolveKey(ProceedingJoinPoint joinPoint, String expression) {
    return resolveKey(joinPoint, expression, null);
  }

  /**
   * 解析缓存 key，支持通过 result 引用方法返回值。
   *
   * @param joinPoint 连接点
   * @param expression SpEL key 表达式
   * @param result 方法执行结果（用于 after 场景的 key 解析）
   * @return 解析后的 key 字符串
   */
  public String resolveKey(ProceedingJoinPoint joinPoint, String expression, Object result) {
    if (hasText(expression)) {
      return resolveExpression(joinPoint, expression, result);
    }
    return defaultKey(joinPoint);
  }

  /**
   * 解析条件表达式（用于 condition 和 unless）。
   *
   * @param joinPoint   连接点
   * @param expression  SpEL 条件表达式
   * @param result      方法执行结果
   * @param defaultValue 表达式为空时的默认值
   * @return 条件计算结果
   */
  public boolean resolveCondition(ProceedingJoinPoint joinPoint, String expression,
      Object result, boolean defaultValue) {
    if (!hasText(expression)) {
      return defaultValue;
    }
    Object value = expressionParser.parseExpression(expression).getValue(context(joinPoint, result));
    return value instanceof Boolean bool ? bool : Boolean.parseBoolean(String.valueOf(value));
  }

  /** 解析 SpEL 表达式并转为字符串。 */
  private String resolveExpression(ProceedingJoinPoint joinPoint, String expression, Object result) {
    Object value = resolveValue(joinPoint, expression, result);
    return value == null ? "" : String.valueOf(value);
  }

  /** 解析 SpEL 表达式值，纯字符串不解析直接返回。 */
  private Object resolveValue(ProceedingJoinPoint joinPoint, String expression, Object result) {
    if (!isExpression(expression)) {
      return expression;
    }
    return expressionParser.parseExpression(expression).getValue(context(joinPoint, result));
  }

  /** 构建 SpEL 评估上下文，注入方法参数和 #result 变量。 */
  private StandardEvaluationContext context(ProceedingJoinPoint joinPoint, Object result) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    StandardEvaluationContext context = new MethodBasedEvaluationContext(
        joinPoint.getTarget(), method, joinPoint.getArgs(), parameterNameDiscoverer);
    context.setVariable("result", result);
    return context;
  }

  /**
   * 生成默认缓存 key：{类名#方法名[参数列表]} 的 SHA-256 摘要。
   */
  private String defaultKey(ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String source = signature.getDeclaringTypeName()
        + "#"
        + signature.getMethod().getName()
        + Arrays.deepToString(joinPoint.getArgs());
    return CacheDigestUtils.sha256(source);
  }

  /**
   * 判断是否为 SpEL 表达式（包含 #、单引号或 T() 语法）。
   */
  private boolean isExpression(String value) {
    return value.contains("#") || value.contains("'") || value.contains("T(");
  }

  /** 判断字符串是否为非空白。 */
  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

}
