
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "successful",
    "failed"
})
public class JobsHistoricSummary {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("successful")
    private Long successful;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("failed")
    private Long failed;

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("successful")
    public Long getSuccessful() {
        return successful;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("successful")
    public void setSuccessful(Long successful) {
        this.successful = successful;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("failed")
    public Long getFailed() {
        return failed;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("failed")
    public void setFailed(Long failed) {
        this.failed = failed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("successful", successful).append("failed", failed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(successful).append(failed).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobsHistoricSummary) == false) {
            return false;
        }
        JobsHistoricSummary rhs = ((JobsHistoricSummary) other);
        return new EqualsBuilder().append(successful, rhs.successful).append(failed, rhs.failed).isEquals();
    }

}
