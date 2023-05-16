
package com.sos.joc.model.history.order.cycle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Moved
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "next",
    "end"
})
public class CyclePrepared {

    @JsonProperty("next")
    private String next;
    @JsonProperty("end")
    private String end;

    @JsonProperty("next")
    public String getNext() {
        return next;
    }

    @JsonProperty("next")
    public void setNext(String next) {
        this.next = next;
    }

    @JsonProperty("end")
    public String getEnd() {
        return end;
    }

    @JsonProperty("end")
    public void setEnd(String end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("next", next).append("end", end).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(next).append(end).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CyclePrepared) == false) {
            return false;
        }
        CyclePrepared rhs = ((CyclePrepared) other);
        return new EqualsBuilder().append(next, rhs.next).append(end, rhs.end).isEquals();
    }

}
