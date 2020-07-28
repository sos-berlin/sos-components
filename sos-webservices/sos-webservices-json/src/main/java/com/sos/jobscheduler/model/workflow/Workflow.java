
package com.sos.jobscheduler.model.workflow;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.deploy.DeployObject;
import com.sos.jobscheduler.model.instruction.Instruction;
import com.sos.joc.model.publish.IJSObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * workflow
 * <p>
 * deploy object with fixed property 'TYPE':'Workflow'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "path",
    "versionId",
    "instructions",
    "jobs"
})
public class Workflow
    extends DeployObject
    implements IJSObject
{

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    private String versionId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    private List<Instruction> instructions = new ArrayList<Instruction>();
    @JsonProperty("jobs")
    private Jobs jobs;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Workflow() {
    }

    /**
     * 
     * @param path
     * @param instructions
     * @param versionId
     * @param jobs
     */
    public Workflow(String path, String versionId, List<Instruction> instructions, Jobs jobs) {
        super();
        this.path = path;
        this.versionId = versionId;
        this.instructions = instructions;
        this.jobs = jobs;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    public String getVersionId() {
        return versionId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    public List<Instruction> getInstructions() {
        return instructions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    @JsonProperty("jobs")
    public Jobs getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).append("versionId", versionId).append("instructions", instructions).append("jobs", jobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(path).append(instructions).append(versionId).append(jobs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Workflow) == false) {
            return false;
        }
        Workflow rhs = ((Workflow) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(path, rhs.path).append(instructions, rhs.instructions).append(versionId, rhs.versionId).append(jobs, rhs.jobs).isEquals();
    }

}
