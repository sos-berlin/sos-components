
package com.sos.joc.model.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.report.TemplateId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * template
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "templateName",
    "isSupported",
    "title",
    "data"
})
public class Template {

    /**
     * Template identifier for report
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateName")
    private TemplateId templateName;
    @JsonProperty("isSupported")
    private Boolean isSupported = true;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    @JsonProperty("data")
    private TemplateData data;


    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("templateId")
    public void setTemplateId(Integer templateId) {
        this.templateName = TemplateId.fromValue(templateId);
    }

    /**
     * Template identifier for report
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateName")
    public TemplateId getTemplateName() {
        return templateName;
    }

    /**
     * Template identifier for report
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateName")
    public void setTemplateName(TemplateId templateName) {
        this.templateName = templateName;
    }

    @JsonProperty("isSupported")
    public Boolean getIsSupported() {
        return isSupported;
    }

    @JsonProperty("isSupported")
    public void setIsSupported(Boolean isSupported) {
        this.isSupported = isSupported;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("data")
    public TemplateData getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(TemplateData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("templateName", templateName).append("isSupported", isSupported).append("title", title).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isSupported).append(title).append(data).append(templateName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Template) == false) {
            return false;
        }
        Template rhs = ((Template) other);
        return new EqualsBuilder().append(isSupported, rhs.isSupported).append(title, rhs.title).append(data, rhs.data).append(templateName, rhs.templateName).isEquals();
    }

}
