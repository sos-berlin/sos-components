
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
    "readyTime",
    "couplingFailedTime",
    "couplingFailedMessage"
})
public class AgentItemEntryItem {

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
    @JsonProperty("couplingFailedTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date couplingFailedTime;
    @JsonProperty("couplingFailedMessage")
    private String couplingFailedMessage;

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
    @JsonProperty("couplingFailedTime")
    public Date getCouplingFailedTime() {
        return couplingFailedTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("couplingFailedTime")
    public void setCouplingFailedTime(Date couplingFailedTime) {
        this.couplingFailedTime = couplingFailedTime;
    }

    @JsonProperty("couplingFailedMessage")
    public String getCouplingFailedMessage() {
        return couplingFailedMessage;
    }

    @JsonProperty("couplingFailedMessage")
    public void setCouplingFailedMessage(String couplingFailedMessage) {
        this.couplingFailedMessage = couplingFailedMessage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("readyTime", readyTime).append("couplingFailedTime", couplingFailedTime).append("couplingFailedMessage", couplingFailedMessage).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(readyTime).append(couplingFailedTime).append(couplingFailedMessage).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentItemEntryItem) == false) {
            return false;
        }
        AgentItemEntryItem rhs = ((AgentItemEntryItem) other);
        return new EqualsBuilder().append(readyTime, rhs.readyTime).append(couplingFailedTime, rhs.couplingFailedTime).append(couplingFailedMessage, rhs.couplingFailedMessage).isEquals();
    }

}
