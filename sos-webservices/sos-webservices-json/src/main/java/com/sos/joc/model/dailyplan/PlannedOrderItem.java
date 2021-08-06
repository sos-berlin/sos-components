
package com.sos.joc.model.dailyplan;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Err;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * dailyPlannedOrderItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "surveyDate",
    "workflowPath",
    "schedulePath",
    "orderId",
    "orderName",
    "cyclicOrder",
    "plannedStartTime",
    "expectedEndTime",
    "startTime",
    "endTime",
    "historyId",
    "node",
    "exitCode",
    "error",
    "startMode",
    "period",
    "late",
    "submitted",
    "state"
})
public class PlannedOrderItem {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date surveyDate;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    private String workflowPath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("schedulePath")
    private String schedulePath;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderName")
    private String orderName;
    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cyclicOrder")
    private CyclicOrderInfos cyclicOrder;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("plannedStartTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date plannedStartTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("expectedEndTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date expectedEndTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date startTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date endTime;
    @JsonProperty("historyId")
    private String historyId;
    /**
     * only for orders
     * 
     */
    @JsonProperty("node")
    @JsonPropertyDescription("only for orders")
    private String node;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    private Integer exitCode;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    private Err error;
    /**
     *  0=single_start, 1=start_start_repeat
     * (Required)
     * 
     */
    @JsonProperty("startMode")
    @JsonPropertyDescription("0=single_start, 1=start_start_repeat")
    private Integer startMode = 0;
    /**
     * undefined for startMode=0
     * 
     */
    @JsonProperty("period")
    @JsonPropertyDescription("undefined for startMode=0")
    private Period period;
    @JsonProperty("late")
    private Boolean late;
    @JsonProperty("submitted")
    private Boolean submitted;
    /**
     * Daily Plan order state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private DailyPlanOrderState state;

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("schedulePath")
    public String getSchedulePath() {
        return schedulePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("schedulePath")
    public void setSchedulePath(String schedulePath) {
        this.schedulePath = schedulePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderName")
    public String getOrderName() {
        return orderName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderName")
    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cyclicOrder")
    public CyclicOrderInfos getCyclicOrder() {
        return cyclicOrder;
    }

    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cyclicOrder")
    public void setCyclicOrder(CyclicOrderInfos cyclicOrder) {
        this.cyclicOrder = cyclicOrder;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("plannedStartTime")
    public Date getPlannedStartTime() {
        return plannedStartTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("plannedStartTime")
    public void setPlannedStartTime(Date plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("expectedEndTime")
    public Date getExpectedEndTime() {
        return expectedEndTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("expectedEndTime")
    public void setExpectedEndTime(Date expectedEndTime) {
        this.expectedEndTime = expectedEndTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startTime")
    public Date getStartTime() {
        return startTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startTime")
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    public Date getEndTime() {
        return endTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @JsonProperty("historyId")
    public String getHistoryId() {
        return historyId;
    }

    @JsonProperty("historyId")
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    /**
     * only for orders
     * 
     */
    @JsonProperty("node")
    public String getNode() {
        return node;
    }

    /**
     * only for orders
     * 
     */
    @JsonProperty("node")
    public void setNode(String node) {
        this.node = node;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    public Integer getExitCode() {
        return exitCode;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    public Err getError() {
        return error;
    }

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    public void setError(Err error) {
        this.error = error;
    }

    /**
     *  0=single_start, 1=start_start_repeat
     * (Required)
     * 
     */
    @JsonProperty("startMode")
    public Integer getStartMode() {
        return startMode;
    }

    /**
     *  0=single_start, 1=start_start_repeat
     * (Required)
     * 
     */
    @JsonProperty("startMode")
    public void setStartMode(Integer startMode) {
        this.startMode = startMode;
    }

    /**
     * undefined for startMode=0
     * 
     */
    @JsonProperty("period")
    public Period getPeriod() {
        return period;
    }

    /**
     * undefined for startMode=0
     * 
     */
    @JsonProperty("period")
    public void setPeriod(Period period) {
        this.period = period;
    }

    @JsonProperty("late")
    public Boolean getLate() {
        return late;
    }

    @JsonProperty("late")
    public void setLate(Boolean late) {
        this.late = late;
    }

    @JsonProperty("submitted")
    public Boolean getSubmitted() {
        return submitted;
    }

    @JsonProperty("submitted")
    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    /**
     * Daily Plan order state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public DailyPlanOrderState getState() {
        return state;
    }

    /**
     * Daily Plan order state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(DailyPlanOrderState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("surveyDate", surveyDate).append("workflowPath", workflowPath).append("schedulePath", schedulePath).append("orderId", orderId).append("orderName", orderName).append("cyclicOrder", cyclicOrder).append("plannedStartTime", plannedStartTime).append("expectedEndTime", expectedEndTime).append("startTime", startTime).append("endTime", endTime).append("historyId", historyId).append("node", node).append("exitCode", exitCode).append("error", error).append("startMode", startMode).append("period", period).append("late", late).append("submitted", submitted).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(period).append(controllerId).append(surveyDate).append(workflowPath).append(orderId).append(error).append(node).append(submitted).append(plannedStartTime).append(late).append(historyId).append(startMode).append(exitCode).append(expectedEndTime).append(startTime).append(endTime).append(state).append(schedulePath).append(orderName).append(cyclicOrder).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlannedOrderItem) == false) {
            return false;
        }
        PlannedOrderItem rhs = ((PlannedOrderItem) other);
        return new EqualsBuilder().append(period, rhs.period).append(controllerId, rhs.controllerId).append(surveyDate, rhs.surveyDate).append(workflowPath, rhs.workflowPath).append(orderId, rhs.orderId).append(error, rhs.error).append(node, rhs.node).append(submitted, rhs.submitted).append(plannedStartTime, rhs.plannedStartTime).append(late, rhs.late).append(historyId, rhs.historyId).append(startMode, rhs.startMode).append(exitCode, rhs.exitCode).append(expectedEndTime, rhs.expectedEndTime).append(startTime, rhs.startTime).append(endTime, rhs.endTime).append(state, rhs.state).append(schedulePath, rhs.schedulePath).append(orderName, rhs.orderName).append(cyclicOrder, rhs.cyclicOrder).isEquals();
    }

}
