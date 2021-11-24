package com.sos.js7.order.initiator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
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
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
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
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.js7.order.initiator.classes.DailyPlanHelper;
import com.sos.js7.order.initiator.classes.OrderCounter;
import com.sos.js7.order.initiator.classes.PlannedOrder;
import com.sos.js7.order.initiator.classes.PlannedOrderKey;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanSubmissions;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.DBLayerInventoryReleasedConfigurations;
import com.sos.js7.order.initiator.db.DBLayerOrderVariables;
import com.sos.js7.order.initiator.db.FilterDailyPlanSubmissions;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterInventoryReleasedConfigurations;
import com.sos.js7.order.initiator.db.FilterOrderVariables;

class CalendarCacheItem {

    Calendar calendar;

}

public class DailyPlanRunner extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanRunner.class);
    private static final String IDENTIFIER = DailyPlanRunner.class.getSimpleName();
    private static final String UTC = "UTC";

    private java.util.Calendar startCalendar;
    private SOSHibernateFactory factory;
    private DailyPlanSettings settings;
    private AtomicLong lastActivityStart = new AtomicLong(0);
    private AtomicLong lastActivityEnd = new AtomicLong(0);
    private List<Schedule> schedules;
    private List<ControllerConfiguration> controllers;
    private Map<String, String> nonWorkingDays;
    private Set<String> createdPlans;

    private boolean fromService = true;
    private boolean firstStart = true;

    public DailyPlanRunner(DailyPlanSettings settings, boolean fromService) {
        this.settings = settings;
        this.fromService = fromService;
    }

    public DailyPlanRunner(List<ControllerConfiguration> controllers, DailyPlanSettings settings, boolean fromService) {
        this.settings = settings;
        this.controllers = controllers;
        this.fromService = fromService;
    }

    public void createPlan(java.util.Calendar calendar) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
            ParseException, SOSException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        try {
            lastActivityStart.set(new Date().getTime());

            LOGGER.info(String.format("[createPlan]creating from %s for %s days ahead, submitting for %s days ahead", DailyPlanHelper.getDate(
                    calendar), settings.getDayAheadPlan(), settings.getDayAheadSubmit()));

            java.util.Calendar savCalendar = java.util.Calendar.getInstance();
            savCalendar.setTime(calendar.getTime());
            for (ControllerConfiguration conf : controllers) {
                String controllerId = conf.getCurrent().getId();

                java.util.Calendar dailyPlanCalendar = java.util.Calendar.getInstance();
                dailyPlanCalendar.setTime(savCalendar.getTime());
                settings.setDailyPlanDate(dailyPlanCalendar.getTime());

                ScheduleSource scheduleSource = new ScheduleSourceDB(controllerId);
                readSchedules(scheduleSource);
                LOGGER.info(String.format("[createPlan][%s]found %s schedules", controllerId, schedules.size()));

                for (int day = 0; day < settings.getDayAheadPlan(); day++) {
                    String date = DailyPlanHelper.dateAsString(dailyPlanCalendar.getTime(), settings.getTimeZone());
                    List<DBItemDailyPlanSubmission> l = getSubmissionsForDate(dailyPlanCalendar, controllerId);
                    if ((l.size() == 0)) {
                        generateDailyPlan(controllerId, null, "", date, false);
                    } else {
                        List<String> copy = l.stream().map(e -> {
                            String d = DailyPlanHelper.getDateTime(e.getSubmissionForDate());
                            return d == null ? "" : d;
                        }).collect(Collectors.toList());
                        LOGGER.info(String.format("[creating][%s][%s][skip][submission(s) found]%s", controllerId, date, String.join(",", copy)));
                    }

                    dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                    settings.setDailyPlanDate(dailyPlanCalendar.getTime());
                }

                dailyPlanCalendar.setTime(savCalendar.getTime());
                settings.setDailyPlanDate(dailyPlanCalendar.getTime());

                for (int day = 0; day < settings.getDayAheadSubmit(); day++) {
                    submitDaysAhead(controllerId, dailyPlanCalendar);
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

    public Map<PlannedOrderKey, PlannedOrder> generateDailyPlan(String controllerId, JocError jocError, String accessToken, String date,
            Boolean withSubmit) throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, IOException, ParseException, SOSException, URISyntaxException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, InterruptedException, ExecutionException, TimeoutException {

        OrderListSynchronizer synchronizer = calculateStartTimes(controllerId, date);
        synchronizer.setJocError(jocError);
        synchronizer.setAccessToken(accessToken);
        OrderCounter c = DailyPlanHelper.getOrderCount(synchronizer.getPlannedOrders());

        String operation = withSubmit ? "creating/submitting" : "creating";
        if (synchronizer.getPlannedOrders().size() > 0) {
            String submissionId = "unknown";
            String submissionDate = "unknown";
            if (synchronizer.getSubmission() != null) {
                submissionId = synchronizer.getSubmission().getId().toString();
                submissionDate = DailyPlanHelper.getDateTime(synchronizer.getSubmission().getSubmissionForDate());
            }

            LOGGER.info(String.format("[%s][%s][%s][calculated][%s][submission id=%s, submission for date=%s]", operation, controllerId, date, c,
                    submissionId, submissionDate));

            synchronizer.addPlannedOrderToControllerAndDB(operation, controllerId, date, withSubmit, fromService);
            EventBus.getInstance().post(new DailyPlanEvent(date));
        } else {
            LOGGER.info(String.format("[%s][%s][%s][skip]%s", operation, controllerId, date, c));
        }
        return synchronizer.getPlannedOrders();
    }

    public void submitOrders(String controllerId, List<DBItemDailyPlanOrder> plannedOrders, String submissionForDate, JocError jocError,
            String accessToken) throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, IOException, ParseException, SOSException, URISyntaxException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, InterruptedException, ExecutionException, TimeoutException {

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

            for (DBItemDailyPlanOrder item : plannedOrders) {

                Schedule schedule = new Schedule();
                schedule.setPath(item.getSchedulePath());
                schedule.setWorkflowPath(item.getWorkflowPath());
                schedule.setWorkflowName(item.getWorkflowName());
                schedule.setSubmitOrderToControllerWhenPlanned(true);
                schedule.setVariableSets(new ArrayList<VariableSet>());

                Variables variables = new Variables();

                FilterOrderVariables filter = new FilterOrderVariables();
                filter.setPlannedOrderId(item.getId());
                List<DBItemDailyPlanVariable> orderVariables = dbLayer.getOrderVariables(filter, 0);
                if (orderVariables != null && orderVariables.size() > 0 && orderVariables.get(0).getVariableValue() != null) {
                    variables = Globals.objectMapper.readValue(orderVariables.get(0).getVariableValue(), Variables.class);
                }

                VariableSet variableSet = new VariableSet();
                variableSet.setVariables(variables);
                schedule.getVariableSets().add(variableSet);

                FreshOrder freshOrder = buildFreshOrder(schedule, variableSet, item.getPlannedStart().getTime(), item.getStartMode(), settings
                        .getTimeZone(), settings.getPeriodBegin());
                freshOrder.setId(item.getOrderId());

                PlannedOrder p = new PlannedOrder();
                p.setControllerId(item.getControllerId());
                p.setSchedule(schedule);
                p.setFreshOrder(freshOrder);
                p.setCalendarId(item.getCalendarId());
                p.setStoredInDb(true);

                synchronizer.add(p, controllerId, submissionForDate);
            }
            // disconnect here to avoid nested sessions
            Globals.disconnect(session);
            session = null;

            if (synchronizer.getPlannedOrders().size() > 0) {
                synchronizer.submitOrdersToController(controllerId, submissionForDate, fromService);
            }
        } finally {
            Globals.disconnect(session);
        }
    }

    private List<DBItemDailyPlanSubmission> getSubmissionsForDate(java.util.Calendar calendar, String controllerId) throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IDENTIFIER);

            DBLayerDailyPlanSubmissions dbLayer = new DBLayerDailyPlanSubmissions(session);
            FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
            filter.setControllerId(controllerId);
            filter.setDateFor(calendar.getTime());

            return dbLayer.getDailyPlanSubmissions(filter, 0);
        } finally {
            Globals.disconnect(session);
        }
    }

    private void submitDaysAhead(String controllerId, java.util.Calendar calendar) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {

        String date = DailyPlanHelper.getDate(calendar);

        List<DBItemDailyPlanSubmission> submissions = getSubmissionsForDate(calendar, controllerId);
        if (submissions == null || submissions.size() == 0) {
            LOGGER.info(String.format("[submitting][%s][%s][skip]no submissions found", controllerId, date));
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

            String submissionForDate = DailyPlanHelper.getDateTime(item.getSubmissionForDate());
            if (plannedOrders == null || plannedOrders.size() == 0) {
                LOGGER.info(String.format("[submitting][%s][%s][submission id=%s, submission for date=%s][skip]0 not submitted orders found",
                        controllerId, date, item.getId(), submissionForDate));
            } else {
                OrderCounter c = DailyPlanHelper.getOrderCount(plannedOrders);
                LOGGER.info(String.format("[submitting][%s][%s][submission id=%s, submission for date=%s]submit %s start ...", controllerId, date,
                        item.getId(), submissionForDate, c));
                submitOrders(controllerId, plannedOrders, submissionForDate, null, "");
                // not log end because asynchronous
                // LOGGER.info(String.format("[submitting][%s][%s][submission=%s]submit end", controllerId, date, submissionForDate));
            }
        }
    }

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
            // TODO duplicate calculation, see com.sos.js7.order.initiator.OrderInitiatorService.start() ->
            // DailyPlanHelper.getStartTimeAsString
            if (!"".equals(settings.getDailyPlanStartTime())) {
                startCalendar = DailyPlanHelper.getDailyplanCalendar(settings.getDailyPlanStartTime(), settings.getTimeZone());
            } else {
                startCalendar = DailyPlanHelper.getDailyplanCalendar(settings.getPeriodBegin(), settings.getTimeZone());
                startCalendar.add(java.util.Calendar.DATE, 1);
                startCalendar.add(java.util.Calendar.MINUTE, -30);
            }
            if (startCalendar.before(now)) {
                startCalendar.add(java.util.Calendar.DATE, 1);
            }
        }

        java.util.Calendar calendar = DailyPlanHelper.getDailyplanCalendar(settings.getPeriodBegin(), settings.getTimeZone());
        calendar.add(java.util.Calendar.DATE, 1);
        String date = DailyPlanHelper.getDate(calendar);

        // TODO createdPlans static? because several instances ...
        if (!createdPlans.contains(date) && (manuelStart || (now.getTimeInMillis() - startCalendar.getTimeInMillis()) > 0)) {
            startCalendar = null;
            createdPlans.add(date);
            try {
                settings.setSubmissionTime(new Date());

                java.util.Calendar nextDayCalendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                nextDayCalendar.add(java.util.Calendar.DATE, 1);
                nextDayCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                nextDayCalendar.set(java.util.Calendar.MINUTE, 0);
                nextDayCalendar.set(java.util.Calendar.SECOND, 0);
                nextDayCalendar.set(java.util.Calendar.MILLISECOND, 0);
                nextDayCalendar.set(java.util.Calendar.MINUTE, 0);
                createPlan(nextDayCalendar);
            } catch (ControllerConnectionResetException | ControllerConnectionRefusedException | ParseException | SOSException | URISyntaxException
                    | InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            if (LOGGER.isTraceEnabled() && startCalendar != null) {
                LOGGER.trace(String.format("wait for start at %s (%s)...", DailyPlanHelper.getDateTime(startCalendar, settings.getTimeZone()),
                        settings.getTimeZone()));
            }
        }
        AJocClusterService.clearLogger();
    }

    public void exit() {
        if (factory != null) {
            factory.close();
        }
    }

    public void readSchedules(ScheduleSource source) throws IOException, SOSHibernateException {
        LOGGER.debug("... readSchedules " + source.getSource());
        schedules = source.getSchedules();
    }

    public void addSchedule(Schedule schedule) {
        LOGGER.debug("... addSchedule " + schedule.getPath());
        if (schedules == null) {
            schedules = new ArrayList<Schedule>();
        }
        schedules.add(schedule);
    }

    private Calendar getCalendar(String controllerId, String calendarName, ConfigurationType type) throws DBMissingDataException, JsonParseException,
            JsonMappingException, IOException, DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException,
            DBOpenSessionException, SOSHibernateException {

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
            DBLayerInventoryReleasedConfigurations dbLayer = new DBLayerInventoryReleasedConfigurations(session);
            FilterInventoryReleasedConfigurations filter = new FilterInventoryReleasedConfigurations();
            filter.setName(calendarName);
            filter.setType(type);

            DBItemInventoryReleasedConfiguration config = dbLayer.getSingleInventoryReleasedConfigurations(filter);
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

    private FreshOrder buildFreshOrder(Schedule schedule, VariableSet variableSet, Long startTime, Integer startMode, String timeZone,
            String periodBegin) {
        FreshOrder order = new FreshOrder();
        order.setId(DailyPlanHelper.buildOrderId(schedule, variableSet, startTime, startMode, timeZone, periodBegin));
        order.setScheduledFor(startTime);
        order.setArguments(variableSet.getVariables());
        order.setWorkflowPath(schedule.getWorkflowName());
        return order;
    }

    private void generateNonWorkingDays(String controllerId, Schedule o, Date date, String dateAsString) throws SOSMissingDataException,
            SOSInvalidDataException, JsonParseException, JsonMappingException, DBMissingDataException, DBConnectionRefusedException,
            DBInvalidDataException, IOException, ParseException, JocConfigurationException, DBOpenSessionException, SOSHibernateException {

        String method = "generateNonWorkingDays";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean isTraceEnabled = LOGGER.isTraceEnabled();

        Date nextDate = DailyPlanHelper.getNextDay(date, settings);

        if (o.getNonWorkingDayCalendars() != null) {
            FrequencyResolver fr = new FrequencyResolver();
            for (AssignedNonWorkingDayCalendars calendars : o.getNonWorkingDayCalendars()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s]NonWorkingDayCalendar=%s", method, controllerId, dateAsString, calendars
                            .getCalendarPath()));
                }
                nonWorkingDays = new HashMap<String, String>();

                Calendar calendar = getCalendar(controllerId, calendars.getCalendarName(), ConfigurationType.NONWORKINGDAYSCALENDAR);
                CalendarDatesFilter filter = new CalendarDatesFilter();
                filter.setDateFrom(DailyPlanHelper.dateAsString(date, settings.getTimeZone()));
                filter.setDateTo(DailyPlanHelper.dateAsString(nextDate, settings.getTimeZone()));
                filter.setCalendar(calendar);

                fr.resolve(filter);
            }
            Set<String> s = fr.getDates().keySet();
            for (String d : s) {
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("[%s][%s][%s]Non working date=%s", method, controllerId, dateAsString, d));
                }
                nonWorkingDays.put(d, controllerId);
            }
        }
    }

    private OrderListSynchronizer calculateStartTimes(String controllerId, String date) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, IOException, SOSMissingDataException,
            SOSInvalidDataException, ParseException, JocConfigurationException, SOSHibernateException, DBOpenSessionException {

        String method = "calculateStartTimes";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean isTraceEnabled = LOGGER.isTraceEnabled();

        Date dailyPlanDate = DailyPlanHelper.stringAsDate(date);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s]%s ...", method, controllerId, date));
        }

        Date actDate = dailyPlanDate;
        Date nextDate = DailyPlanHelper.getNextDay(dailyPlanDate, settings);

        Map<String, CalendarCacheItem> calendarCache = new HashMap<String, CalendarCacheItem>();
        DBItemDailyPlanSubmission item = null;

        OrderListSynchronizer synchronizer = new OrderListSynchronizer(settings);
        for (Schedule schedule : schedules) {
            if (fromService && !schedule.getPlanOrderAutomatically()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip schedule=%s]fromService=true, plan order automatically=false", method, controllerId,
                            date, schedule.getPath()));
                }
            } else {
                if (item == null) {
                    item = addDailyPlanSubmission(controllerId, dailyPlanDate);
                    synchronizer.setSubmission(item);
                }

                generateNonWorkingDays(controllerId, schedule, dailyPlanDate, date);

                for (AssignedCalendars assignedCalendar : schedule.getCalendars()) {
                    if (assignedCalendar.getTimeZone() == null) {
                        assignedCalendar.setTimeZone(UTC);
                    }
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s]calendar=%s", method, controllerId, date, assignedCalendar.getCalendarName()));
                    }

                    CalendarCacheItem calendarCacheItem = calendarCache.get(assignedCalendar.getCalendarName() + "#" + schedule.getPath());
                    String actDateAsString = DailyPlanHelper.dateAsString(actDate, settings.getTimeZone());
                    String nextDateAsString = DailyPlanHelper.dateAsString(nextDate, settings.getTimeZone());
                    String dailyPlanDateAsString = DailyPlanHelper.dateAsString(dailyPlanDate, settings.getTimeZone());

                    if (calendarCacheItem == null) {
                        calendarCacheItem = new CalendarCacheItem();
                        Calendar calendar = getCalendar(controllerId, assignedCalendar.getCalendarName(), ConfigurationType.WORKINGDAYSCALENDAR);
                        calendarCacheItem.calendar = calendar;
                        calendarCache.put(assignedCalendar.getCalendarName() + "#" + schedule.getPath(), calendarCacheItem);
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][%s][calendar=%s][cache]%s", method, controllerId, date, assignedCalendar
                                    .getCalendarName(), SOSString.toString(calendarCacheItem)));
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

                    for (Period p : assignedCalendar.getPeriods()) {
                        Period _period = new Period();
                        _period.setBegin(p.getBegin());
                        _period.setEnd(p.getEnd());
                        _period.setRepeat(p.getRepeat());
                        _period.setSingleStart(p.getSingleStart());
                        _period.setWhenHoliday(p.getWhenHoliday());
                        periodResolver.addStartTimes(_period, dailyPlanDateAsString, assignedCalendar.getTimeZone());
                    }

                    for (String d : dates) {
                        if (isTraceEnabled) {
                            LOGGER.trace(String.format("[%s][%s][%s][calendar=%s]date=%s", method, controllerId, date, assignedCalendar
                                    .getCalendarName(), d));
                        }
                        if (nonWorkingDays != null && nonWorkingDays.get(d) != null) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][%s][calendar=%s][date=%s][skip]date is a non working day", method, controllerId,
                                        date, assignedCalendar.getCalendarName(), d));
                            }
                        } else {
                            Map<Long, Period> startTimes = periodResolver.getStartTimes(d, dailyPlanDateAsString, assignedCalendar.getTimeZone());
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
                                            .getTimeZone(), this.settings.getPeriodBegin());

                                    if (!fromService) {
                                        schedule.setSubmitOrderToControllerWhenPlanned(settings.isSubmit());
                                    }

                                    PlannedOrder plannedOrder = new PlannedOrder();
                                    plannedOrder.setControllerId(controllerId);
                                    plannedOrder.setFreshOrder(freshOrder);
                                    plannedOrder.setWorkflowPath(schedule.getWorkflowPath());
                                    plannedOrder.setCalendarId(calendarCacheItem.calendar.getId());
                                    plannedOrder.setPeriod(periodEntry.getValue());
                                    plannedOrder.setSubmissionHistoryId(item.getId());
                                    plannedOrder.setSchedule(schedule);
                                    if (variableSet.getOrderName() != null && !variableSet.getOrderName().isEmpty()) {
                                        plannedOrder.setOrderName(variableSet.getOrderName());
                                    } else {
                                        plannedOrder.setOrderName(Paths.get(schedule.getPath()).getFileName().toString());
                                    }
                                    synchronizer.add(plannedOrder, controllerId, date);
                                }
                            }
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