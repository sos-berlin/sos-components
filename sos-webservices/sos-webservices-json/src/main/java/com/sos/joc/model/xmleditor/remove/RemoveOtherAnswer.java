
package com.sos.joc.model.xmleditor.remove;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor delete other draft configuration answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "removed",
    "found"
})
public class RemoveOtherAnswer {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("removed")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date removed;
    @JsonProperty("found")
    private Boolean found;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("removed")
    public Date getRemoved() {
        return removed;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("removed")
    public void setRemoved(Date removed) {
        this.removed = removed;
    }

    @JsonProperty("found")
    public Boolean getFound() {
        return found;
    }

    @JsonProperty("found")
    public void setFound(Boolean found) {
        this.found = found;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("removed", removed).append("found", found).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(removed).append(found).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RemoveOtherAnswer) == false) {
            return false;
        }
        RemoveOtherAnswer rhs = ((RemoveOtherAnswer) other);
        return new EqualsBuilder().append(removed, rhs.removed).append(found, rhs.found).isEquals();
    }

}
