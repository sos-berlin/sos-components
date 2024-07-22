
package com.sos.controller.model.fileordersource;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;


/**
 * FileOrderSource
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "versionDate",
    "state",
    "valid",
    "deployed"
})
public class FileOrderSource
    extends com.sos.inventory.model.fileordersource.FileOrderSource
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
    @JsonProperty("valid")
    private Boolean valid;
    @JsonProperty("deployed")
    private Boolean deployed;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FileOrderSource() {
    }

    /**
     * 
     * @param directoryExpr
     * @param pattern
     * @param agentName
     * @param timeZone
     * @param workflowName
     * @param title
     * @param versionDate
     * @param directory
     * @param valid
     * @param deployed
     * @param path
     * @param delay
     * @param state
     * @param documentationName
     */
    public FileOrderSource(String path, Date versionDate, SyncState state, Boolean valid, Boolean deployed, String workflowName, String agentName, String directoryExpr, String directory, String pattern, String timeZone, Long delay, String title, String documentationName, Set<String> tags) {
        super(workflowName, agentName, directoryExpr, directory, pattern, timeZone, delay, title, documentationName, tags);
        this.path = path;
        this.versionDate = versionDate;
        this.state = state;
        this.valid = valid;
        this.deployed = deployed;
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

    @JsonProperty("valid")
    public Boolean getValid() {
        return valid;
    }

    @JsonProperty("valid")
    public void setValid(Boolean valid) {
        this.valid = valid;
    }
    
    @JsonProperty("deployed")
    public Boolean getDeployed() {
        return deployed;
    }

    @JsonProperty("deployed")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).append("versionDate", versionDate).append("state", state).append("valid", valid).append("deployed", deployed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(valid).append(deployed).append(path).append(state).append(versionDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileOrderSource) == false) {
            return false;
        }
        FileOrderSource rhs = ((FileOrderSource) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(valid, rhs.valid).append(deployed, rhs.deployed).append(path, rhs.path).append(state, rhs.state).append(versionDate, rhs.versionDate).isEquals();
    }

}
