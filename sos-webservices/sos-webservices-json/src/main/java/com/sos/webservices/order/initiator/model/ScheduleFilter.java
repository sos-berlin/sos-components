
package com.sos.webservices.order.initiator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Order Template Filter
 * <p>
 * The filter for the list of order template for scheduling orders to JobScheduler
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "schedulePath",
    "folder",
    "recursive"
})
public class ScheduleFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("schedulePath")
    private String schedulePath;
    @JsonProperty("folder")
    private String folder;
    @JsonProperty("recursive")
    private Boolean recursive;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("schedulePath")
    public String getSchedulePath() {
        return schedulePath;
    }

    @JsonProperty("schedulePath")
    public void setSchedulePath(String schedulePath) {
        this.schedulePath = schedulePath;
    }

    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    @JsonProperty("recursive")
    public Boolean getRecursive() {
        return recursive;
    }

    @JsonProperty("recursive")
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("schedulePath", schedulePath).append("folder", folder).append("recursive", recursive).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(controllerId).append(recursive).append(schedulePath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleFilter) == false) {
            return false;
        }
        ScheduleFilter rhs = ((ScheduleFilter) other);
        return new EqualsBuilder().append(folder, rhs.folder).append(controllerId, rhs.controllerId).append(recursive, rhs.recursive).append(schedulePath, rhs.schedulePath).isEquals();
    }

}
