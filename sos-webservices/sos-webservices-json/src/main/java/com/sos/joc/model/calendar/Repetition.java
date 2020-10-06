
package com.sos.joc.model.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * every
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "from",
    "to",
    "repetition",
    "step"
})
public class Repetition {

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String from;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String to;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("repetition")
    private RepetitionText repetition;
    @JsonProperty("step")
    private Integer step = 1;

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    public String getFrom() {
        return from;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    public String getTo() {
        return to;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("repetition")
    public RepetitionText getRepetition() {
        return repetition;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("repetition")
    public void setRepetition(RepetitionText repetition) {
        this.repetition = repetition;
    }

    @JsonProperty("step")
    public Integer getStep() {
        return step;
    }

    @JsonProperty("step")
    public void setStep(Integer step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("from", from).append("to", to).append("repetition", repetition).append("step", step).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(from).append(step).append(to).append(repetition).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Repetition) == false) {
            return false;
        }
        Repetition rhs = ((Repetition) other);
        return new EqualsBuilder().append(from, rhs.from).append(step, rhs.step).append(to, rhs.to).append(repetition, rhs.repetition).isEquals();
    }

}
