
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
    "apiVersion",
    "inventoryVersion",
    "forceCommentsForAuditLog",
    "comments",
    "copy",
    "restore",
    "import",
    "showViews",
    "welcomeDoNotRemindMe",
    "welcomeGotIt",
    "clusterLicense",
    "licenseValidFrom",
    "licenseValidUntil"
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
    @JsonProperty("apiVersion")
    private String apiVersion;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("inventoryVersion")
    private String inventoryVersion;
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
     * suffix/prefix properties
     * <p>
     * 
     * 
     */
    @JsonProperty("copy")
    private SuffixPrefix copy;
    /**
     * suffix/prefix properties
     * <p>
     * 
     * 
     */
    @JsonProperty("restore")
    private SuffixPrefix restore;
    /**
     * suffix/prefix properties
     * <p>
     * 
     * 
     */
    @JsonProperty("import")
    private SuffixPrefix _import;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("showViews")
    private ShowViewProperties showViews;
    @JsonProperty("welcomeDoNotRemindMe")
    private Boolean welcomeDoNotRemindMe = false;
    @JsonProperty("welcomeGotIt")
    private Boolean welcomeGotIt = false;
    @JsonProperty("clusterLicense")
    private Boolean clusterLicense = false;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("licenseValidFrom")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date licenseValidFrom;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("licenseValidUntil")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date licenseValidUntil;

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
    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("inventoryVersion")
    public String getInventoryVersion() {
        return inventoryVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("inventoryVersion")
    public void setInventoryVersion(String inventoryVersion) {
        this.inventoryVersion = inventoryVersion;
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
     * suffix/prefix properties
     * <p>
     * 
     * 
     */
    @JsonProperty("copy")
    public SuffixPrefix getCopy() {
        return copy;
    }

    /**
     * suffix/prefix properties
     * <p>
     * 
     * 
     */
    @JsonProperty("copy")
    public void setCopy(SuffixPrefix copy) {
        this.copy = copy;
    }

    /**
     * suffix/prefix properties
     * <p>
     * 
     * 
     */
    @JsonProperty("restore")
    public SuffixPrefix getRestore() {
        return restore;
    }

    /**
     * suffix/prefix properties
     * <p>
     * 
     * 
     */
    @JsonProperty("restore")
    public void setRestore(SuffixPrefix restore) {
        this.restore = restore;
    }

    /**
     * suffix/prefix properties
     * <p>
     * 
     * 
     */
    @JsonProperty("import")
    public SuffixPrefix getImport() {
        return _import;
    }

    /**
     * suffix/prefix properties
     * <p>
     * 
     * 
     */
    @JsonProperty("import")
    public void setImport(SuffixPrefix _import) {
        this._import = _import;
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

    @JsonProperty("welcomeDoNotRemindMe")
    public Boolean getWelcomeDoNotRemindMe() {
        return welcomeDoNotRemindMe;
    }

    @JsonProperty("welcomeDoNotRemindMe")
    public void setWelcomeDoNotRemindMe(Boolean welcomeDoNotRemindMe) {
        this.welcomeDoNotRemindMe = welcomeDoNotRemindMe;
    }

    @JsonProperty("welcomeGotIt")
    public Boolean getWelcomeGotIt() {
        return welcomeGotIt;
    }

    @JsonProperty("welcomeGotIt")
    public void setWelcomeGotIt(Boolean welcomeGotIt) {
        this.welcomeGotIt = welcomeGotIt;
    }

    @JsonProperty("clusterLicense")
    public Boolean getClusterLicense() {
        return clusterLicense;
    }

    @JsonProperty("clusterLicense")
    public void setClusterLicense(Boolean clusterLicense) {
        this.clusterLicense = clusterLicense;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("licenseValidFrom")
    public Date getLicenseValidFrom() {
        return licenseValidFrom;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("licenseValidFrom")
    public void setLicenseValidFrom(Date licenseValidFrom) {
        this.licenseValidFrom = licenseValidFrom;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("licenseValidUntil")
    public Date getLicenseValidUntil() {
        return licenseValidUntil;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("licenseValidUntil")
    public void setLicenseValidUntil(Date licenseValidUntil) {
        this.licenseValidUntil = licenseValidUntil;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("title", title).append("securityLevel", securityLevel).append("defaultProfileAccount", defaultProfileAccount).append("apiVersion", apiVersion).append("inventoryVersion", inventoryVersion).append("forceCommentsForAuditLog", forceCommentsForAuditLog).append("comments", comments).append("copy", copy).append("restore", restore).append("_import", _import).append("showViews", showViews).append("welcomeDoNotRemindMe", welcomeDoNotRemindMe).append("welcomeGotIt", welcomeGotIt).append("clusterLicense", clusterLicense).append("licenseValidFrom", licenseValidFrom).append("licenseValidUntil", licenseValidUntil).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(comments).append(restore).append(clusterLicense).append(welcomeDoNotRemindMe).append(title).append(licenseValidFrom).append(defaultProfileAccount).append(securityLevel).append(forceCommentsForAuditLog).append(apiVersion).append(_import).append(welcomeGotIt).append(inventoryVersion).append(showViews).append(licenseValidUntil).append(copy).append(deliveryDate).toHashCode();
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
        return new EqualsBuilder().append(comments, rhs.comments).append(restore, rhs.restore).append(clusterLicense, rhs.clusterLicense).append(welcomeDoNotRemindMe, rhs.welcomeDoNotRemindMe).append(title, rhs.title).append(licenseValidFrom, rhs.licenseValidFrom).append(defaultProfileAccount, rhs.defaultProfileAccount).append(securityLevel, rhs.securityLevel).append(forceCommentsForAuditLog, rhs.forceCommentsForAuditLog).append(apiVersion, rhs.apiVersion).append(_import, rhs._import).append(welcomeGotIt, rhs.welcomeGotIt).append(inventoryVersion, rhs.inventoryVersion).append(showViews, rhs.showViews).append(licenseValidUntil, rhs.licenseValidUntil).append(copy, rhs.copy).append(deliveryDate, rhs.deliveryDate).isEquals();
    }

}
