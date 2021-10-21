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
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.schedule.VariableSet;
import com.sos.joc.Globals;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
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

import js7.data_for_java.controller.JControllerState;

class CalendarCacheItem {

    Calendar calendar;

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

    public Map<PlannedOrderKey, PlannedOrder> generateDailyPlan(String controllerId, JocError jocError, String accessToken, String dailyPlanDate,
            Boolean withSubmit) throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, IOException, ParseException, SOSException, URISyntaxException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, InterruptedException, ExecutionException, TimeoutException {

        orderListSynchronizer = calculateStartTimes(controllerId, DailyPlanHelper.stringAsDate(dailyPlanDate));
        orderListSynchronizer.setJocError(jocError);
        orderListSynchronizer.setAccessToken(accessToken);
        OrderCounter o = DailyPlanHelper.getOrderCount(orderListSynchronizer.getListOfPlannedOrders());

        String s = "";
        if (withSubmit) {
            s = " and submitting ";
        }
        LOGGER.info("Creating " + s + o.getCount() + " orders " + o.cycledOrdersDesc() + " for controller " + controllerId + " day: "
                + dailyPlanDate);

        if (orderListSynchronizer.getListOfPlannedOrders().size() > 0) {
            orderListSynchronizer.addPlannedOrderToControllerAndDB(controllerId, withSubmit);
            EventBus.getInstance().post(new DailyPlanEvent(dailyPlanDate));

        }
        return orderListSynchronizer.getListOfPlannedOrders();
    }

    public void generateDailyPlan(String controllerId, String dailyPlanDate, Boolean withSubmit) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {
        generateDailyPlan(controllerId, null, "", dailyPlanDate, withSubmit);
    }

    public void submitOrders(String controllerId, JocError jocError, String accessToken, List<DBItemDailyPlanOrder> listOfPlannedOrders)
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
            OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer(currentstate, orderInitiatorSettings);

            orderListSynchronizer.setAccessToken(accessToken);
            orderListSynchronizer.setJocError(jocError);

            for (DBItemDailyPlanOrder dbItemDailyPlanOrders : listOfPlannedOrders) {

                PlannedOrder p = new PlannedOrder();
                Schedule schedule = new Schedule();
                schedule.setPath(dbItemDailyPlanOrders.getSchedulePath());
                schedule.setWorkflowPath(dbItemDailyPlanOrders.getWorkflowPath());
                schedule.setWorkflowName(dbItemDailyPlanOrders.getWorkflowName());
                schedule.setSubmitOrderToControllerWhenPlanned(true);
                p.setControllerId(dbItemDailyPlanOrders.getControllerId());

                FilterOrderVariables filterOrderVariables = new FilterOrderVariables();

                filterOrderVariables.setPlannedOrderId(dbItemDailyPlanOrders.getId());
                VariableSet variableSet = new VariableSet();
                schedule.setVariableSets(new ArrayList<VariableSet>());
                Variables variables = new Variables();

                List<DBItemDailyPlanVariable> listOfOrderVariables = dbLayerOrderVariables.getOrderVariables(filterOrderVariables, 0);
                if (listOfOrderVariables != null && listOfOrderVariables.size() > 0 && listOfOrderVariables.get(0).getVariableValue() != null) {
                    variables = Globals.objectMapper.readValue(listOfOrderVariables.get(0).getVariableValue(), Variables.class);
                }

                variableSet.setVariables(variables);
                schedule.getVariableSets().add(variableSet);

                FreshOrder freshOrder = buildFreshOrder(schedule, variableSet, dbItemDailyPlanOrders.getPlannedStart().getTime(),
                        dbItemDailyPlanOrders.getStartMode(), orderInitiatorSettings.getTimeZone(), orderInitiatorSettings.getPeriodBegin());
                freshOrder.setId(dbItemDailyPlanOrders.getOrderId());
                p.setSchedule(schedule);
                p.setFreshOrder(freshOrder);
                p.setCalendarId(dbItemDailyPlanOrders.getCalendarId());
                p.setStoredInDb(true);

                orderListSynchronizer.add(controllerId, p);
            }
            // disconnect here to avoid nested sessions
            Globals.disconnect(sosHibernateSession);
            sosHibernateSession = null;

            if (orderListSynchronizer.getListOfPlannedOrders().size() > 0) {
                orderListSynchronizer.submitOrdersToController(controllerId);
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void submitOrders(String controllerId, List<DBItemDailyPlanOrder> listOfPlannedOrders) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {
        submitOrders(controllerId, null, "", listOfPlannedOrders);
    }

    private List<DBItemDailyPlanSubmission> getSubmissionsForDate(java.util.Calendar calendar, String controllerId) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(DAILYPLAN_RUNNER);

            DBLayerDailyPlanSubmissions dbLayerDailyPlan = new DBLayerDailyPlanSubmissions(sosHibernateSession);
            FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
            filter.setControllerId(controllerId);

            filter.setDateFor(calendar.getTime());
            List<DBItemDailyPlanSubmission> listOfDailyPlanSubmissions = dbLayerDailyPlan.getDailyPlanSubmissions(filter, 0);
            return (listOfDailyPlanSubmissions);

        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void createPlan_new(java.util.Calendar calendar) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
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
                    String dailyPlanDate = DailyPlanHelper.dateAsString(dailyPlanCalendar.getTime(), orderInitiatorSettings.getTimeZone());
                    List<DBItemDailyPlanSubmission> l = getSubmissionsForDate(dailyPlanCalendar, controllerConfiguration.getCurrent().getId());
                    if ((l.size() == 0)) {
                        if (logDailyPlan) {
                            LOGGER.info("Creating daily plan for controller: " + controllerConfiguration.getCurrent().getId() + " from "
                                    + dailyPlanDate + " for " + orderInitiatorSettings.getDayAheadPlan() + " days ahead");

                            LOGGER.info("Submitting orders for controller " + controllerConfiguration.getCurrent().getId() + " for "
                                    + orderInitiatorSettings.getDayAheadSubmit() + " days ahead");

                            logDailyPlan = false;
                        }
                        if (day < orderInitiatorSettings.getDayAheadSubmit()) {
                            generateDailyPlan(controllerConfiguration.getCurrent().getId(), dailyPlanDate, true);
                        } else {
                            generateDailyPlan(controllerConfiguration.getCurrent().getId(), dailyPlanDate, false);
                        }
                    } else {
                        LOGGER.info("No orders will be created for " + dailyPlanDate + " as a submission has been found");
                    }

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

    // public void createPlan_old_____first_create_then_submit(java.util.Calendar calendar)
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
                    String dailyPlanDate = DailyPlanHelper.dateAsString(dailyPlanCalendar.getTime(), orderInitiatorSettings.getTimeZone());
                    List<DBItemDailyPlanSubmission> l = getSubmissionsForDate(dailyPlanCalendar, controllerConfiguration.getCurrent().getId());
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
            List<DBItemDailyPlanSubmission> listOfSubmissions = getSubmissionsForDate(calendar, controllerId);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("submitDaysAhead");
            for (DBItemDailyPlanSubmission dbItemDailyPlanSubmissions : listOfSubmissions) {
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.addSubmissionHistoryId(dbItemDailyPlanSubmissions.getId());
                filter.setSubmitted(false);
                DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
                List<DBItemDailyPlanOrder> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);

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

        java.util.Calendar calendar = DailyPlanHelper.getDailyplanCalendar(orderInitiatorSettings.getPeriodBegin(), orderInitiatorSettings
                .getTimeZone());
        calendar.add(java.util.Calendar.DATE, 1);

        java.util.Calendar now = java.util.Calendar.getInstance(TimeZone.getTimeZone(orderInitiatorSettings.getTimeZone()));

        if (startCalendar == null) {

            if (!"".equals(orderInitiatorSettings.getDailyPlanStartTime())) {
                startCalendar = DailyPlanHelper.getDailyplanCalendar(orderInitiatorSettings.getDailyPlanStartTime(), orderInitiatorSettings
                        .getTimeZone());
            } else {
                startCalendar = DailyPlanHelper.getDailyplanCalendar(orderInitiatorSettings.getPeriodBegin(), orderInitiatorSettings.getTimeZone());
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
        LOGGER.debug("... readSchedules " + scheduleSource.fromSource());
        listOfSchedules = scheduleSource.fillListOfSchedules();
    }

    public void addSchedule(Schedule schedule) {
        LOGGER.debug("... addSchedule " + schedule.getPath());
        if (listOfSchedules == null) {
            listOfSchedules = new ArrayList<Schedule>();
        }
        listOfSchedules.add(schedule);
    }

    private Calendar getCalendar(String controllerId, String calendarName, ConfigurationType type) throws DBMissingDataException, JsonParseException,
            JsonMappingException, IOException, DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException,
            DBOpenSessionException, SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
            DBLayerInventoryReleasedConfigurations dbLayer = new DBLayerInventoryReleasedConfigurations(sosHibernateSession);
            FilterInventoryReleasedConfigurations filter = new FilterInventoryReleasedConfigurations();
            filter.setName(calendarName);
            filter.setType(type);

            DBItemInventoryReleasedConfiguration config = dbLayer.getSingleInventoryReleasedConfigurations(filter);
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

    private FreshOrder buildFreshOrder(Schedule schedule, VariableSet variableSet, Long startTime, Integer startMode, String timeZone,
            String periodBegin) {
        FreshOrder freshOrder = new FreshOrder();
        freshOrder.setId(DailyPlanHelper.buildOrderId(schedule, variableSet, startTime, startMode, timeZone, periodBegin));
        freshOrder.setScheduledFor(startTime);
        freshOrder.setArguments(variableSet.getVariables());
        freshOrder.setWorkflowPath(schedule.getWorkflowName());
        return freshOrder;
    }

    private void generateNonWorkingDays(Date dailyPlanDate, Schedule o, String controllerId) throws SOSMissingDataException, SOSInvalidDataException,
            JsonParseException, JsonMappingException, DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException, IOException,
            ParseException, JocConfigurationException, DBOpenSessionException, SOSHibernateException {

        Date nextDate = DailyPlanHelper.getNextDay(dailyPlanDate, orderInitiatorSettings);

        if (o.getNonWorkingDayCalendars() != null) {
            FrequencyResolver fr = new FrequencyResolver();
            for (AssignedNonWorkingDayCalendars assignedNonWorkingCalendars : o.getNonWorkingDayCalendars()) {
                LOGGER.debug("Generate non working dates for:" + assignedNonWorkingCalendars.getCalendarPath());
                listOfNonWorkingDays = new HashMap<String, String>();
                Calendar calendar = getCalendar(controllerId, assignedNonWorkingCalendars.getCalendarName(),
                        ConfigurationType.NONWORKINGDAYSCALENDAR);
                CalendarDatesFilter calendarFilter = new CalendarDatesFilter();

                calendarFilter.setDateFrom(DailyPlanHelper.dateAsString(dailyPlanDate, orderInitiatorSettings.getTimeZone()));
                calendarFilter.setDateTo(DailyPlanHelper.dateAsString(nextDate, orderInitiatorSettings.getTimeZone()));
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
        Date nextDate = DailyPlanHelper.getNextDay(dailyPlanDate, orderInitiatorSettings);
        // SOSHibernateSession sosHibernateSession = null;

        try {
            Map<String, CalendarCacheItem> calendarCache = new HashMap<String, CalendarCacheItem>();

            Set<String> scheduleAdded = new HashSet<String>();
            // not used
            // sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
            DBItemDailyPlanSubmission dbItemDailyPlanSubmissionHistory = null;
            if (currentstate == null) {
                currentstate = getCurrentState(controllerId);
            }
            OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer(currentstate, orderInitiatorSettings);

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
                        String actDateAsString = DailyPlanHelper.dateAsString(actDate, orderInitiatorSettings.getTimeZone());
                        String nextDateAsString = DailyPlanHelper.dateAsString(nextDate, orderInitiatorSettings.getTimeZone());
                        String dailyPlanDateAsString = DailyPlanHelper.dateAsString(dailyPlanDate, orderInitiatorSettings.getTimeZone());

                        if (calendarCacheItem == null) {
                            calendarCacheItem = new CalendarCacheItem();
                            Calendar calendar = getCalendar(controllerId, assignedCalendar.getCalendarName(), ConfigurationType.WORKINGDAYSCALENDAR);
                            calendarCacheItem.calendar = calendar;
                            calendarCache.put(assignedCalendar.getCalendarName() + "#" + schedule.getPath(), calendarCacheItem);
                        } else {
                            System.out.println("cache");
                        }
                        PeriodResolver periodResolver = new PeriodResolver(orderInitiatorSettings);
                        Calendar restrictions = new Calendar();

                        calendarCacheItem.calendar.setFrom(actDateAsString);
                        calendarCacheItem.calendar.setTo(nextDateAsString);
                        String calendarJson = Globals.objectMapper.writeValueAsString(calendarCacheItem.calendar);
                        restrictions.setIncludes(assignedCalendar.getIncludes());
                        String restrictionJson = Globals.objectMapper.writeValueAsString(restrictions);

                        List<String> dates = fr.resolveRestrictions(calendarJson, restrictionJson, actDateAsString, nextDateAsString).getDates();

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
                            LOGGER.trace("Date: " + d);
                            if (listOfNonWorkingDays != null && listOfNonWorkingDays.get(d) != null) {
                                LOGGER.trace(d + "will be ignored as it is a non working day");
                            } else {
                                Map<Long, Period> listOfStartTimes = periodResolver.getStartTimes(d, dailyPlanDateAsString, assignedCalendar
                                        .getTimeZone());
                                for (Entry<Long, Period> periodEntry : listOfStartTimes.entrySet()) {

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
                                        FreshOrder freshOrder = buildFreshOrder(schedule, variableSet, periodEntry.getKey(), startMode,
                                                this.orderInitiatorSettings.getTimeZone(), this.orderInitiatorSettings.getPeriodBegin());

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
                                        if (variableSet.getOrderName() != null && !variableSet.getOrderName().isEmpty()) {
                                            plannedOrder.setOrderName(variableSet.getOrderName());
                                        } else {
                                            plannedOrder.setOrderName(Paths.get(schedule.getPath()).getFileName().toString());
                                        }
                                        if (orderListSynchronizer.add(controllerId, plannedOrder)) {
                                            scheduleAdded.add(schedule.getPath());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return orderListSynchronizer;
        } finally {
            // Globals.disconnect(sosHibernateSession);
        }
    }

    private DBItemDailyPlanSubmission addDailyPlanSubmission(String controllerId, Date dateForPlan) throws JocConfigurationException,
            DBConnectionRefusedException, SOSHibernateException, ParseException {

        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
            DBLayerDailyPlanSubmissions dbLayerDailyPlanSubmissions = new DBLayerDailyPlanSubmissions(sosHibernateSession);
            DBItemDailyPlanSubmission dbItemDailyPlanSubmissionHistory = new DBItemDailyPlanSubmission();
            dbItemDailyPlanSubmissionHistory.setControllerId(controllerId);

            dbItemDailyPlanSubmissionHistory.setSubmissionForDate(dateForPlan);
            dbItemDailyPlanSubmissionHistory.setUserAccount(orderInitiatorSettings.getUserAccount());

            Globals.beginTransaction(sosHibernateSession);
            dbLayerDailyPlanSubmissions.storeSubmission(dbItemDailyPlanSubmissionHistory, orderInitiatorSettings.getSubmissionTime());

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