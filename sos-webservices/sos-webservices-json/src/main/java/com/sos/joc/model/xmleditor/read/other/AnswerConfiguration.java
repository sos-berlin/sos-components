
package com.sos.joc.model.xmleditor.read.other;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor read configuration answer OTHER
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "schema",
    "schemaIdentifier",
    "configuration",
    "configurationJson",
    "recreateJson",
    "state",
    "modified"
})
public class AnswerConfiguration {

    @JsonProperty("id")
    private Integer id;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("schema")
    private String schema;
    @JsonProperty("schemaIdentifier")
    private String schemaIdentifier;
    @JsonProperty("configuration")
    private String configuration;
    @JsonProperty("configurationJson")
    private String configurationJson;
    @JsonProperty("recreateJson")
    private Boolean recreateJson;
    /**
     * xmleditor read others configuration state answer
     * <p>
     * Describes the draft situation
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("Describes the draft situation")
    private AnswerConfigurationState state;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modified;

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("schema")
    public String getSchema() {
        return schema;
    }

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
     * xmleditor read others configuration state answer
     * <p>
     * Describes the draft situation
     * 
     */
    @JsonProperty("state")
    public AnswerConfigurationState getState() {
        return state;
    }

    /**
     * xmleditor read others configuration state answer
     * <p>
     * Describes the draft situation
     * 
     */
    @JsonProperty("state")
    public void setState(AnswerConfigurationState state) {
        this.state = state;
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
        return new ToStringBuilder(this).append("id", id).append("name", name).append("schema", schema).append("schemaIdentifier", schemaIdentifier).append("configuration", configuration).append("configurationJson", configurationJson).append("recreateJson", recreateJson).append("state", state).append("modified", modified).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schema).append(configuration).append(name).append(configurationJson).append(recreateJson).append(modified).append(id).append(schemaIdentifier).append(state).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AnswerConfiguration) == false) {
            return false;
        }
        AnswerConfiguration rhs = ((AnswerConfiguration) other);
        return new EqualsBuilder().append(schema, rhs.schema).append(configuration, rhs.configuration).append(name, rhs.name).append(configurationJson, rhs.configurationJson).append(recreateJson, rhs.recreateJson).append(modified, rhs.modified).append(id, rhs.id).append(schemaIdentifier, rhs.schemaIdentifier).append(state, rhs.state).isEquals();
    }

}
