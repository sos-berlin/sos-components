
package com.sos.joc.model.tag.group;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * common group request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "group"
})
public class RequestFilter {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("group")
    private String group;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("group")
    public String getGroup() {
        return group;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("group")
    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("group", group).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(group).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFilter) == false) {
            return false;
        }
        RequestFilter rhs = ((RequestFilter) other);
        return new EqualsBuilder().append(group, rhs.group).isEquals();
    }

}
