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
 */
public class CacheExpressionResolver {

  private final ExpressionParser expressionParser = new SpelExpressionParser();

  private final ParameterNameDiscoverer parameterNameDiscoverer =
      new DefaultParameterNameDiscoverer();

  public String resolveKey(ProceedingJoinPoint joinPoint, String expression) {
    return resolveKey(joinPoint, expression, null);
  }

  public String resolveKey(ProceedingJoinPoint joinPoint, String expression, Object result) {
    if (hasText(expression)) {
      return resolveExpression(joinPoint, expression, result);
    }
    return defaultKey(joinPoint);
  }

  public boolean resolveCondition(ProceedingJoinPoint joinPoint, String expression,
      Object result, boolean defaultValue) {
    if (!hasText(expression)) {
      return defaultValue;
    }
    Object value = expressionParser.parseExpression(expression).getValue(context(joinPoint, result));
    return value instanceof Boolean bool ? bool : Boolean.parseBoolean(String.valueOf(value));
  }

  private String resolveExpression(ProceedingJoinPoint joinPoint, String expression, Object result) {
    Object value = resolveValue(joinPoint, expression, result);
    return value == null ? "" : String.valueOf(value);
  }

  private Object resolveValue(ProceedingJoinPoint joinPoint, String expression, Object result) {
    if (!isExpression(expression)) {
      return expression;
    }
    return expressionParser.parseExpression(expression).getValue(context(joinPoint, result));
  }

  private StandardEvaluationContext context(ProceedingJoinPoint joinPoint, Object result) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    StandardEvaluationContext context = new MethodBasedEvaluationContext(
        joinPoint.getTarget(), method, joinPoint.getArgs(), parameterNameDiscoverer);
    context.setVariable("result", result);
    return context;
  }

  private String defaultKey(ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String source = signature.getDeclaringTypeName()
        + "#"
        + signature.getMethod().getName()
        + Arrays.deepToString(joinPoint.getArgs());
    return CacheDigestUtils.sha256(source);
  }

  private boolean isExpression(String value) {
    return value.contains("#") || value.contains("'") || value.contains("T(");
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

}
