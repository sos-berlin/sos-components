
package com.sos.joc.model.tag.tagging;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * filter for tagging
 * <p>
 * A tag that is item of 'addTags' and 'deleteTags' won't be processed
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "addTags",
    "deleteTags",
    "auditLog"
})
public class RequestModifyFilter
    extends Folder
{

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("addTags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> addTags = new LinkedHashSet<String>();
    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("deleteTags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> deleteTags = new LinkedHashSet<String>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("addTags")
    public Set<String> getAddTags() {
        return addTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("addTags")
    public void setAddTags(Set<String> addTags) {
        this.addTags = addTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("deleteTags")
    public Set<String> getDeleteTags() {
        return deleteTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("deleteTags")
    public void setDeleteTags(Set<String> deleteTags) {
        this.deleteTags = deleteTags;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("addTags", addTags).append("deleteTags", deleteTags).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(deleteTags).append(addTags).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestModifyFilter) == false) {
            return false;
        }
        RequestModifyFilter rhs = ((RequestModifyFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(deleteTags, rhs.deleteTags).append(addTags, rhs.addTags).append(auditLog, rhs.auditLog).isEquals();
    }

}
