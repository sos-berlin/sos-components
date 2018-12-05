package com.sos.webservices.order.initiator;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderInitiatorSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorSettings.class); 
    private Path hibernateConfigurationFile;
    private String orderTemplatesDirectory;
    private int dayOffset;
    private String jocUrl;
    private boolean runOnStart=true;
    private int runInterval = 1440;
    private String firstRunAt = "00:00:00";
    
    
    public int getDayOffset() {
        return dayOffset;
    }

    public void setDayOffset(int dayOffset) {
        this.dayOffset = dayOffset;
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

    public void setDayOffset(String property) {
        this.dayOffset = Integer.parseInt(property);
    }

    
    public String getJocUrl() {
        return jocUrl;
    }

    
    public void setJocUrl(String jocUrl) {
        this.jocUrl = jocUrl;
    }

    
    public boolean isRunOnStart() {
        return runOnStart;
    }

    
    public void setRunOnStart(boolean runOnStart) {
        this.runOnStart = runOnStart;
    }

    
    public int getRunInterval() {
        return runInterval;
    }

    
    public void setRunInterval(int runInterval) {
        this.runInterval = runInterval;
    }

    public void setRunInterval(String runInterval) {
        try {
            this.runInterval = Integer.parseInt(runInterval);
        }catch (NumberFormatException e) {
            this.runInterval = 1440;
            LOGGER.warn("error during converting " + runInterval + " to int");
            LOGGER.warn("  ... using default 1440");

        }
    }

    
    public String getFirstRunAt() {
        return firstRunAt;
    }

    
    public void setFirstRunAt(String firstRunAt) {
        this.firstRunAt = firstRunAt;
    }

}
