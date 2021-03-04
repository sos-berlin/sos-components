package com.sos.js7.order.initiator;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.ParseException;
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
import com.sos.controller.model.common.Variables;
import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.Schedule;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.joc.Globals;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanSubmissions;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.js7.order.initiator.classes.DailyPlanHelper;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;
import com.sos.js7.order.initiator.classes.PlannedOrder;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanSubmissions;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.DBLayerInventoryConfigurations;
import com.sos.js7.order.initiator.db.DBLayerOrderVariables;
import com.sos.js7.order.initiator.db.FilterDailyPlanSubmissions;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterInventoryConfigurations;
import com.sos.js7.order.initiator.db.FilterOrderVariables;

class CalendarCacheItem {

    Calendar calendar;
    String timeZone;
    Set<String> dates;
    PeriodResolver periodResolver;
}

public class OrderInitiatorRunner extends TimerTask {

    private static final String DAILYPLAN_RUNNER = "DailyplanRunner";
    private static final String UTC = "UTC";
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorRunner.class);
    private List<Schedule> listOfSchedules;
    private boolean fromService = true;
    private boolean firstStart = false;
    private Set<String> createdPlans;
    private List<ControllerConfiguration> controllers;
    private AtomicLong lastActivityStart = new AtomicLong(0);
    private AtomicLong lastActivityEnd = new AtomicLong(0);

    public List<Schedule> getListOfSchedules() {
        return listOfSchedules;
    }

    private Map<String, String> listOfNonWorkingDays;
    private SOSHibernateFactory sosHibernateFactory;
    private OrderListSynchronizer orderListSynchronizer;

    public OrderInitiatorRunner(OrderInitiatorSettings orderInitiatorSettings, boolean fromService) {
        OrderInitiatorGlobals.orderInitiatorSettings = orderInitiatorSettings;
        this.fromService = fromService;
    }

    public OrderInitiatorRunner(List<ControllerConfiguration> controllers, OrderInitiatorSettings orderInitiatorSettings, boolean fromService) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        OrderInitiatorGlobals.orderInitiatorSettings = orderInitiatorSettings;
        this.controllers = controllers;
        this.fromService = fromService;
    }

    public void generateDailyPlan(String dailyPlanDate, Boolean withSubmit) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            IOException, ParseException, SOSException, URISyntaxException, JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, InterruptedException, ExecutionException, TimeoutException {

        orderListSynchronizer = calculateStartTimes(DailyPlanHelper.stringAsDate(dailyPlanDate));
        if (orderListSynchronizer.getListOfPlannedOrders().size() > 0) {
            orderListSynchronizer.addPlannedOrderToControllerAndDB(withSubmit);
        }
        orderListSynchronizer.resetListOfPlannedOrders();
    }

    public void submitOrders(List<DBItemDailyPlanOrders> listOfPlannedOrders) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            IOException, ParseException, SOSException, URISyntaxException, JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, InterruptedException, ExecutionException, TimeoutException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("submitOrders");
            DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);

            OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer();
            for (DBItemDailyPlanOrders dbItemDailyPlanOrders : listOfPlannedOrders) {
                PlannedOrder p = new PlannedOrder();
                Schedule schedule = new Schedule();
                schedule.setPath(dbItemDailyPlanOrders.getSchedulePath());
                schedule.setWorkflowPath(dbItemDailyPlanOrders.getWorkflowPath());
                schedule.setWorkflowName(dbItemDailyPlanOrders.getWorkflowName());
                schedule.setSubmitOrderToControllerWhenPlanned(true);
                p.setControllerId(dbItemDailyPlanOrders.getControllerId());

                FilterOrderVariables filterOrderVariables = new FilterOrderVariables();

                filterOrderVariables.setPlannedOrderId(dbItemDailyPlanOrders.getId());
                Variables variables = new Variables();
                List<DBItemDailyPlanVariables> listOfOrderVariables = dbLayerOrderVariables.getOrderVariables(filterOrderVariables, 0);
                if (listOfOrderVariables != null) {
                    for (DBItemDailyPlanVariables orderVariable : listOfOrderVariables) {
                        switch (orderVariable.getVariableType()) {
                        case 0:
                            variables.setAdditionalProperty(orderVariable.getVariableName(), orderVariable.getVariableValue());
                            break;
                        case 1:
                            variables.setAdditionalProperty(orderVariable.getVariableName(), Boolean.parseBoolean(orderVariable.getVariableValue()));
                            break;
                        case 2:
                            variables.setAdditionalProperty(orderVariable.getVariableName(), Integer.parseInt(orderVariable.getVariableValue()));
                            break;
                        case 3:
                            variables.setAdditionalProperty(orderVariable.getVariableName(), new BigDecimal(orderVariable.getVariableValue()));
                        case 4:
                            variables.setAdditionalProperty(orderVariable.getVariableName(), Double.parseDouble(orderVariable.getVariableValue()));
                            break;

                        }
                    }
                }

                schedule.setVariables(variables);

                FreshOrder freshOrder = buildFreshOrder(schedule, dbItemDailyPlanOrders.getPlannedStart().getTime(), dbItemDailyPlanOrders
                        .getStartMode());
                freshOrder.setId(dbItemDailyPlanOrders.getOrderId());
                p.setSchedule(schedule);
                p.setFreshOrder(freshOrder);
                p.setCalendarId(dbItemDailyPlanOrders.getCalendarId());
                p.setStoredInDb(true);

                orderListSynchronizer.add(p);
            }
            if (orderListSynchronizer.getListOfPlannedOrders().size() > 0) {
                orderListSynchronizer.submitOrdersToController();
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private List<DBItemDailyPlanSubmissions> getSubmissionsForDate(java.util.Calendar calendar, String controllerId) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(DAILYPLAN_RUNNER);

            DBLayerDailyPlanSubmissions dbLayerDailyPlan = new DBLayerDailyPlanSubmissions(sosHibernateSession);
            FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
            filter.setControllerId(controllerId);

            filter.setDateFor(calendar.getTime());
            List<DBItemDailyPlanSubmissions> listOfDailyPlanSubmissions = dbLayerDailyPlan.getDailyPlanSubmissions(filter, 0);
            return (listOfDailyPlanSubmissions);

        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void createPlan(java.util.Calendar calendar) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException,
            ParseException, SOSException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        try {
            lastActivityStart.set(new Date().getTime());

            java.util.Calendar savCalendar = java.util.Calendar.getInstance();
            savCalendar.setTime(calendar.getTime());

            for (ControllerConfiguration controllerConfiguration : controllers) {
                java.util.Calendar dailyPlanCalendar = java.util.Calendar.getInstance();
                dailyPlanCalendar.setTime(savCalendar.getTime());

                OrderInitiatorGlobals.orderInitiatorSettings.setControllerId(controllerConfiguration.getCurrent().getId());
                OrderInitiatorGlobals.dailyPlanDate = dailyPlanCalendar.getTime();
                ScheduleSource scheduleSource = new ScheduleSourceDB(controllerConfiguration.getCurrent().getId());
                readSchedules(scheduleSource);
                boolean logDailyPlan = false;

                for (int day = 0; day < OrderInitiatorGlobals.orderInitiatorSettings.getDayAheadPlan(); day++) {
                    String dailyPlanDate = DailyPlanHelper.dateAsString(dailyPlanCalendar.getTime());
                    List<DBItemDailyPlanSubmissions> l = getSubmissionsForDate(dailyPlanCalendar, controllerConfiguration.getCurrent().getId());
                    if ((l.size() == 0)) {
                        if (!logDailyPlan) {
                            LOGGER.info("Creating daily plan for controller: " + controllerConfiguration.getCurrent().getId() + " from "
                                    + dailyPlanDate + " for " + OrderInitiatorGlobals.orderInitiatorSettings.getDayAheadPlan() + " days ahead");

                            logDailyPlan = true;
                        }
                        LOGGER.debug("Creating daily plans for controller: " + controllerConfiguration.getCurrent().getId() + " from " + dailyPlanDate
                                + " for " + OrderInitiatorGlobals.orderInitiatorSettings.getDayAheadPlan() + " days ahead");
                        generateDailyPlan(dailyPlanDate, false);
                    } else {
                        LOGGER.debug("Will not create for " + dailyPlanDate + " --> Submission found");
                    }

                    dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                    OrderInitiatorGlobals.dailyPlanDate = dailyPlanCalendar.getTime();
                }

                dailyPlanCalendar.setTime(savCalendar.getTime());
                OrderInitiatorGlobals.dailyPlanDate = dailyPlanCalendar.getTime();

                LOGGER.info("Submitting orders for daily plan for " + OrderInitiatorGlobals.orderInitiatorSettings.getDayAheadSubmit()
                        + " days ahead");
                for (int day = 0; day < OrderInitiatorGlobals.orderInitiatorSettings.getDayAheadSubmit(); day++) {

                    submitDaysAhead(dailyPlanCalendar, controllerConfiguration.getCurrent().getId());
                    dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                    OrderInitiatorGlobals.dailyPlanDate = dailyPlanCalendar.getTime();
                }

            }
        } catch (SOSHibernateException | IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException
                | JocConfigurationException | DBOpenSessionException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            lastActivityEnd.set(new Date().getTime());
        }

    }

    private void submitDaysAhead(java.util.Calendar calendar, String controllerId) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, IOException, ParseException, SOSException,
            URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            List<DBItemDailyPlanSubmissions> listOfSubmissions = getSubmissionsForDate(calendar, controllerId);
            String dailyPlanDate = DailyPlanHelper.dateAsString(calendar.getTime());

            LOGGER.debug("Submit days ahead: " + dailyPlanDate);
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("submitDaysAhead");

            for (DBItemDailyPlanSubmissions dbItemDailyPlanSubmissions : listOfSubmissions) {
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.addSubmissionHistoryId(dbItemDailyPlanSubmissions.getId());
                filter.setSubmitted(false);
                DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
                List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
                LOGGER.debug(listOfPlannedOrders.size() + " to be submitted");
                submitOrders(listOfPlannedOrders);

            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void run() {
        if (createdPlans == null) {
            createdPlans = new HashSet<String>();
        }
        LOGGER.debug("firstStart:" + firstStart);

        boolean generateFromManuelStart = false;
        if (!firstStart && StartupMode.manual.equals(OrderInitiatorGlobals.orderInitiatorSettings.getStartMode())) {
            firstStart = true;
            LOGGER.debug("generateFromManuelStart: true");
            generateFromManuelStart = true;
        }

        java.util.Calendar calendar = DailyPlanHelper.getDailyplanCalendar();
        java.util.Calendar now = java.util.Calendar.getInstance(TimeZone.getTimeZone(OrderInitiatorGlobals.orderInitiatorSettings.getTimeZone()));

        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        java.util.Calendar dailyPlanCalendar = java.util.Calendar.getInstance(timeZone);

        if (!createdPlans.contains(DailyPlanHelper.getDayOfYear(calendar)) && (generateFromManuelStart || OrderInitiatorGlobals.orderInitiatorSettings
                .getDailyPlanDaysCreateOnStart() || (now.getTimeInMillis() - calendar.getTimeInMillis()) > 0)) {

            LOGGER.debug("Creating daily plan beginning with " + DailyPlanHelper.getDayOfYear(dailyPlanCalendar));
            createdPlans.add(DailyPlanHelper.getDayOfYear(dailyPlanCalendar));
            try {
                OrderInitiatorGlobals.submissionTime = new Date();
                dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                dailyPlanCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                dailyPlanCalendar.set(java.util.Calendar.MINUTE, 0);
                dailyPlanCalendar.set(java.util.Calendar.SECOND, 0);
                dailyPlanCalendar.set(java.util.Calendar.MILLISECOND, 0);
                dailyPlanCalendar.set(java.util.Calendar.MINUTE, 0);
                createPlan(dailyPlanCalendar);
            } catch (JobSchedulerConnectionResetException | JobSchedulerConnectionRefusedException | ParseException | SOSException
                    | URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public void exit() {
        if (sosHibernateFactory != null) {
            sosHibernateFactory.close();
        }
    }

    public void readSchedules(ScheduleSource scheduleSource) throws IOException, SOSHibernateException {
        LOGGER.debug("... readTemplates " + scheduleSource.fromSource());
        Schedules schedules = new Schedules();
        schedules.fillListOfSchedules(scheduleSource);
        listOfSchedules = schedules.getListOfSchedules();
    }

    private Calendar getCalendar(String controllerId, String calendarName, ConfigurationType type) throws DBMissingDataException, JsonParseException,
            JsonMappingException, IOException, DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException,
            DBOpenSessionException, SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
            DBLayerInventoryConfigurations dbLayer = new DBLayerInventoryConfigurations(sosHibernateSession);
            FilterInventoryConfigurations filter = new FilterInventoryConfigurations();
            filter.setName(calendarName);
            filter.setReleased(true);
            filter.setType(type);

            DBItemInventoryConfiguration config = dbLayer.getSingleInventoryConfigurations(filter);
            if (config == null) {
                throw new DBMissingDataException(String.format("calendar '%s' not found for controller instance %s", calendarName, controllerId));
            }

            Calendar calendar = new ObjectMapper().readValue(config.getContent(), Calendar.class);
            calendar.setId(config.getId());
            calendar.setPath(config.getPath());
            calendar.setName(config.getName());
            return calendar;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private FreshOrder buildFreshOrder(Schedule o, Long startTime, Integer startMode) {
        FreshOrder freshOrder = new FreshOrder();
        freshOrder.setId(DailyPlanHelper.buildOrderId(o, startTime, startMode));
        freshOrder.setScheduledFor(startTime);
        freshOrder.setArguments(o.getVariables());
        freshOrder.setWorkflowPath(o.getWorkflowName());
        return freshOrder;
    }

    private void generateNonWorkingDays(Date dailyPlanDate, Schedule o, String controllerId) throws SOSMissingDataException, SOSInvalidDataException,
            JsonParseException, JsonMappingException, DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException, IOException,
            ParseException, JocConfigurationException, DBOpenSessionException, SOSHibernateException {

        Date nextDate = DailyPlanHelper.getNextDay(dailyPlanDate);

        if (o.getNonWorkingCalendars() != null) {
            FrequencyResolver fr = new FrequencyResolver();
            for (AssignedNonWorkingCalendars assignedNonWorkingCalendars : o.getNonWorkingCalendars()) {
                LOGGER.debug("Generate non working dates for:" + assignedNonWorkingCalendars.getCalendarPath());
                listOfNonWorkingDays = new HashMap<String, String>();
                Calendar calendar = getCalendar(controllerId, assignedNonWorkingCalendars.getCalendarName(),
                        ConfigurationType.NONWORKINGDAYSCALENDAR);
                CalendarDatesFilter calendarFilter = new CalendarDatesFilter();

                calendarFilter.setDateFrom(DailyPlanHelper.dateAsString(dailyPlanDate));
                calendarFilter.setDateTo(DailyPlanHelper.dateAsString(nextDate));
                calendarFilter.setCalendar(calendar);
                fr.resolve(calendarFilter);
            }
            Set<String> s = fr.getDates().keySet();
            for (String d : s) {
                LOGGER.trace("Non working date: " + d);
                listOfNonWorkingDays.put(d, controllerId);
            }
        }
    }

    private OrderListSynchronizer calculateStartTimes(Date dailyPlanDate) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, IOException, SOSMissingDataException,
            SOSInvalidDataException, ParseException, JocConfigurationException, SOSHibernateException, DBOpenSessionException {

        LOGGER.debug(String.format("... calculateStartTimes for %s", dailyPlanDate));

        Date actDate = dailyPlanDate;
        Date nextDate = DailyPlanHelper.getNextDay(dailyPlanDate);
        SOSHibernateSession sosHibernateSession = null;

        try {
            Map<String, CalendarCacheItem> calendarCache = new HashMap<String, CalendarCacheItem>();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
            DBItemDailyPlanSubmissions dbItemDailyPlanSubmissionHistory = null;
            OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer();

            for (Schedule schedule : listOfSchedules) {
                if (fromService && !schedule.getPlanOrderAutomatically()) {
                    LOGGER.debug(String.format("... schedule %s  will not be planned automatically", schedule.getPath()));
                } else {
                    String controllerId = OrderInitiatorGlobals.orderInitiatorSettings.getControllerId();

                    if (dbItemDailyPlanSubmissionHistory == null) {
                        dbItemDailyPlanSubmissionHistory = addDailyPlanSubmission(controllerId, dailyPlanDate);
                    }

                    generateNonWorkingDays(dailyPlanDate, schedule, controllerId);

                    for (AssignedCalendars assignedCalendar : schedule.getCalendars()) {

                        if (assignedCalendar.getTimeZone() == null) {
                            assignedCalendar.setTimeZone(UTC);
                        }
                        FrequencyResolver fr = new FrequencyResolver();
                        LOGGER.debug("Generate dates for:" + assignedCalendar.getCalendarName());
                        CalendarCacheItem calendarCacheItem = calendarCache.get(assignedCalendar.getCalendarName() + "#" + schedule.getPath());
                        String actDateAsString = DailyPlanHelper.dateAsString(actDate);
                        String nextDateAsString = DailyPlanHelper.dateAsString(nextDate);
                        String dailyPlanDateAsString = DailyPlanHelper.dateAsString(dailyPlanDate);

                        if (calendarCacheItem == null) {
                            calendarCacheItem = new CalendarCacheItem();
                            calendarCacheItem.timeZone = assignedCalendar.getTimeZone();
                            calendarCacheItem.periodResolver = new PeriodResolver();
                            Calendar calendar = getCalendar(controllerId, assignedCalendar.getCalendarName(), ConfigurationType.WORKINGDAYSCALENDAR);
                            Calendar restrictions = new Calendar();

                            calendar.setFrom(actDateAsString);
                            calendar.setTo(nextDateAsString);
                            String calendarJson = new ObjectMapper().writeValueAsString(calendar);
                            restrictions.setIncludes(assignedCalendar.getIncludes());

                            fr.resolveRestrictions(calendarJson, calendarJson, actDateAsString, nextDateAsString);
                            Set<String> s = fr.getDates().keySet();

                            for (Period p : assignedCalendar.getPeriods()) {
                                Period _period = new Period();
                                _period.setBegin(p.getBegin());
                                _period.setEnd(p.getEnd());
                                _period.setRepeat(p.getRepeat());
                                _period.setSingleStart(p.getSingleStart());
                                _period.setWhenHoliday(p.getWhenHoliday());
                                calendarCacheItem.periodResolver.addStartTimes(_period, dailyPlanDateAsString, assignedCalendar.getTimeZone());
                            }

                            calendarCacheItem.dates = s;
                            calendarCacheItem.calendar = calendar;
                            calendarCache.put(assignedCalendar.getCalendarName() + "#" + schedule.getPath(), calendarCacheItem);
                        }

                        for (String d : calendarCacheItem.dates) {
                            LOGGER.trace("Date: " + d);
                            if (listOfNonWorkingDays != null && listOfNonWorkingDays.get(d) != null) {
                                LOGGER.trace(d + "will be ignored as it is a non working day");
                            } else {
                                Map<Long, Period> listOfStartTimes = calendarCacheItem.periodResolver.getStartTimes(d, dailyPlanDateAsString,calendarCacheItem.timeZone);
                                for (Entry<Long, Period> periodEntry : listOfStartTimes.entrySet()) {

                                    Integer startMode;
                                    if (periodEntry.getValue().getSingleStart() == null) {
                                        startMode = 1;
                                    } else {
                                        startMode = 0;
                                    }

                                    FreshOrder freshOrder = buildFreshOrder(schedule, periodEntry.getKey(), startMode);

                                    PlannedOrder plannedOrder = new PlannedOrder();
                                    plannedOrder.setControllerId(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId());

                                    plannedOrder.setFreshOrder(freshOrder);
                                    plannedOrder.setCalendarId(calendarCacheItem.calendar.getId());
                                    plannedOrder.setPeriod(periodEntry.getValue());
                                    plannedOrder.setSubmissionHistoryId(dbItemDailyPlanSubmissionHistory.getId());
                                    if (!fromService) {
                                        schedule.setSubmitOrderToControllerWhenPlanned(OrderInitiatorGlobals.orderInitiatorSettings.isSubmit());
                                    }
                                    plannedOrder.setSchedule(schedule);
                                    orderListSynchronizer.add(plannedOrder);

                                }
                            }
                        }
                    }
                }
            }
            return orderListSynchronizer;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private DBItemDailyPlanSubmissions addDailyPlanSubmission(String controllerId, Date dateForPlan) throws JocConfigurationException,
            DBConnectionRefusedException, SOSHibernateException, ParseException {

        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
            DBLayerDailyPlanSubmissions dbLayer = new DBLayerDailyPlanSubmissions(sosHibernateSession);
            DBItemDailyPlanSubmissions dbItemDailyPlanSubmissionHistory = new DBItemDailyPlanSubmissions();
            dbItemDailyPlanSubmissionHistory.setControllerId(controllerId);
            dbItemDailyPlanSubmissionHistory.setSubmissionForDate(dateForPlan);
            dbItemDailyPlanSubmissionHistory.setUserAccount(OrderInitiatorGlobals.orderInitiatorSettings.getUserAccount());

            Globals.beginTransaction(sosHibernateSession);
            dbLayer.storePlan(dbItemDailyPlanSubmissionHistory);

            Globals.commit(sosHibernateSession);
            return dbItemDailyPlanSubmissionHistory;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    public AtomicLong getLastActivityStart() {
        return lastActivityStart;
    }

    public AtomicLong getLastActivityEnd() {
        return lastActivityEnd;
    }

}