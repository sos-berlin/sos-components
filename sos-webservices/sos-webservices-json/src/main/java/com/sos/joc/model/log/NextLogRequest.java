
package com.sos.joc.model.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * next log chunk
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "force"
})
public class NextLogRequest
    extends KeyedLogRequest
{

    /**
     * if true then a next chunk is sent even 'dateTo' or 'numOfLines' are reached
     * 
     */
    @JsonProperty("force")
    @JsonPropertyDescription("if true then a next chunk is sent even 'dateTo' or 'numOfLines' are reached")
    private Boolean force = false;

    /**
     * if true then a next chunk is sent even 'dateTo' or 'numOfLines' are reached
     * 
     */
    @JsonProperty("force")
    public Boolean getForce() {
        return force;
    }

    /**
     * if true then a next chunk is sent even 'dateTo' or 'numOfLines' are reached
     * 
     */
    @JsonProperty("force")
    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("force", force).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(force).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NextLogRequest) == false) {
            return false;
        }
        NextLogRequest rhs = ((NextLogRequest) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(force, rhs.force).isEquals();
    }

}
