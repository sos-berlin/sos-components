package com.sos.jobscheduler.event.master;

import java.nio.file.Path;
import java.util.List;

import com.sos.jobscheduler.event.master.EventHandlerMasterSettings;

import java.util.ArrayList;

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
