package com.sos.webservices.order.initiator;

import java.nio.file.Path;

public class OrderInitiatorSettings {

    private Path hibernateConfigurationFile;
    private String OrderTemplatesDirectory;
    private int dayOffset;

    public int getDayOffset() {
        return dayOffset;
    }

    public void setDayOffset(int dayOffset) {
        this.dayOffset = dayOffset;
    }

    public String getOrderTemplatesDirectory() {
        return OrderTemplatesDirectory;
    }

    public void setOrderTemplatesDirectory(String orderTemplatesDirectory) {
        OrderTemplatesDirectory = orderTemplatesDirectory;
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

}
