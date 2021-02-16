
package com.sos.joc.model.inventory.rename;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter for rename
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "newPath"
})
public class RequestFilter
    extends com.sos.joc.model.inventory.common.RequestFilter
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("newPath")
    private String newPath;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("newPath")
    public String getNewPath() {
        return newPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("newPath")
    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("newPath", newPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(newPath).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(newPath, rhs.newPath).isEquals();
    }

}
