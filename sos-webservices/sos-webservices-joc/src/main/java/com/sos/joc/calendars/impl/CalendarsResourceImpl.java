package com.sos.joc.calendars.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.calendars.resource.ICalendarsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.filters.FilterAfterResponse;
import com.sos.joc.db.calendars.CalendarUsageDBLayer;
import com.sos.joc.db.calendars.CalendarsDBLayer;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.inventory.deprecated.calendar.DBItemCalendar;
import com.sos.joc.db.inventory.deprecated.calendar.DBItemCalendarUsage;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarType;
import com.sos.joc.model.calendar.Calendars;
import com.sos.joc.model.calendar.CalendarsFilter;
import com.sos.joc.model.calendar.UsedBy;
import com.sos.joc.model.common.Folder;

@Path("calendars")
public class CalendarsResourceImpl extends JOCResourceImpl implements ICalendarsResource {

    private static final String API_CALL = "./calendars";

    @Override
    public JOCDefaultResponse postCalendars(String accessToken, CalendarsFilter calendarsFilter) throws Exception {
        return postCalendars(accessToken, calendarsFilter, false);
    }

    @Override
    public JOCDefaultResponse postUsedBy(String accessToken, CalendarsFilter calendarsFilter) throws Exception {
        return postCalendars(accessToken, calendarsFilter, true);
    }

    public JOCDefaultResponse postCalendars(String accessToken, CalendarsFilter calendarsFilter, boolean withUsedBy) throws Exception {
        SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, calendarsFilter, accessToken, calendarsFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(calendarsFilter.getJobschedulerId(), accessToken).getCalendar().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            CalendarsDBLayer dbLayer = new CalendarsDBLayer(connection);
            CalendarUsageDBLayer dbCalendarLayer = new CalendarUsageDBLayer(connection);
            List<DBItemCalendar> dbCalendars = null;

            boolean withFolderFilter = calendarsFilter.getFolders() != null && !calendarsFilter.getFolders().isEmpty();
            boolean hasPermission = true;
            List<Folder> folders = addPermittedFolder(calendarsFilter.getFolders());

            if (calendarsFilter.getCalendars() != null && !calendarsFilter.getCalendars().isEmpty()) {
                calendarsFilter.setRegex(null);
                dbCalendars = dbLayer.getCalendarsFromPaths(dbItemInventoryInstance.getSchedulerId(), new HashSet<String>(calendarsFilter.getCalendars()));

            } else if (calendarsFilter.getCalendarIds() != null && !calendarsFilter.getCalendarIds().isEmpty()) {
                calendarsFilter.setRegex(null);
                dbCalendars = dbLayer.getCalendarsFromIds(new HashSet<Long>(calendarsFilter.getCalendarIds()));

            } else {
                if (calendarsFilter.getType() != null && !calendarsFilter.getType().isEmpty()) {
                    try {
                        CalendarType.fromValue(calendarsFilter.getType().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new JobSchedulerBadRequestException(String.format("Invalid value '%1$s' in 'type' parameter", calendarsFilter
                                .getType()));
                    }
                }
                Set<String> categories = null;
                if (calendarsFilter.getCategories() != null) {
                    categories = new HashSet<String>(calendarsFilter.getCategories());
                }

                Set<String> allFolders = new HashSet<String>();
                Set<String> recursiveFolders = new HashSet<String>();

                if (withFolderFilter && (folders == null || folders.isEmpty())) {
                    hasPermission = false;
                } else if (folders != null && !folders.isEmpty()) {
                    for (Folder folder : folders) {
                        allFolders.add(folder.getFolder());
                        if (folder.getRecursive()) {
                            recursiveFolders.add(folder.getFolder());
                        }
                    }
                }
                if (hasPermission) {
                    dbCalendars = dbLayer.getCalendars(dbItemInventoryInstance.getSchedulerId(), calendarsFilter.getType(), categories, allFolders,
                            recursiveFolders);
                }
            }

            List<Calendar> calendarList = new ArrayList<Calendar>();
            if (dbCalendars != null) {
                DocumentationDBLayer dbDocLayer = new DocumentationDBLayer(connection);
                Map<String,String> documentations = dbDocLayer.getDocumentationPathsOfCalendar(calendarsFilter.getJobschedulerId());
                ObjectMapper om = new ObjectMapper();
                boolean compact = calendarsFilter.getCompact() != null && calendarsFilter.getCompact();
                if (withUsedBy) {
                    compact = false;
                }
                for (DBItemCalendar dbCalendar : dbCalendars) {
                    if (FilterAfterResponse.matchRegex(calendarsFilter.getRegex(), dbCalendar.getName())) {
                        Calendar calendar = om.readValue(dbCalendar.getConfiguration(), Calendar.class);
                        calendar.setId(dbCalendar.getId());
                        calendar.setPath(dbCalendar.getName());
                        calendar.setName(dbCalendar.getBaseName());
                        calendar.setDocumentation(documentations.get(dbCalendar.getName()));
                        if (compact) {
                            calendar.setIncludes(null);
                            calendar.setExcludes(null);
                        }
                        if (withUsedBy) {
                            calendar.setUsedBy(getUsedBy(dbCalendarLayer.getCalendarUsages(dbCalendar.getId())));
                        }
                        calendarList.add(calendar);
                    }
                }
            }
            Calendars entity = new Calendars();
            entity.setCalendars(calendarList);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    private UsedBy getUsedBy(List<DBItemCalendarUsage> calendarUsages) {
        SortedSet<String> orders = new TreeSet<String>();
        SortedSet<String> jobs = new TreeSet<String>();
        SortedSet<String> schedules = new TreeSet<String>();
        boolean usedByExist = false;
        if (calendarUsages != null) {
            for (DBItemCalendarUsage item : calendarUsages) {
                if (item.getObjectType() == null) {
                    continue;
                }
                usedByExist = true;
                switch (item.getObjectType().toUpperCase()) {
                case "ORDER":
                    orders.add(item.getPath());
                    break;
                case "JOB":
                    jobs.add(item.getPath());
                    break;
                case "SCHEDULE":
                    schedules.add(item.getPath());
                    break;
                }
            }
        }
        if (!usedByExist) {
            return null;
        }
        UsedBy entity = new UsedBy();
        if (!orders.isEmpty()) {
            entity.setOrders(new ArrayList<String>(orders));
        }
        if (!jobs.isEmpty()) {
            entity.setJobs(new ArrayList<String>(jobs));
        }
        if (!schedules.isEmpty()) {
            entity.setSchedules(new ArrayList<String>(schedules));
        }
        return entity;
    }

}