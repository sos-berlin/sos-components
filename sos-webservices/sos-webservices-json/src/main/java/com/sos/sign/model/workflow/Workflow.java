
package com.sos.sign.model.workflow;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.model.common.IDeployObject;
import com.sos.sign.model.instruction.Instruction;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * workflow
 * <p>
 * deploy object with fixed property 'TYPE':'Workflow'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "path",
    "versionId",
    "orderPreparation",
    "jobResourcePaths",
    "instructions",
    "jobs"
})
public class Workflow implements IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.WORKFLOW;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    private String versionId;
    /**
     * order or job requirements
     * <p>
     * 
     * 
     */
    @JsonProperty("orderPreparation")
    @JsonAlias({
        "orderRequirements"
    })
    private Requirements orderPreparation;
    @JsonProperty("jobResourcePaths")
    @JsonAlias({
        "jobResourceNames"
    })
    private List<String> jobResourcePaths = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    private List<Instruction> instructions = null;
    /**
     * workflow jobs
     * <p>
     * 
     * (Required)
     * 
     */
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
     * @param jobResourcePaths
     * @param jobs
     * @param tYPE
     * @param orderPreparation
     */
    public Workflow(DeployType tYPE, String path, String versionId, Requirements orderPreparation, List<String> jobResourcePaths, List<Instruction> instructions, Jobs jobs) {
        super();
        this.tYPE = tYPE;
        this.path = path;
        this.versionId = versionId;
        this.orderPreparation = orderPreparation;
        this.jobResourcePaths = jobResourcePaths;
        this.instructions = instructions;
        this.jobs = jobs;
    }

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public DeployType getTYPE() {
        return tYPE;
    }

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(DeployType tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    public String getVersionId() {
        return versionId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    /**
     * order or job requirements
     * <p>
     * 
     * 
     */
    @JsonProperty("orderPreparation")
    public Requirements getOrderPreparation() {
        return orderPreparation;
    }

    /**
     * order or job requirements
     * <p>
     * 
     * 
     */
    @JsonProperty("orderPreparation")
    public void setOrderPreparation(Requirements orderPreparation) {
        this.orderPreparation = orderPreparation;
    }

    @JsonProperty("jobResourcePaths")
    public List<String> getJobResourcePaths() {
        return jobResourcePaths;
    }

    @JsonProperty("jobResourcePaths")
    public void setJobResourcePaths(List<String> jobResourcePaths) {
        this.jobResourcePaths = jobResourcePaths;
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

    /**
     * workflow jobs
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobs")
    public Jobs getJobs() {
        return jobs;
    }

    /**
     * workflow jobs
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobs")
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).append("versionId", versionId).append("orderPreparation", orderPreparation).append("jobResourcePaths", jobResourcePaths).append("instructions", instructions).append("jobs", jobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(instructions).append(versionId).append(jobResourcePaths).append(jobs).append(tYPE).append(orderPreparation).toHashCode();
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
        return new EqualsBuilder().append(path, rhs.path).append(instructions, rhs.instructions).append(versionId, rhs.versionId).append(jobResourcePaths, rhs.jobResourcePaths).append(jobs, rhs.jobs).append(tYPE, rhs.tYPE).append(orderPreparation, rhs.orderPreparation).isEquals();
    }

}
