
package com.sos.joc.model.monitoring;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order object in history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "totalRunningTime",
    "readyTime",
    "lastKnownTime"
})
public class ControllerItemEntryItem {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("totalRunningTime")
    private Long totalRunningTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("readyTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date readyTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastKnownTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date lastKnownTime;

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("totalRunningTime")
    public Long getTotalRunningTime() {
        return totalRunningTime;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("totalRunningTime")
    public void setTotalRunningTime(Long totalRunningTime) {
        this.totalRunningTime = totalRunningTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("readyTime")
    public Date getReadyTime() {
        return readyTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("readyTime")
    public void setReadyTime(Date readyTime) {
        this.readyTime = readyTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastKnownTime")
    public Date getLastKnownTime() {
        return lastKnownTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastKnownTime")
    public void setLastKnownTime(Date lastKnownTime) {
        this.lastKnownTime = lastKnownTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("totalRunningTime", totalRunningTime).append("readyTime", readyTime).append("lastKnownTime", lastKnownTime).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(lastKnownTime).append(readyTime).append(totalRunningTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerItemEntryItem) == false) {
            return false;
        }
        ControllerItemEntryItem rhs = ((ControllerItemEntryItem) other);
        return new EqualsBuilder().append(lastKnownTime, rhs.lastKnownTime).append(readyTime, rhs.readyTime).append(totalRunningTime, rhs.totalRunningTime).isEquals();
    }

}
