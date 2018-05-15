package com.sos.jobscheduler.event.master.handler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EventHandlerSettings {

    private Path hibernateConfiguration;
    private List<EventHandlerMasterSettings> masters;

    public EventHandlerSettings() {
        masters = new ArrayList<EventHandlerMasterSettings>();
    }

    public Path getHibernateConfiguration() {
        return hibernateConfiguration;
    }

    public void setHibernateConfiguration(Path val) {
        hibernateConfiguration = val;
    }

    public List<EventHandlerMasterSettings> getMasters() {
        return masters;
    }

    public void addMaster(EventHandlerMasterSettings master) {
        masters.add(master);
    }

}
