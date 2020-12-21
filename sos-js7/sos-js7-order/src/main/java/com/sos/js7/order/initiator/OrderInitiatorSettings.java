package com.sos.js7.order.initiator;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public class OrderInitiatorSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorSettings.class);
   
    private String controllerId;
    private Path hibernateConfigurationFile;
    private String propertiesFile;
    private String orderTemplatesDirectory;
    private int dayAhead=0;
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

    public int getDayAhead() {
        return dayAhead;
    }

    public void setDayOffset(int dayOffset) {
        this.dayAhead = dayOffset;
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

    public void setDayAhead(String dayAhead) {
        this.dayAhead = Integer.parseInt(dayAhead);
    }
 
 

    
    public String getControllerId() {
        return controllerId;
    }

    
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }
  
}
