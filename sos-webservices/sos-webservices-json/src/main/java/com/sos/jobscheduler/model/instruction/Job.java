
package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.job.JobReturnCode;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobPath",
    "agentPath",
    "returnCodeMeaning"
})
public class Job
    extends Instruction
    implements IInstruction
{

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("jobPath")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    private String jobPath;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("agentPath")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    private String agentPath;
    @JsonProperty("returnCodeMeaning")
    private JobReturnCode returnCodeMeaning;

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("jobPath")
    public String getJobPath() {
        return jobPath;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("jobPath")
    public void setJobPath(String jobPath) {
        this.jobPath = jobPath;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    @JsonProperty("returnCodeMeaning")
    public JobReturnCode getReturnCodeMeaning() {
        return returnCodeMeaning;
    }

    @JsonProperty("returnCodeMeaning")
    public void setReturnCodeMeaning(JobReturnCode returnCodeMeaning) {
        this.returnCodeMeaning = returnCodeMeaning;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jobPath", jobPath).append("agentPath", agentPath).append("returnCodeMeaning", returnCodeMeaning).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(agentPath).append(returnCodeMeaning).append(jobPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Job) == false) {
            return false;
        }
        Job rhs = ((Job) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentPath, rhs.agentPath).append(returnCodeMeaning, rhs.returnCodeMeaning).append(jobPath, rhs.jobPath).isEquals();
    }

}
