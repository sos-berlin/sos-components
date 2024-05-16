
package com.sos.joc.model.reporting.result;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * report result data item
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflow_name",
    "start_time",
    "end_time",
    "order_state",
    "state",
    "duration"
})
public class ReportResultDataItem {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow_name")
    private String workflow_name;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("start_time")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date start_time;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("end_time")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date end_time;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("order_state")
    private Long order_state;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private Long state;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("duration")
    private Long duration;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow_name")
    public String getWorkflow_name() {
        return workflow_name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow_name")
    public void setWorkflow_name(String workflow_name) {
        this.workflow_name = workflow_name;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("start_time")
    public Date getStart_time() {
        return start_time;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("start_time")
    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("end_time")
    public Date getEnd_time() {
        return end_time;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("end_time")
    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("order_state")
    public Long getOrder_state() {
        return order_state;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("order_state")
    public void setOrder_state(Long order_state) {
        this.order_state = order_state;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public Long getState() {
        return state;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(Long state) {
        this.state = state;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("duration")
    public Long getDuration() {
        return duration;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("duration")
    public void setDuration(Long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("workflow_name", workflow_name).append("start_time", start_time).append("end_time", end_time).append("order_state", order_state).append("state", state).append("duration", duration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(duration).append(start_time).append(workflow_name).append(end_time).append(state).append(order_state).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportResultDataItem) == false) {
            return false;
        }
        ReportResultDataItem rhs = ((ReportResultDataItem) other);
        return new EqualsBuilder().append(duration, rhs.duration).append(start_time, rhs.start_time).append(workflow_name, rhs.workflow_name).append(end_time, rhs.end_time).append(state, rhs.state).append(order_state, rhs.order_state).isEquals();
    }

}
