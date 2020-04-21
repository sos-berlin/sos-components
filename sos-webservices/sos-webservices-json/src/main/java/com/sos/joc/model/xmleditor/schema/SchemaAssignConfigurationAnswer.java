
package com.sos.joc.model.xmleditor.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor assign schema configuration answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "schema",
    "schemaIdentifier"
})
public class SchemaAssignConfigurationAnswer {

    @JsonProperty("schema")
    private String schema;
    @JsonProperty("schemaIdentifier")
    private String schemaIdentifier;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("schema", schema).append("schemaIdentifier", schemaIdentifier).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schema).append(schemaIdentifier).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SchemaAssignConfigurationAnswer) == false) {
            return false;
        }
        SchemaAssignConfigurationAnswer rhs = ((SchemaAssignConfigurationAnswer) other);
        return new EqualsBuilder().append(schema, rhs.schema).append(schemaIdentifier, rhs.schemaIdentifier).isEquals();
    }

}
