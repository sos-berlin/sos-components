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
import java.util.concurrent.TimeUnit;
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
import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.Schedule;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanSubmissions;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
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
import com.sos.js7.order.initiator.db.DBLayerInventoryConfigurations;
import com.sos.js7.order.initiator.db.DBLayerOrderVariables;
import com.sos.js7.order.initiator.db.FilterDailyPlanSubmissions;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterInventoryConfigurations;
import com.sos.js7.order.initiator.db.FilterOrderVariables;

import js7.data_for_java.controller.JControllerState;

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
    private boolean firstStart = true;
    private Set<String> createdPlans;
    private List<ControllerConfiguration> controllers;
    private AtomicLong lastActivityStart = new AtomicLong(0);
    private AtomicLong lastActivityEnd = new AtomicLong(0);
    private JControllerState currentstate;
    private java.util.Calendar startCalendar;

    public List<Schedule> getListOfSchedules() {
        return listOfSchedules;
    }

    private Map<String, String> listOfNonWorkingDays;
    private SOSHibernateFactory sosHibernateFactory;
    private OrderListSynchronizer orderListSynchronizer;
    private OrderInitiatorSettings orderInitiatorSettings;

    public OrderInitiatorRunner(OrderInitiatorSettings orderInitiatorSettings, boolean fromService) {
        this.orderInitiatorSettings = orderInitiatorSettings;
        this.fromService = fromService;
    }

    public OrderInitiatorRunner(List<ControllerConfiguration> controllers, OrderInitiatorSettings orderInitiatorSettings, boolean fromService) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        this.orderInitiatorSettings = orderInitiatorSettings;
        this.controllers = controllers;
        this.fromService = fromService;
    }

    public Map<PlannedOrderKey, PlannedOrder> generateDailyPlan(String controllerId, JocError jocError, String accessToken, String dailyPlanDate, Boolean withSubmit)
            throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, IOException, ParseException, SOSException, URISyntaxException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, InterruptedException, ExecutionException, TimeoutException {

        orderListSynchronizer = calculateStartTimes(controllerId, DailyPlanHelper.stringAsDate(dailyPlanDate));
        orderListSynchronizer.setJocError(jocError);
        orderListSynchronizer.setAccessToken(accessToken);
        OrderCounter o = DailyPlanHelper.getOrderCount(orderListSynchronizer.getListOfPlannedOrders());

        LOGGER.info("Creating " + o.getCount() + " orders " + o.cycledOrdersDesc() + " for controller " + controllerId + " day: " + dailyPlanDate);

        if (orderListSynchronizer.getListOfPlannedOrders().size() > 0) {
            orderListSynchronizer.addPlannedOrderToControllerAndDB(controllerId, withSubmit);
        }
         return orderListSynchronizer.getListOfPlannedOrders();
    }

    public void generateDailyPlan(String controllerId, String dailyPlanDate, Boolean withSubmit) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {
        generateDailyPlan(controllerId, null, "", dailyPlanDate, withSubmit);
    }

    public void submitOrders(String controllerId, JocError jocError, String accessToken, List<DBItemDailyPlanOrders> listOfPlannedOrders)
            throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, IOException, ParseException, SOSException, URISyntaxException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, InterruptedException, ExecutionException, TimeoutException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("submitOrders");
            DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);

            if (currentstate == null) {
                currentstate = getCurrentState(controllerId);
            }
            OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer(currentstate,orderInitiatorSettings);

            orderListSynchronizer.setAccessToken(accessToken);
            orderListSynchronizer.setJocError(jocError);

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

                orderListSynchronizer.add(controllerId, p);
            }
            if (orderListSynchronizer.getListOfPlannedOrders().size() > 0) {
                orderListSynchronizer.submitOrdersToController(controllerId);
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void submitOrders(String controllerId, List<DBItemDailyPlanOrders> listOfPlannedOrders) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {
        submitOrders(controllerId, null, "", listOfPlannedOrders);
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

    public void createPlan(java.util.Calendar calendar) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
            ParseException, SOSException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        try {
            lastActivityStart.set(new Date().getTime());

            java.util.Calendar savCalendar = java.util.Calendar.getInstance();
            savCalendar.setTime(calendar.getTime());

            for (ControllerConfiguration controllerConfiguration : controllers) {
                java.util.Calendar dailyPlanCalendar = java.util.Calendar.getInstance();
                dailyPlanCalendar.setTime(savCalendar.getTime());

                orderInitiatorSettings.setDailyPlanDate(dailyPlanCalendar.getTime());
                ScheduleSource scheduleSource = new ScheduleSourceDB(controllerConfiguration.getCurrent().getId());
                readSchedules(scheduleSource);
                boolean logDailyPlan = true;

                currentstate = getCurrentState(controllerConfiguration.getCurrent().getId());

                for (int day = 0; day < orderInitiatorSettings.getDayAheadPlan(); day++) {
                    String dailyPlanDate = DailyPlanHelper.dateAsString(dailyPlanCalendar.getTime());
                    List<DBItemDailyPlanSubmissions> l = getSubmissionsForDate(dailyPlanCalendar, controllerConfiguration.getCurrent().getId());
                    if ((l.size() == 0)) {
                        if (logDailyPlan) {
                            LOGGER.info("Creating daily plan for controller: " + controllerConfiguration.getCurrent().getId() + " from "
                                    + dailyPlanDate + " for " + orderInitiatorSettings.getDayAheadPlan() + " days ahead");

                            logDailyPlan = false;
                        }
                        generateDailyPlan(controllerConfiguration.getCurrent().getId(), dailyPlanDate, false);
                    } else {
                        LOGGER.info("No orders will be created for " + dailyPlanDate + " as a submission has been found");
                    }

                    dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                    orderInitiatorSettings.setDailyPlanDate(dailyPlanCalendar.getTime());
                }

                dailyPlanCalendar.setTime(savCalendar.getTime());
                orderInitiatorSettings.setDailyPlanDate(dailyPlanCalendar.getTime());
                logDailyPlan = true;

                LOGGER.info("Submitting orders for controller " + controllerConfiguration.getCurrent().getId() + " for " + orderInitiatorSettings
                        .getDayAheadSubmit() + " days ahead");

                for (int day = 0; day < orderInitiatorSettings.getDayAheadSubmit(); day++) {

                    submitDaysAhead(dailyPlanCalendar, controllerConfiguration.getCurrent().getId());
                    dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                    orderInitiatorSettings.setDailyPlanDate(dailyPlanCalendar.getTime());
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
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            List<DBItemDailyPlanSubmissions> listOfSubmissions = getSubmissionsForDate(calendar, controllerId);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("submitDaysAhead");
            for (DBItemDailyPlanSubmissions dbItemDailyPlanSubmissions : listOfSubmissions) {
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.addSubmissionHistoryId(dbItemDailyPlanSubmissions.getId());
                filter.setSubmitted(false);
                DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
                List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);

                OrderCounter o = DailyPlanHelper.getOrderCount(listOfPlannedOrders);

                LOGGER.info("Submitting " + o.getCount() + " orders " + o.cycledOrdersDesc() + " for controller " + controllerId + " day: "
                        + DailyPlanHelper.getDayOfYear(calendar));
                submitOrders(controllerId, listOfPlannedOrders);
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void run() {
        if (createdPlans == null) {
            createdPlans = new HashSet<String>();
        }
        AJocClusterService.setLogger("dailyplan");
        boolean generateFromManuelStart = false;
        if (firstStart && StartupMode.manual.equals(orderInitiatorSettings.getStartMode())) {
            firstStart = false;
            LOGGER.debug("generateFromManuelStart: true");
            generateFromManuelStart = true;
        }

        java.util.Calendar calendar = DailyPlanHelper.getDailyplanCalendar(orderInitiatorSettings.getPeriodBegin(),orderInitiatorSettings.getTimeZone());
        calendar.add(java.util.Calendar.DATE, 1);

        java.util.Calendar now = java.util.Calendar.getInstance(TimeZone.getTimeZone(orderInitiatorSettings.getTimeZone()));

        if (startCalendar == null) {

            if (!"".equals(orderInitiatorSettings.getDailyPlanStartTime())) {
                startCalendar = DailyPlanHelper.getDailyplanCalendar(orderInitiatorSettings.getDailyPlanStartTime(),orderInitiatorSettings.getTimeZone());
            } else {
                startCalendar = DailyPlanHelper.getDailyplanCalendar(orderInitiatorSettings.getPeriodBegin(),orderInitiatorSettings.getTimeZone());
                startCalendar.add(java.util.Calendar.DATE, 1);
                startCalendar.add(java.util.Calendar.MINUTE, -30);
            }
            if (startCalendar.before(now)) {
                startCalendar.add(java.util.Calendar.DATE, 1);
            }
        }

        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        java.util.Calendar dailyPlanCalendar = java.util.Calendar.getInstance(timeZone);

        if (!createdPlans.contains(DailyPlanHelper.getDayOfYear(calendar)) && (generateFromManuelStart || (now.getTimeInMillis() - startCalendar
                .getTimeInMillis()) > 0)) {

            startCalendar = null;
            createdPlans.add(DailyPlanHelper.getDayOfYear(calendar));
            try {
                orderInitiatorSettings.setSubmissionTime(new Date());
                dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                dailyPlanCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                dailyPlanCalendar.set(java.util.Calendar.MINUTE, 0);
                dailyPlanCalendar.set(java.util.Calendar.SECOND, 0);
                dailyPlanCalendar.set(java.util.Calendar.MILLISECOND, 0);
                dailyPlanCalendar.set(java.util.Calendar.MINUTE, 0);
                LOGGER.info("Creating daily plan starting with " + DailyPlanHelper.getDayOfYear(calendar));
                createPlan(dailyPlanCalendar);
            } catch (ControllerConnectionResetException | ControllerConnectionRefusedException | ParseException | SOSException | URISyntaxException
                    | InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        AJocClusterService.clearLogger();
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

        Date nextDate = DailyPlanHelper.getNextDay(dailyPlanDate, orderInitiatorSettings);

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

    private static JControllerState getCurrentState(String controllerId) {
        JControllerState currentstate = null;
        try {
            int i = 0;
            do {
                try {
                    currentstate = Proxy.of(controllerId).currentState();
                } catch (ControllerConnectionRefusedException e) {
                    i = i + 1;
                    TimeUnit.SECONDS.sleep(1);
                }
            } while ((currentstate == null) && (i < 60));

        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return currentstate;
    }

    private OrderListSynchronizer calculateStartTimes(String controllerId, Date dailyPlanDate) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, IOException, SOSMissingDataException,
            SOSInvalidDataException, ParseException, JocConfigurationException, SOSHibernateException, DBOpenSessionException {

        LOGGER.debug(String.format("... calculateStartTimes for %s", dailyPlanDate));

        Date actDate = dailyPlanDate;
        Date nextDate = DailyPlanHelper.getNextDay(dailyPlanDate,orderInitiatorSettings);
        SOSHibernateSession sosHibernateSession = null;

        try {
            Map<String, CalendarCacheItem> calendarCache = new HashMap<String, CalendarCacheItem>();

            Set<String> scheduleAdded = new HashSet<String>();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
            DBItemDailyPlanSubmissions dbItemDailyPlanSubmissionHistory = null;
            if (currentstate == null) {
                currentstate = getCurrentState(controllerId);
            }
            OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer(currentstate,orderInitiatorSettings);

            for (Schedule schedule : listOfSchedules) {
                if (fromService && !schedule.getPlanOrderAutomatically()) {
                    LOGGER.debug(String.format("... schedule %s  will not be planned automatically", schedule.getPath()));
                } else {

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
                            calendarCacheItem.periodResolver = new PeriodResolver(orderInitiatorSettings);
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
                                Map<Long, Period> listOfStartTimes = calendarCacheItem.periodResolver.getStartTimes(d, dailyPlanDateAsString,
                                        calendarCacheItem.timeZone);
                                for (Entry<Long, Period> periodEntry : listOfStartTimes.entrySet()) {

                                    Integer startMode;
                                    if (periodEntry.getValue().getSingleStart() == null) {
                                        startMode = 1;
                                    } else {
                                        startMode = 0;
                                    }

                                    FreshOrder freshOrder = buildFreshOrder(schedule, periodEntry.getKey(), startMode);

                                    PlannedOrder plannedOrder = new PlannedOrder();
                                    plannedOrder.setControllerId(controllerId);

                                    plannedOrder.setFreshOrder(freshOrder);
                                    plannedOrder.setWorkflowPath(schedule.getWorkflowPath());
                                    plannedOrder.setCalendarId(calendarCacheItem.calendar.getId());
                                    plannedOrder.setPeriod(periodEntry.getValue());
                                    plannedOrder.setSubmissionHistoryId(dbItemDailyPlanSubmissionHistory.getId());
                                    if (!fromService) {
                                        schedule.setSubmitOrderToControllerWhenPlanned(orderInitiatorSettings.isSubmit());
                                    }
                                    plannedOrder.setSchedule(schedule);
                                    if (orderListSynchronizer.add(controllerId, plannedOrder)) {
                                        scheduleAdded.add(schedule.getPath());
                                    }

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
            DBLayerDailyPlanSubmissions dbLayerDailyPlanSubmissions = new DBLayerDailyPlanSubmissions(sosHibernateSession);
            DBItemDailyPlanSubmissions dbItemDailyPlanSubmissionHistory = new DBItemDailyPlanSubmissions();
            dbItemDailyPlanSubmissionHistory.setControllerId(controllerId);
            dbItemDailyPlanSubmissionHistory.setSubmissionForDate(dateForPlan);
            dbItemDailyPlanSubmissionHistory.setUserAccount(orderInitiatorSettings.getUserAccount());

            Globals.beginTransaction(sosHibernateSession);
            dbLayerDailyPlanSubmissions.storeSubmission(dbItemDailyPlanSubmissionHistory,orderInitiatorSettings.getSubmissionTime());

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