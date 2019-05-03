
package com.sos.jobscheduler.model.instruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * retry
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "try",
    "catch",
    "maxTries",
    "retryDelays"
})
public class Retry
    extends Instruction
    implements com.sos.jobscheduler.model.instruction.IInstruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("try")
    private List<com.sos.jobscheduler.model.instruction.IInstruction> _try = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("catch")
    private List<RetryInCatch> _catch = new ArrayList<RetryInCatch>(Arrays.asList(new RetryInCatch()));
    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("maxTries")
    private Integer maxTries;
    @JsonProperty("retryDelays")
    private List<Integer> retryDelays = null;

    @JsonProperty("try")
    public List<com.sos.jobscheduler.model.instruction.IInstruction> getTry() {
        return _try;
    }

    @JsonProperty("try")
    public void setTry(List<com.sos.jobscheduler.model.instruction.IInstruction> _try) {
        this._try = _try;
    }

    @JsonProperty("catch")
    public List<RetryInCatch> getCatch() {
        return _catch;
    }

    @JsonProperty("catch")
    public void setCatch(List<RetryInCatch> _catch) {
        this._catch = _catch;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("maxTries")
    public Integer getMaxTries() {
        return maxTries;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("maxTries")
    public void setMaxTries(Integer maxTries) {
        this.maxTries = maxTries;
    }

    @JsonProperty("retryDelays")
    public List<Integer> getRetryDelays() {
        return retryDelays;
    }

    @JsonProperty("retryDelays")
    public void setRetryDelays(List<Integer> retryDelays) {
        this.retryDelays = retryDelays;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("_try", _try).append("_catch", _catch).append("maxTries", maxTries).append("retryDelays", retryDelays).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(_try).append(_catch).append(maxTries).append(retryDelays).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Retry) == false) {
            return false;
        }
        Retry rhs = ((Retry) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(_try, rhs._try).append(_catch, rhs._catch).append(maxTries, rhs.maxTries).append(retryDelays, rhs.retryDelays).isEquals();
    }

}
