
package com.sos.joc.model.yade;

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
 * save and response configuration
 * <p>
 * compact=true -> required fields + possibly profile, mandator, target
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "id",
    "parent_id",
    "profile",
    "mandator",
    "state",
    "_operation",
    "start",
    "end",
    "error",
    "source",
    "target",
    "jump",
    "numOfFiles",
    "hasIntervention",
    "jobschedulerId",
    "orderId",
    "jobChain",
    "jobChainNode",
    "job",
    "taskId"
})
public class Transfer {

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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    private Long id;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("parent_id")
    @JacksonXmlProperty(localName = "parent_id")
    private Long parent_id;
    @JsonProperty("profile")
    @JacksonXmlProperty(localName = "profile")
    private String profile;
    @JsonProperty("mandator")
    @JacksonXmlProperty(localName = "mandator")
    private String mandator;
    /**
     * transfer state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    private TransferState state;
    /**
     *  yade operation
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_operation")
    @JacksonXmlProperty(localName = "_operation")
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
    @JacksonXmlProperty(localName = "start")
    private Date start;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("end")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "end")
    private Date end;
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
     * protocol, host, port, account
     * <p>
     * compact=true -> only required fields
     * (Required)
     * 
     */
    @JsonProperty("source")
    @JsonPropertyDescription("compact=true -> only required fields")
    @JacksonXmlProperty(localName = "source")
    private ProtocolFragment source;
    /**
     * protocol, host, port, account
     * <p>
     * compact=true -> only required fields
     * 
     */
    @JsonProperty("target")
    @JsonPropertyDescription("compact=true -> only required fields")
    @JacksonXmlProperty(localName = "target")
    private ProtocolFragment target;
    /**
     * protocol, host, port, account
     * <p>
     * compact=true -> only required fields
     * 
     */
    @JsonProperty("jump")
    @JsonPropertyDescription("compact=true -> only required fields")
    @JacksonXmlProperty(localName = "jump")
    private ProtocolFragment jump;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFiles")
    @JacksonXmlProperty(localName = "numOfFiles")
    private Integer numOfFiles;
    @JsonProperty("hasIntervention")
    @JacksonXmlProperty(localName = "hasIntervention")
    private Boolean hasIntervention = false;
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    private String orderId;
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
    @JsonProperty("jobChainNode")
    @JacksonXmlProperty(localName = "jobChainNode")
    private String jobChainNode;
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("taskId")
    @JacksonXmlProperty(localName = "taskId")
    private Long taskId;

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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("parent_id")
    @JacksonXmlProperty(localName = "parent_id")
    public Long getParent_id() {
        return parent_id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("parent_id")
    @JacksonXmlProperty(localName = "parent_id")
    public void setParent_id(Long parent_id) {
        this.parent_id = parent_id;
    }

    @JsonProperty("profile")
    @JacksonXmlProperty(localName = "profile")
    public String getProfile() {
        return profile;
    }

    @JsonProperty("profile")
    @JacksonXmlProperty(localName = "profile")
    public void setProfile(String profile) {
        this.profile = profile;
    }

    @JsonProperty("mandator")
    @JacksonXmlProperty(localName = "mandator")
    public String getMandator() {
        return mandator;
    }

    @JsonProperty("mandator")
    @JacksonXmlProperty(localName = "mandator")
    public void setMandator(String mandator) {
        this.mandator = mandator;
    }

    /**
     * transfer state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public TransferState getState() {
        return state;
    }

    /**
     * transfer state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
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
    @JacksonXmlProperty(localName = "_operation")
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
    @JacksonXmlProperty(localName = "_operation")
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
    @JacksonXmlProperty(localName = "start")
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
    @JacksonXmlProperty(localName = "start")
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
    @JacksonXmlProperty(localName = "end")
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
    @JacksonXmlProperty(localName = "end")
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
     * protocol, host, port, account
     * <p>
     * compact=true -> only required fields
     * (Required)
     * 
     */
    @JsonProperty("source")
    @JacksonXmlProperty(localName = "source")
    public ProtocolFragment getSource() {
        return source;
    }

    /**
     * protocol, host, port, account
     * <p>
     * compact=true -> only required fields
     * (Required)
     * 
     */
    @JsonProperty("source")
    @JacksonXmlProperty(localName = "source")
    public void setSource(ProtocolFragment source) {
        this.source = source;
    }

    /**
     * protocol, host, port, account
     * <p>
     * compact=true -> only required fields
     * 
     */
    @JsonProperty("target")
    @JacksonXmlProperty(localName = "target")
    public ProtocolFragment getTarget() {
        return target;
    }

    /**
     * protocol, host, port, account
     * <p>
     * compact=true -> only required fields
     * 
     */
    @JsonProperty("target")
    @JacksonXmlProperty(localName = "target")
    public void setTarget(ProtocolFragment target) {
        this.target = target;
    }

    /**
     * protocol, host, port, account
     * <p>
     * compact=true -> only required fields
     * 
     */
    @JsonProperty("jump")
    @JacksonXmlProperty(localName = "jump")
    public ProtocolFragment getJump() {
        return jump;
    }

    /**
     * protocol, host, port, account
     * <p>
     * compact=true -> only required fields
     * 
     */
    @JsonProperty("jump")
    @JacksonXmlProperty(localName = "jump")
    public void setJump(ProtocolFragment jump) {
        this.jump = jump;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFiles")
    @JacksonXmlProperty(localName = "numOfFiles")
    public Integer getNumOfFiles() {
        return numOfFiles;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfFiles")
    @JacksonXmlProperty(localName = "numOfFiles")
    public void setNumOfFiles(Integer numOfFiles) {
        this.numOfFiles = numOfFiles;
    }

    @JsonProperty("hasIntervention")
    @JacksonXmlProperty(localName = "hasIntervention")
    public Boolean getHasIntervention() {
        return hasIntervention;
    }

    @JsonProperty("hasIntervention")
    @JacksonXmlProperty(localName = "hasIntervention")
    public void setHasIntervention(Boolean hasIntervention) {
        this.hasIntervention = hasIntervention;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
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

    @JsonProperty("jobChainNode")
    @JacksonXmlProperty(localName = "jobChainNode")
    public String getJobChainNode() {
        return jobChainNode;
    }

    @JsonProperty("jobChainNode")
    @JacksonXmlProperty(localName = "jobChainNode")
    public void setJobChainNode(String jobChainNode) {
        this.jobChainNode = jobChainNode;
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("taskId")
    @JacksonXmlProperty(localName = "taskId")
    public Long getTaskId() {
        return taskId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("taskId")
    @JacksonXmlProperty(localName = "taskId")
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("id", id).append("parent_id", parent_id).append("profile", profile).append("mandator", mandator).append("state", state).append("_operation", _operation).append("start", start).append("end", end).append("error", error).append("source", source).append("target", target).append("jump", jump).append("numOfFiles", numOfFiles).append("hasIntervention", hasIntervention).append("jobschedulerId", jobschedulerId).append("orderId", orderId).append("jobChain", jobChain).append("jobChainNode", jobChainNode).append("job", job).append("taskId", taskId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(mandator).append(numOfFiles).append(surveyDate).append(orderId).append(profile).append(start).append(jobChain).append(source).append(error).append(hasIntervention).append(target).append(_operation).append(jobChainNode).append(parent_id).append(end).append(id).append(state).append(jobschedulerId).append(job).append(taskId).append(jump).toHashCode();
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
        return new EqualsBuilder().append(mandator, rhs.mandator).append(numOfFiles, rhs.numOfFiles).append(surveyDate, rhs.surveyDate).append(orderId, rhs.orderId).append(profile, rhs.profile).append(start, rhs.start).append(jobChain, rhs.jobChain).append(source, rhs.source).append(error, rhs.error).append(hasIntervention, rhs.hasIntervention).append(target, rhs.target).append(_operation, rhs._operation).append(jobChainNode, rhs.jobChainNode).append(parent_id, rhs.parent_id).append(end, rhs.end).append(id, rhs.id).append(state, rhs.state).append(jobschedulerId, rhs.jobschedulerId).append(job, rhs.job).append(taskId, rhs.taskId).append(jump, rhs.jump).isEquals();
    }

}
