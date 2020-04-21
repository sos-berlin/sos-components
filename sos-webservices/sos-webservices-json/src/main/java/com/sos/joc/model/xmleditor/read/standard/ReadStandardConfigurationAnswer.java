
package com.sos.joc.model.xmleditor.read.standard;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.xmleditor.common.AnswerMessage;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor read standard configuration (YADE, NOTIFICATION) answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configuration",
    "configurationJson",
    "recreateJson",
    "schema",
    "schemaIdentifier",
    "state",
    "warning",
    "modified"
})
public class ReadStandardConfigurationAnswer {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    private String configuration;
    @JsonProperty("configurationJson")
    private String configurationJson;
    @JsonProperty("recreateJson")
    private Boolean recreateJson;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schema")
    private String schema;
    @JsonProperty("schemaIdentifier")
    private String schemaIdentifier;
    /**
     * xmleditor read standard configuration (YADE, NOTIFICATION) answer
     * <p>
     * Describes the situation live/draft
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("Describes the situation live/draft")
    private ReadStandardConfigurationAnswerState state;
    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("warning")
    private AnswerMessage warning;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    public String getConfiguration() {
        return configuration;
    }

    /**
     * 
     * (Required)
     * 
     */
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("schema")
    public String getSchema() {
        return schema;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schema")
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @JsonProperty("schemaIdentifier")
    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    @JsonProperty("schemaIdentifier")
    public void setSchemaIdentifier(String schemaIdentifier) {
        this.schemaIdentifier = schemaIdentifier;
    }

    /**
     * xmleditor read standard configuration (YADE, NOTIFICATION) answer
     * <p>
     * Describes the situation live/draft
     * (Required)
     * 
     */
    @JsonProperty("state")
    public ReadStandardConfigurationAnswerState getState() {
        return state;
    }

    /**
     * xmleditor read standard configuration (YADE, NOTIFICATION) answer
     * <p>
     * Describes the situation live/draft
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(ReadStandardConfigurationAnswerState state) {
        this.state = state;
    }

    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("warning")
    public AnswerMessage getWarning() {
        return warning;
    }

    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("warning")
    public void setWarning(AnswerMessage warning) {
        this.warning = warning;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configuration", configuration).append("configurationJson", configurationJson).append("recreateJson", recreateJson).append("schema", schema).append("schemaIdentifier", schemaIdentifier).append("state", state).append("warning", warning).append("modified", modified).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schema).append(configuration).append(configurationJson).append(recreateJson).append(warning).append(modified).append(schemaIdentifier).append(state).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadStandardConfigurationAnswer) == false) {
            return false;
        }
        ReadStandardConfigurationAnswer rhs = ((ReadStandardConfigurationAnswer) other);
        return new EqualsBuilder().append(schema, rhs.schema).append(configuration, rhs.configuration).append(configurationJson, rhs.configurationJson).append(recreateJson, rhs.recreateJson).append(warning, rhs.warning).append(modified, rhs.modified).append(schemaIdentifier, rhs.schemaIdentifier).append(state, rhs.state).isEquals();
    }

}
