
package com.sos.joc.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.JocSecurityLevel;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "title",
    "securityLevel",
    "defaultProfileAccount",
    "forceCommentsForAuditLog",
    "comments",
    "showViews"
})
public class Properties {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    @JsonProperty("title")
    private String title;
    /**
     * Security Level of JOC Cockpit
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("securityLevel")
    private JocSecurityLevel securityLevel;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("defaultProfileAccount")
    private String defaultProfileAccount;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("forceCommentsForAuditLog")
    private Boolean forceCommentsForAuditLog = false;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("comments")
    private List<String> comments = new ArrayList<String>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("showViews")
    private ShowViewProperties showViews;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Security Level of JOC Cockpit
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("securityLevel")
    public JocSecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    /**
     * Security Level of JOC Cockpit
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("securityLevel")
    public void setSecurityLevel(JocSecurityLevel securityLevel) {
        this.securityLevel = securityLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("defaultProfileAccount")
    public String getDefaultProfileAccount() {
        return defaultProfileAccount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("defaultProfileAccount")
    public void setDefaultProfileAccount(String defaultProfileAccount) {
        this.defaultProfileAccount = defaultProfileAccount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("forceCommentsForAuditLog")
    public Boolean getForceCommentsForAuditLog() {
        return forceCommentsForAuditLog;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("forceCommentsForAuditLog")
    public void setForceCommentsForAuditLog(Boolean forceCommentsForAuditLog) {
        this.forceCommentsForAuditLog = forceCommentsForAuditLog;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("comments")
    public List<String> getComments() {
        return comments;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("comments")
    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("showViews")
    public ShowViewProperties getShowViews() {
        return showViews;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("showViews")
    public void setShowViews(ShowViewProperties showViews) {
        this.showViews = showViews;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("title", title).append("securityLevel", securityLevel).append("defaultProfileAccount", defaultProfileAccount).append("forceCommentsForAuditLog", forceCommentsForAuditLog).append("comments", comments).append("showViews", showViews).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(securityLevel).append(forceCommentsForAuditLog).append(comments).append(showViews).append(deliveryDate).append(title).append(defaultProfileAccount).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Properties) == false) {
            return false;
        }
        Properties rhs = ((Properties) other);
        return new EqualsBuilder().append(securityLevel, rhs.securityLevel).append(forceCommentsForAuditLog, rhs.forceCommentsForAuditLog).append(comments, rhs.comments).append(showViews, rhs.showViews).append(deliveryDate, rhs.deliveryDate).append(title, rhs.title).append(defaultProfileAccount, rhs.defaultProfileAccount).isEquals();
    }

}
