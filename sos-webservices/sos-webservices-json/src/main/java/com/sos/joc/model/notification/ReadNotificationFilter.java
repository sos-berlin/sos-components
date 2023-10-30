
package com.sos.joc.model.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * read notification
 * <p>
 * Request Filter to read a notification
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "forceRelease"
})
public class ReadNotificationFilter {

    @JsonProperty("forceRelease")
    private Boolean forceRelease = false;

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
        return new ToStringBuilder(this).append("forceRelease", forceRelease).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(forceRelease).toHashCode();
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
        return new EqualsBuilder().append(forceRelease, rhs.forceRelease).isEquals();
    }

}
