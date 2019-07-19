
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
 * hostPortParam
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "uri",
    "filename",
    "timeout",
    "auditLog"
})
public class UriParameter {

    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * uri
     * <p>
     * URI of a JobScheduler
     * 
     */
    @JsonProperty("uri")
    @JsonPropertyDescription("URI of a JobScheduler")
    private URI uri;
    @JsonProperty("filename")
    private String filename;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    private Integer timeout;
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
    @JsonProperty("uri")
    public URI getUri() {
        return uri;
    }

    /**
     * uri
     * <p>
     * URI of a JobScheduler
     * 
     */
    @JsonProperty("uri")
    public void setUri(URI uri) {
        this.uri = uri;
    }

    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     * @return
     *     The timeout
     */
    @JsonProperty("timeout")
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     * @param timeout
     *     The timeout
     */
    @JsonProperty("timeout")
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
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
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("uri", uri).append("filename", filename).append("timeout", timeout).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(filename).append(jobschedulerId).append(auditLog).append(uri).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UriParameter) == false) {
            return false;
        }
        UriParameter rhs = ((UriParameter) other);
        return new EqualsBuilder().append(filename, rhs.filename).append(jobschedulerId, rhs.jobschedulerId).append(timeout, rhs.timeout).append(auditLog, rhs.auditLog).append(uri, rhs.uri).isEquals();
    }

}
