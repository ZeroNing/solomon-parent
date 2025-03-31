package com.steven.solomon.base.profile;

import org.springframework.boot.context.properties.ConfigurationProperties;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;

import java.io.Serializable;
import java.util.List;

@ConfigurationProperties("doc")
public class SwaggerProfile {

    /**
     * 文档名称
     */
    private String title;
    /**
     * 是否启用文档
     */
    private boolean enabled;

    private List<DocRequestParameter> globalRequestParameters;

    public static class DocRequestParameter implements Serializable {
        /**
         * 参数名
         */
        private String name;
        /**
         * 参数位置类型
         */
        private ParameterType in;
        /**
         * 参数描述说明
         */
        private String description;
        /**
         * 是否必填参数
         */
        private Boolean required;
        /**
         * 是否已弃用参数
         */
        private Boolean deprecated;
        /**
         * 是否隐藏参数
         */
        private Boolean hidden;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ParameterType getIn() {
            return in;
        }

        public void setIn(ParameterType in) {
            this.in = in;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }

        public Boolean getDeprecated() {
            return deprecated;
        }

        public void setDeprecated(Boolean deprecated) {
            this.deprecated = deprecated;
        }

        public Boolean getHidden() {
            return hidden;
        }

        public void setHidden(Boolean hidden) {
            this.hidden = hidden;
        }
    }

    public List<DocRequestParameter> getGlobalRequestParameters() {
        return globalRequestParameters;
    }

    public void setGlobalRequestParameters(List<DocRequestParameter> globalRequestParameters) {
        this.globalRequestParameters = globalRequestParameters;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
