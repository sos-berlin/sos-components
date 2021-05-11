
package com.sos.joc.model.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * audit detail
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "type"
})
public class AuditLogDetailItem {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * audit object types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private ObjectType type;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * audit object types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public ObjectType getType() {
        return type;
    }

    /**
     * audit object types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(ObjectType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("type", type).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AuditLogDetailItem) == false) {
            return false;
        }
        AuditLogDetailItem rhs = ((AuditLogDetailItem) other);
        return new EqualsBuilder().append(type, rhs.type).append(path, rhs.path).isEquals();
    }

}
