
package com.sos.joc.model.dailyplan;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * plannedOrders filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "calendarId",
    "submissionHistoryId",
    "states",
    "late",
    "withSubmit",
    "dailyPlanDate",
    "timeZone",
    "folders",
    "orderTemplates",
    "workflow",
    "orderId"
})
public class PlannedOrdersFilter {

    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarId")
    private Long calendarId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    private Long submissionHistoryId;
    @JsonProperty("states")
    private List<OrderStateText> states = null;
    @JsonProperty("late")
    private Boolean late;
    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    @JsonPropertyDescription("controls if the order should be submitted to the controller")
    private Boolean withSubmit = true;
    @JsonProperty("dailyPlanDate")
    private String dailyPlanDate;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = null;
    @JsonProperty("orderTemplates")
    private List<String> orderTemplates = null;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("workflow")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String workflow;
    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarId")
    public Long getCalendarId() {
        return calendarId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarId")
    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    public Long getSubmissionHistoryId() {
        return submissionHistoryId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    public void setSubmissionHistoryId(Long submissionHistoryId) {
        this.submissionHistoryId = submissionHistoryId;
    }

    @JsonProperty("states")
    public List<OrderStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<OrderStateText> states) {
        this.states = states;
    }

    @JsonProperty("late")
    public Boolean getLate() {
        return late;
    }

    @JsonProperty("late")
    public void setLate(Boolean late) {
        this.late = late;
    }

    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    public Boolean getWithSubmit() {
        return withSubmit;
    }

    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    public void setWithSubmit(Boolean withSubmit) {
        this.withSubmit = withSubmit;
    }

    @JsonProperty("dailyPlanDate")
    public String getDailyPlanDate() {
        return dailyPlanDate;
    }

    @JsonProperty("dailyPlanDate")
    public void setDailyPlanDate(String dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
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
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    @JsonProperty("orderTemplates")
    public List<String> getOrderTemplates() {
        return orderTemplates;
    }

    @JsonProperty("orderTemplates")
    public void setOrderTemplates(List<String> orderTemplates) {
        this.orderTemplates = orderTemplates;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
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
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("calendarId", calendarId).append("submissionHistoryId", submissionHistoryId).append("states", states).append("late", late).append("withSubmit", withSubmit).append("dailyPlanDate", dailyPlanDate).append("timeZone", timeZone).append("folders", folders).append("orderTemplates", orderTemplates).append("workflow", workflow).append("orderId", orderId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dailyPlanDate).append(folders).append(controllerId).append(workflow).append(orderId).append(timeZone).append(orderTemplates).append(states).append(calendarId).append(late).append(submissionHistoryId).append(withSubmit).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlannedOrdersFilter) == false) {
            return false;
        }
        PlannedOrdersFilter rhs = ((PlannedOrdersFilter) other);
        return new EqualsBuilder().append(dailyPlanDate, rhs.dailyPlanDate).append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(workflow, rhs.workflow).append(orderId, rhs.orderId).append(timeZone, rhs.timeZone).append(orderTemplates, rhs.orderTemplates).append(states, rhs.states).append(calendarId, rhs.calendarId).append(late, rhs.late).append(submissionHistoryId, rhs.submissionHistoryId).append(withSubmit, rhs.withSubmit).isEquals();
    }

}
