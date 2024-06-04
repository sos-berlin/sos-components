
package com.sos.joc.model.reporting;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * delete reports
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "reportIds",
    "auditLog"
})
public class DeleteReports {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Long> reportIds = new LinkedHashSet<Long>();
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
    @JsonProperty("reportIds")
    public Set<Long> getReportIds() {
        return reportIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportIds")
    public void setReportIds(Set<Long> reportIds) {
        this.reportIds = reportIds;
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
        return new ToStringBuilder(this).append("reportIds", reportIds).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(reportIds).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteReports) == false) {
            return false;
        }
        DeleteReports rhs = ((DeleteReports) other);
        return new EqualsBuilder().append(reportIds, rhs.reportIds).append(auditLog, rhs.auditLog).isEquals();
    }

}
