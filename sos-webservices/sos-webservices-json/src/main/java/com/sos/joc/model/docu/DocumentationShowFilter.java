
package com.sos.joc.model.docu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.JobSchedulerObjectType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Documentation content filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "type",
    "path"
})
public class DocumentationShowFilter {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * JobScheduler object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private JobSchedulerObjectType type;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * JobScheduler object type
     * <p>
     * 
     * (Required)
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
     * (Required)
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("type", type).append("path", path).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(controllerId).append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DocumentationShowFilter) == false) {
            return false;
        }
        DocumentationShowFilter rhs = ((DocumentationShowFilter) other);
        return new EqualsBuilder().append(path, rhs.path).append(controllerId, rhs.controllerId).append(type, rhs.type).isEquals();
    }

}
