
package com.sos.joc.model.publish;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    private String account;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("folder")
    @JsonPropertyDescription("absolute path of an object.")
    private String folder;
    /**
     * controllerId
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("auditlogId")
    private Long auditlogId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("version")
    private String version;
    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("deployType")
    private ConfigurationType deployType;
    /**
     * JOC PGP Key Type, based on the Security Level of JOC Cockpit
     * <p>
     * 
     * 
     */
    @JsonProperty("operation")
    private OperationType operation;
    /**
     * deployment state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private DeploymentState state;
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
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * Limits the number of resulting items; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("Limits the number of resulting items; -1=unlimited")
    private Integer limit = 5000;

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

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("auditlogId")
    public Long getAuditlogId() {
        return auditlogId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("auditlogId")
    public void setAuditlogId(Long auditlogId) {
        this.auditlogId = auditlogId;
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
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("deployType")
    public ConfigurationType getDeployType() {
        return deployType;
    }

    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("deployType")
    public void setDeployType(ConfigurationType deployType) {
        this.deployType = deployType;
    }

    /**
     * JOC PGP Key Type, based on the Security Level of JOC Cockpit
     * <p>
     * 
     * 
     */
    @JsonProperty("operation")
    public OperationType getOperation() {
        return operation;
    }

    /**
     * JOC PGP Key Type, based on the Security Level of JOC Cockpit
     * <p>
     * 
     * 
     */
    @JsonProperty("operation")
    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    /**
     * deployment state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public DeploymentState getState() {
        return state;
    }

    /**
     * deployment state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(DeploymentState state) {
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
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Limits the number of resulting items; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * Limits the number of resulting items; -1=unlimited
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
