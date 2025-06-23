package com.sos.joc.dailyplan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.board.PlanSchemas;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSchedule;
import com.sos.joc.dailyplan.common.DailyPlanScheduleWorkflow;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.OrderCounter;
import com.sos.joc.dailyplan.common.PeriodResolver;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.common.PlannedOrderKey;
import com.sos.joc.dailyplan.db.DBBeanReleasedSchedule2DeployedWorkflow;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanSubmissions;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.DBLayerOrderVariables;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
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
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;

import js7.data.plan.PlanId;
import js7.data.plan.PlanSchemaId;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.plan.JPlan;
import js7.data_for_java.plan.JPlanSchemaState;
import js7.data_for_java.plan.JPlanStatus;
import js7.proxy.javaapi.JControllerProxy;

public class DailyPlanRunner extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanRunner.class);
    private static final String IDENTIFIER = DailyPlanRunner.class.getSimpleName();

    private AtomicLong lastActivityStart = new AtomicLong(0);
    private AtomicLong lastActivityEnd = new AtomicLong(0);
    private DailyPlanSettings settings;
    private java.util.Calendar startCalendar;
    private Map<String, Long> durations = null;

    private Map<String, Calendar> calculateStartTimesCalendars = new HashMap<String, Calendar>();
    private Map<String, Calendar> calculateStartTimesNonWorkingCalendars = new HashMap<String, Calendar>();

    public DailyPlanRunner(DailyPlanSettings settings) {
        this.settings = settings;
    }

    // service
    // TODO currently runs every 60? seconds -the extends TimerTask should be replaced with a schedule executer (see cleanup service)
    public void run() {
        JocClusterServiceLogger.setLogger("dailyplan");

        boolean createPlan = false;
        boolean isAutomaticStart = isAutomaticStart();
        try {
            // TODO createdPlans static? because several instances ...
            if (isAutomaticStart || settings.isRunNow()) {
                LOGGER.info(String.format("[%s][start]%s", settings.getStartMode(), settings.toString()));

                clear();
                startCalendar = null;
                try {
                    createPlan = true;
                    lastActivityStart.set(new Date().getTime());

                    settings.setSubmissionTime(new Date());
                    createPlan(settings.getStartMode(), settings.getControllers(), DailyPlanHelper.getNextDayCalendar());
                } catch (ControllerConnectionResetException | ControllerConnectionRefusedException | ParseException | SOSException
                        | URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                if (LOGGER.isTraceEnabled() && startCalendar != null) {
                    LOGGER.trace(String.format("wait for start at %s (%s)...", SOSDate.tryGetDateWithTimeZoneAsString(startCalendar.getTime(),
                            settings.getTimeZone()), settings.getTimeZone()));
                }
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (createPlan) {
                clear();

                try {
                    recreateProjections(settings);
                } catch (Throwable ex) {
                    LOGGER.error(ex.toString(), ex);
                } finally {
                    lastActivityEnd.set(new Date().getTime());
                }
                settings.setStartMode(StartupMode.automatic);
                startCalendar = DailyPlanHelper.getStartTimeCalendar(settings);
                LOGGER.info(DailyPlanHelper.getNextStartMsg(settings, startCalendar));
            }
        }
    }

    private void clear() {
        calculateStartTimesCalendars.clear();
        calculateStartTimesNonWorkingCalendars.clear();
    }

    private boolean isAutomaticStart() {
        java.util.Calendar now = java.util.Calendar.getInstance(TimeZone.getTimeZone(settings.getTimeZone()));
        if (startCalendar == null) {
            startCalendar = DailyPlanHelper.getStartTimeCalendar(settings);
        }
        return (now.getTimeInMillis() - startCalendar.getTimeInMillis()) > 0;
    }

    public static void recreateProjections(DailyPlanSettings settings) throws Exception {
        String add = DailyPlanHelper.getCallerForLog(settings);
        LOGGER.info(String.format("[%s]%s[recreateProjections]creating for %s months ahead", settings.getStartMode(), add, settings
                .getProjectionsMonthAhead()));

        new DailyPlanProjections().process(settings);
    }

    /* service */
    private void createPlan(StartupMode startupMode, List<ControllerConfiguration> controllers, java.util.Calendar calendar)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {

        String method = "createPlan";
        try {
            durations = null;

            LOGGER.info(String.format("[%s][%s]creating from %s for %s days ahead, submitting for %s days ahead", startupMode, method, SOSDate
                    .getDateAsString(calendar), settings.getDaysAheadPlan(), settings.getDaysAheadSubmit()));

            java.util.Calendar savCalendar = java.util.Calendar.getInstance();
            savCalendar.setTime(calendar.getTime());

            int ageOfPlansToBeClosedAutomatically = settings.getAgeOfPlansToBeClosedAutomatically();

            for (ControllerConfiguration conf : controllers) {
                String controllerId = conf.getCurrent().getId();

                Collection<DailyPlanSchedule> controllerSchedules = convert(getReleasedSchedule2DeployedWorkflow(controllerId), true);

                java.util.Calendar dailyPlanCalendar = java.util.Calendar.getInstance();
                dailyPlanCalendar.setTime(savCalendar.getTime());
                settings.setDailyPlanDate(dailyPlanCalendar.getTime());

                if (controllerSchedules.size() > 0) {
                    LOGGER.info(String.format("[%s][%s][%s][Plan Order automatically]found %s schedules", startupMode, method, controllerId,
                            controllerSchedules.size()));

                    for (int day = 0; day < settings.getDaysAheadPlan(); day++) {
                        String dailyPlanDate = SOSDate.getDateWithTimeZoneAsString(dailyPlanCalendar.getTime(), settings.getTimeZone());
                        List<DBItemDailyPlanSubmission> l = getSubmissionsForDate(controllerId, dailyPlanCalendar);
                        if ((l.size() == 0)) {
                            generateDailyPlan(startupMode, controllerId, controllerSchedules, dailyPlanDate, false, null, "");
                        } else {
                            List<String> copy = l.stream().map(e -> {
                                String d;
                                try {
                                    d = SOSDate.getDateTimeAsString(e.getCreated());
                                } catch (SOSInvalidDataException e1) {
                                    d = null;
                                }
                                return d == null ? "" : d;
                            }).collect(Collectors.toList());
                            LOGGER.info(String.format("[%s][%s][%s][%s][skip][submission(s) found][created]%s", startupMode, method, controllerId,
                                    dailyPlanDate, String.join(",", copy)));
                        }

                        dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                        settings.setDailyPlanDate(dailyPlanCalendar.getTime());
                    }
                } else {
                    LOGGER.info(String.format("[%s][%s][%s][skip][Plan Order automatically]no schedules found", startupMode, method, controllerId));
                }

                dailyPlanCalendar.setTime(savCalendar.getTime());
                settings.setDailyPlanDate(dailyPlanCalendar.getTime());

                for (int day = 0; day < settings.getDaysAheadSubmit(); day++) {
                    submitDaysAhead(startupMode, controllerId, dailyPlanCalendar);
                    dailyPlanCalendar.add(java.util.Calendar.DATE, 1);
                    settings.setDailyPlanDate(dailyPlanCalendar.getTime());
                }

                closeOldPlans(startupMode, controllerId, ageOfPlansToBeClosedAutomatically);
                setUnknownPlansAreOpenFromDate(startupMode, controllerId);

            }

            if (ageOfPlansToBeClosedAutomatically == 0) {
                LOGGER.info(String.format("[%s][closePlans][skip] because of daily plan settings", startupMode));
            }
        } catch (SOSHibernateException | IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException
                | JocConfigurationException | DBOpenSessionException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /* service (createPlan) & DailyPlanOrdersGenerateImpl */
    public Map<PlannedOrderKey, PlannedOrder> generateDailyPlan(StartupMode startupMode, String controllerId,
            Collection<DailyPlanSchedule> dailyPlanSchedules, String dailyPlanDate, Boolean withSubmit, JocError jocError, String accessToken)
            throws JsonParseException, JsonMappingException, DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException,
            JocConfigurationException, DBOpenSessionException, ControllerConnectionResetException, ControllerConnectionRefusedException,
            SOSInvalidDataException, SOSMissingDataException, SOSHibernateException, ParseException, IOException, ExecutionException {
        return generateDailyPlan(startupMode, controllerId, dailyPlanSchedules, dailyPlanDate, null, withSubmit, jocError, accessToken, false);
    }

    /* service (createPlan) & DailyPlanOrdersGenerateImpl */
    public Map<PlannedOrderKey, PlannedOrder> generateDailyPlan(StartupMode startupMode, String controllerId,
            Collection<DailyPlanSchedule> dailyPlanSchedules, String dailyPlanDate, Boolean withSubmit, JocError jocError, String accessToken,
            boolean includeLate) throws JsonParseException, JsonMappingException, DBMissingDataException, DBConnectionRefusedException,
            DBInvalidDataException, JocConfigurationException, DBOpenSessionException, ControllerConnectionResetException,
            ControllerConnectionRefusedException, SOSInvalidDataException, SOSMissingDataException, SOSHibernateException, ParseException,
            IOException, ExecutionException {
        return generateDailyPlan(startupMode, controllerId, dailyPlanSchedules, dailyPlanDate, null, withSubmit, jocError, accessToken, includeLate);
    }

    /* DailyPlanOrdersGenerateImpl */
    public Map<PlannedOrderKey, PlannedOrder> generateDailyPlan(StartupMode startupMode, String controllerId,
            Collection<DailyPlanSchedule> dailyPlanSchedules, String dailyPlanDate, DBItemDailyPlanSubmission submission, Boolean withSubmit,
            JocError jocError, String accessToken, boolean includeLate) throws JsonParseException, JsonMappingException, DBMissingDataException,
            DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException, DBOpenSessionException, SOSInvalidDataException,
            SOSMissingDataException, SOSHibernateException, ParseException, IOException, ControllerConnectionResetException,
            ControllerConnectionRefusedException, ExecutionException {

        String operation = withSubmit ? "creating/submitting" : "creating";
        String caller = DailyPlanHelper.getCallerForLog(settings);
        String lp = String.format("[%s]%s[%s][%s][%s]", startupMode, caller, operation, controllerId, dailyPlanDate);
        if (dailyPlanSchedules == null || dailyPlanSchedules.size() == 0) {
            LOGGER.info(String.format("%s[skip]found 0 schedules", lp));
            // TODO initialize OrderListSynchronizer before calculateStartTimes and return synchronizer.getPlannedOrders()
            return new TreeMap<PlannedOrderKey, PlannedOrder>();
        }

        OrderListSynchronizer synchronizer = calculateStartTimes(startupMode, controllerId, dailyPlanSchedules, dailyPlanDate, submission,
                includeLate);
        synchronizer.setJocError(jocError);
        synchronizer.setAccessToken(accessToken);
        OrderCounter c = DailyPlanHelper.getOrderCount(synchronizer.getPlannedOrders());

        if (synchronizer.getPlannedOrders().size() > 0) {
            Long submissionId = null;
            String submissionCreated = "unknown";
            if (synchronizer.getSubmission() != null) {
                submissionId = synchronizer.getSubmission().getId();
                submissionCreated = SOSDate.getDateTimeAsString(synchronizer.getSubmission().getCreated());
            }

            LOGGER.info(String.format("%s[calculated][%s][submission created=%s, id=%s]", lp, c, submissionCreated, submissionId));

            calculateDurations(controllerId, dailyPlanDate, dailyPlanSchedules);

            synchronizer.addPlannedOrderToControllerAndDB(startupMode, operation, controllerId, dailyPlanDate, withSubmit, durations);
            EventBus.getInstance().post(new DailyPlanEvent(controllerId, dailyPlanDate));
        } else {
            LOGGER.info(String.format("%s[skip]%s", lp, c));
        }
        return synchronizer.getPlannedOrders();
    }

    /* DailyPlanModifyOrderImpl **/
    public OrderListSynchronizer calculateStartTimes(StartupMode startupMode, String controllerId, Collection<DailyPlanSchedule> dailyPlanSchedules,
            String dailyPlanDate, DBItemDailyPlanSubmission submission, Long calendarId, JocError jocError, String accessToken)
            throws JsonParseException, JsonMappingException, DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException,
            JocConfigurationException, DBOpenSessionException, SOSInvalidDataException, SOSMissingDataException, SOSHibernateException,
            ParseException, IOException, ControllerConnectionResetException, ControllerConnectionRefusedException, ExecutionException {

        if (dailyPlanSchedules == null || dailyPlanSchedules.size() == 0) {
            return new OrderListSynchronizer(settings);
        }

        String caller = DailyPlanHelper.getCallerForLog(settings);
        String lp = String.format("[%s]%s[%s][%s]", startupMode, caller, controllerId, dailyPlanDate);

        // with using everyday Calendar
        OrderListSynchronizer synchronizer = calculateStartTimes(startupMode, controllerId, dailyPlanSchedules, dailyPlanDate, submission,
                calendarId);
        synchronizer.setJocError(jocError);
        synchronizer.setAccessToken(accessToken);
        OrderCounter c = DailyPlanHelper.getOrderCount(synchronizer.getPlannedOrders());

        if (synchronizer.getPlannedOrders().size() > 0) {
            Long submissionId = null;
            String submissionCreated = "unknown";
            if (synchronizer.getSubmission() != null) {
                submissionId = synchronizer.getSubmission().getId();
                submissionCreated = SOSDate.getDateTimeAsString(synchronizer.getSubmission().getCreated());
            }

            LOGGER.info(String.format("%s[calculated][%s][submission created=%s, id=%s]", lp, c, submissionCreated, submissionId));

            calculateDurations(controllerId, dailyPlanDate, dailyPlanSchedules);
        } else {
            LOGGER.info(String.format("[%s[skip]%s", lp, c));
        }

        return synchronizer;
    }

    /* DailyPlanModifyOrderImpl **/
    public OrderListSynchronizer getEmptySynchronizer() {
        return new OrderListSynchronizer(settings);
    }

    /* DailyPlanModifyOrderImpl etc and service **/
    public void addPlannedOrderToControllerAndDB(StartupMode startupMode, String controllerId, String dailyPlanDate, Boolean withSubmit,
            OrderListSynchronizer synchronizer) throws DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException,
            JocConfigurationException, DBOpenSessionException, SOSHibernateException, ParseException, IOException, ControllerConnectionResetException,
            ControllerConnectionRefusedException, ExecutionException {

        String operation = withSubmit ? "creating/submitting" : "creating";
        if (synchronizer.getPlannedOrders().size() > 0) {
            synchronizer.addPlannedOrderToControllerAndDB(startupMode, operation, controllerId, dailyPlanDate, withSubmit, durations);
        }
    }

    private void calculateDurations(String controllerId, String date, Collection<DailyPlanSchedule> dailyPlanSchedules) throws SOSHibernateException {
        Set<String> schedulesWorkflowPaths = new HashSet<>();
        for (DailyPlanSchedule s : dailyPlanSchedules) {
            for (DailyPlanScheduleWorkflow w : s.getWorkflows()) {
                if (!schedulesWorkflowPaths.contains(w.getPath())) {// distinct
                    schedulesWorkflowPaths.add(w.getPath());
                }
            }
        }

        Set<String> workflowPaths = null;
        if (durations == null || durations.size() == 0) {
            durations = new HashMap<>();
            workflowPaths = schedulesWorkflowPaths;
        } else {
            workflowPaths = schedulesWorkflowPaths.stream().filter(e -> {
                return !durations.containsKey(e);
            }).collect(Collectors.toSet());
        }
        if (workflowPaths != null && workflowPaths.size() > 0) {
            calculateDurations(controllerId, date, workflowPaths);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[calculateDurations][%s][%s][skip]already calculated", controllerId, date));
            }
        }
    }

    private void calculateDurations(String controllerId, String date, Set<String> workflowPaths) throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("calculateDurations");
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);

            for (String workflowPath : workflowPaths) {
                Long seconds;
                try {
                    seconds = dbLayer.getWorkflowAvg(controllerId, workflowPath);
                    seconds = seconds == null ? 0L : seconds;
                } catch (Throwable e) {
                    seconds = 0L;
                    LOGGER.warn(String.format("[calculateDurations][%s][%s]%s", controllerId, workflowPath, e.toString()), e);
                }
                durations.put(workflowPath, seconds * 1_000);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[calculateDurations][%s][%s][workflow=%s]%s", controllerId, date, workflowPath, SOSDate
                            .getDurationOfSeconds(seconds)));
                }
            }
        } finally {
            Globals.disconnect(session);
        }
    }

    /* service (submitDaysAhead) & DailyPlanModifyOrderImpl, DailyPlanSubmitOrdersImpl **/
    public void submitOrders(StartupMode startupMode, String controllerId, List<DBItemDailyPlanOrder> items, String dailyPlanDate, JocError jocError,
            String accessToken) throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, IOException, ParseException, SOSException, URISyntaxException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, InterruptedException, ExecutionException, TimeoutException {
        submitOrders(startupMode, controllerId, items, dailyPlanDate, null, jocError, accessToken);
    }

    public void submitOrders(StartupMode startupMode, String controllerId, List<DBItemDailyPlanOrder> items, String dailyPlanDate,
            Boolean forceJobAdmission, JocError jocError, String accessToken) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            IOException, ParseException, SOSException, URISyntaxException, ControllerConnectionResetException, ControllerConnectionRefusedException,
            InterruptedException, ExecutionException, TimeoutException {

        SOSHibernateSession session = null;
        try {
            OrderListSynchronizer synchronizer = new OrderListSynchronizer(settings);
            synchronizer.setAccessToken(accessToken);
            synchronizer.setJocError(jocError);

            String sessionIdentifier = "submitOrders";
            if (!SOSString.isEmpty(dailyPlanDate)) {
                sessionIdentifier += "-" + dailyPlanDate;
            }
            session = Globals.createSosHibernateStatelessConnection(sessionIdentifier);
            DBLayerOrderVariables dbLayer = new DBLayerOrderVariables(session);

            Map<String, DBItemDailyPlanVariable> cyclicVariables = new HashMap<>();
            for (DBItemDailyPlanOrder item : items) {
                Schedule schedule = new Schedule();
                schedule.setPath(item.getSchedulePath());
                schedule.setWorkflowNames(Arrays.asList(item.getWorkflowName()));
                schedule.setSubmitOrderToControllerWhenPlanned(true);
                schedule.setOrderParameterisations(new ArrayList<OrderParameterisation>());

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

                OrderParameterisation orderParameterisation = new OrderParameterisation();
                if (item.getOrderParameterisation() != null) {
                    OrderParameterisation storedOP = Globals.objectMapper.readValue(item.getOrderParameterisation(), OrderParameterisation.class);
                    orderParameterisation.setPositions(storedOP.getPositions());
                    orderParameterisation.setForceJobAdmission(storedOP.getForceJobAdmission());
                    orderParameterisation.setPriority(storedOP.getPriority());
                }
                if (forceJobAdmission != null) {
                    orderParameterisation.setForceJobAdmission(forceJobAdmission);
                }
                if (item.getPriority() != null) {
                    orderParameterisation.setPriority(item.getPriority().intValue());
                }
                orderParameterisation.setVariables(variables);
                schedule.getOrderParameterisations().add(orderParameterisation);

                DailyPlanScheduleWorkflow dailyPlanScheduleWorkflow = new DailyPlanScheduleWorkflow(item.getWorkflowName(), item.getWorkflowPath(),
                        null);
                FreshOrder freshOrder = buildFreshOrder(dailyPlanDate, schedule, dailyPlanScheduleWorkflow, orderParameterisation, item
                        .getPlannedStart().getTime(), item.getStartMode());
                freshOrder.setId(item.getOrderId());

                DailyPlanSchedule dailyPlanSchedule = new DailyPlanSchedule(schedule, Arrays.asList(dailyPlanScheduleWorkflow));
                PlannedOrder p = new PlannedOrder(item.getControllerId(), freshOrder, dailyPlanSchedule, dailyPlanScheduleWorkflow, item
                        .getCalendarId(), orderParameterisation.getTags());
                p.setStoredInDb(true);

                synchronizer.add(startupMode, p, controllerId, dailyPlanDate);
            }
            // disconnect here to avoid nested sessions
            Globals.disconnect(session);
            session = null;

            if (synchronizer.getPlannedOrders().size() > 0) {
                synchronizer.submitOrdersToController(startupMode, controllerId, dailyPlanDate);
            }
        } finally {
            Globals.disconnect(session);
        }
    }

    // service
    private List<DBBeanReleasedSchedule2DeployedWorkflow> getReleasedSchedule2DeployedWorkflow(String controllerId) throws SOSHibernateException,
            IOException {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IDENTIFIER);
            return new DBLayerSchedules(session).getReleasedSchedule2DeployedWorkflows(controllerId, null);
        } finally {
            Globals.disconnect(session);
        }
    }

    // service - use only with getPlanOrderAutomatically and not check the folder permissions
    private Collection<DailyPlanSchedule> convert(List<DBBeanReleasedSchedule2DeployedWorkflow> items, boolean infoOnMissingDeployedWorkflow) {
        return convert(items, true, false, null, null, infoOnMissingDeployedWorkflow);
    }

    public Collection<DailyPlanSchedule> getDailyPlanSchedules(String controllerId, boolean infoOnMissingDeployedWorkflow) throws Exception {
        return convert(getReleasedSchedule2DeployedWorkflow(controllerId), infoOnMissingDeployedWorkflow);
    }

    // DailyPlanOrdersGenerateImpl, SchedulesImpl
    public Collection<DailyPlanSchedule> convert(List<DBBeanReleasedSchedule2DeployedWorkflow> items, Set<Folder> permittedFolders,
            Map<String, Boolean> checkedFolders, boolean onlyPlanOrderAutomatically, boolean infoOnMissingDeployedWorkflow) {
        return convert(items, onlyPlanOrderAutomatically, true, permittedFolders, checkedFolders, infoOnMissingDeployedWorkflow);
    }

    private Collection<DailyPlanSchedule> convert(List<DBBeanReleasedSchedule2DeployedWorkflow> items, boolean onlyPlanOrderAutomatically,
            boolean checkPermissions, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders, boolean infoOnMissingDeployedWorkflow) {
        if (items == null || items.size() == 0) {
            return new ArrayList<DailyPlanSchedule>();
        }

        String method = "convert";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        Map<String, DailyPlanSchedule> releasedSchedules = new HashMap<>();
        for (DBBeanReleasedSchedule2DeployedWorkflow item : items) {
            String key = item.getScheduleName();
            DailyPlanSchedule dps = null;
            if (releasedSchedules.containsKey(key)) {
                dps = releasedSchedules.get(key);
            } else {
                String content = item.getScheduleContent();
                if (SOSString.isEmpty(content)) {
                    LOGGER.warn(String.format("[%s][skip][content is empty]%s", method, SOSHibernate.toString(item)));
                    dps = new DailyPlanSchedule(null);
                    continue;
                }
                Schedule schedule;
                try {
                    schedule = Globals.objectMapper.readValue(item.getScheduleContent(), Schedule.class);
                    if (schedule != null) {
                        schedule.setPath(item.getSchedulePath());
                    }
                    dps = new DailyPlanSchedule(schedule);
                } catch (Throwable e) {
                    LOGGER.error(String.format("[%s][%s][exception]%s", method, SOSHibernate.toString(item), e.toString()), e);
                    continue;
                }
                if (schedule == null) {
                    LOGGER.warn(String.format("[%s][skip][schedule is null]%s", method, SOSHibernate.toString(item)));
                    continue;
                }
                // TODO: maybe duplicate with line 590 below
                dps.getWorkflows().add(new DailyPlanScheduleWorkflow(item.getWorkflowName(), item.getWorkflowPath(), item.getWorkflowContent()));
            }
            if (dps.getSchedule() == null) {
                continue;
            }
            dps.addWorkflow(new DailyPlanScheduleWorkflow(item.getWorkflowName(), item.getWorkflowPath(), item.getWorkflowContent()));
            releasedSchedules.put(key, dps);
        }

        List<DailyPlanSchedule> result = new ArrayList<>();
        for (Map.Entry<String, DailyPlanSchedule> entry : releasedSchedules.entrySet()) {
            DailyPlanSchedule dailyPlanSchedule = entry.getValue();
            Schedule schedule = dailyPlanSchedule.getSchedule();
            if (schedule == null) {
                continue;
            }

            if (onlyPlanOrderAutomatically && !schedule.getPlanOrderAutomatically()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][schedule=%s][onlyPlanOrderAutomatically=true]schedule.getPlanOrderAutomatically=false",
                            method, schedule.getPath()));
                }
                continue;
            }

            schedule = JocInventory.setWorkflowNames(schedule);
            List<DailyPlanScheduleWorkflow> scheduleWorkflows = new ArrayList<>();
            boolean clear = false;
            int namesSize = schedule.getWorkflowNames().size();
            wn: for (String wn : schedule.getWorkflowNames()) {
                DailyPlanScheduleWorkflow dpw = dailyPlanSchedule.getWorkflow(wn);
                if (dpw == null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][skip][schedule=%s][workflow=%s]workflow is not deployed on current controller", method,
                                schedule.getPath(), wn));
                    }
                    continue;
                } else {
                    if (namesSize >= JocInventory.SCHEDULE_MIN_MULTIPLE_WORKFLOWS_SIZE) {// check only multiple workflows
                        if (dpw.getContent() != null) {
                            try {
                                Workflow w = Globals.objectMapper.readValue(dpw.getContent(), Workflow.class);
                                Requirements r = w.getOrderPreparation();
                                if (r != null && r.getParameters() != null && r.getParameters().getAdditionalProperties() != null && r.getParameters()
                                        .getAdditionalProperties().size() > 0) {
                                    LOGGER.warn(String.format(
                                            "[%s][skip][schedule=%s][%s workflows=%s][workflow=%s][%s order variables]multiple workflows with order variables are not permitted",
                                            method, schedule.getPath(), namesSize, String.join(",", schedule.getWorkflowNames()), dpw.getName(), r
                                                    .getParameters().getAdditionalProperties().size()));
                                    clear = true;
                                }
                            } catch (Throwable e) {
                                LOGGER.error(String.format("[%s][skip][schedule=%s][%s workflows=%s][workflow=%s]%s", method, schedule.getPath(),
                                        namesSize, String.join(",", schedule.getWorkflowNames()), dpw.getName(), e.toString()), e);
                                clear = true;
                            }
                        }
                    }
                    if (!clear) {
                        if (!isWorkflowPermitted(dpw.getPath(), permittedFolders, checkedFolders)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][skip][schedule=%s][workflow not permitted]workflow=%s", method, schedule.getPath(),
                                        dpw.getPath()));
                            }
                            continue;
                        }
                    }
                }
                if (clear) {
                    break wn;
                } else {
                    scheduleWorkflows.add(dpw);
                }
            }

            if (clear) {
                scheduleWorkflows.clear();
            }

            if (scheduleWorkflows.size() == 0) {
                if (infoOnMissingDeployedWorkflow) {
                    LOGGER.info(String.format("[%s][skip][schedule=%s]no deployed/permitted workflows found", method, schedule.getPath()));
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][skip][schedule=%s]no deployed/permitted workflows found", method, schedule.getPath()));
                    }
                }
                continue;
            }
            schedule.setWorkflowNames(scheduleWorkflows.stream().map(l -> {
                return l.getName();
            }).collect(Collectors.toList()));
            DailyPlanSchedule dps = new DailyPlanSchedule(schedule, scheduleWorkflows);
            result.add(dps);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][schedule=%s]workflow=%s", method, schedule.getPath(), dps.getWorkflowsAsString()));
            }
        }
        return result;
    }

    private boolean isWorkflowPermitted(String workflowPath, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders) {
        if (checkedFolders == null) {// from service
            return true;
        }
        String folder = DailyPlanHelper.getFolderFromPath(workflowPath);
        Boolean result = checkedFolders.get(folder);
        if (result == null) {
            result = JOCResourceImpl.canAdd(workflowPath, permittedFolders);
            checkedFolders.put(folder, result);
        }
        return result;
    }

    private boolean isCalendarPermitted(Calendar calendar) {
        if (calendar.getPath() == null || SOSCollection.isEmpty(settings.getPermittedFolders())) {
            return true;
        }
        return SOSAuthFolderPermissions.isPermittedForFolder(DailyPlanHelper.getFolderFromPath(calendar.getPath()), settings.getPermittedFolders());
    }

    private List<DBItemDailyPlanSubmission> getSubmissionsForDate(String controllerId, java.util.Calendar calendar) throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IDENTIFIER);
            DBLayerDailyPlanSubmissions dbLayer = new DBLayerDailyPlanSubmissions(session);
            return dbLayer.getSubmissions(controllerId, calendar.getTime());
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

        List<DBItemDailyPlanSubmission> submissions = getSubmissionsForDate(controllerId, calendar);
        if (submissions == null || submissions.size() == 0) {
            LOGGER.info(String.format("[%s][submitting][%s][%s][skip]no submissions found", startupMode, controllerId, date));
            return;
        }

        for (DBItemDailyPlanSubmission item : submissions) {
            List<DBItemDailyPlanOrder> plannedOrders;

            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection("submitDaysAhead");
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                plannedOrders = dbLayer.getDailyPlanOrdersBySubmission(item.getId(), false);
            } finally {
                Globals.disconnect(session);
            }

            String submissionForDate = SOSDate.getDateTimeAsString(item.getSubmissionForDate());
            if (plannedOrders == null || plannedOrders.size() == 0) {
                LOGGER.info(String.format("[%s][submitting][%s][%s][submission created=%s, id=%s][skip]0 not submitted orders found", startupMode,
                        controllerId, date, SOSDate.getDateTimeAsString(item.getCreated()), item.getId()));
            } else {
                OrderCounter c = DailyPlanHelper.getOrderCount(plannedOrders);
                LOGGER.info(String.format("[%s][submitting][%s][%s][submission created=%s, id=%s]submit %s start ...", startupMode, controllerId,
                        date, SOSDate.getDateTimeAsString(item.getCreated()), item.getId(), c));

                submitOrders(startupMode, controllerId, plannedOrders, submissionForDate, null, "");
                // not log end because asynchronous
                // LOGGER.info(String.format("[submitting][%s][%s][submission=%s]submit end", controllerId, date, submissionForDate));
            }
        }
    }

    // service
    private void closeOldPlans(StartupMode startupMode, String controllerId, int ageOfPlansToBeClosedAutomatically) {

        if (ageOfPlansToBeClosedAutomatically > 0) {
            try {
                LocalDate ld = LocalDate.now(ZoneOffset.UTC);
                if (ageOfPlansToBeClosedAutomatically > 1) {
                    ld = ld.minusDays(Integer.valueOf(ageOfPlansToBeClosedAutomatically - 1).longValue());
                }
                String date = ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
                LOGGER.info(String.format("[%s][closePlans][%s]older than %s", startupMode, controllerId, date));
                PlanSchemaId dailyPlanSchemaId = PlanSchemaId.of(PlanSchemas.DailyPlanPlanSchemaId);
                Predicate<Map.Entry<PlanId, JPlan>> isDailyPlanPlan = e -> e.getKey().planSchemaId().equals(dailyPlanSchemaId);
                Predicate<Map.Entry<PlanId, JPlan>> planIsOlder = e -> e.getKey().planKey().string().compareTo(date) < 0;
                Predicate<Map.Entry<PlanId, JPlan>> isOpen = e -> !e.getValue().isClosed();
                JControllerProxy proxy = Proxy.of(controllerId);

                proxy.currentState().toPlan().entrySet().stream().filter(isDailyPlanPlan).filter(planIsOlder).filter(isOpen).map(Map.Entry::getKey)
                        .map(pId -> {
                            LOGGER.info(String.format("[%s][closePlan][%s]try %s/%s", startupMode, controllerId, PlanSchemas.DailyPlanPlanSchemaId,
                                    pId.planKey().string()));
                            return JControllerCommand.changePlan(pId, JPlanStatus.Closed());
                        }).map(JControllerCommand::apply).forEach(command -> proxy.api().executeCommand(command).thenAccept(e -> ProblemHelper
                                .postProblemEventIfExist(e, null, null, controllerId)));
            } catch (Exception e) {
                LOGGER.error(String.format("[%s][closePlans][%s]fails: %s", startupMode, controllerId, e.toString()), e);
            }
        }
    }

    // service
    private void setUnknownPlansAreOpenFromDate(StartupMode startupMode, String controllerId) {

        try {
            LocalDate ld = LocalDate.now(ZoneOffset.UTC);
            ld = ld.minusMonths(1l);
            String date = ld.format(DateTimeFormatter.ISO_LOCAL_DATE);

            LOGGER.info(String.format("[%s][unknownPlansCanBeOpenFrom][%s] %s", startupMode, controllerId, date));
            PlanSchemaId dailyPlanSchemaId = PlanSchemaId.of(PlanSchemas.DailyPlanPlanSchemaId);
            JControllerProxy proxy = Proxy.of(controllerId);

            JPlanSchemaState schemaState = proxy.currentState().idToPlanSchemaState().get(dailyPlanSchemaId);
            Value val = schemaState.namedValues().get(PlanSchemas.DailyPlanThresholdKey);
            Map<String, Value> newNamedValues = new HashMap<>(schemaState.namedValues());
            if (val != null) {
                String oldValue = val.toStringValueString().getOrElse(null);
                if (oldValue == null || (oldValue != null && oldValue.compareTo(date) < 0)) {
                    newNamedValues.put(PlanSchemas.DailyPlanThresholdKey, StringValue.of(date));
                    proxy.api().executeCommand(JControllerCommand.apply(JControllerCommand.changePlanSchema(dailyPlanSchemaId, Optional.of(
                            newNamedValues), Optional.empty()))).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, null, null, controllerId));
                }
            } else {
                newNamedValues.put(PlanSchemas.DailyPlanThresholdKey, StringValue.of(date));
                proxy.api().executeCommand(JControllerCommand.apply(JControllerCommand.changePlanSchema(dailyPlanSchemaId, Optional.of(
                        newNamedValues), Optional.empty()))).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, null, null, controllerId));
            }

        } catch (Exception e) {
            LOGGER.error(String.format("[%s][unknownPlansAreOpenFrom][%s]fails: %s", startupMode, controllerId, e.toString()), e);
        }
    }

    private Calendar getWorkingDaysCalendar(String controllerId, String calendarName) throws DBMissingDataException, JsonParseException,
            JsonMappingException, IOException, DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException,
            DBOpenSessionException, SOSHibernateException {

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IDENTIFIER + "-getWorkingDaysCalendar");
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            DBItemInventoryReleasedConfiguration config = dbLayer.getReleasedConfiguration(calendarName, ConfigurationType.WORKINGDAYSCALENDAR);
            session.close();
            session = null;

            if (config == null) {
                throw new DBMissingDataException(String.format("calendar '%s' not found for controller instance %s", calendarName, controllerId));
            }

            Calendar calendar = Globals.objectMapper.readValue(config.getContent(), Calendar.class);
            calendar.setId(config.getId());
            calendar.setPath(config.getPath());
            calendar.setName(config.getName());
            return calendar;
        } finally {
            Globals.disconnect(session);
        }
    }

    private FreshOrder buildFreshOrder(String dailyPlanDate, Schedule schedule, DailyPlanScheduleWorkflow scheduleWorkflow,
            OrderParameterisation orderParameterisation, Long startTime, Integer startMode) throws SOSInvalidDataException {
        FreshOrder order = new FreshOrder();
        order.setId(DailyPlanHelper.buildOrderId(dailyPlanDate, schedule, orderParameterisation, startTime, startMode));
        order.setScheduledFor(startTime);
        order.setArguments(orderParameterisation.getVariables());
        order.setWorkflowPath(scheduleWorkflow.getName());
        order.setPositions(orderParameterisation.getPositions());
        order.setForceJobAdmission(orderParameterisation.getForceJobAdmission());
        order.setPriority(orderParameterisation.getPriority() == null ? 0 : orderParameterisation.getPriority());
        return order;
    }

    public static boolean isFromService(StartupMode mode) {
        if (mode == null) {
            return false;
        }
        return mode.equals(StartupMode.automatic) || mode.equals(StartupMode.manual_restart);
    }

    public OrderListSynchronizer calculateStartTimes(StartupMode startupMode, String controllerId, Collection<DailyPlanSchedule> dailyPlanSchedules,
            String dailyPlanDate, DBItemDailyPlanSubmission submission) throws SOSInvalidDataException, ParseException, JsonParseException,
            JsonMappingException, DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException,
            DBOpenSessionException, SOSMissingDataException, SOSHibernateException, IOException {
        return calculateStartTimes(startupMode, controllerId, dailyPlanSchedules, dailyPlanDate, submission, null, false);
    }

    private OrderListSynchronizer calculateStartTimes(StartupMode startupMode, String controllerId, Collection<DailyPlanSchedule> dailyPlanSchedules,
            String dailyPlanDate, DBItemDailyPlanSubmission submission, boolean includeLate) throws SOSInvalidDataException, ParseException,
            JsonParseException, JsonMappingException, DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException,
            JocConfigurationException, DBOpenSessionException, SOSMissingDataException, SOSHibernateException, IOException {
        return calculateStartTimes(startupMode, controllerId, dailyPlanSchedules, dailyPlanDate, submission, null, includeLate);
    }

    private OrderListSynchronizer calculateStartTimes(StartupMode startupMode, String controllerId, Collection<DailyPlanSchedule> dailyPlanSchedules,
            String dailyPlanDate, DBItemDailyPlanSubmission submission, Long usingEveryDayCalendarWithId) throws SOSInvalidDataException,
            ParseException, JsonParseException, JsonMappingException, DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException,
            JocConfigurationException, DBOpenSessionException, SOSMissingDataException, SOSHibernateException, IOException {
        return calculateStartTimes(startupMode, controllerId, dailyPlanSchedules, dailyPlanDate, submission, usingEveryDayCalendarWithId, false);
    }

    private OrderListSynchronizer calculateStartTimes(StartupMode startupMode, String controllerId, Collection<DailyPlanSchedule> dailyPlanSchedules,
            String dailyPlanDate, DBItemDailyPlanSubmission submission, Long usingEveryDayCalendarWithId, boolean includeLate)
            throws SOSInvalidDataException, ParseException, JsonParseException, JsonMappingException, DBMissingDataException,
            DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException, DBOpenSessionException, SOSMissingDataException,
            SOSHibernateException, IOException {

        String method = "calculateStartTimes";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        Date date = SOSDate.getDate(dailyPlanDate);
        Date actDate = date;
        Date nextDate = DailyPlanHelper.getNextDay(date, settings);
        String caller = DailyPlanHelper.getCallerForLog(settings);
        String lp = String.format("[%s]%s[%s][%s][%s]", startupMode, caller, method, controllerId, dailyPlanDate);
        boolean fromService = isFromService(startupMode);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%sactDate=%s,nextDate=%s", lp, SOSDate.getDateAsString(actDate), SOSDate.getDateAsString(nextDate)));
        }

        // Map<String, Calendar> calendars = new HashMap<String, Calendar>();
        // Map<String, Calendar> nonWorkingCalendars = new HashMap<String, Calendar>();

        OrderListSynchronizer synchronizer = new OrderListSynchronizer(settings);
        InventoryDBLayer invDbLayer = new InventoryDBLayer(null);
        String submissionForDate = dailyPlanDate;
        if (submission != null) {
            synchronizer.setSubmission(submission);
            submissionForDate = SOSDate.getDateAsString(submission.getSubmissionForDate());
        }

        // introduced for optimization with JOC-1647
        // instead of 2 days (current and next day), only the current dailyPlanDate is used for the calendar/frequency resolver calculation
        for (DailyPlanSchedule dailyPlanSchedule : dailyPlanSchedules) {
            Schedule schedule = dailyPlanSchedule.getSchedule();
            String logPrefix = String.format("%s[schedule=%s]", lp, schedule.getPath());
            String logNonWorkingDayCalendars = nonWorkingDayCalendarsLog(schedule);
            if (fromService && !schedule.getPlanOrderAutomatically()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[skip]fromService=true, plan order automatically=false", logPrefix, schedule.getPath()));
                }
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s", logPrefix));
                }

                for (AssignedCalendars assignedCalendar : schedule.getCalendars()) {
                    if (assignedCalendar.getTimeZone() == null) {
                        assignedCalendar.setTimeZone(SOSDate.TIMEZONE_UTC);
                    }

                    String actDateAsString = SOSDate.getDateAsString(actDate);
                    String nextDateAsString = SOSDate.getDateAsString(nextDate);
                    String dailyPlanDateAsString = SOSDate.getDateAsString(date);

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[calendar=%s][timeZone=%s][actDate=%s][nextDate=%s]dailyPlanDate=%s", logPrefix,
                                assignedCalendar.getCalendarName(), assignedCalendar.getTimeZone(), actDateAsString, nextDateAsString,
                                dailyPlanDateAsString));
                    }

                    String calendarsKey = assignedCalendar.getCalendarName();// + "#" + schedule.getPath();
                    Calendar calendar = calculateStartTimesCalendars.get(calendarsKey);
                    if (calendar == null) {
                        if (usingEveryDayCalendarWithId != null) {
                            calendar = getEveryDayCalendar(usingEveryDayCalendarWithId, assignedCalendar.getCalendarName());
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[WorkingDaysCalendar=%s][usingEveryDayCalendar]%s", logPrefix, assignedCalendar
                                        .getCalendarName(), SOSString.toString(calendar, true)));
                            }
                        } else {
                            try {
                                calendar = getWorkingDaysCalendar(controllerId, assignedCalendar.getCalendarName());
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("%s[WorkingDaysCalendar=%s][db]%s", logPrefix, assignedCalendar.getCalendarName(),
                                            SOSString.toString(calendar, true)));
                                }
                                if (!fromService && !isCalendarPermitted(calendar)) {
                                    if (isDebugEnabled) {
                                        LOGGER.debug(String.format("%s[WorkingDaysCalendar=%s]not permitted", logPrefix, assignedCalendar
                                                .getCalendarName()));
                                    }
                                    continue;
                                }
                            } catch (DBMissingDataException e) {
                                LOGGER.warn(String.format("%s[WorkingDaysCalendar=%s][skip]not found", logPrefix, assignedCalendar
                                        .getCalendarName()));
                                continue;
                            }
                        }
                        calculateStartTimesCalendars.put(calendarsKey, calendar);
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[WorkingDaysCalendar=%s][cache]%s", logPrefix, assignedCalendar.getCalendarName(), SOSString
                                    .toString(calendar)));
                        }
                    }
                    calendar.setFrom(actDateAsString);
                    calendar.setTo(actDateAsString);

                    Calendar restrictions = new Calendar();
                    restrictions.setIncludes(assignedCalendar.getIncludes());
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[WorkingDaysCalendar=%s][includes]%s", logPrefix, assignedCalendar.getCalendarName(), SOSString
                                .toString(restrictions)));
                    }

                    int plannedOrdersCount = 0;
                    List<String> frequencyResolverDates = null;
                    try {
                        PeriodResolver periodResolver = null;
                        frequencyResolverDates = new FrequencyResolver().resolveRestrictions(calendar, restrictions, actDateAsString, actDateAsString)
                                .getDates();
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[WorkingDaysCalendar=%s][FrequencyResolver]dates=%s", logPrefix, assignedCalendar
                                    .getCalendarName(), String.join(",", frequencyResolverDates)));
                        }

                        // NonWorkingDays handling - NEXTNONWORKINGDAY, PREVIOUSNONWORKINGDAY
                        // frequencyResolverDates is empty because no dates in the regular calendar or the next day
                        boolean checkNonWorkingDaysNextPrev = true;
                        if (!frequencyResolverDates.contains(actDateAsString)) {
                            List<String> newFrequencyResolverDates = new ArrayList<>();

                            TreeSet<String> nonWorkingDaysForNext = new TreeSet<>();
                            String prevDateAsStringForNext = "";

                            Set<String> nonWorkingDaysForPrev = new TreeSet<>();
                            String nextDateAsStringForPrev = "";

                            List<Period> nonEmptyWhenHolidays = assignedCalendar.getPeriods().stream().filter(p -> p.getWhenHoliday() != null)
                                    .collect(Collectors.toList());
                            List<Period> allowedPeriods = new ArrayList<>();
                            if (nonEmptyWhenHolidays.size() > 0) {
                                boolean added = false;
                                for (Period p : nonEmptyWhenHolidays) {
                                    switch (p.getWhenHoliday()) {
                                    case NEXTNONWORKINGDAY:
                                        added = false;
                                        if (isDebugEnabled) {
                                            LOGGER.debug(String.format("%s[NonWorkingDays][NEXTNONWORKINGDAY]start...", logPrefix));
                                        }
                                        if (isDayAfterNonWorkingDays(isDebugEnabled, logPrefix, invDbLayer, schedule,
                                                calculateStartTimesNonWorkingCalendars, actDate, actDateAsString, prevDateAsStringForNext,
                                                nonWorkingDaysForNext, p)) {
                                            // check if this day is allowed in the regular calendar
                                            String firstNonWorkingDay = DailyPlanHelper.getFirstNonWorkingDayFromLastBlock(nonWorkingDaysForNext);
                                            if (isDebugEnabled) {
                                                LOGGER.debug(String.format("%s[NonWorkingDays][NEXTNONWORKINGDAY]firstNonWorkingDay=%s", logPrefix,
                                                        firstNonWorkingDay));
                                            }
                                            if (firstNonWorkingDay != null) {
                                                Calendar calendayCopy = calculateStartTimesCalendars.get(calendarsKey);
                                                calendayCopy.setFrom(firstNonWorkingDay);
                                                calendayCopy.setTo(actDateAsString);

                                                List<String> workingDates = new FrequencyResolver().resolveRestrictions(calendayCopy, restrictions,
                                                        firstNonWorkingDay, actDateAsString).getDates();
                                                if (isDebugEnabled) {
                                                    LOGGER.debug(String.format("%s[NonWorkingDays][NEXTNONWORKINGDAY]workingDates=%s", logPrefix,
                                                            String.join(",", workingDates)));
                                                }
                                                if (workingDates.size() > 0) {
                                                    added = true;
                                                    allowedPeriods.add(p);
                                                }
                                            }
                                        }
                                        if (isDebugEnabled) {
                                            LOGGER.debug(String.format("%s[NonWorkingDays][NEXTNONWORKINGDAY][%s]end", logPrefix, added ? "added"
                                                    : "skip"));
                                        }
                                        break;
                                    case PREVIOUSNONWORKINGDAY:
                                        added = false;
                                        if (isDebugEnabled) {
                                            LOGGER.debug(String.format("%s[NonWorkingDays][PREVIOUSNONWORKINGDAY]start...", logPrefix));
                                        }
                                        if (isDayBeforeNonWorkingDays(isDebugEnabled, logPrefix, invDbLayer, schedule,
                                                calculateStartTimesNonWorkingCalendars, actDateAsString, nextDateAsStringForPrev,
                                                nonWorkingDaysForPrev, p)) {
                                            // check if this day is allowed in the regular calendar
                                            String lastNonWorkingDay = DailyPlanHelper.getLastNonWorkingDayFromFirstBlock(nonWorkingDaysForPrev);
                                            if (isDebugEnabled) {
                                                LOGGER.debug(String.format("%s[NonWorkingDays][PREVIOUSNONWORKINGDAY]lastNonWorkingDay=%s", logPrefix,
                                                        lastNonWorkingDay));
                                            }
                                            if (lastNonWorkingDay != null) {
                                                Calendar calendayCopy = calculateStartTimesCalendars.get(calendarsKey);
                                                calendayCopy.setFrom(actDateAsString);
                                                calendayCopy.setTo(lastNonWorkingDay);

                                                List<String> workingDates = new FrequencyResolver().resolveRestrictions(calendayCopy, restrictions,
                                                        actDateAsString, lastNonWorkingDay).getDates();
                                                if (isDebugEnabled) {
                                                    LOGGER.debug(String.format("%s[NonWorkingDays][PREVIOUSNONWORKINGDAY]workingDates=%s", logPrefix,
                                                            String.join(",", workingDates)));
                                                }
                                                if (workingDates.size() > 0) {
                                                    added = true;
                                                    allowedPeriods.add(p);
                                                }
                                            }
                                        }
                                        if (isDebugEnabled) {
                                            LOGGER.debug(String.format("%s[NonWorkingDays][PREVIOUSNONWORKINGDAY]end", logPrefix));
                                        }
                                        break;
                                    default:
                                        break;
                                    }
                                }

                                if (allowedPeriods.size() > 0) {
                                    checkNonWorkingDaysNextPrev = false;
                                    periodResolver = createPeriodResolver(allowedPeriods, dailyPlanDateAsString, assignedCalendar.getTimeZone());
                                    if (!newFrequencyResolverDates.contains(actDateAsString)) {
                                        newFrequencyResolverDates.add(actDateAsString);
                                    }
                                    frequencyResolverDates.clear();
                                    frequencyResolverDates.addAll(newFrequencyResolverDates);

                                    if (isDebugEnabled) {
                                        LOGGER.debug(String.format("%s[NonWorkingDays][added][days=%s]allowedPeriods=%s", logPrefix, String.join(",",
                                                frequencyResolverDates), allowedPeriods.size()));
                                    }
                                } else {
                                    if (isDebugEnabled) {
                                        LOGGER.debug(String.format("%s[NonWorkingDays][skip]day=%s", logPrefix, actDateAsString));
                                    }
                                }
                            }
                        }

                        if (frequencyResolverDates.size() > 0) {
                            if (periodResolver == null) {
                                periodResolver = createPeriodResolver(assignedCalendar.getPeriods(), dailyPlanDateAsString, assignedCalendar
                                        .getTimeZone());
                            }

                            Set<String> nonWorkingDaysForSuppress = new TreeSet<>();
                            for (String frequencyResolverDate : frequencyResolverDates) {
                                Map<Long, Period> startTimes = periodResolver.getStartTimes(frequencyResolverDate, dailyPlanDateAsString,
                                        assignedCalendar.getTimeZone(), includeLate);

                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format(
                                            "%s[WorkingDaysCalendar=%s][frequencyResolverDate=%s][checkNonWorkingDaysNextPrev=%s][periodResolver]startTimes size=%s",
                                            logPrefix, assignedCalendar.getCalendarName(), frequencyResolverDate, checkNonWorkingDaysNextPrev,
                                            startTimes.size()));

                                }

                                st: for (Entry<Long, Period> periodEntry : startTimes.entrySet()) {
                                    Period p = periodEntry.getValue();
                                    if (p.getWhenHoliday() != null) {
                                        switch (p.getWhenHoliday()) {
                                        case IGNORE:
                                            // do nothing - log only
                                            if (isDebugEnabled) {
                                                LOGGER.debug(String.format("%s[frequencyResolverDate=%s][NonWorkingDays][IGNORE][skip][%s]",
                                                        logPrefix, frequencyResolverDate, DailyPlanHelper.toString(p)));
                                            }
                                            break;
                                        case SUPPRESS:
                                            if (isNonWorkingDay(isDebugEnabled, logPrefix, invDbLayer, schedule,
                                                    calculateStartTimesNonWorkingCalendars, actDateAsString, nextDateAsString,
                                                    nonWorkingDaysForSuppress, p, frequencyResolverDate)) {
                                                continue st;
                                            } else {
                                                break;
                                            }
                                            // suppress because if the current day is a NonWorking day - it is a not a Next/Previous NonWorking day
                                        case NEXTNONWORKINGDAY:
                                        case PREVIOUSNONWORKINGDAY:
                                            if (checkNonWorkingDaysNextPrev && isNonWorkingDay(isDebugEnabled, logPrefix, invDbLayer, schedule,
                                                    calculateStartTimesNonWorkingCalendars, actDateAsString, nextDateAsString,
                                                    nonWorkingDaysForSuppress, p, frequencyResolverDate)) {
                                                continue st;
                                            } else {
                                                break;
                                            }
                                        default:
                                            break;
                                        }
                                    }

                                    Integer startMode;
                                    if (p.getSingleStart() == null) {
                                        startMode = 1;
                                    } else {
                                        startMode = 0;
                                    }

                                    if (settings.isCalculateAbsoluteMainPeriodsOnly()) {
                                        synchronizer.addAbsoluteMainPeriod(controllerId, schedule, periodEntry);
                                        continue;
                                    }

                                    if (schedule.getOrderParameterisations() == null || schedule.getOrderParameterisations().size() == 0) {
                                        OrderParameterisation orderParameterisation = new OrderParameterisation();
                                        schedule.setOrderParameterisations(new ArrayList<OrderParameterisation>());
                                        schedule.getOrderParameterisations().add(orderParameterisation);
                                    }

                                    for (OrderParameterisation orderParameterisation : schedule.getOrderParameterisations()) {
                                        for (DailyPlanScheduleWorkflow sw : dailyPlanSchedule.getWorkflows()) {

                                            if (synchronizer.getSubmission() == null) {
                                                synchronizer.setSubmission(addDailyPlanSubmission(controllerId, date));
                                            }

                                            FreshOrder freshOrder = buildFreshOrder(submissionForDate, dailyPlanSchedule.getSchedule(), sw,
                                                    orderParameterisation, periodEntry.getKey(), startMode);

                                            if (!fromService) {
                                                schedule.setSubmitOrderToControllerWhenPlanned(settings.isSubmit());
                                            }

                                            PlannedOrder plannedOrder = new PlannedOrder(controllerId, freshOrder, dailyPlanSchedule, sw, calendar
                                                    .getId(), orderParameterisation.getTags());
                                            plannedOrder.setPeriod(p);
                                            plannedOrder.setSubmissionHistoryId(synchronizer.getSubmission().getId());
                                            plannedOrder.setSubmissionForDate(synchronizer.getSubmission().getSubmissionForDate());
                                            plannedOrder.setOrderName(DailyPlanHelper.getOrderName(schedule, orderParameterisation));
                                            synchronizer.add(startupMode, plannedOrder, controllerId, submissionForDate);
                                            plannedOrdersCount++;
                                        }
                                    }
                                }

                            }
                        }
                    } catch (Throwable e) {
                        if (frequencyResolverDates == null) {
                            frequencyResolverDates = new ArrayList<>();
                        }
                        LOGGER.error(String.format("%s[WorkingDaysCalendar=%s,timeZone=%s][FrequencyResolver dates=%s]%s", logPrefix, assignedCalendar
                                .getCalendarName(), assignedCalendar.getTimeZone(), String.join(",", frequencyResolverDates), e.toString()), e);
                    }
                    if (settings.isCalculateAbsoluteMainPeriodsOnly()) {

                    } else {
                        if (plannedOrdersCount == 0) {
                            LOGGER.info(String.format("%s[WorkingDaysCalendar=%s,timeZone=%s]%s0 planned orders", logPrefix, assignedCalendar
                                    .getCalendarName(), assignedCalendar.getTimeZone(), logNonWorkingDayCalendars));
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[WorkingDaysCalendar=%s,timeZone=%s]%s%s planned orders", logPrefix, assignedCalendar
                                        .getCalendarName(), assignedCalendar.getTimeZone(), logNonWorkingDayCalendars, plannedOrdersCount));
                            }
                        }
                    }
                }
            }
        }
        return synchronizer;
    }

    private String nonWorkingDayCalendarsLog(Schedule schedule) {
        if (schedule.getNonWorkingDayCalendars() == null || schedule.getNonWorkingDayCalendars().size() == 0) {
            return "";
        }
        return "[NonWorkingDayCalendars=" + schedule.getNonWorkingDayCalendars().stream().map(e -> {
            if (e.getCalendarName() == null) {
                return "unknown";
            }
            return e.getCalendarName();
        }).collect(Collectors.joining(",")) + "]";

    }

    private boolean isDayAfterNonWorkingDays(boolean isDebugEnabled, String logPrefix, InventoryDBLayer dbLayer, Schedule schedule,
            Map<String, Calendar> nonWorkingCalendars, Date date, String dateAsString, String prevDateAsString, TreeSet<String> nonWorkingDays,
            Period p) throws Exception {
        String method = "isDayAfterNonWorkingDays";
        if (nonWorkingDays.size() == 0) {
            // get not working days from previous 7 days to date
            prevDateAsString = SOSDate.getDateAsString(DailyPlanHelper.getDay(SOSDate.getDate(dateAsString), settings, -7));
            nonWorkingDays.addAll(DailyPlanHelper.getNonWorkingDays(method, dbLayer, schedule.getNonWorkingDayCalendars(), prevDateAsString,
                    dateAsString, nonWorkingCalendars));
        }
        String d = "";
        if (nonWorkingDays.size() > 0) {
            d = DailyPlanHelper.getDayAfterNonWorkingDays(nonWorkingDays);
            if (d.equals(dateAsString)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format(
                            "%s[NonWorkingDays][NEXTNONWORKINGDAY][isDayAfterNonWorkingDays=true][from=%s,to=%s][nonWorkingDays=%s]dayAfterNonWorkingDays=%s",
                            logPrefix, prevDateAsString, dateAsString, String.join(",", nonWorkingDays), d));
                }
                return true;
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format(
                    "%s[NonWorkingDays][NEXTNONWORKINGDAY][isDayAfterNonWorkingDays=false][from=%s,to=%s][nonWorkingDays=%s]dayAfterNonWorkingDays=%s",
                    logPrefix, prevDateAsString, dateAsString, String.join(",", nonWorkingDays), d));
        }
        return false;
    }

    private boolean isDayBeforeNonWorkingDays(boolean isDebugEnabled, String logPrefix, InventoryDBLayer dbLayer, Schedule schedule,
            Map<String, Calendar> nonWorkingCalendars, String dateAsString, String nextDateAsString, Set<String> nonWorkingDays, Period p)
            throws Exception {
        String method = "isDayBeforeNonWorkingDays";
        if (nonWorkingDays.size() == 0) {
            // get not working days from date for next 7 days
            nextDateAsString = SOSDate.getDateAsString(DailyPlanHelper.getDay(SOSDate.getDate(dateAsString), settings, 7));
            nonWorkingDays.addAll(DailyPlanHelper.getNonWorkingDays(method, dbLayer, schedule.getNonWorkingDayCalendars(), dateAsString,
                    nextDateAsString, nonWorkingCalendars));
        }
        String d = "";
        if (nonWorkingDays.size() > 0) {
            d = DailyPlanHelper.getDayBeforeNonWorkingDays(nonWorkingDays);
            if (d.equals(dateAsString)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format(
                            "%s[NonWorkingDays][PREVIOUSNONWORKINGDAY][isDayBeforeNonWorkingDays=true][from=%s,to=%s][nonWorkingDays=%s]getDayBeforeNonWorkingDays=%s",
                            logPrefix, dateAsString, nextDateAsString, String.join(",", nonWorkingDays), d));
                }
                return true;
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format(
                    "%s[NonWorkingDays][PREVIOUSNONWORKINGDAY][isDayBeforeNonWorkingDays=false][from=%s,to=%s][nonWorkingDays=%s]getDayBeforeNonWorkingDays=%s",
                    logPrefix, dateAsString, nextDateAsString, String.join(",", nonWorkingDays), d));
        }
        return false;
    }

    private boolean isNonWorkingDay(boolean isDebugEnabled, String logPrefix, InventoryDBLayer dbLayer, Schedule schedule,
            Map<String, Calendar> nonWorkingCalendars, String dateAsString, String nextDateAsString, Set<String> nonWorkingDays, Period p,
            String frequencyResolverDate) throws Exception {
        String method = "isNonWorkingDay";
        if (nonWorkingDays.size() == 0) {
            nonWorkingDays.addAll(DailyPlanHelper.getNonWorkingDays(method, dbLayer, schedule.getNonWorkingDayCalendars(), dateAsString,
                    nextDateAsString, nonWorkingCalendars));
        }
        if (nonWorkingDays.contains(dateAsString)) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[FrequencyResolver date=%s][NonWorkingDays][%s][isNonWorkingDay=true][%s]nonWorkingDays=%s", logPrefix,
                        frequencyResolverDate, p.getWhenHoliday(), DailyPlanHelper.toString(p), String.join(",", nonWorkingDays)));
            }
            return true;
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[FrequencyResolver date=%s][NonWorkingDays][%s][isNonWorkingDay=false][%s]nonWorkingDays=%s", logPrefix,
                    frequencyResolverDate, p.getWhenHoliday(), DailyPlanHelper.toString(p), String.join(",", nonWorkingDays)));
        }
        return false;
    }

    private PeriodResolver createPeriodResolver(List<Period> periods, String date, String timeZone) throws Exception {
        PeriodResolver pr = new PeriodResolver(settings);
        for (Period p : periods) {
            Period period = new Period();
            period.setBegin(p.getBegin());
            period.setEnd(p.getEnd());
            period.setRepeat(p.getRepeat());
            period.setSingleStart(p.getSingleStart());
            period.setWhenHoliday(p.getWhenHoliday());
            try {
                pr.addStartTimes(period, date, timeZone);
            } catch (Throwable e) {
                throw new Exception(String.format("[%s][timeZone=%s][%s]%s", date, timeZone, DailyPlanHelper.toString(period), e.toString()), e);
            }
        }
        return pr;
    }

    private DBItemDailyPlanSubmission addDailyPlanSubmission(String controllerId, Date dailyPlanDate) throws JocConfigurationException,
            DBConnectionRefusedException, SOSHibernateException, ParseException {

        SOSHibernateSession session = null;
        try {
            DBItemDailyPlanSubmission item = new DBItemDailyPlanSubmission();
            item.setControllerId(controllerId);
            item.setSubmissionForDate(dailyPlanDate);
            item.setUserAccount(settings.getUserAccount());
            item.setCreated(settings.getSubmissionTime());

            session = Globals.createSosHibernateStatelessConnection("addDailyPlanSubmission");
            session.setAutoCommit(false);
            Globals.beginTransaction(session);
            session.save(item);
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

    private Calendar getEveryDayCalendar(Long id, String name) {
        WeekDays wDays = new WeekDays();
        wDays.setDays(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        Frequencies includes = new Frequencies();
        includes.setWeekdays(Collections.singletonList(wDays));
        Calendar cal = new Calendar();
        cal.setIncludes(includes);
        cal.setName(name);
        cal.setId(id);
        return cal;
    }

}