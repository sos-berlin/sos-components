
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
    "url",
    "previousEntry",
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("url")
    private String url;
    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("previousEntry")
    private ControllerItemEntryItem previousEntry;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("previousEntry")
    public ControllerItemEntryItem getPreviousEntry() {
        return previousEntry;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("previousEntry")
    public void setPreviousEntry(ControllerItemEntryItem previousEntry) {
        this.previousEntry = previousEntry;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("url", url).append("previousEntry", previousEntry).append("entries", entries).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(entries).append(controllerId).append(url).append(previousEntry).toHashCode();
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
        return new EqualsBuilder().append(entries, rhs.entries).append(controllerId, rhs.controllerId).append(url, rhs.url).append(previousEntry, rhs.previousEntry).isEquals();
    }

}
