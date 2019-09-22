
package com.sos.jobscheduler.history.order;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order history log entry
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "date",
    "logLevel",
    "logType",
    "orderKey",
    "position",
    "agentPath",
    "agentUrl",
    "jobName",
    "returnCode",
    "error",
    "errorStatus",
    "errorReason",
    "errorCode",
    "errorText"
})
public class LogEntry {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("date")
    private Date date;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    private String logLevel;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logType")
    private String logType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderKey")
    private String orderKey;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    private String position;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    private String agentPath;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentUrl")
    private String agentUrl;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    private String jobName;
    @JsonProperty("returnCode")
    private Long returnCode;
    @JsonProperty("error")
    private Boolean error;
    @JsonProperty("errorStatus")
    private String errorStatus;
    @JsonProperty("errorReason")
    private String errorReason;
    @JsonProperty("errorCode")
    private String errorCode;
    @JsonProperty("errorText")
    private String errorText;

    /**
     * No args constructor for use in serialization
     * 
     */
    public LogEntry() {
    }

    /**
     * 
     * @param date
     * @param logType
     * @param orderKey
     * @param jobName
     * @param errorCode
     * @param error
     * @param agentPath
     * @param returnCode
     * @param errorText
     * @param logLevel
     * @param errorReason
     * @param errorStatus
     * @param position
     * @param agentUrl
     */
    public LogEntry(Date date, String logLevel, String logType, String orderKey, String position, String agentPath, String agentUrl, String jobName, Long returnCode, Boolean error, String errorStatus, String errorReason, String errorCode, String errorText) {
        super();
        this.date = date;
        this.logLevel = logLevel;
        this.logType = logType;
        this.orderKey = orderKey;
        this.position = position;
        this.agentPath = agentPath;
        this.agentUrl = agentUrl;
        this.jobName = jobName;
        this.returnCode = returnCode;
        this.error = error;
        this.errorStatus = errorStatus;
        this.errorReason = errorReason;
        this.errorCode = errorCode;
        this.errorText = errorText;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("date")
    public Date getDate() {
        return date;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("date")
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logType")
    public String getLogType() {
        return logType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logType")
    public void setLogType(String logType) {
        this.logType = logType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderKey")
    public String getOrderKey() {
        return orderKey;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderKey")
    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentUrl")
    public String getAgentUrl() {
        return agentUrl;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentUrl")
    public void setAgentUrl(String agentUrl) {
        this.agentUrl = agentUrl;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    public String getJobName() {
        return jobName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @JsonProperty("returnCode")
    public Long getReturnCode() {
        return returnCode;
    }

    @JsonProperty("returnCode")
    public void setReturnCode(Long returnCode) {
        this.returnCode = returnCode;
    }

    @JsonProperty("error")
    public Boolean getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(Boolean error) {
        this.error = error;
    }

    @JsonProperty("errorStatus")
    public String getErrorStatus() {
        return errorStatus;
    }

    @JsonProperty("errorStatus")
    public void setErrorStatus(String errorStatus) {
        this.errorStatus = errorStatus;
    }

    @JsonProperty("errorReason")
    public String getErrorReason() {
        return errorReason;
    }

    @JsonProperty("errorReason")
    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    @JsonProperty("errorCode")
    public String getErrorCode() {
        return errorCode;
    }

    @JsonProperty("errorCode")
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @JsonProperty("errorText")
    public String getErrorText() {
        return errorText;
    }

    @JsonProperty("errorText")
    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("date", date).append("logLevel", logLevel).append("logType", logType).append("orderKey", orderKey).append("position", position).append("agentPath", agentPath).append("agentUrl", agentUrl).append("jobName", jobName).append("returnCode", returnCode).append("error", error).append("errorStatus", errorStatus).append("errorReason", errorReason).append("errorCode", errorCode).append("errorText", errorText).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(date).append(logType).append(orderKey).append(jobName).append(errorCode).append(error).append(agentPath).append(returnCode).append(errorText).append(logLevel).append(errorReason).append(errorStatus).append(position).append(agentUrl).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LogEntry) == false) {
            return false;
        }
        LogEntry rhs = ((LogEntry) other);
        return new EqualsBuilder().append(date, rhs.date).append(logType, rhs.logType).append(orderKey, rhs.orderKey).append(jobName, rhs.jobName).append(errorCode, rhs.errorCode).append(error, rhs.error).append(agentPath, rhs.agentPath).append(returnCode, rhs.returnCode).append(errorText, rhs.errorText).append(logLevel, rhs.logLevel).append(errorReason, rhs.errorReason).append(errorStatus, rhs.errorStatus).append(position, rhs.position).append(agentUrl, rhs.agentUrl).isEquals();
    }

}
