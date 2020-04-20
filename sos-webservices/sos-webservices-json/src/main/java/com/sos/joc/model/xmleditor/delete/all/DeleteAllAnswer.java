
package com.sos.joc.model.xmleditor.delete.all;

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
    "deleted"
})
public class DeleteAllAnswer {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deleted")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deleted;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deleted", deleted).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deleted).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteAllAnswer) == false) {
            return false;
        }
        DeleteAllAnswer rhs = ((DeleteAllAnswer) other);
        return new EqualsBuilder().append(deleted, rhs.deleted).isEquals();
    }

}
