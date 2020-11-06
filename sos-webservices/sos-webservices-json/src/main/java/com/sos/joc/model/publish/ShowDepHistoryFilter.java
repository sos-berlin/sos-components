
package com.sos.joc.model.publish;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * SOS PGP Key Pair
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
    "deploymentDate",
    "deleteDate",
    "from",
    "to"
})
public class ShowDepHistoryFilter {

    @JsonProperty("account")
    private String account;
    @JsonProperty("path")
    private String path;
    @JsonProperty("folder")
    private String folder;
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("commitId")
    private String commitId;
    @JsonProperty("version")
    private String version;
    @JsonProperty("deployType")
    private String deployType;
    @JsonProperty("operation")
    private String operation;
    @JsonProperty("state")
    private String state;
    @JsonProperty("deploymentDate")
    private Date deploymentDate;
    @JsonProperty("deleteDate")
    private Date deleteDate;
    @JsonProperty("from")
    private Date from;
    @JsonProperty("to")
    private Date to;

    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("commitId")
    public String getCommitId() {
        return commitId;
    }

    @JsonProperty("commitId")
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("deployType")
    public String getDeployType() {
        return deployType;
    }

    @JsonProperty("deployType")
    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    @JsonProperty("operation")
    public String getOperation() {
        return operation;
    }

    @JsonProperty("operation")
    public void setOperation(String operation) {
        this.operation = operation;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
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

    @JsonProperty("from")
    public Date getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(Date from) {
        this.from = from;
    }

    @JsonProperty("to")
    public Date getTo() {
        return to;
    }

    @JsonProperty("to")
    public void setTo(Date to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("account", account).append("path", path).append("folder", folder).append("controllerId", controllerId).append("commitId", commitId).append("version", version).append("deployType", deployType).append("operation", operation).append("state", state).append("deploymentDate", deploymentDate).append("deleteDate", deleteDate).append("from", from).append("to", to).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(commitId).append(version).append(deployType).append(path).append(folder).append(deploymentDate).append(from).append(state).append(to).append(operation).append(account).append(deleteDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ShowDepHistoryFilter) == false) {
            return false;
        }
        ShowDepHistoryFilter rhs = ((ShowDepHistoryFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(commitId, rhs.commitId).append(version, rhs.version).append(deployType, rhs.deployType).append(path, rhs.path).append(folder, rhs.folder).append(deploymentDate, rhs.deploymentDate).append(from, rhs.from).append(state, rhs.state).append(to, rhs.to).append(operation, rhs.operation).append(account, rhs.account).append(deleteDate, rhs.deleteDate).isEquals();
    }

}
