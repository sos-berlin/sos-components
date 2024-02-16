
package com.sos.joc.model.reporting;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * template
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "templateId",
    "title",
    "data"
})
public class Template {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateId")
    private Integer templateId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("data")
    private TemplateData data;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateId")
    public Integer getTemplateId() {
        return templateId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateId")
    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }
    
    @JsonProperty("id")
    public void setId(String id) {
        this.templateId = Integer.valueOf(id.replaceAll("\\D", ""));
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

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
        return new ToStringBuilder(this).append("templateId", templateId).append("title", title).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(templateId).append(title).append(data).toHashCode();
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
        return new EqualsBuilder().append(templateId, rhs.templateId).append(title, rhs.title).append(data, rhs.data).isEquals();
    }

}
