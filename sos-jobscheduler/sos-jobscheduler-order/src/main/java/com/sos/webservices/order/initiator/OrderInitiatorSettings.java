package com.sos.webservices.order.initiator;

import java.nio.file.Path;

public class OrderInitiatorSettings {
    private Path hibernateConfigurationFile;

    
    public Path getHibernateConfigurationFile() {
        return hibernateConfigurationFile;
    }

    
    public void setHibernateConfigurationFile(Path hibernateConfigurationFile) {
        this.hibernateConfigurationFile = hibernateConfigurationFile;
    }
    

}
