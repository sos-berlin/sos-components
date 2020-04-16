
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
 * url params
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "url",
    "filename",
    "withFailover",
    "auditLog"
})
public class UrlParameter {

    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * uri
     * <p>
     * URI of a JobScheduler
     * 
     */
    @JsonProperty("url")
    @JsonPropertyDescription("URI of a JobScheduler")
    private URI url;
    @JsonProperty("withFailover")
    private Boolean withFailover = true;
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

    @JsonProperty("withFailover")
    public Boolean getWithFailover() {
        return withFailover;
    }

    @JsonProperty("withFailover")
    public void setWithFailover(Boolean withFailover) {
        this.withFailover = withFailover;
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
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("url", url).append("withFailover", withFailover).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(auditLog).append(url).append(withFailover).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UrlParameter) == false) {
            return false;
        }
        UrlParameter rhs = ((UrlParameter) other);
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(auditLog, rhs.auditLog).append(url, rhs.url).append(withFailover, rhs.withFailover).isEquals();
    }

}
