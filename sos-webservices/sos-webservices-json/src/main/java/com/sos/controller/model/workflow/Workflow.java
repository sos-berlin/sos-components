
package com.sos.controller.model.workflow;

import java.util.Date;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Requirements;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * workflow
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "isCurrentVersion",
    "versionDate",
    "state",
    "suspended",
    "fileOrderSources",
    "forkListVariables",
    "hasExpectedNoticeBoards",
    "hasPostNoticeBoards",
    "hasConsumeNoticeBoards",
    "hasAddOrderDependencies",
    "numOfStoppedInstructions",
    "numOfSkippedInstructions"
})
public class Workflow
    extends com.sos.inventory.model.workflow.Workflow
{

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    @JsonProperty("isCurrentVersion")
    private Boolean isCurrentVersion = true;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date versionDate;
    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private SyncState state;
    /**
     * true if state._text == SUSPENDED or SUSPENDING
     * 
     */
    @JsonProperty("suspended")
    @JsonPropertyDescription("true if state._text == SUSPENDED or SUSPENDING")
    private Boolean suspended = false;
    @JsonProperty("fileOrderSources")
    private List<FileOrderSource> fileOrderSources = null;
    @JsonProperty("forkListVariables")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> forkListVariables = null;
    @JsonProperty("hasExpectedNoticeBoards")
    private Boolean hasExpectedNoticeBoards;
    @JsonProperty("hasPostNoticeBoards")
    private Boolean hasPostNoticeBoards;
    @JsonProperty("hasConsumeNoticeBoards")
    private Boolean hasConsumeNoticeBoards;
    @JsonProperty("hasAddOrderDependencies")
    private Boolean hasAddOrderDependencies;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfStoppedInstructions")
    private Integer numOfStoppedInstructions;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfSkippedInstructions")
    private Integer numOfSkippedInstructions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Workflow() {
    }

    /**
     * 
     * @param hasExpectedNoticeBoards
     * @param instructions
     * @param hasPostNoticeBoards
     * @param jobResourceNames
     * @param jobs
     * @param timeZone
     * @param title
     * @param versionDate
     * @param version
     * @param suspended
     * @param orderPreparation
     * @param hasConsumeNoticeBoards
     * @param path
     * @param numOfStoppedInstructions
     * @param fileOrderSources
     * @param versionId
     * @param forkListVariables
     * @param isCurrentVersion
     * @param state
     * @param hasAddOrderDependencies
     * @param documentationName
     * @param numOfSkippedInstructions
     */
    public Workflow(String path, Boolean isCurrentVersion, Date versionDate, SyncState state, Boolean suspended, List<FileOrderSource> fileOrderSources, Set<String> forkListVariables, Boolean hasExpectedNoticeBoards, Boolean hasPostNoticeBoards, Boolean hasConsumeNoticeBoards, Boolean hasAddOrderDependencies, Integer numOfStoppedInstructions, Integer numOfSkippedInstructions, String version, String versionId, String timeZone, String title, String documentationName, Requirements orderPreparation, List<String> jobResourceNames, List<Instruction> instructions, Jobs jobs) {
        super(version, versionId, timeZone, title, documentationName, orderPreparation, jobResourceNames, instructions, jobs);
        this.path = path;
        this.isCurrentVersion = isCurrentVersion;
        this.versionDate = versionDate;
        this.state = state;
        this.suspended = suspended;
        this.fileOrderSources = fileOrderSources;
        this.forkListVariables = forkListVariables;
        this.hasExpectedNoticeBoards = hasExpectedNoticeBoards;
        this.hasPostNoticeBoards = hasPostNoticeBoards;
        this.hasConsumeNoticeBoards = hasConsumeNoticeBoards;
        this.hasAddOrderDependencies = hasAddOrderDependencies;
        this.numOfStoppedInstructions = numOfStoppedInstructions;
        this.numOfSkippedInstructions = numOfSkippedInstructions;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("isCurrentVersion")
    public Boolean getIsCurrentVersion() {
        return isCurrentVersion;
    }

    @JsonProperty("isCurrentVersion")
    public void setIsCurrentVersion(Boolean isCurrentVersion) {
        this.isCurrentVersion = isCurrentVersion;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    public Date getVersionDate() {
        return versionDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public SyncState getState() {
        return state;
    }

    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(SyncState state) {
        this.state = state;
    }

    /**
     * true if state._text == SUSPENDED or SUSPENDING
     * 
     */
    @JsonProperty("suspended")
    public Boolean getSuspended() {
        return suspended;
    }

    /**
     * true if state._text == SUSPENDED or SUSPENDING
     * 
     */
    @JsonProperty("suspended")
    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    @JsonProperty("fileOrderSources")
    public List<FileOrderSource> getFileOrderSources() {
        return fileOrderSources;
    }

    @JsonProperty("fileOrderSources")
    public void setFileOrderSources(List<FileOrderSource> fileOrderSources) {
        this.fileOrderSources = fileOrderSources;
    }

    @JsonProperty("forkListVariables")
    public Set<String> getForkListVariables() {
        return forkListVariables;
    }

    @JsonProperty("forkListVariables")
    public void setForkListVariables(Set<String> forkListVariables) {
        this.forkListVariables = forkListVariables;
    }

    @JsonProperty("hasExpectedNoticeBoards")
    public Boolean getHasExpectedNoticeBoards() {
        return hasExpectedNoticeBoards;
    }

    @JsonProperty("hasExpectedNoticeBoards")
    public void setHasExpectedNoticeBoards(Boolean hasExpectedNoticeBoards) {
        this.hasExpectedNoticeBoards = hasExpectedNoticeBoards;
    }

    @JsonProperty("hasPostNoticeBoards")
    public Boolean getHasPostNoticeBoards() {
        return hasPostNoticeBoards;
    }

    @JsonProperty("hasPostNoticeBoards")
    public void setHasPostNoticeBoards(Boolean hasPostNoticeBoards) {
        this.hasPostNoticeBoards = hasPostNoticeBoards;
    }

    @JsonProperty("hasConsumeNoticeBoards")
    public Boolean getHasConsumeNoticeBoards() {
        return hasConsumeNoticeBoards;
    }

    @JsonProperty("hasConsumeNoticeBoards")
    public void setHasConsumeNoticeBoards(Boolean hasConsumeNoticeBoards) {
        this.hasConsumeNoticeBoards = hasConsumeNoticeBoards;
    }

    @JsonProperty("hasAddOrderDependencies")
    public Boolean getHasAddOrderDependencies() {
        return hasAddOrderDependencies;
    }

    @JsonProperty("hasAddOrderDependencies")
    public void setHasAddOrderDependencies(Boolean hasAddOrderDependencies) {
        this.hasAddOrderDependencies = hasAddOrderDependencies;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfStoppedInstructions")
    public Integer getNumOfStoppedInstructions() {
        return numOfStoppedInstructions;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfStoppedInstructions")
    public void setNumOfStoppedInstructions(Integer numOfStoppedInstructions) {
        this.numOfStoppedInstructions = numOfStoppedInstructions;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfSkippedInstructions")
    public Integer getNumOfSkippedInstructions() {
        return numOfSkippedInstructions;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfSkippedInstructions")
    public void setNumOfSkippedInstructions(Integer numOfSkippedInstructions) {
        this.numOfSkippedInstructions = numOfSkippedInstructions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).append("isCurrentVersion", isCurrentVersion).append("versionDate", versionDate).append("state", state).append("suspended", suspended).append("fileOrderSources", fileOrderSources).append("forkListVariables", forkListVariables).append("hasExpectedNoticeBoards", hasExpectedNoticeBoards).append("hasPostNoticeBoards", hasPostNoticeBoards).append("hasConsumeNoticeBoards", hasConsumeNoticeBoards).append("hasAddOrderDependencies", hasAddOrderDependencies).append("numOfStoppedInstructions", numOfStoppedInstructions).append("numOfSkippedInstructions", numOfSkippedInstructions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(hasExpectedNoticeBoards).append(hasPostNoticeBoards).append(versionDate).append(suspended).append(hasConsumeNoticeBoards).append(path).append(numOfStoppedInstructions).append(fileOrderSources).append(forkListVariables).append(isCurrentVersion).append(state).append(hasAddOrderDependencies).append(numOfSkippedInstructions).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(hasExpectedNoticeBoards, rhs.hasExpectedNoticeBoards).append(hasPostNoticeBoards, rhs.hasPostNoticeBoards).append(versionDate, rhs.versionDate).append(suspended, rhs.suspended).append(hasConsumeNoticeBoards, rhs.hasConsumeNoticeBoards).append(path, rhs.path).append(numOfStoppedInstructions, rhs.numOfStoppedInstructions).append(fileOrderSources, rhs.fileOrderSources).append(forkListVariables, rhs.forkListVariables).append(isCurrentVersion, rhs.isCurrentVersion).append(state, rhs.state).append(hasAddOrderDependencies, rhs.hasAddOrderDependencies).append(numOfSkippedInstructions, rhs.numOfSkippedInstructions).isEquals();
    }

}
