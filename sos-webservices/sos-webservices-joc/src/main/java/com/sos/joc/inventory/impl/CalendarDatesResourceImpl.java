package com.sos.joc.inventory.impl;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.inventory.resource.ICalendarDatesResource;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.calendar.Dates;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class CalendarDatesResourceImpl extends JOCResourceImpl implements ICalendarDatesResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, CalendarDatesFilter.class);
            CalendarDatesFilter in = Globals.objectMapper.readValue(inBytes, CalendarDatesFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(read(in));
            }
            return response;

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Dates read(CalendarDatesFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {

            boolean calendarIdIsDefined = in.getId() != null;
            boolean calendarPathIsDefined = !SOSString.isEmpty(in.getPath());

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            if (calendarPathIsDefined || calendarIdIsDefined) {
                DBItemInventoryConfiguration calendarItem = null;
                if (calendarIdIsDefined) {
                    calendarItem = dbLayer.getConfiguration(in.getId());
                    if (calendarItem == null) {
                        throw new DBMissingDataException(String.format("Couldn't find the calendar with id '%1$d'", in.getId()));
                    }
                    in.setPath(calendarItem.getPath());
                } else {
                    java.nio.file.Path calendarPath = JocInventory.normalizePath(in.getPath());
                    String calendarName = calendarPath.getFileName().toString();
                    if (in.getPath().contains("/")) { // is path
                        calendarItem = dbLayer.getCalendar(calendarPath.toString().replace('\\', '/'));
                    } else { // is name
                        calendarItem = dbLayer.getCalendarByName(calendarName);
                    }
                    if (calendarItem == null) {
                        throw new DBMissingDataException(String.format("Couldn't find the calendar '%1$s'", calendarName));
                    }
                    in.setId(calendarItem.getId());
                }
                checkFolderPermissions(calendarItem.getPath());
                in.setCalendar(Globals.objectMapper.readValue(calendarItem.getContent(), Calendar.class));
            } else if (!SOSString.isEmpty(in.getCalendar().getPath())) {
                checkFolderPermissions(in.getCalendar().getPath());
            }

            if (in.getCalendar() == null) {
                throw new JocMissingRequiredParameterException("undefined 'calendar'");
            }

            return new FrequencyResolver().resolve(in);
        } finally {
            Globals.disconnect(session);
        }
    }

}
