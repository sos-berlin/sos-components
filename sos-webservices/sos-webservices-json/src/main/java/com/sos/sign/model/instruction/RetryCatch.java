
package com.sos.sign.model.instruction;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * retry
 * <p>
 * instruction with fixed property 'TYPE':'Try' (with a retry object in the catch)
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "maxTries",
    "retryDelays",
    "try",
    "catch"
})
public class RetryCatch
    extends Instruction
{

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
    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("try")
    private Instructions _try;
    /**
     * catch instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("catch")
    private Instructions _catch;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RetryCatch() {
        this._catch = new Instructions(Arrays.asList(new RetryInCatch()));
    }
    
    /**
     * 
     * @param _try
     * @param maxTries
     * @param retryDelays
     */
    public RetryCatch(Integer maxTries, List<Integer> retryDelays, Instructions _try) {
        super();
        this.maxTries = maxTries;
        this.retryDelays = retryDelays;
        this._try = _try;
        this._catch = new Instructions(Arrays.asList(new RetryInCatch()));
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

    /**
     * instructions
     * <p>
     * 
     * 
     */
    @JsonProperty("try")
    public Instructions getTry() {
        return _try;
    }

    /**
     * instructions
     * <p>
     * 
     * 
     */
    @JsonProperty("try")
    public void setTry(Instructions _try) {
        this._try = _try;
    }

    /**
     * catch instructions
     * <p>
     * 
     * 
     */
    @JsonProperty("catch")
    public Instructions getCatch() {
        return _catch;
    }

    /**
     * catch instructions
     * <p>
     * 
     * 
     */
    @JsonProperty("catch")
    public void setCatch(Instructions _catch) {
        this._catch = _catch;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("maxTries", maxTries).append("retryDelays", retryDelays).append("_try", _try).append("_catch", _catch).toString();
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
        if ((other instanceof RetryCatch) == false) {
            return false;
        }
        RetryCatch rhs = ((RetryCatch) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(_try, rhs._try).append(_catch, rhs._catch).append(maxTries, rhs.maxTries).append(retryDelays, rhs.retryDelays).isEquals();
    }

}
