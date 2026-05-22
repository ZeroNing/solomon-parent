package com.steven.solomon.base.profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Swagger/OpenAPI文档配置。
 *
 * <p>配置前缀为 {@code solomon.swagger}，用于设置接口文档标题、版本和全局请求参数。
 * Springdoc自身的页面路径、分组扫描仍然使用 {@code springdoc.*} 配置。</p>
 */
@ConfigurationProperties("solomon.swagger")
public class SwaggerProfile {

  /**
   * 是否启用Solomon文档增强配置。
   */
  private boolean enabled = true;

  /**
   * OpenAPI文档标题。
   */
  private String title = "Solomon API";

  /**
   * OpenAPI文档版本。
   */
  private String version = "1.0.0";

  /**
   * OpenAPI文档描述。
   */
  private String description = "";

  /**
   * 全局请求参数列表，常用于token、tenantCode等请求头。
   */
  private List<DocRequestParameter> globalRequestParameters = new ArrayList<>();

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
   * Swagger全局请求参数配置。
   */
  public static class DocRequestParameter implements Serializable {

    private static final long serialVersionUID = -5307736496899117806L;

    /**
     * 参数名称。
     */
    private String name;

    /**
     * 参数位置，支持header、query、path、cookie。
     */
    private String in = "header";

    /**
     * 参数说明。
     */
    private String description;

    /**
     * 是否必填。
     */
    private boolean required;

    /**
     * 是否隐藏；隐藏后不会写入OpenAPI文档。
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
