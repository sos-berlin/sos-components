package com.sos.js7.order.initiator;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderInitiatorSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorSettings.class);

    private String controllerId;
    private Path hibernateConfigurationFile;
    private String propertiesFile;
    private String orderTemplatesDirectory;
    private int dayAheadPlan = 0;
    private int dayAheadSubmit = 0;
    private String userAccount = "JS7";
    private String timeZone = "UTC";
    private String periodBegin = "00:00";
    private boolean overwrite = false;
    private boolean submit = true;

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

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
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

}
