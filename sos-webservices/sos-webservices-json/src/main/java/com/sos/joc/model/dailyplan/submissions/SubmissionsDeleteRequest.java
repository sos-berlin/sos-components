
package com.sos.joc.model.dailyplan.submissions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * daily plan delete submissions request
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "filter"
})
public class SubmissionsDeleteRequest {

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
     * daily plan delete submissions request filter
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("filter")
    private SubmissionsDeleteRequestFilter filter;

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
     * daily plan delete submissions request filter
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("filter")
    public SubmissionsDeleteRequestFilter getFilter() {
        return filter;
    }

    /**
     * daily plan delete submissions request filter
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("filter")
    public void setFilter(SubmissionsDeleteRequestFilter filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("filter", filter).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(filter).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubmissionsDeleteRequest) == false) {
            return false;
        }
        SubmissionsDeleteRequest rhs = ((SubmissionsDeleteRequest) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(filter, rhs.filter).isEquals();
    }

}
