
package com.sos.joc.model.history.order.sleep;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * sleep
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "until"
})
public class Sleep {

    @JsonProperty("until")
    private String until;

    @JsonProperty("until")
    public String getUntil() {
        return until;
    }

    @JsonProperty("until")
    public void setUntil(String until) {
        this.until = until;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("until", until).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(until).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Sleep) == false) {
            return false;
        }
        Sleep rhs = ((Sleep) other);
        return new EqualsBuilder().append(until, rhs.until).isEquals();
    }

}
