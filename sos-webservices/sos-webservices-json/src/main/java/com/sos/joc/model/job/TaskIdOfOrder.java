
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    "position"
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    private String position;

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    public String getPosition() {
        return position;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("historyId", historyId).append("position", position).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(historyId).append(position).toHashCode();
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
        return new EqualsBuilder().append(historyId, rhs.historyId).append(position, rhs.position).isEquals();
    }

}
