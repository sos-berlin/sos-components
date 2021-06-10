
package com.sos.joc.model.yade;

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
 * save and response configuration
 * <p>
 * compact=true -> required fields + possibly numOfFiles, end, error
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "id",
    "profile",
    "state",
    "_operation",
    "start",
    "end",
    "error",
    "source",
    "target",
    "jump",
    "numOfFiles",
    "controllerId",
    "workflowPath",
    "orderId",
    "job",
    "jobPosition",
    "historyId"
})
public class Transfer {

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
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("profile")
    private String profile;
    /**
     * transfer state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private TransferState state;
    /**
     *  yade operation
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_operation")
    private Operation _operation;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("start")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date start;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("end")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date end;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    private Err error;
    /**
     * protocol, host, port, account
     * <p>
     * 
     * 
     */
    @JsonProperty("source")
    private ProtocolFragment source;
    /**
     * protocol, host, port, account
     * <p>
     * 
     * 
     */
    @JsonProperty("target")
    private ProtocolFragment target;
    /**
     * protocol, host, port, account
     * <p>
     * 
     * 
     */
    @JsonProperty("jump")
    private ProtocolFragment jump;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFiles")
    private Long numOfFiles;
    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
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
     * (Required)
     * 
     */
    @JsonProperty("job")
    private String job;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobPosition")
    private String jobPosition;
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
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("profile")
    public String getProfile() {
        return profile;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("profile")
    public void setProfile(String profile) {
        this.profile = profile;
    }

    /**
     * transfer state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public TransferState getState() {
        return state;
    }

    /**
     * transfer state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(TransferState state) {
        this.state = state;
    }

    /**
     *  yade operation
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_operation")
    public Operation get_operation() {
        return _operation;
    }

    /**
     *  yade operation
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_operation")
    public void set_operation(Operation _operation) {
        this._operation = _operation;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("start")
    public Date getStart() {
        return start;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("start")
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("end")
    public Date getEnd() {
        return end;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("end")
    public void setEnd(Date end) {
        this.end = end;
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
     * protocol, host, port, account
     * <p>
     * 
     * 
     */
    @JsonProperty("source")
    public ProtocolFragment getSource() {
        return source;
    }

    /**
     * protocol, host, port, account
     * <p>
     * 
     * 
     */
    @JsonProperty("source")
    public void setSource(ProtocolFragment source) {
        this.source = source;
    }

    /**
     * protocol, host, port, account
     * <p>
     * 
     * 
     */
    @JsonProperty("target")
    public ProtocolFragment getTarget() {
        return target;
    }

    /**
     * protocol, host, port, account
     * <p>
     * 
     * 
     */
    @JsonProperty("target")
    public void setTarget(ProtocolFragment target) {
        this.target = target;
    }

    /**
     * protocol, host, port, account
     * <p>
     * 
     * 
     */
    @JsonProperty("jump")
    public ProtocolFragment getJump() {
        return jump;
    }

    /**
     * protocol, host, port, account
     * <p>
     * 
     * 
     */
    @JsonProperty("jump")
    public void setJump(ProtocolFragment jump) {
        this.jump = jump;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFiles")
    public Long getNumOfFiles() {
        return numOfFiles;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFiles")
    public void setNumOfFiles(Long numOfFiles) {
        this.numOfFiles = numOfFiles;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
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
     * (Required)
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobPosition")
    public String getJobPosition() {
        return jobPosition;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobPosition")
    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("id", id).append("profile", profile).append("state", state).append("_operation", _operation).append("start", start).append("end", end).append("error", error).append("source", source).append("target", target).append("jump", jump).append("numOfFiles", numOfFiles).append("controllerId", controllerId).append("workflowPath", workflowPath).append("orderId", orderId).append("job", job).append("jobPosition", jobPosition).append("historyId", historyId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(numOfFiles).append(surveyDate).append(controllerId).append(workflowPath).append(orderId).append(profile).append(start).append(source).append(error).append(target).append(jobPosition).append(_operation).append(historyId).append(end).append(id).append(state).append(job).append(jump).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Transfer) == false) {
            return false;
        }
        Transfer rhs = ((Transfer) other);
        return new EqualsBuilder().append(numOfFiles, rhs.numOfFiles).append(surveyDate, rhs.surveyDate).append(controllerId, rhs.controllerId).append(workflowPath, rhs.workflowPath).append(orderId, rhs.orderId).append(profile, rhs.profile).append(start, rhs.start).append(source, rhs.source).append(error, rhs.error).append(target, rhs.target).append(jobPosition, rhs.jobPosition).append(_operation, rhs._operation).append(historyId, rhs.historyId).append(end, rhs.end).append(id, rhs.id).append(state, rhs.state).append(job, rhs.job).append(jump, rhs.jump).isEquals();
    }

}
