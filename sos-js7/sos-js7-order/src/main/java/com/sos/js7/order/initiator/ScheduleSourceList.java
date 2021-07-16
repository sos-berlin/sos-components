package com.sos.js7.order.initiator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterSchedules;

public class ScheduleSourceList extends ScheduleSource {

    private String controllerId;
    private List<String> schedules;

    public ScheduleSourceList(String controllerId, List<String> schedules) {
        super();
        this.controllerId = controllerId;
        this.schedules = schedules;
    }

    @Override
    public List<Schedule> fillListOfSchedules() throws IOException, SOSHibernateException {
        List<Schedule> listOfSchedules = new ArrayList<>();
        FilterSchedules filterSchedules = new FilterSchedules();
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("ScheduleSourceDB");
            DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);

            filterSchedules.addControllerId(controllerId);
            if (this.schedules != null) {
                filterSchedules.setListOfScheduleNames(this.schedules.stream().map(s -> Paths.get(s).getFileName().toString()).distinct().collect(
                        Collectors.toList()));
            }
            
            List<DBItemInventoryReleasedConfiguration> listOfSchedulesDbItems = dbLayerSchedules.getSchedules(filterSchedules, 0);
            listOfSchedules = listOfSchedulesDbItems.stream().map(DBItemInventoryReleasedConfiguration::getSchedule).collect(Collectors.toList());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

        return listOfSchedules;
    }

    @Override
    public String fromSource() {
        return "List";
    }

}
