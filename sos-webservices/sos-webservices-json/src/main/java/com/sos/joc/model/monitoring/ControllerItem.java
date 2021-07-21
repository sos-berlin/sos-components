
package com.sos.joc.model.monitoring;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * notification object in monitoring notifications collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "entries"
})
public class ControllerItem {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entries")
    private List<ControllerItemEntryItem> entries = new ArrayList<ControllerItemEntryItem>();

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entries")
    public List<ControllerItemEntryItem> getEntries() {
        return entries;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entries")
    public void setEntries(List<ControllerItemEntryItem> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("entries", entries).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(entries).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerItem) == false) {
            return false;
        }
        ControllerItem rhs = ((ControllerItem) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(entries, rhs.entries).isEquals();
    }

}
