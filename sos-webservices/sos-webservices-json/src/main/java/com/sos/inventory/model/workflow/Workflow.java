
package com.sos.inventory.model.workflow;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IDeployObject;
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
    "versionId",
    "orderRequirements",
    "jobResourceNames",
    "instructions",
    "title",
    "documentationName",
    "jobs"
})
public class Workflow implements IConfigurationObject, IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.WORKFLOW;
    /**
     * string without < and >
     * <p>
     * 
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
    @JsonProperty("jobResourceNames")
    @JsonAlias({
        "jobResourcePaths"
    })
    private List<String> jobResourceNames = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    private List<Instruction> instructions = null;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    private String documentationName;
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
     * @param instructions
     * @param versionId
     * @param orderRequirements
     * @param jobResourceNames
     * @param jobs
     * @param documentationName
     * 
     * @param title
     */
    public Workflow(String versionId, Requirements orderRequirements, List<String> jobResourceNames, List<Instruction> instructions, String title, String documentationName, Jobs jobs) {
        super();
        this.versionId = versionId;
        this.orderRequirements = orderRequirements;
        this.jobResourceNames = jobResourceNames;
        this.instructions = instructions;
        this.title = title;
        this.documentationName = documentationName;
        this.jobs = jobs;
    }

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    public DeployType getTYPE() {
        return tYPE;
    }

    /**
     * string without < and >
     * <p>
     * 
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

    @JsonProperty("jobResourceNames")
    public List<String> getJobResourceNames() {
        return jobResourceNames;
    }

    @JsonProperty("jobResourceNames")
    public void setJobResourceNames(List<String> jobResourceNames) {
        this.jobResourceNames = jobResourceNames;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public String getDocumentationName() {
        return documentationName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public void setDocumentationName(String documentationName) {
        this.documentationName = documentationName;
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
        return new ToStringBuilder(this).append("tYPE", tYPE).append("versionId", versionId).append("orderRequirements", orderRequirements).append("jobResourceNames", jobResourceNames).append("instructions", instructions).append("title", title).append("documentationName", documentationName).append("jobs", jobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(instructions).append(versionId).append(orderRequirements).append(jobResourceNames).append(jobs).append(documentationName).append(tYPE).append(title).toHashCode();
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
        return new EqualsBuilder().append(instructions, rhs.instructions).append(versionId, rhs.versionId).append(orderRequirements, rhs.orderRequirements).append(jobResourceNames, rhs.jobResourceNames).append(jobs, rhs.jobs).append(documentationName, rhs.documentationName).append(tYPE, rhs.tYPE).append(title, rhs.title).isEquals();
    }

}
