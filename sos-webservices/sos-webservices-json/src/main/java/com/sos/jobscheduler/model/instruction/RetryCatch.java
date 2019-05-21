
package com.sos.jobscheduler.model.instruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private List<Integer> retryDelays = new ArrayList<Integer>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("try")
    private List<Instruction> _try = new ArrayList<Instruction>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("catch")
    private List<Instruction> _catch = new ArrayList<Instruction>(Arrays.asList(new RetryInCatch()));

    /**
     * No args constructor for use in serialization
     * 
     */
    public RetryCatch() {
    }

    /**
     * 
     * @param _try
     * @param _catch
     * @param maxTries
     * @param retryDelays
     */
    public RetryCatch(Integer maxTries, List<Integer> retryDelays, List<Instruction> _try, List<Instruction> _catch) {
        super();
        this.maxTries = maxTries;
        this.retryDelays = retryDelays;
        this._try = _try;
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

    @JsonProperty("try")
    public List<Instruction> getTry() {
        return _try;
    }

    @JsonProperty("try")
    public void setTry(List<Instruction> _try) {
        this._try = _try;
    }

    @JsonProperty("catch")
    public List<Instruction> getCatch() {
        return _catch;
    }

    @JsonProperty("catch")
    @JsonIgnore
    public void setCatch(List<Instruction> _catch) {
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
