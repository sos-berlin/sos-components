
package com.sos.controller.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "problem"
})
public class OutcomeReason {

    /**
     * Succeeded, Failed, Disrupted, Cancelled, Killed, TimedOut
     * 
     */
    @JsonProperty("TYPE")
    @JsonPropertyDescription("Succeeded, Failed, Disrupted, Cancelled, Killed, TimedOut")
    private String tYPE;
    @JsonProperty("problem")
    private OutcomeReasonProblem problem;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OutcomeReason() {
    }

    /**
     * 
     * @param problem
     * @param tYPE
     */
    public OutcomeReason(String tYPE, OutcomeReasonProblem problem) {
        super();
        this.tYPE = tYPE;
        this.problem = problem;
    }

    /**
     * Succeeded, Failed, Disrupted, Cancelled, Killed, TimedOut
     * 
     */
    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * Succeeded, Failed, Disrupted, Cancelled, Killed, TimedOut
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("problem")
    public OutcomeReasonProblem getProblem() {
        return problem;
    }

    @JsonProperty("problem")
    public void setProblem(OutcomeReasonProblem problem) {
        this.problem = problem;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("problem", problem).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(problem).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OutcomeReason) == false) {
            return false;
        }
        OutcomeReason rhs = ((OutcomeReason) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(problem, rhs.problem).isEquals();
    }

}
