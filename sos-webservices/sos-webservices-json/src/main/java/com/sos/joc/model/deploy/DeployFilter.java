
package com.sos.joc.model.deploy;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Deploy filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "jsObjects",
    "replaceRepo",
    "auditLog"
})
public class DeployFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("jsObjects")
    private List<JSObject> jsObjects = new ArrayList<JSObject>();
    @JsonProperty("replaceRepo")
    private Boolean replaceRepo;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("jsObjects")
    public List<JSObject> getJsObjects() {
        return jsObjects;
    }

    @JsonProperty("jsObjects")
    public void setJsObjects(List<JSObject> jsObjects) {
        this.jsObjects = jsObjects;
    }

    @JsonProperty("replaceRepo")
    public Boolean getReplaceRepo() {
        return replaceRepo;
    }

    @JsonProperty("replaceRepo")
    public void setReplaceRepo(Boolean replaceRepo) {
        this.replaceRepo = replaceRepo;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("jsObjects", jsObjects).append("replaceRepo", replaceRepo).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jsObjects).append(jobschedulerId).append(auditLog).append(replaceRepo).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployFilter) == false) {
            return false;
        }
        DeployFilter rhs = ((DeployFilter) other);
        return new EqualsBuilder().append(jsObjects, rhs.jsObjects).append(jobschedulerId, rhs.jobschedulerId).append(auditLog, rhs.auditLog).append(replaceRepo, rhs.replaceRepo).isEquals();
    }

}
