
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Expression
 * <p>
 * Expression for Condition
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "expression",
    "validatedExpression",
    "value",
    "jobStreamEvents"
})
public class ConditionExpression {

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("expression")
    private String expression;
    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("validatedExpression")
    private String validatedExpression;
    @JsonProperty("value")
    private Boolean value;
    @JsonProperty("jobStreamEvents")
    private List<String> jobStreamEvents = new ArrayList<String>();

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("expression")
    public String getExpression() {
        return expression;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("expression")
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("validatedExpression")
    public String getValidatedExpression() {
        return validatedExpression;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("validatedExpression")
    public void setValidatedExpression(String validatedExpression) {
        this.validatedExpression = validatedExpression;
    }

    @JsonProperty("value")
    public Boolean getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(Boolean value) {
        this.value = value;
    }

    @JsonProperty("jobStreamEvents")
    public List<String> getJobStreamEvents() {
        return jobStreamEvents;
    }

    @JsonProperty("jobStreamEvents")
    public void setJobStreamEvents(List<String> jobStreamEvents) {
        this.jobStreamEvents = jobStreamEvents;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("expression", expression).append("validatedExpression", validatedExpression).append("value", value).append("jobStreamEvents", jobStreamEvents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(validatedExpression).append(expression).append(value).append(jobStreamEvents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConditionExpression) == false) {
            return false;
        }
        ConditionExpression rhs = ((ConditionExpression) other);
        return new EqualsBuilder().append(validatedExpression, rhs.validatedExpression).append(expression, rhs.expression).append(value, rhs.value).append(jobStreamEvents, rhs.jobStreamEvents).isEquals();
    }

}
