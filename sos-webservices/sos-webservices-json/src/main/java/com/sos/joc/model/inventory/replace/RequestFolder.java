
package com.sos.joc.model.inventory.replace;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter for replace
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "search",
    "replace",
    "auditLog"
})
public class RequestFolder {

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("search")
    private String search;
    @JsonProperty("replace")
    private String replace = "";
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("search")
    public String getSearch() {
        return search;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("search")
    public void setSearch(String search) {
        this.search = search;
    }

    @JsonProperty("replace")
    public String getReplace() {
        return replace;
    }

    @JsonProperty("replace")
    public void setReplace(String replace) {
        this.replace = replace;
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
        return new ToStringBuilder(this).append("path", path).append("search", search).append("replace", replace).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(replace).append(path).append(search).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFolder) == false) {
            return false;
        }
        RequestFolder rhs = ((RequestFolder) other);
        return new EqualsBuilder().append(replace, rhs.replace).append(path, rhs.path).append(search, rhs.search).append(auditLog, rhs.auditLog).isEquals();
    }

}
