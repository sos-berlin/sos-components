
package com.sos.sign.model.workflow;

import java.util.List;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "path",
    "versionId",
    "orderRequirements",
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
    private DeployType tYPE = DeployType.fromValue("Workflow");
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
    @JsonProperty("orderRequirements")
    private Requirements orderRequirements;
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
     * @param orderRequirements
     * @param jobs
     * @param tYPE
     */
    public Workflow(DeployType tYPE, String path, String versionId, Requirements orderRequirements, List<Instruction> instructions, Jobs jobs) {
        super();
        this.tYPE = tYPE;
        this.path = path;
        this.versionId = versionId;
        this.orderRequirements = orderRequirements;
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
    @JsonProperty("orderRequirements")
    public Requirements getOrderRequirements() {
        return orderRequirements;
    }

    /**
     * order or job requirements
     * <p>
     * 
     * 
     */
    @JsonProperty("orderRequirements")
    public void setOrderRequirements(Requirements orderRequirements) {
        this.orderRequirements = orderRequirements;
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
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).append("versionId", versionId).append("orderRequirements", orderRequirements).append("instructions", instructions).append("jobs", jobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(instructions).append(versionId).append(orderRequirements).append(jobs).append(tYPE).toHashCode();
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
        return new EqualsBuilder().append(path, rhs.path).append(instructions, rhs.instructions).append(versionId, rhs.versionId).append(orderRequirements, rhs.orderRequirements).append(jobs, rhs.jobs).append(tYPE, rhs.tYPE).isEquals();
    }

}
