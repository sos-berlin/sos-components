
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * In-Condition
 * <p>
 * In Condition
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "jobStream",
    "consumed",
    "markExpression",
    "skipOutCondition",
    "haveReferenceToOtherFolders",
    "conditionExpression",
    "nextPeriod",
    "inconditionCommands",
    "outconditions"
})
public class InCondition {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    private String jobStream;
    @JsonProperty("consumed")
    private Boolean consumed;
    @JsonProperty("markExpression")
    private Boolean markExpression;
    @JsonProperty("skipOutCondition")
    private Boolean skipOutCondition;
    @JsonProperty("haveReferenceToOtherFolders")
    private Boolean haveReferenceToOtherFolders;
    /**
     * Expression
     * <p>
     * Expression for Condition
     * 
     */
    @JsonProperty("conditionExpression")
    @JsonPropertyDescription("Expression for Condition")
    private ConditionExpression conditionExpression;
    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("nextPeriod")
    @JsonPropertyDescription("Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date nextPeriod;
    @JsonProperty("inconditionCommands")
    private List<InConditionCommand> inconditionCommands = new ArrayList<InConditionCommand>();
    @JsonProperty("outconditions")
    private List<JobstreamConditions> outconditions = new ArrayList<JobstreamConditions>();

    /**
     * non negative long
     * <p>
     * 
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
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    public String getJobStream() {
        return jobStream;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    @JsonProperty("consumed")
    public Boolean getConsumed() {
        return consumed;
    }

    @JsonProperty("consumed")
    public void setConsumed(Boolean consumed) {
        this.consumed = consumed;
    }

    @JsonProperty("markExpression")
    public Boolean getMarkExpression() {
        return markExpression;
    }

    @JsonProperty("markExpression")
    public void setMarkExpression(Boolean markExpression) {
        this.markExpression = markExpression;
    }

    @JsonProperty("skipOutCondition")
    public Boolean getSkipOutCondition() {
        return skipOutCondition;
    }

    @JsonProperty("skipOutCondition")
    public void setSkipOutCondition(Boolean skipOutCondition) {
        this.skipOutCondition = skipOutCondition;
    }

    @JsonProperty("haveReferenceToOtherFolders")
    public Boolean getHaveReferenceToOtherFolders() {
        return haveReferenceToOtherFolders;
    }

    @JsonProperty("haveReferenceToOtherFolders")
    public void setHaveReferenceToOtherFolders(Boolean haveReferenceToOtherFolders) {
        this.haveReferenceToOtherFolders = haveReferenceToOtherFolders;
    }

    /**
     * Expression
     * <p>
     * Expression for Condition
     * 
     */
    @JsonProperty("conditionExpression")
    public ConditionExpression getConditionExpression() {
        return conditionExpression;
    }

    /**
     * Expression
     * <p>
     * Expression for Condition
     * 
     */
    @JsonProperty("conditionExpression")
    public void setConditionExpression(ConditionExpression conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("nextPeriod")
    public Date getNextPeriod() {
        return nextPeriod;
    }

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("nextPeriod")
    public void setNextPeriod(Date nextPeriod) {
        this.nextPeriod = nextPeriod;
    }

    @JsonProperty("inconditionCommands")
    public List<InConditionCommand> getInconditionCommands() {
        return inconditionCommands;
    }

    @JsonProperty("inconditionCommands")
    public void setInconditionCommands(List<InConditionCommand> inconditionCommands) {
        this.inconditionCommands = inconditionCommands;
    }

    @JsonProperty("outconditions")
    public List<JobstreamConditions> getOutconditions() {
        return outconditions;
    }

    @JsonProperty("outconditions")
    public void setOutconditions(List<JobstreamConditions> outconditions) {
        this.outconditions = outconditions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("jobStream", jobStream).append("consumed", consumed).append("markExpression", markExpression).append("skipOutCondition", skipOutCondition).append("haveReferenceToOtherFolders", haveReferenceToOtherFolders).append("conditionExpression", conditionExpression).append("nextPeriod", nextPeriod).append("inconditionCommands", inconditionCommands).append("outconditions", outconditions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(consumed).append(markExpression).append(haveReferenceToOtherFolders).append(conditionExpression).append(nextPeriod).append(outconditions).append(jobStream).append(id).append(inconditionCommands).append(skipOutCondition).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InCondition) == false) {
            return false;
        }
        InCondition rhs = ((InCondition) other);
        return new EqualsBuilder().append(consumed, rhs.consumed).append(markExpression, rhs.markExpression).append(haveReferenceToOtherFolders, rhs.haveReferenceToOtherFolders).append(conditionExpression, rhs.conditionExpression).append(nextPeriod, rhs.nextPeriod).append(outconditions, rhs.outconditions).append(jobStream, rhs.jobStream).append(id, rhs.id).append(inconditionCommands, rhs.inconditionCommands).append(skipOutCondition, rhs.skipOutCondition).isEquals();
    }

}
