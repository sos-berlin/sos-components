
package com.sos.controller.model.workflow;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "fileOrderSources"
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

    /**
     * No args constructor for use in serialization
     * 
     */
    public Workflow() {
    }

    /**
     * 
     * @param documentationPath
     * @param instructions
     * @param jobResourceNames
     * @param jobs
     * @param title
     * @param versionDate
     * @param path
     * @param fileOrderSources
     * @param versionId
     * @param isCurrentVersion
     * @param orderRequirements
     * @param state
     */
    public Workflow(String path, Boolean isCurrentVersion, Date versionDate, SyncState state, List<FileOrderSource> fileOrderSources, String versionId, Requirements orderRequirements, List<String> jobResourceNames, List<Instruction> instructions, String title, String documentationPath, Jobs jobs) {
        super(versionId, orderRequirements, jobResourceNames, instructions, title, documentationPath, jobs);
        this.path = path;
        this.isCurrentVersion = isCurrentVersion;
        this.versionDate = versionDate;
        this.state = state;
        this.fileOrderSources = fileOrderSources;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).append("isCurrentVersion", isCurrentVersion).append("versionDate", versionDate).append("state", state).append("fileOrderSources", fileOrderSources).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(path).append(fileOrderSources).append(state).append(isCurrentVersion).append(versionDate).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(path, rhs.path).append(fileOrderSources, rhs.fileOrderSources).append(state, rhs.state).append(isCurrentVersion, rhs.isCurrentVersion).append(versionDate, rhs.versionDate).isEquals();
    }

}
