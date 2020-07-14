
package com.sos.joc.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobScheduler object
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "path"
})
public class JobSchedulerObject {

    /**
     * JobScheduler object type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    private JobSchedulerObjectType type;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;

    /**
     * JobScheduler object type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public JobSchedulerObjectType getType() {
        return type;
    }

    /**
     * JobScheduler object type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public void setType(JobSchedulerObjectType type) {
        this.type = type;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("path", path).toString();
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
        if ((other instanceof JobSchedulerObject) == false) {
            return false;
        }
        JobSchedulerObject rhs = ((JobSchedulerObject) other);
        return new EqualsBuilder().append(type, rhs.type).append(path, rhs.path).isEquals();
    }

}
