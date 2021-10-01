
package com.sos.joc.model.publish;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * History Item Of A Deployment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "account",
    "path",
    "folder",
    "controllerId",
    "commitId",
    "version",
    "deployType",
    "operation",
    "state",
    "errorMessage",
    "deploymentDate",
    "deleteDate",
    "invConfigurationId",
    "deploymentId",
    "workflowCount",
    "jobResourceCount",
    "fileOrderSourceCount",
    "boardCount",
    "lockCount"
})
public class DepHistoryItem {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    private String account;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    private String folder;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("commitId")
    private String commitId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("version")
    private String version;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("deployType")
    private String deployType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("operation")
    private String operation;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private String state;
    @JsonProperty("errorMessage")
    private String errorMessage;
    @JsonProperty("deploymentDate")
    private Date deploymentDate;
    @JsonProperty("deleteDate")
    private Date deleteDate;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("invConfigurationId")
    private Long invConfigurationId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("deploymentId")
    private Long deploymentId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowCount")
    private Long workflowCount;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobResourceCount")
    private Long jobResourceCount;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("fileOrderSourceCount")
    private Long fileOrderSourceCount;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("boardCount")
    private Long boardCount;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("lockCount")
    private Long lockCount;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * string without < and >
     * <p>
     * 
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
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("commitId")
    public String getCommitId() {
        return commitId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("commitId")
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * string without < and >
     * <p>
     * 
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
    @JsonProperty("deployType")
    public String getDeployType() {
        return deployType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("deployType")
    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("operation")
    public String getOperation() {
        return operation;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("operation")
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("errorMessage")
    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonProperty("errorMessage")
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @JsonProperty("deploymentDate")
    public Date getDeploymentDate() {
        return deploymentDate;
    }

    @JsonProperty("deploymentDate")
    public void setDeploymentDate(Date deploymentDate) {
        this.deploymentDate = deploymentDate;
    }

    @JsonProperty("deleteDate")
    public Date getDeleteDate() {
        return deleteDate;
    }

    @JsonProperty("deleteDate")
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("invConfigurationId")
    public Long getInvConfigurationId() {
        return invConfigurationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("invConfigurationId")
    public void setInvConfigurationId(Long invConfigurationId) {
        this.invConfigurationId = invConfigurationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("deploymentId")
    public Long getDeploymentId() {
        return deploymentId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("deploymentId")
    public void setDeploymentId(Long deploymentId) {
        this.deploymentId = deploymentId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowCount")
    public Long getWorkflowCount() {
        return workflowCount;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowCount")
    public void setWorkflowCount(Long workflowCount) {
        this.workflowCount = workflowCount;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobResourceCount")
    public Long getJobResourceCount() {
        return jobResourceCount;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobResourceCount")
    public void setJobResourceCount(Long jobResourceCount) {
        this.jobResourceCount = jobResourceCount;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("fileOrderSourceCount")
    public Long getFileOrderSourceCount() {
        return fileOrderSourceCount;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("fileOrderSourceCount")
    public void setFileOrderSourceCount(Long fileOrderSourceCount) {
        this.fileOrderSourceCount = fileOrderSourceCount;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("boardCount")
    public Long getBoardCount() {
        return boardCount;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("boardCount")
    public void setBoardCount(Long boardCount) {
        this.boardCount = boardCount;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("lockCount")
    public Long getLockCount() {
        return lockCount;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("lockCount")
    public void setLockCount(Long lockCount) {
        this.lockCount = lockCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("account", account).append("path", path).append("folder", folder).append("controllerId", controllerId).append("commitId", commitId).append("version", version).append("deployType", deployType).append("operation", operation).append("state", state).append("errorMessage", errorMessage).append("deploymentDate", deploymentDate).append("deleteDate", deleteDate).append("invConfigurationId", invConfigurationId).append("deploymentId", deploymentId).append("workflowCount", workflowCount).append("jobResourceCount", jobResourceCount).append("fileOrderSourceCount", fileOrderSourceCount).append("boardCount", boardCount).append("lockCount", lockCount).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(lockCount).append(jobResourceCount).append(boardCount).append(controllerId).append(fileOrderSourceCount).append(errorMessage).append(commitId).append(invConfigurationId).append(version).append(deployType).append(path).append(folder).append(workflowCount).append(deploymentDate).append(deploymentId).append(state).append(operation).append(account).append(deleteDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DepHistoryItem) == false) {
            return false;
        }
        DepHistoryItem rhs = ((DepHistoryItem) other);
        return new EqualsBuilder().append(lockCount, rhs.lockCount).append(jobResourceCount, rhs.jobResourceCount).append(boardCount, rhs.boardCount).append(controllerId, rhs.controllerId).append(fileOrderSourceCount, rhs.fileOrderSourceCount).append(errorMessage, rhs.errorMessage).append(commitId, rhs.commitId).append(invConfigurationId, rhs.invConfigurationId).append(version, rhs.version).append(deployType, rhs.deployType).append(path, rhs.path).append(folder, rhs.folder).append(workflowCount, rhs.workflowCount).append(deploymentDate, rhs.deploymentDate).append(deploymentId, rhs.deploymentId).append(state, rhs.state).append(operation, rhs.operation).append(account, rhs.account).append(deleteDate, rhs.deleteDate).isEquals();
    }

}
