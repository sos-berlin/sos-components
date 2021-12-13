package com.sos.joc.dailyplan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.schedule.VariableSet;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.OrderCounter;
import com.sos.joc.dailyplan.common.PeriodResolver;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.common.PlannedOrderKey;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanSubmissions;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.DBLayerOrderVariables;
import com.sos.joc.dailyplan.db.DBLayerReleasedConfigurations;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
import com.sos.joc.dailyplan.db.FilterDailyPlanSubmissions;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;

class CalendarCacheItem {

    Calendar calendar;

}

public class DailyPlanRunner extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanRunner.class);
    private static final String IDENTIFIER = DailyPlanRunner.class.getSimpleName();
    private static final String UTC = "UTC";

    private AtomicLong lastActivityStart = new AtomicLong(0);
    private AtomicLong lastActivityEnd = new AtomicLong(0);
    private DailyPlanSettings settings;
    private java.util.Calendar startCalendar;
    private Map<String, String> nonWorkingDays;
    private Set<String> createdPlans;

    private boolean firstStart = true;

    public DailyPlanRunner(DailyPlanSettings settings) {
        this.settings = settings;
    }

    // service
    public void run() {
        if (createdPlans == null) {
            createdPlans = new HashSet<String>();
        }
        AJocClusterService.setLogger("dailyplan");
        boolean manuelStart = false;
        if (firstStart && StartupMode.manual_restart.equals(settings.getStartMode())) {
            firstStart = false;
            LOGGER.debug("manuelStart: true");
            manuelStart = true;
        }

        java.util.Calendar now = java.util.Calendar.getInstance(TimeZone.getTimeZone(settings.getTimeZone()));
        if (startCalendar == null) {
            // TODO duplicate calculation, see com.sos.joc.dailyplan.DailyPlanService.start() -> DailyPlanHelper.getStartTimeAsString
            if (!"".equals(settings.getDailyPlanStartTime())) {
                startCalendar = DailyPlanHelper.getCalendar(settings.getDailyPlanStartTime(), settings.getTimeZone());
            } else {
                startCalendar = DailyPlanHelper.getCalendar(settings.getPeriodBegin(), settings.getTimeZone());
                startCalendar.add(java.util.Calendar.DATE, 1);
                startCalendar.add(java.util.Calendar.MINUTE, -30);
            }
            if (startCalendar.before(now)) {
                startCalendar.add(java.util.Calendar.DATE, 1);
            }
        }

        java.util.Calendar calendar = DailyPlanHelper.getCalendar(settings.getPeriodBegin(), settings.getTimeZone());
        calendar.add(java.util.Calendar.DATE, 1);
        String date;
        try {
            date = SOSDate.getDateAsString(calendar);
        } catch (SOSInvalidDataException e) {
            LOGGER.error(e.toString(), e);
            return;
        }

        // TODO createdPlans static? because several instances ...
        if (!createdPlans.contains(date) && (manuelStart || (now.getTimeInMillis() - startCalendar.getTimeInMillis()) > 0)) {
            startCalendar = null;
            createdPlans.add(date);
            try {
                settings.setSubmissionTime(new Date());
                createPlan(settings.getStartMode(), settings.getControllers(), DailyPlanHelper.getNextDayCalendar());
            } catch (ControllerConnectionResetException | ControllerConnectionRefusedException | ParseException | SOSException | URISyntaxException
                    | InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            if (LOGGER.isTraceEnabled() && startCalendar != null) {
                try {
                    LOGGER.trace(String.format("wait for start at %s (%s)...", SOSDate.format(startCalendar.getTime(), settings.getTimeZone()),
                            settings.getTimeZone()));
                } catch (SOSInvalidDataException e) {

                }
            }
        }
        // AJocClusterService.clearAllLoggers();
    }

    /* service */
    private void createPlan(StartupMode startupMode, List<ControllerConfiguration> controllers, java.util.Calendar calendar)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {

        try {
            lastActivityStart.set(new Date().getTime());

            LOGGER.info(String.format("[%s][createPlan]creating from %s for %s days ahead, submitting for %s days ahead", startupMode, SOSDate
                    .getDateAsString(calendar), settings.getDayAheadPlan(), settings.getDayAheadSubmit()));

            Collection<Schedule> schedules = getAllSchedules();
            if (schedules.size() == 0) {
                LOGGER.info(String.format("[%s][createPlan][skip]found 0 schedules", startupMode));
                return;
            } else {
                LOGGER.info(String.format("[%s][createPlan]found %s schedules", startupMode, schedules.size()));
            }
            java.util.Calendar savCalendar = java.util.Calendar.getInstance();
            savCalendar.setTime(calendar.getTime());
            for (ControllerConfiguration conf : controllers) {
                String controllerId = conf.getCurrent().getId();

                java.util.Calendar dailyPlanCalendar = java.util.Calendar.getInstance();
                dailyPlanCalendar.setTime(savCalendar.getTime());
                settings.setDailyPlanDate(dailyPlanCalendar.getTime());

                for (int day = 0; day < settings.getDayAheadPlan(); day++) {
                    String date = SOSDate.getDateWithTimeZoneAsString(dailyPlanCalendar.getTime(), settings.getTimeZone());
                    List<DBItemDailyPlanSubmission> l = getSubmissionsForDate(dailyPlanCalendar, controllerId);
                    if ((l.size() == 0)) {
                        generateDailyPlan(startupMode, controllerId, schedules, date, false, null, "");
                    } else {
                        List<String> copy = l.stream().map(e -> {
                            String d;
                            try {
                                d = SOSDate.getDateTimeAsString(e.getSubmissionForDate());
                            } catch (SOSInvalidDataException e1) {
                                d = null;
                            }
                            return d == null ? "" : d;
                        }).collect(Collectors.toList());
                        LOGGER.info(String.format("[%s][creating][%s][%s][skip][submission(s) found]%s", startupMode, controllerId, date, String.join(
                                ",", copy)));
                    }

                    dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                    settings.setDailyPlanDate(dailyPlanCalendar.getTime());
                }

                dailyPlanCalendar.setTime(savCalendar.getTime());
                settings.setDailyPlanDate(dailyPlanCalendar.getTime());

                for (int day = 0; day < settings.getDayAheadSubmit(); day++) {
                    submitDaysAhead(startupMode, controllerId, dailyPlanCalendar);
                    dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                    settings.setDailyPlanDate(dailyPlanCalendar.getTime());
                }

            }
        } catch (SOSHibernateException | IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException
                | JocConfigurationException | DBOpenSessionException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            lastActivityEnd.set(new Date().getTime());
        }
    }

    /* service (createPlan) & DailyPlanModifyOrderImpl, DailyPlanOrdersGenerateImpl **/
    public Map<PlannedOrderKey, PlannedOrder> generateDailyPlan(StartupMode startupMode, String controllerId, Collection<Schedule> schedules,
            String date, Boolean withSubmit, JocError jocError, String accessToken) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            IOException, ParseException, SOSException, URISyntaxException, ControllerConnectionResetException, ControllerConnectionRefusedException,
            InterruptedException, ExecutionException, TimeoutException {

        String operation = withSubmit ? "creating/submitting" : "creating";
        if (schedules == null || schedules.size() == 0) {
            LOGGER.info(String.format("[%s][%s][%s][%s][skip]found 0 schedules", startupMode, operation, controllerId, date));
            // TODO initialize OrderListSynchronizer before calculateStartTimes and return synchronizer.getPlannedOrders()
            return new TreeMap<PlannedOrderKey, PlannedOrder>();
        }

        OrderListSynchronizer synchronizer = calculateStartTimes(startupMode, controllerId, schedules, date);
        synchronizer.setJocError(jocError);
        synchronizer.setAccessToken(accessToken);
        OrderCounter c = DailyPlanHelper.getOrderCount(synchronizer.getPlannedOrders());

        if (synchronizer.getPlannedOrders().size() > 0) {
            String submissionId = "unknown";
            String submissionDate = "unknown";
            if (synchronizer.getSubmission() != null) {
                submissionId = synchronizer.getSubmission().getId().toString();
                submissionDate = SOSDate.getDateTimeAsString(synchronizer.getSubmission().getSubmissionForDate());
            }

            LOGGER.info(String.format("[%s][%s][%s][%s][calculated][%s][submission date=%s, id=%s]", startupMode, operation, controllerId, date, c,
                    submissionDate, submissionId));

            synchronizer.addPlannedOrderToControllerAndDB(startupMode, operation, controllerId, date, withSubmit);
            EventBus.getInstance().post(new DailyPlanEvent(date));
        } else {
            LOGGER.info(String.format("[%s][%s][%s][%s][skip]%s", startupMode, operation, controllerId, date, c));
        }
        return synchronizer.getPlannedOrders();
    }

    /* service (submitDaysAhead) & DailyPlanModifyOrderImpl, DailyPlanSubmitOrdersImpl **/
    public void submitOrders(StartupMode startupMode, String controllerId, List<DBItemDailyPlanOrder> items, String submissionForDate,
            JocError jocError, String accessToken) throws JsonParseException, JsonMappingException, DBConnectionRefusedException,
            DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, IOException, ParseException,
            SOSException, URISyntaxException, ControllerConnectionResetException, ControllerConnectionRefusedException, InterruptedException,
            ExecutionException, TimeoutException {

        SOSHibernateSession session = null;
        try {
            OrderListSynchronizer synchronizer = new OrderListSynchronizer(settings);
            synchronizer.setAccessToken(accessToken);
            synchronizer.setJocError(jocError);

            String sessionIdentifier = "submitOrders";
            if (!SOSString.isEmpty(submissionForDate)) {
                sessionIdentifier += "-" + submissionForDate;
            }
            session = Globals.createSosHibernateStatelessConnection(sessionIdentifier);
            DBLayerOrderVariables dbLayer = new DBLayerOrderVariables(session);

            Map<String, DBItemDailyPlanVariable> cyclicVariables = new HashMap<>();
            for (DBItemDailyPlanOrder item : items) {

                Schedule schedule = new Schedule();
                schedule.setPath(item.getSchedulePath());
                schedule.setWorkflowPath(item.getWorkflowPath());
                schedule.setWorkflowName(item.getWorkflowName());
                schedule.setSubmitOrderToControllerWhenPlanned(true);
                schedule.setVariableSets(new ArrayList<VariableSet>());

                DBItemDailyPlanVariable orderVariable = null;
                if (item.isCyclic()) {
                    String cyclicMainPart = OrdersHelper.getCyclicOrderIdMainPart(item.getOrderId());
                    if (cyclicVariables.containsKey(cyclicMainPart)) {
                        orderVariable = cyclicVariables.get(cyclicMainPart);
                    } else {
                        orderVariable = dbLayer.getOrderVariable(item.getControllerId(), item.getOrderId(), true);
                        cyclicVariables.put(cyclicMainPart, orderVariable);
                    }
                } else {
                    orderVariable = dbLayer.getOrderVariable(item.getControllerId(), item.getOrderId(), false);
                }
                Variables variables = new Variables();
                if (orderVariable != null && orderVariable.getVariableValue() != null) {
                    variables = Globals.objectMapper.readValue(orderVariable.getVariableValue(), Variables.class);
                }

                VariableSet variableSet = new VariableSet();
                variableSet.setVariables(variables);
                schedule.getVariableSets().add(variableSet);

                FreshOrder freshOrder = buildFreshOrder(schedule, variableSet, item.getPlannedStart().getTime(), item.getStartMode(), settings
                        .getTimeZone());
                freshOrder.setId(item.getOrderId());

                PlannedOrder p = new PlannedOrder(item.getControllerId(), freshOrder, schedule, item.getCalendarId());
                p.setStoredInDb(true);

                synchronizer.add(startupMode, p, controllerId, submissionForDate);
            }
            // disconnect here to avoid nested sessions
            Globals.disconnect(session);
            session = null;

            if (synchronizer.getPlannedOrders().size() > 0) {
                synchronizer.submitOrdersToController(startupMode, controllerId, submissionForDate);
            }
        } finally {
            Globals.disconnect(session);
        }
    }

    // service
    private Collection<Schedule> getAllSchedules() throws SOSHibernateException, IOException {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IDENTIFIER);

            DBLayerSchedules dbLayer = new DBLayerSchedules(session);
            List<DBItemInventoryReleasedConfiguration> result = dbLayer.getAllSchedules();
            session.close();
            session = null;

            return convert(result);
        } finally {
            Globals.disconnect(session);
        }
    }

    // service - use only with getPlanOrderAutomatically and not check the folder permissions
    private Collection<Schedule> convert(List<DBItemInventoryReleasedConfiguration> items) {
        return convert(items, true, false, null, null);
    }

    // DailyPlanOrdersGenerateImpl, SchedulesImpl
    public Collection<Schedule> convert(List<DBItemInventoryReleasedConfiguration> items, Set<Folder> permittedFolders,
            Map<String, Boolean> checkedFolders) {
        return convert(items, false, true, permittedFolders, checkedFolders);
    }

    private Collection<Schedule> convert(List<DBItemInventoryReleasedConfiguration> items, boolean onlyPlanOrderAutomatically,
            boolean checkPermissions, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders) {
        if (items == null || items.size() == 0) {
            return new ArrayList<Schedule>();
        }

        String method = "convert";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<Schedule> result = new ArrayList<Schedule>();
        for (DBItemInventoryReleasedConfiguration item : items) {
            String content = item.getContent();
            if (SOSString.isEmpty(content)) {
                LOGGER.warn(String.format("[%s][skip][content is empty]%s", method, SOSHibernate.toString(item)));
                continue;
            }
            Schedule schedule;
            try {
                schedule = objectMapper.readValue(item.getContent(), Schedule.class);
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s][%s][exception]%s", method, SOSHibernate.toString(item), e.toString()), e);
                continue;
            }
            if (schedule == null) {
                LOGGER.warn(String.format("[%s][skip][schedule is null]%s", method, SOSHibernate.toString(item)));
                continue;
            }
            String path = WorkflowPaths.getPathOrNull(schedule.getWorkflowName());
            if (path == null) {
                LOGGER.warn(String.format("[%s][skip][deployment path not found][workflow=%s]%s", method, schedule.getWorkflowName(), SOSHibernate
                        .toString(item)));
                continue;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][schedule=%s]workflow=%s", method, item.getPath(), path));
            }
            if (checkPermissions) {
                if (!isWorkflowPermitted(path, permittedFolders, checkedFolders)) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][skip][not permitted]workflow=%s", method, path));
                    }
                    continue;
                }
            }

            if (onlyPlanOrderAutomatically && !schedule.getPlanOrderAutomatically()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][onlyPlanOrderAutomatically=true][schedule.getPlanOrderAutomatically=false]%s", method,
                            schedule.getWorkflowName(), SOSHibernate.toString(item)));
                }
                continue;
            }

            schedule.setPath(item.getPath());
            schedule.setWorkflowPath(path);
            result.add(schedule);
        }
        return result;
    }

    private boolean isWorkflowPermitted(String workflowPath, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders) {
        String folder = DailyPlanHelper.getFolderFromPath(workflowPath);
        Boolean result = checkedFolders.get(folder);
        if (result == null) {
            result = JOCResourceImpl.canAdd(workflowPath, permittedFolders);
            checkedFolders.put(folder, result);
        }
        return result;
    }

    private List<DBItemDailyPlanSubmission> getSubmissionsForDate(java.util.Calendar calendar, String controllerId) throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IDENTIFIER);

            DBLayerDailyPlanSubmissions dbLayer = new DBLayerDailyPlanSubmissions(session);
            FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
            filter.setControllerId(controllerId);
            filter.setDateFor(calendar.getTime());

            return dbLayer.getSubmissions(filter, 0);
        } finally {
            Globals.disconnect(session);
        }
    }

    // service
    private void submitDaysAhead(StartupMode startupMode, String controllerId, java.util.Calendar calendar) throws JsonParseException,
            JsonMappingException, DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException,
            SOSException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        String date = SOSDate.getDateAsString(calendar);

        List<DBItemDailyPlanSubmission> submissions = getSubmissionsForDate(calendar, controllerId);
        if (submissions == null || submissions.size() == 0) {
            LOGGER.info(String.format("[%s][submitting][%s][%s][skip]no submissions found", startupMode, controllerId, date));
            return;
        }

        for (DBItemDailyPlanSubmission item : submissions) {
            List<DBItemDailyPlanOrder> plannedOrders;

            SOSHibernateSession session = null;
            try {
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setSortMode(null);
                filter.setOrderCriteria(null);
                filter.addSubmissionHistoryId(item.getId());
                filter.setSubmitted(false);

                session = Globals.createSosHibernateStatelessConnection("submitDaysAhead");
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                plannedOrders = dbLayer.getDailyPlanList(filter, 0);
            } finally {
                Globals.disconnect(session);
            }

            String submissionForDate = SOSDate.getDateTimeAsString(item.getSubmissionForDate());
            if (plannedOrders == null || plannedOrders.size() == 0) {
                LOGGER.info(String.format("[%s][submitting][%s][%s][submission date=%s, id=%s][skip]0 not submitted orders found", startupMode,
                        controllerId, date, submissionForDate, item.getId()));
            } else {
                OrderCounter c = DailyPlanHelper.getOrderCount(plannedOrders);
                LOGGER.info(String.format("[%s][submitting][%s][%s][submission date=%s, id=%s]submit %s start ...", startupMode, controllerId, date,
                        submissionForDate, item.getId(), c));
                submitOrders(startupMode, controllerId, plannedOrders, submissionForDate, null, "");
                // not log end because asynchronous
                // LOGGER.info(String.format("[submitting][%s][%s][submission=%s]submit end", controllerId, date, submissionForDate));
            }
        }
    }

    private Calendar getCalendar(String controllerId, String calendarName, ConfigurationType type) throws DBMissingDataException, JsonParseException,
            JsonMappingException, IOException, DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException,
            DBOpenSessionException, SOSHibernateException {

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IDENTIFIER + "-getCalendar");
            DBLayerReleasedConfigurations dbLayer = new DBLayerReleasedConfigurations(session);
            DBItemInventoryReleasedConfiguration config = dbLayer.getReleasedConfiguration(type, calendarName);
            session.close();
            session = null;

            if (config == null) {
                throw new DBMissingDataException(String.format("calendar '%s' not found for controller instance %s", calendarName, controllerId));
            }

            Calendar calendar = new ObjectMapper().readValue(config.getContent(), Calendar.class);
            calendar.setId(config.getId());
            calendar.setPath(config.getPath());
            calendar.setName(config.getName());
            return calendar;
        } finally {
            Globals.disconnect(session);
        }
    }

    private FreshOrder buildFreshOrder(Schedule schedule, VariableSet variableSet, Long startTime, Integer startMode, String timeZone)
            throws SOSInvalidDataException {
        FreshOrder order = new FreshOrder();
        order.setId(DailyPlanHelper.buildOrderId(schedule, variableSet, startTime, startMode, timeZone));
        order.setScheduledFor(startTime);
        order.setArguments(variableSet.getVariables());
        order.setWorkflowPath(schedule.getWorkflowName());
        return order;
    }

    private void generateNonWorkingDays(String controllerId, Schedule schedule, Date date, String dateAsString) throws SOSMissingDataException,
            SOSInvalidDataException, JsonParseException, JsonMappingException, DBMissingDataException, DBConnectionRefusedException,
            DBInvalidDataException, IOException, ParseException, JocConfigurationException, DBOpenSessionException, SOSHibernateException {

        String method = "generateNonWorkingDays";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean isTraceEnabled = LOGGER.isTraceEnabled();

        Date nextDate = DailyPlanHelper.getNextDay(date, settings);

        if (schedule.getNonWorkingDayCalendars() != null) {
            FrequencyResolver fr = new FrequencyResolver();
            for (AssignedNonWorkingDayCalendars calendars : schedule.getNonWorkingDayCalendars()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][%s]NonWorkingDayCalendar=%s", method, controllerId, dateAsString, schedule.getPath(),
                            calendars.getCalendarPath()));
                }
                nonWorkingDays = new HashMap<String, String>();

                Calendar calendar = null;
                try {
                    calendar = getCalendar(controllerId, calendars.getCalendarName(), ConfigurationType.NONWORKINGDAYSCALENDAR);
                } catch (DBMissingDataException e) {
                    LOGGER.warn(String.format("[%s][%s][%s][%s][NonWorkingDayCalendar=%s][skip]not found", method, controllerId, dateAsString,
                            schedule.getPath(), calendars.getCalendarPath()));
                    continue;
                }

                CalendarDatesFilter filter = new CalendarDatesFilter();
                filter.setDateFrom(SOSDate.getDateWithTimeZoneAsString(date, settings.getTimeZone()));
                filter.setDateTo(SOSDate.getDateWithTimeZoneAsString(nextDate, settings.getTimeZone()));
                filter.setCalendar(calendar);

                fr.resolve(filter);
            }
            Set<String> s = fr.getDates().keySet();
            for (String d : s) {
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("[%s][%s][%s][%s]Non working date=%s", method, controllerId, dateAsString, schedule.getPath(), d));
                }
                nonWorkingDays.put(d, controllerId);
            }
        }
    }

    public static boolean isFromService(StartupMode mode) {
        if (mode == null) {
            return false;
        }
        return mode.equals(StartupMode.automatic) || mode.equals(StartupMode.manual_restart);
    }

    private OrderListSynchronizer calculateStartTimes(StartupMode startupMode, String controllerId, Collection<Schedule> schedules, String date)
            throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException,
            IOException, SOSMissingDataException, SOSInvalidDataException, ParseException, JocConfigurationException, SOSHibernateException,
            DBOpenSessionException {

        String method = "calculateStartTimes";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean isTraceEnabled = LOGGER.isTraceEnabled();

        Date dailyPlanDate = SOSDate.getDate(date);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s]%s ...", method, controllerId, date));
        }

        Date actDate = dailyPlanDate;
        Date nextDate = DailyPlanHelper.getNextDay(dailyPlanDate, settings);
        boolean fromService = isFromService(startupMode);

        Map<String, CalendarCacheItem> calendarCache = new HashMap<String, CalendarCacheItem>();

        OrderListSynchronizer synchronizer = new OrderListSynchronizer(settings);
        for (Schedule schedule : schedules) {
            if (fromService && !schedule.getPlanOrderAutomatically()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip schedule=%s]fromService=true, plan order automatically=false", method, controllerId,
                            date, schedule.getPath()));
                }
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s]schedule=%s", method, controllerId, date, schedule.getPath()));
                }
                generateNonWorkingDays(controllerId, schedule, dailyPlanDate, date);

                for (AssignedCalendars assignedCalendar : schedule.getCalendars()) {
                    if (assignedCalendar.getTimeZone() == null) {
                        assignedCalendar.setTimeZone(UTC);
                    }

                    CalendarCacheItem calendarCacheItem = calendarCache.get(assignedCalendar.getCalendarName() + "#" + schedule.getPath());
                    String actDateAsString = SOSDate.getDateWithTimeZoneAsString(actDate, settings.getTimeZone());
                    String nextDateAsString = SOSDate.getDateWithTimeZoneAsString(nextDate, settings.getTimeZone());
                    String dailyPlanDateAsString = SOSDate.getDateWithTimeZoneAsString(dailyPlanDate, settings.getTimeZone());

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][%s][calendar=%s][timeZone=%s][actDate=%s][nextDate=%s]dailyPlanDate=%s", method,
                                controllerId, date, schedule.getPath(), assignedCalendar.getCalendarName(), assignedCalendar.getTimeZone(),
                                actDateAsString, nextDateAsString, dailyPlanDateAsString));
                    }

                    if (calendarCacheItem == null) {
                        calendarCacheItem = new CalendarCacheItem();
                        Calendar calendar = null;
                        try {
                            calendar = getCalendar(controllerId, assignedCalendar.getCalendarName(), ConfigurationType.WORKINGDAYSCALENDAR);
                        } catch (DBMissingDataException e) {
                            LOGGER.warn(String.format("[%s][%s][%s][%s][WorkingDayCalendar=%s][skip]not found", method, controllerId, date, schedule
                                    .getPath(), assignedCalendar.getCalendarName()));
                            continue;
                        }
                        calendarCacheItem.calendar = calendar;
                        calendarCache.put(assignedCalendar.getCalendarName() + "#" + schedule.getPath(), calendarCacheItem);
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][%s][%s][WorkingDayCalendar=%s][cache]%s", method, controllerId, date, schedule
                                    .getPath(), assignedCalendar.getCalendarName(), SOSString.toString(calendarCacheItem)));
                        }
                    }
                    PeriodResolver periodResolver = new PeriodResolver(settings);
                    Calendar restrictions = new Calendar();

                    calendarCacheItem.calendar.setFrom(actDateAsString);
                    calendarCacheItem.calendar.setTo(nextDateAsString);
                    String calendarJson = Globals.objectMapper.writeValueAsString(calendarCacheItem.calendar);
                    restrictions.setIncludes(assignedCalendar.getIncludes());
                    String restrictionJson = Globals.objectMapper.writeValueAsString(restrictions);

                    List<String> dates = new FrequencyResolver().resolveRestrictions(calendarJson, restrictionJson, actDateAsString, nextDateAsString)
                            .getDates();

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][%s][calendar=%s][FrequencyResolver]dates=%s", method, controllerId, date, schedule
                                .getPath(), assignedCalendar.getCalendarName(), String.join(",", dates)));
                    }

                    for (Period p : assignedCalendar.getPeriods()) {
                        Period _period = new Period();
                        _period.setBegin(p.getBegin());
                        _period.setEnd(p.getEnd());
                        _period.setRepeat(p.getRepeat());
                        _period.setSingleStart(p.getSingleStart());
                        _period.setWhenHoliday(p.getWhenHoliday());
                        periodResolver.addStartTimes(_period, dailyPlanDateAsString, assignedCalendar.getTimeZone());
                    }

                    int plannedOrdersCount = 0;
                    for (String d : dates) {
                        if (isTraceEnabled) {
                            LOGGER.trace(String.format("[%s][%s][%s][%s][calendar=%s]date=%s", method, controllerId, date, schedule.getPath(),
                                    assignedCalendar.getCalendarName(), d));
                        }
                        if (nonWorkingDays != null && nonWorkingDays.get(d) != null) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][%s][%s][calendar=%s][date=%s][skip]date is a non working day", method,
                                        controllerId, date, schedule.getPath(), assignedCalendar.getCalendarName(), d));
                            }
                        } else {
                            Map<Long, Period> startTimes = periodResolver.getStartTimes(d, dailyPlanDateAsString, assignedCalendar.getTimeZone());

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][%s][%s][calendar=%s][timeZone=%s][date=%s][periodResolver]startTimes size=%s",
                                        method, controllerId, date, schedule.getPath(), assignedCalendar.getCalendarName(), assignedCalendar
                                                .getTimeZone(), d, startTimes.size()));
                            }

                            for (Entry<Long, Period> periodEntry : startTimes.entrySet()) {
                                Integer startMode;
                                if (periodEntry.getValue().getSingleStart() == null) {
                                    startMode = 1;
                                } else {
                                    startMode = 0;
                                }

                                if (schedule.getVariableSets() == null || schedule.getVariableSets().size() == 0) {
                                    VariableSet variableSet = new VariableSet();
                                    schedule.setVariableSets(new ArrayList<VariableSet>());
                                    schedule.getVariableSets().add(variableSet);
                                }

                                for (VariableSet variableSet : schedule.getVariableSets()) {
                                    FreshOrder freshOrder = buildFreshOrder(schedule, variableSet, periodEntry.getKey(), startMode, this.settings
                                            .getTimeZone());

                                    if (!fromService) {
                                        schedule.setSubmitOrderToControllerWhenPlanned(settings.isSubmit());
                                    }

                                    if (synchronizer.getSubmission() == null) {
                                        synchronizer.setSubmission(addDailyPlanSubmission(controllerId, dailyPlanDate));
                                    }

                                    PlannedOrder plannedOrder = new PlannedOrder(controllerId, freshOrder, schedule, calendarCacheItem.calendar
                                            .getId());
                                    plannedOrder.setWorkflowPath(schedule.getWorkflowPath());
                                    plannedOrder.setPeriod(periodEntry.getValue());
                                    plannedOrder.setSubmissionHistoryId(synchronizer.getSubmission().getId());
                                    if (variableSet.getOrderName() != null && !variableSet.getOrderName().isEmpty()) {
                                        plannedOrder.setOrderName(variableSet.getOrderName());
                                    } else {
                                        plannedOrder.setOrderName(Paths.get(schedule.getPath()).getFileName().toString());
                                    }
                                    synchronizer.add(startupMode, plannedOrder, controllerId, date);
                                    plannedOrdersCount++;
                                }
                            }
                        }
                    }

                    if (plannedOrdersCount == 0) {
                        LOGGER.info(String.format("[%s][%s][%s][%s][skip][schedule=%s][calendar=%s][timeZone=%s]0 planned orders", startupMode,
                                method, controllerId, date, schedule.getPath(), assignedCalendar.getCalendarName(), assignedCalendar.getTimeZone()));

                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][%s][%s][calendar=%s]%s planned orders", method, controllerId, date, schedule
                                    .getPath(), assignedCalendar.getCalendarName(), plannedOrdersCount));
                        }
                    }
                }
            }
        }
        return synchronizer;
    }

    private DBItemDailyPlanSubmission addDailyPlanSubmission(String controllerId, Date dateForPlan) throws JocConfigurationException,
            DBConnectionRefusedException, SOSHibernateException, ParseException {

        SOSHibernateSession session = null;
        try {
            DBItemDailyPlanSubmission item = new DBItemDailyPlanSubmission();
            item.setControllerId(controllerId);
            item.setSubmissionForDate(dateForPlan);
            item.setUserAccount(settings.getUserAccount());

            session = Globals.createSosHibernateStatelessConnection("addDailyPlanSubmission");
            session.setAutoCommit(false);
            DBLayerDailyPlanSubmissions dbLayer = new DBLayerDailyPlanSubmissions(session);
            Globals.beginTransaction(session);
            dbLayer.storeSubmission(item, settings.getSubmissionTime());
            Globals.commit(session);

            return item;
        } finally {
            Globals.disconnect(session);
        }
    }

    public AtomicLong getLastActivityStart() {
        return lastActivityStart;
    }

    public AtomicLong getLastActivityEnd() {
        return lastActivityEnd;
    }

}