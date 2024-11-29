
package com.sos.joc.model.monitoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.monitoring.enums.EntryItemSource;
import com.sos.joc.model.monitoring.enums.TotalRunningTimeSource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * origin of item properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "item",
    "totalRunningTime"
})
public class AgentItemEntryItemSource {

    /**
     * Origin of the entry
     * <p>
     * 
     * 
     */
    @JsonProperty("item")
    private EntryItemSource item;
    /**
     * Origin of the lastKnownTime
     * <p>
     * 
     * 
     */
    @JsonProperty("totalRunningTime")
    private TotalRunningTimeSource totalRunningTime;

    /**
     * Origin of the entry
     * <p>
     * 
     * 
     */
    @JsonProperty("item")
    public EntryItemSource getItem() {
        return item;
    }

    /**
     * Origin of the entry
     * <p>
     * 
     * 
     */
    @JsonProperty("item")
    public void setItem(EntryItemSource item) {
        this.item = item;
    }

    /**
     * Origin of the lastKnownTime
     * <p>
     * 
     * 
     */
    @JsonProperty("totalRunningTime")
    public TotalRunningTimeSource getTotalRunningTime() {
        return totalRunningTime;
    }

    /**
     * Origin of the lastKnownTime
     * <p>
     * 
     * 
     */
    @JsonProperty("totalRunningTime")
    public void setTotalRunningTime(TotalRunningTimeSource totalRunningTime) {
        this.totalRunningTime = totalRunningTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("item", item).append("totalRunningTime", totalRunningTime).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(item).append(totalRunningTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentItemEntryItemSource) == false) {
            return false;
        }
        AgentItemEntryItemSource rhs = ((AgentItemEntryItemSource) other);
        return new EqualsBuilder().append(item, rhs.item).append(totalRunningTime, rhs.totalRunningTime).isEquals();
    }

}
