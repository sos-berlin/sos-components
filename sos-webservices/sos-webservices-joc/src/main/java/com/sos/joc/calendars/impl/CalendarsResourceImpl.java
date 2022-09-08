package com.sos.joc.calendars.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.joc.Globals;
import com.sos.joc.calendars.resource.ICalendarsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.calendar.Calendars;
import com.sos.joc.model.calendar.CalendarsFilter;
import com.sos.joc.model.common.Folder;
import com.sos.schema.JsonValidator;

@Path("calendars")
public class CalendarsResourceImpl extends JOCResourceImpl implements ICalendarsResource {

    private static final String API_CALL = "./calendars";
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarsResourceImpl.class);

    @Override
    public JOCDefaultResponse postCalendars(String accessToken, byte[] calendarsFilter) {
        return postCalendars(accessToken, calendarsFilter, false);
    }

    // @Override
    // public JOCDefaultResponse postUsedBy(String accessToken, byte[] calendarsFilter) {
    // return postCalendars(accessToken, calendarsFilter, true);
    // }

    public JOCDefaultResponse postCalendars(String accessToken, byte[] filterBytes, boolean withUsedBy) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, CalendarsFilter.class);
            CalendarsFilter calendarsFilter = Globals.objectMapper.readValue(filterBytes, CalendarsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getCalendars().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            List<DBItemInventoryReleasedConfiguration> dbCalendars = null;

            boolean withFolderFilter = calendarsFilter.getFolders() != null && !calendarsFilter.getFolders().isEmpty();
            final Set<Folder> folders = folderPermissions.getPermittedFolders(calendarsFilter.getFolders());

            if (calendarsFilter.getCalendarIds() != null && !calendarsFilter.getCalendarIds().isEmpty()) {
                calendarsFilter.setRegex(null);
                dbCalendars = dbLayer.getReleasedConfigurations(calendarsFilter.getCalendarIds());

            } else if (calendarsFilter.getCalendarPaths() != null && !calendarsFilter.getCalendarPaths().isEmpty()) {
                calendarsFilter.setRegex(null);
                dbCalendars = dbLayer.getReleasedCalendarsByNames(calendarsFilter.getCalendarPaths().stream().map(p -> JocInventory.pathToName(p))
                        .distinct().collect(Collectors.toList()));

            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                // no folder permission
            } else {
                Collection<Integer> types = Arrays.asList(CalendarType.WORKINGDAYSCALENDAR.intValue(), CalendarType.NONWORKINGDAYSCALENDAR
                        .intValue());
                if (calendarsFilter.getType() != null) {
                    types = Arrays.asList(calendarsFilter.getType().intValue());
                }
                dbCalendars = dbLayer.getConfigurationsByType(types);
            }

            Calendars entity = new Calendars();

            if (dbCalendars != null && !dbCalendars.isEmpty()) {
                JocError jocError = getJocError();
                Stream<DBItemInventoryReleasedConfiguration> stream = dbCalendars.stream().filter(item -> folderIsPermitted(item.getFolder(), folders));

                if (calendarsFilter.getRegex() != null && !calendarsFilter.getRegex().isEmpty()) {
                    Predicate<String> regex = Pattern.compile(calendarsFilter.getRegex().replaceAll("%", ".*"), Pattern.CASE_INSENSITIVE)
                            .asPredicate();
                    stream = stream.filter(item -> regex.test(item.getPath()));
                }
                
                entity.setCalendars(stream.map(item -> {
                    try {
                        Calendar cal = Globals.objectMapper.readValue(item.getContent(), Calendar.class);
                        cal.setId(item.getId());
                        cal.setPath(item.getPath());
                        cal.setName(item.getName());
                        cal.setTitle(item.getTitle());
                        cal.setType(CalendarType.fromValue(item.getType()));
                        if (calendarsFilter.getCompact() == Boolean.TRUE) {
                            cal.setIncludes(null);
                            cal.setExcludes(null);
                        }
                        return cal;
                    } catch (Exception e) {
                        if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                            LOGGER.info(jocError.printMetaInfo());
                            jocError.clearMetaInfo();
                        }
                        LOGGER.error(String.format("[%s] %s", item.getPath(), e.toString()));
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }

            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

}