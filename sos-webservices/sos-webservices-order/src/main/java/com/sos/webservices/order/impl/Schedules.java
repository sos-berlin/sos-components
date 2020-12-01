package com.sos.webservices.order.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterSchedules;
import com.sos.webservices.order.initiator.model.Schedule;
import com.sos.webservices.order.initiator.model.ScheduleFilter;
import com.sos.webservices.order.initiator.model.SchedulesList;
import com.sos.webservices.order.resource.ISchedulesResource;

@Path("schedules")
public class Schedules extends JOCResourceImpl implements ISchedulesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(Schedules.class);
    private static final String API_CALL = "./schedules/list";

    @Override
    public JOCDefaultResponse postSchedules(String xAccessToken, ScheduleFilter scheduleFilter) {
        SOSHibernateSession sosHibernateSession = null;
        LOGGER.debug("reading list of order templates");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, scheduleFilter, xAccessToken, scheduleFilter.getControllerId(),
                    getPermissonsJocCockpit(scheduleFilter.getControllerId(), xAccessToken).getWorkflow().getExecute().isAddOrder());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            SchedulesList schedulesList = new SchedulesList();
            schedulesList.setSchedules(new ArrayList<Schedule>());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);
            FilterSchedules filterSchedules = new FilterSchedules();
            filterSchedules.setControllerId(scheduleFilter.getControllerId());
            filterSchedules.setPath(scheduleFilter.getSchedulePath());

            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<DBItemInventoryConfiguration> listOfSchedules = dbLayerSchedules.getSchedules(filterSchedules, 0);
            for (DBItemInventoryConfiguration dbItemInventoryConfiguration : listOfSchedules) {
                if (dbItemInventoryConfiguration.getContent() != null) {
                    Schedule schedule = objectMapper.readValue(dbItemInventoryConfiguration.getContent(), Schedule.class);
                    schedule.setPath(dbItemInventoryConfiguration.getPath());
                    schedulesList.getSchedules().add(schedule);
                }
            }

            return JOCDefaultResponse.responseStatus200(schedulesList);

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

}
