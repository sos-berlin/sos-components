
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * taskId of Order
 * <p>
 * Only relevant for order jobs; cause=order resp.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "historyId",
    "state"
})
public class TaskIdOfOrder {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    private Long historyId;
    /**
     * name of the job chain node
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("name of the job chain node")
    private String state;

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    public Long getHistoryId() {
        return historyId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    /**
     * name of the job chain node
     * (Required)
     * 
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * name of the job chain node
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("historyId", historyId).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(historyId).append(state).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TaskIdOfOrder) == false) {
            return false;
        }
        TaskIdOfOrder rhs = ((TaskIdOfOrder) other);
        return new EqualsBuilder().append(historyId, rhs.historyId).append(state, rhs.state).isEquals();
    }

}
