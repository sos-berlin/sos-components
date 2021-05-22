
package com.sos.joc.model.docu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * DocumentationFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "documentation",
    "assignReference",
    "auditLog"
})
public class DocumentationFilter {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("documentation")
    private String documentation;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("assignReference")
    private String assignReference;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("documentation")
    public String getDocumentation() {
        return documentation;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("documentation")
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("assignReference")
    public String getAssignReference() {
        return assignReference;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("assignReference")
    public void setAssignReference(String assignReference) {
        this.assignReference = assignReference;
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
        return new ToStringBuilder(this).append("documentation", documentation).append("assignReference", assignReference).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(assignReference).append(auditLog).append(documentation).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DocumentationFilter) == false) {
            return false;
        }
        DocumentationFilter rhs = ((DocumentationFilter) other);
        return new EqualsBuilder().append(assignReference, rhs.assignReference).append(auditLog, rhs.auditLog).append(documentation, rhs.documentation).isEquals();
    }

}
