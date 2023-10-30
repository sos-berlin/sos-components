
package com.sos.sign.model.job;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.JobReturnCodeHelper;


/**
 * job return code meaning
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "success",
    "failure"
})
public class JobReturnCode extends JobReturnCodeHelper {

    @JsonProperty("success")
    private String success;
    @JsonProperty("failure")
    private String failure;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JobReturnCode() {
    }

    /**
     * 
     * @param success
     * @param failure
     */
    public JobReturnCode(Object success, Object failure) {
        super();
        this.success = getCodes(success, TYPE.SUCCESS);
        this.failure = getCodes(failure, TYPE.FAILURE);
    }

    @JsonProperty("success")
    public String getSuccess() {
        return success;
    }

    @JsonProperty("success")
    public void setSuccess(Object success) {
        this.success = getCodes(success, TYPE.SUCCESS);
    }
    
    @JsonIgnore
    public void setSuccess(String success) {
        this.success = getCodes(success, TYPE.SUCCESS);
    }
    
    @JsonIgnore
    public void setSuccess(List<Integer> success) {
        this.success = getCodes(success, TYPE.SUCCESS);
    }
    
    @JsonIgnore
    public void setSuccessNormalized(String success) {
        getCodes(success, TYPE.SUCCESS);
        this.success = normalized(TYPE.SUCCESS);
    }
    
    @JsonIgnore
    public boolean isInSuccess(Integer success) {
        return isInReturnCodes(success, TYPE.SUCCESS);
    }

    @JsonProperty("failure")
    public String getFailure() {
        if (failure != null && "none".equals(failure)) {
            return "";
        }
        return failure;
    }

    @JsonProperty("failure")
    public void setFailure(Object failure) {
        this.failure = getCodes(failure, TYPE.FAILURE);
    }
    
    @JsonIgnore
    public void setFailure(String failure) {
        this.failure = getCodes(failure, TYPE.FAILURE);
    }
    
    @JsonIgnore
    public void setFailure(List<Integer> failure) {
        this.failure = getCodes(failure, TYPE.FAILURE);
    }
    
    @JsonIgnore
    public boolean isInFailures(Integer failure) {
        return isInReturnCodes(failure, TYPE.FAILURE);
    }
    
    @JsonIgnore
    public void setFailureNormalized(Object warning) {
        deleteIntersectionByOtherIntervals(TYPE.FAILURE, warning, TYPE.WARNING);
        this.failure = normalized(TYPE.FAILURE);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("success", success).append("failure", failure).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(success).append(failure).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobReturnCode) == false) {
            return false;
        }
        JobReturnCode rhs = ((JobReturnCode) other);
        return new EqualsBuilder().append(success, rhs.success).append(failure, rhs.failure).isEquals();
    }

}
