
package com.sos.joc.model.xmleditor.delete;

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
    "deleted",
    "found"
})
public class DeleteOtherDraftAnswer {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deleted")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deleted;
    @JsonProperty("found")
    private Boolean found;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deleted")
    public Date getDeleted() {
        return deleted;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deleted")
    public void setDeleted(Date deleted) {
        this.deleted = deleted;
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
        return new ToStringBuilder(this).append("deleted", deleted).append("found", found).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deleted).append(found).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteOtherDraftAnswer) == false) {
            return false;
        }
        DeleteOtherDraftAnswer rhs = ((DeleteOtherDraftAnswer) other);
        return new EqualsBuilder().append(deleted, rhs.deleted).append(found, rhs.found).isEquals();
    }

}
