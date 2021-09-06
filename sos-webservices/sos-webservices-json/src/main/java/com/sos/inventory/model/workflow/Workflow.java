
package com.sos.inventory.model.workflow;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.IInventoryObject;
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
    "version",
    "versionId",
    "orderPreparation",
    "jobResourceNames",
    "instructions",
    "title",
    "documentationName",
    "jobs",
    "timeZone"
})
public class Workflow implements IInventoryObject, IConfigurationObject, IDeployObject
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
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("inventory repository version")
    private String version = "1.0.0";
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
    @JsonProperty("orderPreparation")
    @JsonAlias({
        "orderRequirements"
    })
    private Requirements orderPreparation;
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
    @JsonProperty("timeZone")
    private String timeZone = "Etc/UTC";

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
     * @param jobResourceNames
     * @param jobs
     * @param timeZone
     * @param documentationName
     * @param title
     * @param version
     * @param orderPreparation
     */
    public Workflow(String version, String versionId, Requirements orderPreparation, List<String> jobResourceNames, List<Instruction> instructions,
            String title, String documentationName, Jobs jobs, String timeZone) {
        super();
        this.version = version;
        this.versionId = versionId;
        this.orderPreparation = orderPreparation;
        this.jobResourceNames = jobResourceNames;
        this.instructions = instructions;
        this.title = title;
        this.documentationName = documentationName;
        this.jobs = jobs;
        this.timeZone = timeZone;
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
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
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

    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("version", version).append("versionId", versionId).append("orderPreparation", orderPreparation).append("jobResourceNames", jobResourceNames).append("instructions", instructions).append("title", title).append("documentationName", documentationName).append("jobs", jobs).append("timeZone", timeZone).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(instructions).append(versionId).append(jobResourceNames).append(jobs).append(timeZone).append(documentationName).append(tYPE).append(title).append(version).append(orderPreparation).toHashCode();
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
        return new EqualsBuilder().append(instructions, rhs.instructions).append(versionId, rhs.versionId).append(jobResourceNames, rhs.jobResourceNames).append(jobs, rhs.jobs).append(timeZone, rhs.timeZone).append(documentationName, rhs.documentationName).append(tYPE, rhs.tYPE).append(title, rhs.title).append(version, rhs.version).append(orderPreparation, rhs.orderPreparation).isEquals();
    }

}
