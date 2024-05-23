
package com.sos.joc.model.security.configuration.permissions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.configuration.permissions.joc.Administration;
import com.sos.joc.model.security.configuration.permissions.joc.AuditLog;
import com.sos.joc.model.security.configuration.permissions.joc.Calendars;
import com.sos.joc.model.security.configuration.permissions.joc.Cluster;
import com.sos.joc.model.security.configuration.permissions.joc.DailyPlan;
import com.sos.joc.model.security.configuration.permissions.joc.Documentations;
import com.sos.joc.model.security.configuration.permissions.joc.Encipherment;
import com.sos.joc.model.security.configuration.permissions.joc.FileTransfer;
import com.sos.joc.model.security.configuration.permissions.joc.Inventory;
import com.sos.joc.model.security.configuration.permissions.joc.Notification;
import com.sos.joc.model.security.configuration.permissions.joc.Others;
import com.sos.joc.model.security.configuration.permissions.joc.Reports;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "getLog",
    "administration",
    "cluster",
    "inventory",
    "calendars",
    "documentations",
    "auditLog",
    "dailyPlan",
    "fileTransfer",
    "notification",
    "encipherment",
    "reports",
    "others"
})
public class JocPermissions {

    @JsonProperty("getLog")
    private Boolean getLog = false;
    @JsonProperty("administration")
    private Administration administration;
    @JsonProperty("cluster")
    private Cluster cluster;
    @JsonProperty("inventory")
    private Inventory inventory;
    @JsonProperty("calendars")
    private Calendars calendars;
    @JsonProperty("documentations")
    private Documentations documentations;
    @JsonProperty("auditLog")
    private AuditLog auditLog;
    @JsonProperty("dailyPlan")
    private DailyPlan dailyPlan;
    @JsonProperty("fileTransfer")
    private FileTransfer fileTransfer;
    @JsonProperty("notification")
    private Notification notification;
    @JsonProperty("encipherment")
    private Encipherment encipherment;
    @JsonProperty("reports")
    private Reports reports;
    @JsonProperty("others")
    private Others others;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JocPermissions() {
    }

    /**
     * 
     * @param encipherment
     * @param cluster
     * @param reports
     * @param auditLog
     * @param fileTransfer
     * @param getLog
     * @param administration
     * @param documentations
     * @param inventory
     * @param notification
     * @param dailyPlan
     * @param calendars
     * @param others
     */
    public JocPermissions(Boolean getLog, Administration administration, Cluster cluster, Inventory inventory, Calendars calendars, Documentations documentations, AuditLog auditLog, DailyPlan dailyPlan, FileTransfer fileTransfer, Notification notification, Encipherment encipherment, Reports reports, Others others) {
        super();
        this.getLog = getLog;
        this.administration = administration;
        this.cluster = cluster;
        this.inventory = inventory;
        this.calendars = calendars;
        this.documentations = documentations;
        this.auditLog = auditLog;
        this.dailyPlan = dailyPlan;
        this.fileTransfer = fileTransfer;
        this.notification = notification;
        this.encipherment = encipherment;
        this.reports = reports;
        this.others = others;
    }

    @JsonProperty("getLog")
    public Boolean getGetLog() {
        return getLog;
    }

    @JsonProperty("getLog")
    public void setGetLog(Boolean getLog) {
        this.getLog = getLog;
    }

    @JsonProperty("administration")
    public Administration getAdministration() {
        return administration;
    }

    @JsonProperty("administration")
    public void setAdministration(Administration administration) {
        this.administration = administration;
    }

    @JsonProperty("cluster")
    public Cluster getCluster() {
        return cluster;
    }

    @JsonProperty("cluster")
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    @JsonProperty("inventory")
    public Inventory getInventory() {
        return inventory;
    }

    @JsonProperty("inventory")
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @JsonProperty("calendars")
    public Calendars getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    public void setCalendars(Calendars calendars) {
        this.calendars = calendars;
    }

    @JsonProperty("documentations")
    public Documentations getDocumentations() {
        return documentations;
    }

    @JsonProperty("documentations")
    public void setDocumentations(Documentations documentations) {
        this.documentations = documentations;
    }

    @JsonProperty("auditLog")
    public AuditLog getAuditLog() {
        return auditLog;
    }

    @JsonProperty("auditLog")
    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    @JsonProperty("dailyPlan")
    public DailyPlan getDailyPlan() {
        return dailyPlan;
    }

    @JsonProperty("dailyPlan")
    public void setDailyPlan(DailyPlan dailyPlan) {
        this.dailyPlan = dailyPlan;
    }

    @JsonProperty("fileTransfer")
    public FileTransfer getFileTransfer() {
        return fileTransfer;
    }

    @JsonProperty("fileTransfer")
    public void setFileTransfer(FileTransfer fileTransfer) {
        this.fileTransfer = fileTransfer;
    }

    @JsonProperty("notification")
    public Notification getNotification() {
        return notification;
    }

    @JsonProperty("notification")
    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    @JsonProperty("encipherment")
    public Encipherment getEncipherment() {
        return encipherment;
    }

    @JsonProperty("encipherment")
    public void setEncipherment(Encipherment encipherment) {
        this.encipherment = encipherment;
    }

    @JsonProperty("reports")
    public Reports getReports() {
        return reports;
    }

    @JsonProperty("reports")
    public void setReports(Reports reports) {
        this.reports = reports;
    }

    @JsonProperty("others")
    public Others getOthers() {
        return others;
    }

    @JsonProperty("others")
    public void setOthers(Others others) {
        this.others = others;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("getLog", getLog).append("administration", administration).append("cluster", cluster).append("inventory", inventory).append("calendars", calendars).append("documentations", documentations).append("auditLog", auditLog).append("dailyPlan", dailyPlan).append("fileTransfer", fileTransfer).append("notification", notification).append("encipherment", encipherment).append("reports", reports).append("others", others).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(encipherment).append(cluster).append(reports).append(auditLog).append(fileTransfer).append(getLog).append(administration).append(documentations).append(inventory).append(notification).append(dailyPlan).append(calendars).append(others).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JocPermissions) == false) {
            return false;
        }
        JocPermissions rhs = ((JocPermissions) other);
        return new EqualsBuilder().append(encipherment, rhs.encipherment).append(cluster, rhs.cluster).append(reports, rhs.reports).append(auditLog, rhs.auditLog).append(fileTransfer, rhs.fileTransfer).append(getLog, rhs.getLog).append(administration, rhs.administration).append(documentations, rhs.documentations).append(inventory, rhs.inventory).append(notification, rhs.notification).append(dailyPlan, rhs.dailyPlan).append(calendars, rhs.calendars).append(others, rhs.others).isEquals();
    }

}
