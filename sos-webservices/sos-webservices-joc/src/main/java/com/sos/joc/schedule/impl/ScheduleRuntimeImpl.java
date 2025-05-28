package com.sos.joc.schedule.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.dailyplan.common.DailyPlanRuntimeAndProjectionsHelper;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.RunTime;
import com.sos.joc.model.order.ScheduleDatesFilter;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.joc.schedule.resource.IScheduleRuntimeResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("schedule")
public class ScheduleRuntimeImpl extends JOCResourceImpl implements IScheduleRuntimeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleRuntimeImpl.class);
    private static final String API_CALL = "./schedule/runtime";

    @Override
    public JOCDefaultResponse postScheduleRuntime(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.DAILYPLAN);
            JsonValidator.validate(filterBytes, ScheduleDatesFilter.class);
            ScheduleDatesFilter in = Globals.objectMapper.readValue(filterBytes, ScheduleDatesFilter.class);
            JocPermissions perms = getBasicJocPermissions(accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, perms.getCalendars().getView() || perms.getDailyPlan().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            RunTime entity = new RunTime();
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            if (!SOSCollection.isEmpty(in.getCalendars())) {
                if (in.getTimeZone() == null || in.getTimeZone().isEmpty()) {
                    in.setTimeZone(SOSDate.TIMEZONE_UTC);
                }
                final ZoneId timezone = ZoneId.of(in.getTimeZone());

                FrequencyResolver fr = new FrequencyResolver();
                if (in.getDateFrom() == null) {
                    in.setDateFrom(fr.getToday());
                }
                if (in.getDateTo() == null) {
                    in.setDateTo(fr.getLastDayOfCurrentYear());
                }

                session = Globals.createSosHibernateStatelessConnection(API_CALL);
                InventoryDBLayer dbLayer = new InventoryDBLayer(session);
                final List<Calendar> nonWorkingDayCalendars = DailyPlanRuntimeAndProjectionsHelper.getNonWorkingDayCalendars(dbLayer, in
                        .getNonWorkingDayCalendars());
                List<DBItemInventoryReleasedConfiguration> workingDbCalendars = dbLayer.getReleasedCalendarsByNames(in.getCalendars().stream().map(
                        AssignedCalendars::getCalendarName).distinct().collect(Collectors.toList()));
                Globals.disconnect(session);
                session = null;

                SortedSet<Period> periods = new TreeSet<>(Comparator.comparing(p -> p.getSingleStart() == null ? p.getBegin() : p.getSingleStart()));

                if (workingDbCalendars != null && !workingDbCalendars.isEmpty()) {
                    Map<String, DBItemInventoryReleasedConfiguration> nameContentMap = workingDbCalendars.stream().collect(Collectors.toMap(
                            DBItemInventoryReleasedConfiguration::getName, Function.identity()));

                    for (AssignedCalendars c : in.getCalendars()) {
                        DBItemInventoryReleasedConfiguration item = nameContentMap.get(c.getCalendarName());
                        if (item == null) {
                            continue;
                        }
                        if (!folderIsPermitted(item.getFolder(), permittedFolders)) {
                            continue;
                        }
                        Calendar restrictions = new Calendar();
                        restrictions.setIncludes(c.getIncludes());
                        // restrictions.setExcludes(c.getExcludes());
                        Calendar basedOn = Globals.objectMapper.readValue(item.getContent(), Calendar.class);
                        Set<Period> singleDatesPeriods = ConcurrentHashMap.newKeySet();
                        SOSDate.getDatesInRange(in.getDateFrom(), in.getDateTo()).stream().parallel().forEach(asDailyPlanSingleDate -> {
                            try {
                                List<String> workingDates = new FrequencyResolver().resolveRestrictions(basedOn, restrictions, asDailyPlanSingleDate,
                                        asDailyPlanSingleDate).getDates();

                                List<String> nonWorkingDates = new ArrayList<>();
                                Set<String> workingDatesExtendedWithNonWorkingPrevNext = new HashSet<>();

                                DailyPlanRuntimeAndProjectionsHelper.applyAdjustmentForNonWorkingDates(c, nonWorkingDayCalendars,
                                        asDailyPlanSingleDate, workingDates, nonWorkingDates, workingDatesExtendedWithNonWorkingPrevNext);
                                singleDatesPeriods.addAll(workingDates.stream().flatMap(date -> DailyPlanRuntimeAndProjectionsHelper
                                        .getSingleDatePeriods(date, c.getPeriods(), nonWorkingDates, workingDatesExtendedWithNonWorkingPrevNext,
                                                timezone)).collect(Collectors.toSet()));
                            } catch (Throwable e) {
                                LOGGER.info("[" + API_CALL + "][" + asDailyPlanSingleDate + "]" + e, e);
                            }
                        });
                        periods.addAll(singleDatesPeriods);
                    }
                }
                entity.setPeriods(new ArrayList<>(periods));
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
