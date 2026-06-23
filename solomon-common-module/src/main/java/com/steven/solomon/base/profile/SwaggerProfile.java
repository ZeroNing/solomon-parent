package com.steven.solomon.base.profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Solomon 接口文档配置项。
 *
 * <p>配置前缀为 {@code solomon.swagger}。Springdoc 自身的接口扫描、分组和页面路径
 * 仍然使用 {@code springdoc.*}，这里仅保存 Solomon 对接口文档做增强时需要的配置。</p>
 */
@ConfigurationProperties("solomon.swagger")
@Validated
public class SwaggerProfile {

  /**
   * 是否启用 Solomon 的 OpenAPI 增强配置。
   */
  private boolean enabled = true;

  /**
   * OpenAPI 文档标题。
   */
  @NotBlank(message = "solomon.swagger.title must not be blank")
  private String title = "Solomon API";

  /**
   * OpenAPI 文档版本。
   */
  @NotBlank(message = "solomon.swagger.version must not be blank")
  private String version = "1.0.0";

  /**
   * OpenAPI 文档描述。
   */
  private String description = "";

  /**
   * 需要写入每个接口的全局请求参数，例如 token、tenantCode、language。
   */
  private List<@Valid DocRequestParameter> globalRequestParameters = new ArrayList<>();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<DocRequestParameter> getGlobalRequestParameters() {
    return globalRequestParameters;
  }

  public void setGlobalRequestParameters(List<DocRequestParameter> globalRequestParameters) {
    this.globalRequestParameters = globalRequestParameters;
  }

  /**
   * OpenAPI 全局请求参数配置。
   */
  public static class DocRequestParameter implements Serializable {

    private static final long serialVersionUID = -5307736496899117806L;

    /**
     * 参数名称，例如 token。
     */
    @NotBlank(message = "solomon.swagger.global-request-parameters[].name must not be blank")
    private String name;

    /**
     * 参数位置，支持 header、query、path、cookie。
     */
    @Pattern(regexp = "header|query|path|cookie", message = "solomon.swagger.global-request-parameters[].in must be one of header, query, path, cookie")
    private String in = "header";

    /**
     * 参数说明，会展示在 Swagger UI / Knife4j 页面中。
     */
    private String description;

    /**
     * 是否必填。
     */
    private boolean required;

    /**
     * 是否隐藏；隐藏后不会写入 OpenAPI 文档。
     */
    private boolean hidden;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getIn() {
      return in;
    }

    public void setIn(String in) {
      this.in = in;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public boolean isRequired() {
      return required;
    }

    public void setRequired(boolean required) {
      this.required = required;
    }

    public boolean isHidden() {
      return hidden;
    }

    public void setHidden(boolean hidden) {
      this.hidden = hidden;
    }
  }
}
