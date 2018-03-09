
package com.sos.joc.model.plan;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.Err;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * dailyPlanItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "job",
    "jobChain",
    "orderId",
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
    "state"
})
public class PlanItem {

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "surveyDate")
    private Date surveyDate;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "job")
    private String job;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "jobChain")
    private String jobChain;
    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    private String orderId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("plannedStartTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "plannedStartTime")
    private Date plannedStartTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("expectedEndTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "expectedEndTime")
    private Date expectedEndTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "startTime")
    private Date startTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "endTime")
    private Date endTime;
    @JsonProperty("historyId")
    @JacksonXmlProperty(localName = "historyId")
    private String historyId;
    /**
     * only for orders
     * 
     */
    @JsonProperty("node")
    @JsonPropertyDescription("only for orders")
    @JacksonXmlProperty(localName = "node")
    private String node;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    @JacksonXmlProperty(localName = "exitCode")
    private Integer exitCode;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    private Err error;
    /**
     *  0=single_start, 1=start_start_repeat, 2=start_end_repeat
     * 
     */
    @JsonProperty("startMode")
    @JsonPropertyDescription("0=single_start, 1=start_start_repeat, 2=start_end_repeat")
    @JacksonXmlProperty(localName = "startMode")
    private Integer startMode = 0;
    /**
     * undefined for startMode=0
     * 
     */
    @JsonProperty("period")
    @JsonPropertyDescription("undefined for startMode=0")
    @JacksonXmlProperty(localName = "period")
    private Period period;
    @JsonProperty("late")
    @JacksonXmlProperty(localName = "late")
    private Boolean late;
    /**
     * plan state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    private PlanState state;

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public String getJobChain() {
        return jobChain;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("plannedStartTime")
    @JacksonXmlProperty(localName = "plannedStartTime")
    public Date getPlannedStartTime() {
        return plannedStartTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("plannedStartTime")
    @JacksonXmlProperty(localName = "plannedStartTime")
    public void setPlannedStartTime(Date plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("expectedEndTime")
    @JacksonXmlProperty(localName = "expectedEndTime")
    public Date getExpectedEndTime() {
        return expectedEndTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("expectedEndTime")
    @JacksonXmlProperty(localName = "expectedEndTime")
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
    @JacksonXmlProperty(localName = "startTime")
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
    @JacksonXmlProperty(localName = "startTime")
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
    @JacksonXmlProperty(localName = "endTime")
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
    @JacksonXmlProperty(localName = "endTime")
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @JsonProperty("historyId")
    @JacksonXmlProperty(localName = "historyId")
    public String getHistoryId() {
        return historyId;
    }

    @JsonProperty("historyId")
    @JacksonXmlProperty(localName = "historyId")
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    /**
     * only for orders
     * 
     */
    @JsonProperty("node")
    @JacksonXmlProperty(localName = "node")
    public String getNode() {
        return node;
    }

    /**
     * only for orders
     * 
     */
    @JsonProperty("node")
    @JacksonXmlProperty(localName = "node")
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
    @JacksonXmlProperty(localName = "exitCode")
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
    @JacksonXmlProperty(localName = "exitCode")
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
    @JacksonXmlProperty(localName = "error")
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
    @JacksonXmlProperty(localName = "error")
    public void setError(Err error) {
        this.error = error;
    }

    /**
     *  0=single_start, 1=start_start_repeat, 2=start_end_repeat
     * 
     */
    @JsonProperty("startMode")
    @JacksonXmlProperty(localName = "startMode")
    public Integer getStartMode() {
        return startMode;
    }

    /**
     *  0=single_start, 1=start_start_repeat, 2=start_end_repeat
     * 
     */
    @JsonProperty("startMode")
    @JacksonXmlProperty(localName = "startMode")
    public void setStartMode(Integer startMode) {
        this.startMode = startMode;
    }

    /**
     * undefined for startMode=0
     * 
     */
    @JsonProperty("period")
    @JacksonXmlProperty(localName = "period")
    public Period getPeriod() {
        return period;
    }

    /**
     * undefined for startMode=0
     * 
     */
    @JsonProperty("period")
    @JacksonXmlProperty(localName = "period")
    public void setPeriod(Period period) {
        this.period = period;
    }

    @JsonProperty("late")
    @JacksonXmlProperty(localName = "late")
    public Boolean getLate() {
        return late;
    }

    @JsonProperty("late")
    @JacksonXmlProperty(localName = "late")
    public void setLate(Boolean late) {
        this.late = late;
    }

    /**
     * plan state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public PlanState getState() {
        return state;
    }

    /**
     * plan state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public void setState(PlanState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("job", job).append("jobChain", jobChain).append("orderId", orderId).append("plannedStartTime", plannedStartTime).append("expectedEndTime", expectedEndTime).append("startTime", startTime).append("endTime", endTime).append("historyId", historyId).append("node", node).append("exitCode", exitCode).append("error", error).append("startMode", startMode).append("period", period).append("late", late).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(period).append(surveyDate).append(orderId).append(jobChain).append(error).append(node).append(plannedStartTime).append(late).append(historyId).append(startMode).append(exitCode).append(expectedEndTime).append(startTime).append(endTime).append(state).append(job).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlanItem) == false) {
            return false;
        }
        PlanItem rhs = ((PlanItem) other);
        return new EqualsBuilder().append(period, rhs.period).append(surveyDate, rhs.surveyDate).append(orderId, rhs.orderId).append(jobChain, rhs.jobChain).append(error, rhs.error).append(node, rhs.node).append(plannedStartTime, rhs.plannedStartTime).append(late, rhs.late).append(historyId, rhs.historyId).append(startMode, rhs.startMode).append(exitCode, rhs.exitCode).append(expectedEndTime, rhs.expectedEndTime).append(startTime, rhs.startTime).append(endTime, rhs.endTime).append(state, rhs.state).append(job, rhs.job).isEquals();
    }

}
