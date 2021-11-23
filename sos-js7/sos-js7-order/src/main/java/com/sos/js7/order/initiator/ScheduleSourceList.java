package com.sos.js7.order.initiator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.schedule.Schedule;
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
    public List<Schedule> getSchedules() throws IOException, SOSHibernateException {
        SOSHibernateSession session = null;
        try {
            FilterSchedules filter = new FilterSchedules();
            filter.addControllerId(controllerId);

            session = Globals.createSosHibernateStatelessConnection("ScheduleSourceList");
            DBLayerSchedules dbLayer = new DBLayerSchedules(session);

            if (this.schedules != null) {
                filter.setListOfScheduleNames(this.schedules.stream().map(s -> Paths.get(s).getFileName().toString()).distinct().collect(Collectors
                        .toList()));
            }

            List<DBItemInventoryReleasedConfiguration> items = dbLayer.getSchedules(filter, 0);
            session.close();
            session = null;

            return items.stream().map(DBItemInventoryReleasedConfiguration::getSchedule).collect(Collectors.toList());
        } finally {
            Globals.disconnect(session);
        }
    }

    @Override
    public String getSource() {
        return "List";
    }

}
