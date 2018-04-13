
package com.sos.jobscheduler.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "TYPE",
    "jobPath",
    "agentPath",
    "success",
    "failure"
})
public class Job
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    private String tYPE = "Job";
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("jobPath")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "jobPath")
    private String jobPath;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("agentPath")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "agentPath")
    private String agentPath;
    @JsonProperty("success")
    @JacksonXmlProperty(localName = "succes")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "success")
    private List<Integer> success = null;
    @JsonProperty("failure")
    @JacksonXmlProperty(localName = "failure")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "failure")
    private List<Integer> failure = null;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("jobPath")
    @JacksonXmlProperty(localName = "jobPath")
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
    @JacksonXmlProperty(localName = "jobPath")
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
    @JacksonXmlProperty(localName = "agentPath")
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
    @JacksonXmlProperty(localName = "agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    @JsonProperty("success")
    @JacksonXmlProperty(localName = "succes")
    public List<Integer> getSuccess() {
        return success;
    }

    @JsonProperty("success")
    @JacksonXmlProperty(localName = "succes")
    public void setSuccess(List<Integer> success) {
        this.success = success;
    }

    @JsonProperty("failure")
    @JacksonXmlProperty(localName = "failure")
    public List<Integer> getFailure() {
        return failure;
    }

    @JsonProperty("failure")
    @JacksonXmlProperty(localName = "failure")
    public void setFailure(List<Integer> failure) {
        this.failure = failure;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("tYPE", tYPE).append("jobPath", jobPath).append("agentPath", agentPath).append("success", success).append("failure", failure).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(agentPath).append(jobPath).append(tYPE).append(success).append(failure).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentPath, rhs.agentPath).append(jobPath, rhs.jobPath).append(tYPE, rhs.tYPE).append(success, rhs.success).append(failure, rhs.failure).isEquals();
    }

}
