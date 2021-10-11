
package com.sos.joc.model.publish;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * detail Filter For The Deployment History
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
    "auditlogId",
    "version",
    "deployType",
    "operation",
    "state",
    "deploymentDate",
    "deleteDate",
    "from",
    "to",
    "timeZone",
    "limit"
})
public class DepHistoryDetailFilter {

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
    @JsonProperty("auditlogId")
    private String auditlogId;
    @JsonProperty("version")
    private String version;
    @JsonProperty("deployType")
    private String deployType;
    @JsonProperty("operation")
    private String operation;
    @JsonProperty("state")
    private String state;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deploymentDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deploymentDate;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deleteDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deleteDate;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("from")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String from;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("to")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String to;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * Only for db history URIs. Restricts the number of delivered items; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("Only for db history URIs. Restricts the number of delivered items; -1=unlimited")
    private Integer limit = 5000;

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

    @JsonProperty("auditlogId")
    public String getAuditlogId() {
        return auditlogId;
    }

    @JsonProperty("auditlogId")
    public void setAuditlogId(String auditlogId) {
        this.auditlogId = auditlogId;
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

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deploymentDate")
    public Date getDeploymentDate() {
        return deploymentDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deploymentDate")
    public void setDeploymentDate(Date deploymentDate) {
        this.deploymentDate = deploymentDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deleteDate")
    public Date getDeleteDate() {
        return deleteDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deleteDate")
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("from")
    public String getFrom() {
        return from;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("from")
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("to")
    public String getTo() {
        return to;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("to")
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Only for db history URIs. Restricts the number of delivered items; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * Only for db history URIs. Restricts the number of delivered items; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("account", account).append("path", path).append("folder", folder).append("controllerId", controllerId).append("commitId", commitId).append("auditlogId", auditlogId).append("version", version).append("deployType", deployType).append("operation", operation).append("state", state).append("deploymentDate", deploymentDate).append("deleteDate", deleteDate).append("from", from).append("to", to).append("timeZone", timeZone).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(timeZone).append(commitId).append(version).append(deployType).append(path).append(auditlogId).append(folder).append(deploymentDate).append(limit).append(from).append(state).append(to).append(operation).append(account).append(deleteDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DepHistoryDetailFilter) == false) {
            return false;
        }
        DepHistoryDetailFilter rhs = ((DepHistoryDetailFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(timeZone, rhs.timeZone).append(commitId, rhs.commitId).append(version, rhs.version).append(deployType, rhs.deployType).append(path, rhs.path).append(auditlogId, rhs.auditlogId).append(folder, rhs.folder).append(deploymentDate, rhs.deploymentDate).append(limit, rhs.limit).append(from, rhs.from).append(state, rhs.state).append(to, rhs.to).append(operation, rhs.operation).append(account, rhs.account).append(deleteDate, rhs.deleteDate).isEquals();
    }

}
