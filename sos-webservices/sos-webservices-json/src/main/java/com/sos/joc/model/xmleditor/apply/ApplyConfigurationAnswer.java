
package com.sos.joc.model.xmleditor.apply;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.xmleditor.common.AnswerMessage;
import com.sos.joc.model.xmleditor.validate.ErrorMessage;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor apply configuration answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "schemaIdentifier",
    "configuration",
    "configurationJson",
    "recreateJson",
    "modified",
    "validationError",
    "message"
})
public class ApplyConfigurationAnswer {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("schemaIdentifier")
    private String schemaIdentifier;
    @JsonProperty("configuration")
    private String configuration;
    @JsonProperty("configurationJson")
    private String configurationJson;
    @JsonProperty("recreateJson")
    private Boolean recreateJson;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modified;
    /**
     * xmleditor validate configuration error answer
     * <p>
     * 
     * 
     */
    @JsonProperty("validationError")
    private ErrorMessage validationError;
    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    private AnswerMessage message;

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("schemaIdentifier")
    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    @JsonProperty("schemaIdentifier")
    public void setSchemaIdentifier(String schemaIdentifier) {
        this.schemaIdentifier = schemaIdentifier;
    }

    @JsonProperty("configuration")
    public String getConfiguration() {
        return configuration;
    }

    @JsonProperty("configuration")
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @JsonProperty("configurationJson")
    public String getConfigurationJson() {
        return configurationJson;
    }

    @JsonProperty("configurationJson")
    public void setConfigurationJson(String configurationJson) {
        this.configurationJson = configurationJson;
    }

    @JsonProperty("recreateJson")
    public Boolean getRecreateJson() {
        return recreateJson;
    }

    @JsonProperty("recreateJson")
    public void setRecreateJson(Boolean recreateJson) {
        this.recreateJson = recreateJson;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public Date getModified() {
        return modified;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * xmleditor validate configuration error answer
     * <p>
     * 
     * 
     */
    @JsonProperty("validationError")
    public ErrorMessage getValidationError() {
        return validationError;
    }

    /**
     * xmleditor validate configuration error answer
     * <p>
     * 
     * 
     */
    @JsonProperty("validationError")
    public void setValidationError(ErrorMessage validationError) {
        this.validationError = validationError;
    }

    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public AnswerMessage getMessage() {
        return message;
    }

    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public void setMessage(AnswerMessage message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("name", name).append("schemaIdentifier", schemaIdentifier).append("configuration", configuration).append("configurationJson", configurationJson).append("recreateJson", recreateJson).append("modified", modified).append("validationError", validationError).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configuration).append(name).append(configurationJson).append(recreateJson).append(modified).append(id).append(schemaIdentifier).append(message).append(validationError).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ApplyConfigurationAnswer) == false) {
            return false;
        }
        ApplyConfigurationAnswer rhs = ((ApplyConfigurationAnswer) other);
        return new EqualsBuilder().append(configuration, rhs.configuration).append(name, rhs.name).append(configurationJson, rhs.configurationJson).append(recreateJson, rhs.recreateJson).append(modified, rhs.modified).append(id, rhs.id).append(schemaIdentifier, rhs.schemaIdentifier).append(message, rhs.message).append(validationError, rhs.validationError).isEquals();
    }

}
