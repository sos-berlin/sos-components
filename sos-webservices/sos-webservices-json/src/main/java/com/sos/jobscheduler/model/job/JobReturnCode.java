
package com.sos.jobscheduler.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "success",
    "failure"
})
public class JobReturnCode {

    @JsonProperty("success")
    private List<Integer> success = null;
    @JsonProperty("failure")
    private List<Integer> failure = null;

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
