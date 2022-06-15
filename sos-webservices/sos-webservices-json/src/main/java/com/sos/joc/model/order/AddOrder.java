
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * add order command
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderName",
    "workflowPath",
    "scheduledFor",
    "timeZone",
    "arguments",
    "startPosition",
    "endPosition",
    "startPositionString",
    "endPositionString"
})
public class AddOrder {

    @JsonProperty("orderName")
    private String orderName;
    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    @JsonPropertyDescription("absolute path of an object.")
    private String workflowPath;
    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("scheduledFor")
    @JsonPropertyDescription("ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty")
    private String scheduledFor;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    private String timeZone;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables arguments;
    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> startPosition = new ArrayList<Object>();
    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("endPosition")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> endPosition = new ArrayList<Object>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("startPositionString")
    private String startPositionString;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("endPositionString")
    private String endPositionString;

    @JsonProperty("orderName")
    public String getOrderName() {
        return orderName;
    }

    @JsonProperty("orderName")
    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("scheduledFor")
    public String getScheduledFor() {
        return scheduledFor;
    }

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("scheduledFor")
    public void setScheduledFor(String scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public Variables getArguments() {
        return arguments;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Variables arguments) {
        this.arguments = arguments;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    public List<Object> getStartPosition() {
        return startPosition;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    public void setStartPosition(List<Object> startPosition) {
        this.startPosition = startPosition;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("endPosition")
    public List<Object> getEndPosition() {
        return endPosition;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("endPosition")
    public void setEndPosition(List<Object> endPosition) {
        this.endPosition = endPosition;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("startPositionString")
    public String getStartPositionString() {
        return startPositionString;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("startPositionString")
    public void setStartPositionString(String startPositionString) {
        this.startPositionString = startPositionString;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("endPositionString")
    public String getEndPositionString() {
        return endPositionString;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("endPositionString")
    public void setEndPositionString(String endPositionString) {
        this.endPositionString = endPositionString;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orderName", orderName).append("workflowPath", workflowPath).append("scheduledFor", scheduledFor).append("timeZone", timeZone).append("arguments", arguments).append("startPosition", startPosition).append("endPosition", endPosition).append("startPositionString", startPositionString).append("endPositionString", endPositionString).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(startPositionString).append(workflowPath).append(endPosition).append(endPositionString).append(scheduledFor).append(timeZone).append(arguments).append(startPosition).append(orderName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AddOrder) == false) {
            return false;
        }
        AddOrder rhs = ((AddOrder) other);
        return new EqualsBuilder().append(startPositionString, rhs.startPositionString).append(workflowPath, rhs.workflowPath).append(endPosition, rhs.endPosition).append(endPositionString, rhs.endPositionString).append(scheduledFor, rhs.scheduledFor).append(timeZone, rhs.timeZone).append(arguments, rhs.arguments).append(startPosition, rhs.startPosition).append(orderName, rhs.orderName).isEquals();
    }

}
