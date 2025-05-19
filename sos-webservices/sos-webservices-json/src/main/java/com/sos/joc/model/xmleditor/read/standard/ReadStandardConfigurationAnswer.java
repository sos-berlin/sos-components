
package com.sos.joc.model.xmleditor.read.standard;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.xmleditor.validate.ValidateConfigurationAnswer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * xmleditor read standard configuration (YADE, NOTIFICATION) answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "configuration",
    "configurationJson",
    "recreateJson",
    "schema",
    "schemaIdentifier",
    "state",
    "released",
    "hasReleases",
    "validation",
    "configurationDate"
})
public class ReadStandardConfigurationAnswer {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
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
     * version state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private ItemStateEnum state;
    @JsonProperty("released")
    private Boolean released;
    @JsonProperty("hasReleases")
    private Boolean hasReleases;
    /**
     * xmleditor validate configuration answer
     * <p>
     * 
     * 
     */
    @JsonProperty("validation")
    @JsonPropertyDescription("")
    private ValidateConfigurationAnswer validation;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date configurationDate;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
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
     * version state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public ItemStateEnum getState() {
        return state;
    }

    /**
     * version state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(ItemStateEnum state) {
        this.state = state;
    }

    @JsonProperty("released")
    public Boolean getReleased() {
        return released;
    }

    @JsonProperty("released")
    public void setReleased(Boolean released) {
        this.released = released;
    }

    @JsonProperty("hasReleases")
    public Boolean getHasReleases() {
        return hasReleases;
    }

    @JsonProperty("hasReleases")
    public void setHasReleases(Boolean hasReleases) {
        this.hasReleases = hasReleases;
    }

    /**
     * xmleditor validate configuration answer
     * <p>
     * 
     * 
     */
    @JsonProperty("validation")
    public ValidateConfigurationAnswer getValidation() {
        return validation;
    }

    /**
     * xmleditor validate configuration answer
     * <p>
     * 
     * 
     */
    @JsonProperty("validation")
    public void setValidation(ValidateConfigurationAnswer validation) {
        this.validation = validation;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
    public Date getConfigurationDate() {
        return configurationDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
    public void setConfigurationDate(Date configurationDate) {
        this.configurationDate = configurationDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("name", name).append("configuration", configuration).append("configurationJson", configurationJson).append("recreateJson", recreateJson).append("schema", schema).append("schemaIdentifier", schemaIdentifier).append("state", state).append("released", released).append("hasReleases", hasReleases).append("validation", validation).append("configurationDate", configurationDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schema).append(configurationDate).append(configuration).append(name).append(configurationJson).append(recreateJson).append(id).append(schemaIdentifier).append(state).append(released).append(hasReleases).append(validation).toHashCode();
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
        return new EqualsBuilder().append(schema, rhs.schema).append(configurationDate, rhs.configurationDate).append(configuration, rhs.configuration).append(name, rhs.name).append(configurationJson, rhs.configurationJson).append(recreateJson, rhs.recreateJson).append(id, rhs.id).append(schemaIdentifier, rhs.schemaIdentifier).append(state, rhs.state).append(released, rhs.released).append(hasReleases, rhs.hasReleases).append(validation, rhs.validation).isEquals();
    }

}
