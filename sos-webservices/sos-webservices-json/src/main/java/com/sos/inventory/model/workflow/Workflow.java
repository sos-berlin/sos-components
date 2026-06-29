
package com.sos.inventory.model.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.job.Job;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
    "timeZone",
    "dayOffset",
    "title",
    "documentationName",
    "orderPreparation",
    "jobResourceNames",
    "instructions",
    "jobs"
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
    private String version = "1.7.2";
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("versionId")
    private String versionId;
    @JsonProperty("timeZone")
    private String timeZone = "Etc/UTC";
    /**
     * time in format HH:MM:SS
     * 
     */
    @JsonProperty("dayOffset")
    @JsonPropertyDescription("time in format HH:MM:SS")
    private String dayOffset;
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
     * OrderPreparation
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
     * workflow jobs
     * <p>
     * 
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
     * @param dayOffset
     * @param jobResourceNames
     * @param jobs
     * @param timeZone
     * @param documentationName
     * @param title
     * @param version
     * @param orderPreparation
     */
    public Workflow(String version, String versionId, String timeZone, String dayOffset, String title, String documentationName, Requirements orderPreparation, List<String> jobResourceNames, List<Instruction> instructions, Jobs jobs) {
        super();
        this.version = version;
        this.versionId = versionId;
        this.timeZone = timeZone;
        this.dayOffset = dayOffset;
        this.title = title;
        this.documentationName = documentationName;
        this.orderPreparation = orderPreparation;
        this.jobResourceNames = jobResourceNames;
        this.instructions = instructions;
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

    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * time in format HH:MM:SS
     * 
     */
    @JsonProperty("dayOffset")
    public String getDayOffset() {
        return dayOffset;
    }

    /**
     * time in format HH:MM:SS
     * 
     */
    @JsonProperty("dayOffset")
    public void setDayOffset(String dayOffset) {
        this.dayOffset = dayOffset;
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
     * OrderPreparation
     * <p>
     * 
     * 
     */
    @JsonProperty("orderPreparation")
    public Requirements getOrderPreparation() {
        return orderPreparation;
    }

    /**
     * OrderPreparation
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
     * workflow jobs
     * <p>
     * 
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
     * 
     */
    @JsonProperty("jobs")
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("timeZone", timeZone).append("title", title).append("dayOffset", dayOffset).append("documentationName", documentationName).append("orderPreparation", orderPreparation).append("jobResourceNames", jobResourceNames).append("instructions", instructions).append("jobs", jobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(instructions).append(jobResourceNames).append(jobs).append(timeZone).append(dayOffset).append(documentationName).append(tYPE).append(title).append(orderPreparation).toHashCode();
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
        return new EqualsBuilder().append(instructions, rhs.instructions).append(jobResourceNames, rhs.jobResourceNames).append(jobs, rhs.jobs).append(timeZone, rhs.timeZone).append(dayOffset, rhs.dayOffset).append(documentationName, rhs.documentationName).append(tYPE, rhs.tYPE).append(title, rhs.title).append(orderPreparation, rhs.orderPreparation).isEquals();
    }
    
    @Override
    public boolean sufficientlyEquals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Workflow) == false) {
            return false;
        }
        Workflow rhs = ((Workflow) other);
        boolean sufficientlyEqual = new EqualsBuilder().append(instructions, rhs.instructions).append(jobResourceNames, rhs.jobResourceNames)
                .append(timeZone, rhs.timeZone).append(tYPE, rhs.tYPE).append(orderPreparation, rhs.orderPreparation).isEquals();
        if(sufficientlyEqual) {
            Map<String, Job> thisJobs = Optional.ofNullable(jobs).map(Jobs::getAdditionalProperties).orElse(Collections.emptyMap());
            Map<String, Job> otherJobs = Optional.ofNullable(rhs.jobs).map(Jobs::getAdditionalProperties).orElse(Collections.emptyMap());
            if(!thisJobs.keySet().equals(otherJobs.keySet())) {
                sufficientlyEqual = false;
            } else {
                for(Map.Entry<String, Job> entry : thisJobs.entrySet()) {
                    Job thisJob = entry.getValue();
                    Job otherJob = otherJobs.get(entry.getKey());
                    if(!thisJob.sufficientlyEquals(otherJob)) {
                        sufficientlyEqual = false;
                        break;
                    }
                }
            }
        }
        return sufficientlyEqual; 
    }

}
