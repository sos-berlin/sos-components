
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
    "shutdownTime"
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
     * (Required)
     * 
     */
    @JsonProperty("shutdownTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date shutdownTime;

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
     * (Required)
     * 
     */
    @JsonProperty("shutdownTime")
    public Date getShutdownTime() {
        return shutdownTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("shutdownTime")
    public void setShutdownTime(Date shutdownTime) {
        this.shutdownTime = shutdownTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("totalRunningTime", totalRunningTime).append("readyTime", readyTime).append("shutdownTime", shutdownTime).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(readyTime).append(totalRunningTime).append(shutdownTime).toHashCode();
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
        return new EqualsBuilder().append(readyTime, rhs.readyTime).append(totalRunningTime, rhs.totalRunningTime).append(shutdownTime, rhs.shutdownTime).isEquals();
    }

}
