package com.sos.joc.inventory.impl;

import java.io.IOException;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
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
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.calendar.Dates;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class CalendarDatesResourceImpl extends JOCResourceImpl implements ICalendarDatesResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, CalendarDatesFilter.class);
            CalendarDatesFilter in = Globals.objectMapper.readValue(inBytes, CalendarDatesFilter.class);

            JOCDefaultResponse response = checkPermissions(accessToken, in);
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
            if (!calendarIdIsDefined && !calendarPathIsDefined && in.getCalendar() == null) {
                throw new JocMissingRequiredParameterException("'id', 'calendar' or 'path' parameter is required");
            }
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            if (calendarPathIsDefined || calendarIdIsDefined) {
                DBItemInventoryConfiguration calendarItem = null;
                if (calendarPathIsDefined) {
                    calendarItem = dbLayer.getConfiguration(in.getId());
                    if (calendarItem == null) {
                        throw new DBMissingDataException(String.format("calendar with id '%1$d' not found", in.getId()));
                    }
                    in.setPath(calendarItem.getPath());
                } else {
                    String calendarPath = Globals.normalizePath(in.getPath());
                    calendarItem = dbLayer.getCalendar(calendarPath);
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
            
            return getCalendarDates(dbLayer, in);
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final CalendarDatesFilter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getInventory().getConfigurations().isView();
        return init(IMPL_PATH, in, accessToken, "", permission);
    }
    
    private static Dates getCalendarDates(InventoryDBLayer dbLayer, CalendarDatesFilter calendarFilter) throws SOSHibernateException,
            JsonParseException, JsonMappingException, IOException, SOSMissingDataException, SOSInvalidDataException {

        if (calendarFilter.getCalendar() == null) {
            throw new JocMissingRequiredParameterException("undefined 'calendar'");
        }

        Dates dates = null;
        FrequencyResolver fr = new FrequencyResolver();

        if (!SOSString.isEmpty(calendarFilter.getCalendar().getBasedOn())) {
            String calendarPath = Globals.normalizePath(calendarFilter.getCalendar().getBasedOn());

            DBItemInventoryConfiguration calendarItem = dbLayer.getCalendar(calendarPath);
            if (calendarItem == null) {
                throw new DBMissingDataException(String.format("calendar '%1$s' not found", calendarPath));
            }
            Calendar basedCalendar = Globals.objectMapper.readValue(calendarItem.getContent(), Calendar.class);
            if (SOSString.isEmpty(calendarFilter.getDateFrom())) {
                calendarFilter.setDateFrom(fr.getToday());
            }
            dates = fr.resolveRestrictions(basedCalendar, calendarFilter.getCalendar(), calendarFilter.getDateFrom(), calendarFilter.getDateTo());
        } else {
            dates = fr.resolve(calendarFilter);
        }

        return dates;
    }

}
