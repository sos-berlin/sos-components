
package com.sos.inventory.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job return code meaning
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "success",
    "failure"
})
public class JobReturnCode
    extends JobReturnCodeWarning
{

    @JsonProperty("success")
    private List<Integer> success = null;
    @JsonProperty("failure")
    private List<Integer> failure = null;

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
     * @param warning
     */
    public JobReturnCode(List<Integer> success, List<Integer> failure, List<Integer> warning) {
        super(warning);
        this.success = success;
        this.failure = failure;
    }

    @JsonProperty("success")
    public List<Integer> getSuccess() {
        return success;
    }

    @JsonProperty("success")
    public void setSuccess(List<Integer> success) {
        this.success = success;
    }

    @JsonProperty("failure")
    public List<Integer> getFailure() {
        return failure;
    }

    @JsonProperty("failure")
    public void setFailure(List<Integer> failure) {
        this.failure = failure;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("success", success).append("failure", failure).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(success).append(failure).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(success, rhs.success).append(failure, rhs.failure).isEquals();
    }

}
