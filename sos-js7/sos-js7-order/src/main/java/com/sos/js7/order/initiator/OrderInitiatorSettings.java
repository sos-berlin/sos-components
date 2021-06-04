package com.sos.js7.order.initiator;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;

public class OrderInitiatorSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorSettings.class);

    private Path hibernateConfigurationFile;
    private String propertiesFile;
    private String orderTemplatesDirectory;
    private int dayAheadPlan = 0;
    private int dayAheadSubmit = 0;
    private String userAccount = "JS7";
    private String timeZone = "UTC";
    private String periodBegin = "00:00";
    private String dailyPlanStartTime;
    private boolean overwrite = false;
    private boolean submit = true;
    private StartupMode startMode;
    private Long auditLogId=0L;

    public boolean isSubmit() {
        return submit;
    }

    public void setSubmit(boolean submit) {
        this.submit = submit;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getPeriodBegin() {
        return periodBegin;
    }

    public void setPeriodBegin(String periodBegin) {
        this.periodBegin = periodBegin;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public String getOrderTemplatesDirectory() {
        return orderTemplatesDirectory;
    }

    public void setOrderTemplatesDirectory(String orderTemplatesDirectory) {
        this.orderTemplatesDirectory = orderTemplatesDirectory;
    }

    public Path getHibernateConfigurationFile() {
        return hibernateConfigurationFile;
    }

    public void setHibernateConfigurationFile(Path hibernateConfigurationFile) {
        this.hibernateConfigurationFile = hibernateConfigurationFile;
    }

 
    public int getDayAheadPlan() {
        return dayAheadPlan;
    }

    public void setDayAheadPlan(int dayAheadPlan) {
        this.dayAheadPlan = dayAheadPlan;
    }

    public int getDayAheadSubmit() {
        return dayAheadSubmit;
    }

    public void setDayAheadSubmit(int dayAheadSubmit) {
        this.dayAheadSubmit = dayAheadSubmit;
    }

    public void setDayAheadSubmit(String dayAheadSubmit) {
        try {
            this.dayAheadSubmit = Integer.parseInt(dayAheadSubmit);
        } catch (NumberFormatException e) {
            this.dayAheadSubmit = 0;
            LOGGER.warn("Could not set setting for dayAheadSubmit: " + dayAheadSubmit);
        }
    }

    public void setDayAheadPlan(String dayAheadPlan) {
        try {
            this.dayAheadPlan = Integer.parseInt(dayAheadPlan);
        } catch (NumberFormatException e) {
            this.dayAheadSubmit = 0;
            LOGGER.warn("Could not set setting for dayAheadPlan: " + dayAheadPlan);
        }
    }
 
    
    public StartupMode getStartMode() {
        return startMode;
    }

    
    public void setStartMode(StartupMode startMode) {
        this.startMode = startMode;
    }

    
    public String getDailyPlanStartTime() {
        return dailyPlanStartTime;
    }

    
    public void setDailyPlanStartTime(String dailyPlanStartTime) {
        this.dailyPlanStartTime = dailyPlanStartTime;
    }

    
    public Long getAuditLogId() {
        return auditLogId;
    }

    
    public void setAuditLogId(Long auditLogId) {
        this.auditLogId = auditLogId;
    }

}
