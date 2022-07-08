
package com.sos.joc.model.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * read notification
 * <p>
 * forceRelease for notification
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "name",
    "forceRelease"
})
public class ReadNotificationFilter {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("forceRelease")
    private Boolean forceRelease;

    /**
     * controllerId
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
     * controllerId
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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("forceRelease")
    public Boolean getForceRelease() {
        return forceRelease;
    }

    @JsonProperty("forceRelease")
    public void setForceRelease(Boolean forceRelease) {
        this.forceRelease = forceRelease;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("name", name).append("forceRelease", forceRelease).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(forceRelease).append(controllerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadNotificationFilter) == false) {
            return false;
        }
        ReadNotificationFilter rhs = ((ReadNotificationFilter) other);
        return new EqualsBuilder().append(name, rhs.name).append(forceRelease, rhs.forceRelease).append(controllerId, rhs.controllerId).isEquals();
    }

}
