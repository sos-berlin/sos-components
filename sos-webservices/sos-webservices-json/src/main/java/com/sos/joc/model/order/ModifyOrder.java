
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.common.NameValuePair;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * modify order command
 * <p>
 * NOTE: orderId is required too except for add order
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderId",
    "workflow",
    "state",
    "endState",
    "at",
    "timeZone",
    "resume",
    "removeSetback",
    "title",
    "priority",
    "params",
    "runTime",
    "calendars"
})
public class ModifyOrder {

    @JsonProperty("orderId")
    private String orderId;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String workflow;
    /**
     * the name of the node
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("the name of the node")
    private String state;
    /**
     * the name of the end node
     * 
     */
    @JsonProperty("endState")
    @JsonPropertyDescription("the name of the end node")
    private String endState;
    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS
     * 
     */
    @JsonProperty("at")
    @JsonPropertyDescription("ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS")
    private String at;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * only useful when changing order state of suspended orders
     * 
     */
    @JsonProperty("resume")
    @JsonPropertyDescription("only useful when changing order state of suspended orders")
    private Boolean resume;
    /**
     * only useful when order has a setback
     * 
     */
    @JsonProperty("removeSetback")
    @JsonPropertyDescription("only useful when order has a setback")
    private Boolean removeSetback;
    @JsonProperty("title")
    private String title;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    private Integer priority;
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    /**
     * A run_time xml is expected which is specified in the <xsd:complexType name='run_time'> element of  http://www.sos-berlin.com/schema/scheduler.xsd
     * 
     */
    @JsonProperty("runTime")
    @JsonPropertyDescription("A run_time xml is expected which is specified in the <xsd:complexType name='run_time'> element of  http://www.sos-berlin.com/schema/scheduler.xsd")
    private String runTime;
    @JsonProperty("calendars")
    private List<Calendar> calendars = new ArrayList<Calendar>();

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public String getWorkflow() {
        return workflow;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    /**
     * the name of the node
     * 
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * the name of the node
     * 
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    /**
     * the name of the end node
     * 
     */
    @JsonProperty("endState")
    public String getEndState() {
        return endState;
    }

    /**
     * the name of the end node
     * 
     */
    @JsonProperty("endState")
    public void setEndState(String endState) {
        this.endState = endState;
    }

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS
     * 
     */
    @JsonProperty("at")
    public String getAt() {
        return at;
    }

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS
     * 
     */
    @JsonProperty("at")
    public void setAt(String at) {
        this.at = at;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * only useful when changing order state of suspended orders
     * 
     */
    @JsonProperty("resume")
    public Boolean getResume() {
        return resume;
    }

    /**
     * only useful when changing order state of suspended orders
     * 
     */
    @JsonProperty("resume")
    public void setResume(Boolean resume) {
        this.resume = resume;
    }

    /**
     * only useful when order has a setback
     * 
     */
    @JsonProperty("removeSetback")
    public Boolean getRemoveSetback() {
        return removeSetback;
    }

    /**
     * only useful when order has a setback
     * 
     */
    @JsonProperty("removeSetback")
    public void setRemoveSetback(Boolean removeSetback) {
        this.removeSetback = removeSetback;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public Integer getPriority() {
        return priority;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    public List<NameValuePair> getParams() {
        return params;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }

    /**
     * A run_time xml is expected which is specified in the <xsd:complexType name='run_time'> element of  http://www.sos-berlin.com/schema/scheduler.xsd
     * 
     */
    @JsonProperty("runTime")
    public String getRunTime() {
        return runTime;
    }

    /**
     * A run_time xml is expected which is specified in the <xsd:complexType name='run_time'> element of  http://www.sos-berlin.com/schema/scheduler.xsd
     * 
     */
    @JsonProperty("runTime")
    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    @JsonProperty("calendars")
    public List<Calendar> getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    public void setCalendars(List<Calendar> calendars) {
        this.calendars = calendars;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orderId", orderId).append("workflow", workflow).append("state", state).append("endState", endState).append("at", at).append("timeZone", timeZone).append("resume", resume).append("removeSetback", removeSetback).append("title", title).append("priority", priority).append("params", params).append("runTime", runTime).append("calendars", calendars).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(resume).append(workflow).append(orderId).append(endState).append(timeZone).append(title).append(priority).append(params).append(at).append(calendars).append(state).append(runTime).append(removeSetback).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModifyOrder) == false) {
            return false;
        }
        ModifyOrder rhs = ((ModifyOrder) other);
        return new EqualsBuilder().append(resume, rhs.resume).append(workflow, rhs.workflow).append(orderId, rhs.orderId).append(endState, rhs.endState).append(timeZone, rhs.timeZone).append(title, rhs.title).append(priority, rhs.priority).append(params, rhs.params).append(at, rhs.at).append(calendars, rhs.calendars).append(state, rhs.state).append(runTime, rhs.runTime).append(removeSetback, rhs.removeSetback).isEquals();
    }

}
