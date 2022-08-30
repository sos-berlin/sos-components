
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Out-Condition
 * <p>
 * Out Condition
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "jobStream",
    "conditionExpression",
    "outconditionEvents",
    "inconditions"
})
public class OutCondition {

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
    /**
     * Expression
     * <p>
     * Expression for Condition
     * 
     */
    @JsonProperty("conditionExpression")
    @JsonPropertyDescription("Expression for Condition")
    private ConditionExpression conditionExpression;
    @JsonProperty("outconditionEvents")
    private List<OutConditionEvent> outconditionEvents = new ArrayList<OutConditionEvent>();
    @JsonProperty("inconditions")
    private List<JobstreamConditions> inconditions = new ArrayList<JobstreamConditions>();

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

    @JsonProperty("outconditionEvents")
    public List<OutConditionEvent> getOutconditionEvents() {
        return outconditionEvents;
    }

    @JsonProperty("outconditionEvents")
    public void setOutconditionEvents(List<OutConditionEvent> outconditionEvents) {
        this.outconditionEvents = outconditionEvents;
    }

    @JsonProperty("inconditions")
    public List<JobstreamConditions> getInconditions() {
        return inconditions;
    }

    @JsonProperty("inconditions")
    public void setInconditions(List<JobstreamConditions> inconditions) {
        this.inconditions = inconditions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("jobStream", jobStream).append("conditionExpression", conditionExpression).append("outconditionEvents", outconditionEvents).append("inconditions", inconditions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobStream).append(inconditions).append(conditionExpression).append(outconditionEvents).append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OutCondition) == false) {
            return false;
        }
        OutCondition rhs = ((OutCondition) other);
        return new EqualsBuilder().append(jobStream, rhs.jobStream).append(inconditions, rhs.inconditions).append(conditionExpression, rhs.conditionExpression).append(outconditionEvents, rhs.outconditionEvents).append(id, rhs.id).isEquals();
    }

}
