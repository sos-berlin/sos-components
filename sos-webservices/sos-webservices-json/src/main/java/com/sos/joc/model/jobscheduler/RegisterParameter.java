
package com.sos.joc.model.jobscheduler;

import java.net.URI;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * register params
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "id",
    "url",
    "role",
    "auditLog"
})
public class RegisterParameter {

    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * uri
     * <p>
     * URI of a JobScheduler
     * 
     */
    @JsonProperty("url")
    @JsonPropertyDescription("URI of a JobScheduler")
    private URI url;
    @JsonProperty("role")
    private Role role;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * uri
     * <p>
     * URI of a JobScheduler
     * 
     */
    @JsonProperty("url")
    public URI getUrl() {
        return url;
    }

    /**
     * uri
     * <p>
     * URI of a JobScheduler
     * 
     */
    @JsonProperty("url")
    public void setUrl(URI url) {
        this.url = url;
    }

    @JsonProperty("role")
    public Role getRole() {
        return role;
    }

    @JsonProperty("role")
    public void setRole(Role role) {
        this.role = role;
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
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("id", id).append("url", url).append("role", role).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(role).append(jobschedulerId).append(auditLog).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RegisterParameter) == false) {
            return false;
        }
        RegisterParameter rhs = ((RegisterParameter) other);
        return new EqualsBuilder().append(id, rhs.id).append(role, rhs.role).append(jobschedulerId, rhs.jobschedulerId).append(auditLog, rhs.auditLog).append(url, rhs.url).isEquals();
    }

}
