package com.sos.joc.calendar.impl;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.joc.Globals;
import com.sos.joc.calendar.resource.ICalendarDatesResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.calendar.Dates;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;

@Path("calendar")
public class CalendarDatesResourceImpl extends JOCResourceImpl implements ICalendarDatesResource {

    private static final String API_CALL = "./calendar/dates";

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(API_CALL, inBytes, accessToken);
            JsonValidator.validate(inBytes, CalendarDatesFilter.class);
            CalendarDatesFilter in = Globals.objectMapper.readValue(inBytes, CalendarDatesFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getCalendar().getView().isStatus());
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

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            if (calendarPathIsDefined || calendarIdIsDefined) {
                DBItemInventoryReleasedConfiguration calendarItem = null;
                if (calendarIdIsDefined) {
                    calendarItem = dbLayer.getReleasedConfiguration(in.getId());
                    if (calendarItem == null) {
                        throw new DBMissingDataException(String.format("calendar with id '%1$d' not found", in.getId()));
                    }
                    in.setPath(calendarItem.getPath());
                } else {
                    String calendarPath = Globals.normalizePath(in.getPath());
                    calendarItem = dbLayer.getReleasedConfiguration(calendarPath, ConfigurationType.WORKINGDAYSCALENDAR.intValue());
                    if (calendarItem == null) {
                        throw new DBMissingDataException(String.format("calendar '%1$s' not found", calendarPath));
                    }
                    in.setId(calendarItem.getId());
                }
                checkFolderPermissions(in.getPath());
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
