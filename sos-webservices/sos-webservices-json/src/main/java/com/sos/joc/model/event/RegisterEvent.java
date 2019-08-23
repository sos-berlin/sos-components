
package com.sos.joc.model.event;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * register event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobscheduler",
    "close"
})
public class RegisterEvent {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobscheduler")
    private List<JobSchedulerObjects> jobscheduler = new ArrayList<JobSchedulerObjects>();
    @JsonProperty("close")
    private Boolean close = false;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobscheduler")
    public List<JobSchedulerObjects> getJobscheduler() {
        return jobscheduler;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobscheduler")
    public void setJobscheduler(List<JobSchedulerObjects> jobscheduler) {
        this.jobscheduler = jobscheduler;
    }

    @JsonProperty("close")
    public Boolean getClose() {
        return close;
    }

    @JsonProperty("close")
    public void setClose(Boolean close) {
        this.close = close;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobscheduler", jobscheduler).append("close", close).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(close).append(jobscheduler).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RegisterEvent) == false) {
            return false;
        }
        RegisterEvent rhs = ((RegisterEvent) other);
        return new EqualsBuilder().append(close, rhs.close).append(jobscheduler, rhs.jobscheduler).isEquals();
    }

}
