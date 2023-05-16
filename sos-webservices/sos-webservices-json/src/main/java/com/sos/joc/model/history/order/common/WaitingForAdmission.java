
package com.sos.joc.model.history.order.common;

import java.util.ArrayList;
import java.util.List;
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
    "entries"
})
public class WaitingForAdmission {

    @JsonProperty("entries")
    private List<String> entries = new ArrayList<String>();

    @JsonProperty("entries")
    public List<String> getEntries() {
        return entries;
    }

    @JsonProperty("entries")
    public void setEntries(List<String> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("entries", entries).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(entries).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WaitingForAdmission) == false) {
            return false;
        }
        WaitingForAdmission rhs = ((WaitingForAdmission) other);
        return new EqualsBuilder().append(entries, rhs.entries).isEquals();
    }

}
