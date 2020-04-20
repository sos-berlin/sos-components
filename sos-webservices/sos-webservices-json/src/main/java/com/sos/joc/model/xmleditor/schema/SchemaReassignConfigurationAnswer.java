
package com.sos.joc.model.xmleditor.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor reassign schema configuration answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "schema",
    "schemaIdentifier",
    "configurationJson",
    "recreateJson"
})
public class SchemaReassignConfigurationAnswer {

    @JsonProperty("schema")
    private String schema;
    @JsonProperty("schemaIdentifier")
    private String schemaIdentifier;
    @JsonProperty("configurationJson")
    private String configurationJson;
    @JsonProperty("recreateJson")
    private Boolean recreateJson;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("schema", schema).append("schemaIdentifier", schemaIdentifier).append("configurationJson", configurationJson).append("recreateJson", recreateJson).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schema).append(configurationJson).append(recreateJson).append(schemaIdentifier).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SchemaReassignConfigurationAnswer) == false) {
            return false;
        }
        SchemaReassignConfigurationAnswer rhs = ((SchemaReassignConfigurationAnswer) other);
        return new EqualsBuilder().append(schema, rhs.schema).append(configurationJson, rhs.configurationJson).append(recreateJson, rhs.recreateJson).append(schemaIdentifier, rhs.schemaIdentifier).isEquals();
    }

}
