
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
import com.sos.inventory.model.deploy.DeployType;
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
    "fileOrderSources",
    "forkListVariables",
    "hasExpectedNoticeBoards",
    "hasPostNoticeBoards"
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
    @JsonProperty("fileOrderSources")
    private List<FileOrderSource> fileOrderSources = null;
    @JsonProperty("forkListVariables")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> forkListVariables = null;
    @JsonProperty("hasExpectedNoticeBoards")
    private Boolean hasExpectedNoticeBoards;
    @JsonProperty("hasPostNoticeBoards")
    private Boolean hasPostNoticeBoards;

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
     * @param tYPE
     * @param title
     * @param versionDate
     * @param version
     * @param orderPreparation
     * @param path
     * @param fileOrderSources
     * @param versionId
     * @param forkListVariables
     * @param isCurrentVersion
     * @param state
     * @param documentationName
     */
    public Workflow(String path, Boolean isCurrentVersion, Date versionDate, SyncState state, List<FileOrderSource> fileOrderSources, Set<String> forkListVariables,
            Boolean hasExpectedNoticeBoards, Boolean hasPostNoticeBoards, DeployType tYPE, String version, String versionId, Requirements orderPreparation,
            List<String> jobResourceNames, List<Instruction> instructions, String title, String documentationName, Jobs jobs, String timeZone) {
        super(version, versionId, orderPreparation, jobResourceNames, instructions, title, documentationName, jobs, timeZone);
        this.path = path;
        this.isCurrentVersion = isCurrentVersion;
        this.versionDate = versionDate;
        this.state = state;
        this.fileOrderSources = fileOrderSources;
        this.forkListVariables = forkListVariables;
        this.hasExpectedNoticeBoards = hasExpectedNoticeBoards;
        this.hasPostNoticeBoards = hasPostNoticeBoards;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).append("isCurrentVersion", isCurrentVersion).append("versionDate", versionDate).append("state", state).append("fileOrderSources", fileOrderSources).append("forkListVariables", forkListVariables).append("hasExpectedNoticeBoards", hasExpectedNoticeBoards).append("hasPostNoticeBoards", hasPostNoticeBoards).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(path).append(hasExpectedNoticeBoards).append(fileOrderSources).append(forkListVariables).append(hasPostNoticeBoards).append(isCurrentVersion).append(state).append(versionDate).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(path, rhs.path).append(hasExpectedNoticeBoards, rhs.hasExpectedNoticeBoards).append(fileOrderSources, rhs.fileOrderSources).append(forkListVariables, rhs.forkListVariables).append(hasPostNoticeBoards, rhs.hasPostNoticeBoards).append(isCurrentVersion, rhs.isCurrentVersion).append(state, rhs.state).append(versionDate, rhs.versionDate).isEquals();
    }

}
