
package com.sos.joc.model.publish.git.commands;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.repository.Category;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter To checkout a specific branch in a local repository
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "branch",
    "tag",
    "folder",
    "category",
    "auditLog"
})
public class CheckoutFilter {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("branch")
    private String branch;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("tag")
    private String tag;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    private String folder;
    /**
     * Repository Category, based on the local environment or environment independent/able to roll out
     * <p>
     * 
     * 
     */
    @JsonProperty("category")
    private Category category;
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
     * 
     */
    @JsonProperty("branch")
    public String getBranch() {
        return branch;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("branch")
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("tag")
    public String getTag() {
        return tag;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("tag")
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * Repository Category, based on the local environment or environment independent/able to roll out
     * <p>
     * 
     * 
     */
    @JsonProperty("category")
    public Category getCategory() {
        return category;
    }

    /**
     * Repository Category, based on the local environment or environment independent/able to roll out
     * <p>
     * 
     * 
     */
    @JsonProperty("category")
    public void setCategory(Category category) {
        this.category = category;
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
        return new ToStringBuilder(this).append("branch", branch).append("tag", tag).append("folder", folder).append("category", category).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tag).append(folder).append(category).append(auditLog).append(branch).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CheckoutFilter) == false) {
            return false;
        }
        CheckoutFilter rhs = ((CheckoutFilter) other);
        return new EqualsBuilder().append(tag, rhs.tag).append(folder, rhs.folder).append(category, rhs.category).append(auditLog, rhs.auditLog).append(branch, rhs.branch).isEquals();
    }

}
