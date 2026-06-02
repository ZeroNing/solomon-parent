package com.steven.solomon.config;

import com.steven.solomon.enums.FileChoiceEnum;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * S3 协议兼容存储条件。
 *
 * <p>很多云厂商直接兼容 S3 协议，不需要单独 SDK；这些供应商统一复用 {@code solomon-amazon-s3} 模块。</p>
 */
public class S3CompatibleStorageCondition implements Condition {

  /** 兼容 S3 协议的供应商枚举集合。 */
  private static final Set<FileChoiceEnum> S3_COMPATIBLE_CHOICES = EnumSet.of(
      FileChoiceEnum.S3,
      FileChoiceEnum.KODO,
      FileChoiceEnum.ZOS,
      FileChoiceEnum.KS3,
      FileChoiceEnum.EOS,
      FileChoiceEnum.NOS,
      FileChoiceEnum.B2,
      FileChoiceEnum.JD,
      FileChoiceEnum.YANDEX,
      FileChoiceEnum.AMAZON,
      FileChoiceEnum.SHARKTECH,
      FileChoiceEnum.DIDI,
      FileChoiceEnum.BOTO3,
      FileChoiceEnum.TOS,
      FileChoiceEnum.R2,
      FileChoiceEnum.GOOGLE_CLOUD_STORAGE,
      FileChoiceEnum.UOS,
      FileChoiceEnum.AZURE,
      FileChoiceEnum.INSPUR
  );

  /**
   * 判断当前配置是否匹配 S3 兼容存储供应商。
   *
   * @param context  条件上下文
   * @param metadata 注解元数据
   * @return 如果 {@code file.choice} 属于 S3 兼容供应商集合则返回 {@code true}
   */
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String choice = context.getEnvironment().getProperty("file.choice");
    if (choice == null || choice.isBlank()) {
      return false;
    }
    try {
      return S3_COMPATIBLE_CHOICES.contains(FileChoiceEnum.valueOf(choice.toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
