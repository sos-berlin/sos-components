
package com.sos.joc.model.xmleditor.read.other;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor read OTHER configuration answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configuration",
    "configurations",
    "schemas"
})
public class ReadOtherConfigurationAnswer {

    /**
     * xmleditor read configuration answer OTHER
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("")
    private AnswerConfiguration configuration;
    @JsonProperty("configurations")
    private Object configurations;
    @JsonProperty("schemas")
    private Object schemas;

    /**
     * xmleditor read configuration answer OTHER
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public AnswerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * xmleditor read configuration answer OTHER
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(AnswerConfiguration configuration) {
        this.configuration = configuration;
    }

    @JsonProperty("configurations")
    public Object getConfigurations() {
        return configurations;
    }

    @JsonProperty("configurations")
    public void setConfigurations(Object configurations) {
        this.configurations = configurations;
    }

    @JsonProperty("schemas")
    public Object getSchemas() {
        return schemas;
    }

    @JsonProperty("schemas")
    public void setSchemas(Object schemas) {
        this.schemas = schemas;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configuration", configuration).append("configurations", configurations).append("schemas", schemas).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configuration).append(configurations).append(schemas).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadOtherConfigurationAnswer) == false) {
            return false;
        }
        ReadOtherConfigurationAnswer rhs = ((ReadOtherConfigurationAnswer) other);
        return new EqualsBuilder().append(configuration, rhs.configuration).append(configurations, rhs.configurations).append(schemas, rhs.schemas).isEquals();
    }

}
