
package com.sos.joc.model.monitoring.notification.system;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * notification filter with notification id
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "notificationIds",
    "comment",
    "auditLog"
})
public class SystemNotificationAcknowledgeFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("notificationIds")
    private List<Long> notificationIds = new ArrayList<Long>();
    @JsonProperty("comment")
    private String comment;
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
    @JsonProperty("notificationIds")
    public List<Long> getNotificationIds() {
        return notificationIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("notificationIds")
    public void setNotificationIds(List<Long> notificationIds) {
        this.notificationIds = notificationIds;
    }

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
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
        return new ToStringBuilder(this).append("notificationIds", notificationIds).append("comment", comment).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(notificationIds).append(comment).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SystemNotificationAcknowledgeFilter) == false) {
            return false;
        }
        SystemNotificationAcknowledgeFilter rhs = ((SystemNotificationAcknowledgeFilter) other);
        return new EqualsBuilder().append(notificationIds, rhs.notificationIds).append(comment, rhs.comment).append(auditLog, rhs.auditLog).isEquals();
    }

}
