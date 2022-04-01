package com.sos.joc.dailyplan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
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
import com.sos.joc.dailyplan.db.DBLayerReleasedConfigurations;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
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

public class DailyPlanRunner extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanRunner.class);
    private static final String IDENTIFIER = DailyPlanRunner.class.getSimpleName();
    private static final String UTC = "UTC";

    private AtomicLong lastActivityStart = new AtomicLong(0);
    private AtomicLong lastActivityEnd = new AtomicLong(0);
    private DailyPlanSettings settings;
    private java.util.Calendar startCalendar;
    private Map<String, String> nonWorkingDays;
    private Map<String, Long> durations = null;
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

        String method = "createPlan";
        try {
            lastActivityStart.set(new Date().getTime());
            durations = null;

            LOGGER.info(String.format("[%s][%s]creating from %s for %s days ahead, submitting for %s days ahead", startupMode, method, SOSDate
                    .getDateAsString(calendar), settings.getDayAheadPlan(), settings.getDayAheadSubmit()));

            java.util.Calendar savCalendar = java.util.Calendar.getInstance();
            savCalendar.setTime(calendar.getTime());
            for (ControllerConfiguration conf : controllers) {
                String controllerId = conf.getCurrent().getId();

                Collection<DailyPlanSchedule> controllerSchedules = convert(getReleasedSchedule2DeployedWorkflow(controllerId), true);

                java.util.Calendar dailyPlanCalendar = java.util.Calendar.getInstance();
                dailyPlanCalendar.setTime(savCalendar.getTime());
                settings.setDailyPlanDate(dailyPlanCalendar.getTime());

                if (controllerSchedules.size() > 0) {
                    LOGGER.info(String.format("[%s][%s][%s][Plan Order automatically]found %s schedules", startupMode, method, controllerId,
                            controllerSchedules.size()));

                    for (int day = 0; day < settings.getDayAheadPlan(); day++) {
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
    public Map<PlannedOrderKey, PlannedOrder> generateDailyPlan(StartupMode startupMode, String controllerId,
            Collection<DailyPlanSchedule> dailyPlanSchedules, String dailyPlanDate, Boolean withSubmit, JocError jocError, String accessToken)
            throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, IOException, ParseException, SOSException, URISyntaxException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, InterruptedException, ExecutionException, TimeoutException {
        return generateDailyPlan(startupMode, controllerId, dailyPlanSchedules, dailyPlanDate, null, withSubmit, jocError, accessToken);
    }

    /* DailyPlanModifyOrderImpl **/
    public Map<PlannedOrderKey, PlannedOrder> generateDailyPlan(StartupMode startupMode, String controllerId,
            Collection<DailyPlanSchedule> dailyPlanSchedules, String dailyPlanDate, DBItemDailyPlanSubmission submission, Boolean withSubmit,
            JocError jocError, String accessToken) throws JsonParseException, JsonMappingException, DBConnectionRefusedException,
            DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, IOException, ParseException,
            SOSException, URISyntaxException, ControllerConnectionResetException, ControllerConnectionRefusedException, InterruptedException,
            ExecutionException, TimeoutException {

        String operation = withSubmit ? "creating/submitting" : "creating";
        if (dailyPlanSchedules == null || dailyPlanSchedules.size() == 0) {
            LOGGER.info(String.format("[%s][%s][%s][%s][skip]found 0 schedules", startupMode, operation, controllerId, dailyPlanDate));
            // TODO initialize OrderListSynchronizer before calculateStartTimes and return synchronizer.getPlannedOrders()
            return new TreeMap<PlannedOrderKey, PlannedOrder>();
        }

        OrderListSynchronizer synchronizer = calculateStartTimes(startupMode, controllerId, dailyPlanSchedules, dailyPlanDate, submission);
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

            LOGGER.info(String.format("[%s][%s][%s][%s][calculated][%s][submission created=%s, id=%s]", startupMode, operation, controllerId,
                    dailyPlanDate, c, submissionCreated, submissionId));

            calculateDurations(controllerId, dailyPlanDate, dailyPlanSchedules);

            synchronizer.addPlannedOrderToControllerAndDB(startupMode, operation, controllerId, dailyPlanDate, withSubmit, durations);
            EventBus.getInstance().post(new DailyPlanEvent(dailyPlanDate));
        } else {
            LOGGER.info(String.format("[%s][%s][%s][%s][skip]%s", startupMode, operation, controllerId, dailyPlanDate, c));
        }
        return synchronizer.getPlannedOrders();
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

                DailyPlanScheduleWorkflow dailyPlanScheduleWorkflow = new DailyPlanScheduleWorkflow(item.getWorkflowName(), item.getWorkflowPath(),
                        null);
                FreshOrder freshOrder = buildFreshOrder(dailyPlanDate, schedule, dailyPlanScheduleWorkflow, variableSet, item.getPlannedStart()
                        .getTime(), item.getStartMode());
                freshOrder.setId(item.getOrderId());

                DailyPlanSchedule dailyPlanSchedule = new DailyPlanSchedule(schedule, Arrays.asList(dailyPlanScheduleWorkflow));
                PlannedOrder p = new PlannedOrder(item.getControllerId(), freshOrder, dailyPlanSchedule, dailyPlanScheduleWorkflow, item
                        .getCalendarId());
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

    // DailyPlanOrdersGenerateImpl, SchedulesImpl
    public Collection<DailyPlanSchedule> convert(List<DBBeanReleasedSchedule2DeployedWorkflow> items, Set<Folder> permittedFolders,
            Map<String, Boolean> checkedFolders, boolean infoOnMissingDeployedWorkflow) {
        return convert(items, false, true, permittedFolders, checkedFolders, infoOnMissingDeployedWorkflow);
    }

    private Collection<DailyPlanSchedule> convert(List<DBBeanReleasedSchedule2DeployedWorkflow> items, boolean onlyPlanOrderAutomatically,
            boolean checkPermissions, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders, boolean infoOnMissingDeployedWorkflow) {
        if (items == null || items.size() == 0) {
            return new ArrayList<DailyPlanSchedule>();
        }

        String method = "convert";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
                    schedule = objectMapper.readValue(item.getScheduleContent(), Schedule.class);
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
                                Workflow w = objectMapper.readValue(dpw.getContent(), Workflow.class);
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

    private FreshOrder buildFreshOrder(String dailyPlanDate, Schedule schedule, DailyPlanScheduleWorkflow scheduleWorkflow, VariableSet variableSet,
            Long startTime, Integer startMode) throws SOSInvalidDataException {
        FreshOrder order = new FreshOrder();
        order.setId(DailyPlanHelper.buildOrderId(dailyPlanDate, schedule, variableSet, startTime, startMode));
        order.setScheduledFor(startTime);
        order.setArguments(variableSet.getVariables());
        order.setWorkflowPath(scheduleWorkflow.getName());
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

    private OrderListSynchronizer calculateStartTimes(StartupMode startupMode, String controllerId, Collection<DailyPlanSchedule> dailyPlanSchedules,
            String dailyPlanDate, DBItemDailyPlanSubmission submission) throws JsonParseException, JsonMappingException, DBConnectionRefusedException,
            DBInvalidDataException, DBMissingDataException, IOException, SOSMissingDataException, SOSInvalidDataException, ParseException,
            JocConfigurationException, SOSHibernateException, DBOpenSessionException {

        String method = "calculateStartTimes";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean isTraceEnabled = LOGGER.isTraceEnabled();

        Date date = SOSDate.getDate(dailyPlanDate);
        Date actDate = date;
        Date nextDate = DailyPlanHelper.getNextDay(date, settings);
        boolean fromService = isFromService(startupMode);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s][dailyPlanDate=%s]actDate=%s,nextDate=%s", startupMode, method, controllerId, dailyPlanDate,
                    SOSDate.getDateAsString(actDate), SOSDate.getDateAsString(nextDate)));
        }

        Map<String, Calendar> calendars = new HashMap<String, Calendar>();
        OrderListSynchronizer synchronizer = new OrderListSynchronizer(settings);
        if (submission != null) {
            synchronizer.setSubmission(submission);
        }
        for (DailyPlanSchedule dailyPlanSchedule : dailyPlanSchedules) {
            Schedule schedule = dailyPlanSchedule.getSchedule();

            if (fromService && !schedule.getPlanOrderAutomatically()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][%s][skip schedule=%s]fromService=true, plan order automatically=false", startupMode,
                            method, controllerId, dailyPlanDate, schedule.getPath()));
                }
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][%s]schedule=%s", startupMode, method, controllerId, dailyPlanDate, schedule.getPath()));
                }
                generateNonWorkingDays(controllerId, schedule, date, dailyPlanDate);

                for (AssignedCalendars assignedCalendar : schedule.getCalendars()) {
                    if (assignedCalendar.getTimeZone() == null) {
                        assignedCalendar.setTimeZone(UTC);
                    }

                    String actDateAsString = SOSDate.getDateAsString(actDate);
                    String nextDateAsString = SOSDate.getDateAsString(nextDate);
                    String dailyPlanDateAsString = SOSDate.getDateAsString(date);

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][%s][%s][calendar=%s][timeZone=%s][actDate=%s][nextDate=%s]dailyPlanDate=%s",
                                startupMode, method, controllerId, dailyPlanDate, schedule.getPath(), assignedCalendar.getCalendarName(),
                                assignedCalendar.getTimeZone(), actDateAsString, nextDateAsString, dailyPlanDateAsString));
                    }

                    String calendarsKey = assignedCalendar.getCalendarName() + "#" + schedule.getPath();
                    Calendar calendar = calendars.get(calendarsKey);
                    if (calendar == null) {
                        try {
                            calendar = getCalendar(controllerId, assignedCalendar.getCalendarName(), ConfigurationType.WORKINGDAYSCALENDAR);
                        } catch (DBMissingDataException e) {
                            LOGGER.warn(String.format("[%s][%s][%s][%s][%s][WorkingDayCalendar=%s][skip]not found", startupMode, method, controllerId,
                                    dailyPlanDate, schedule.getPath(), assignedCalendar.getCalendarName()));
                            continue;
                        }
                        calendars.put(calendarsKey, calendar);
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][%s][%s][%s][WorkingDayCalendar=%s][cache]%s", startupMode, method, controllerId,
                                    dailyPlanDate, schedule.getPath(), assignedCalendar.getCalendarName(), SOSString.toString(calendar)));
                        }
                    }
                    calendar.setFrom(actDateAsString);
                    calendar.setTo(nextDateAsString);

                    Calendar restrictions = new Calendar();
                    restrictions.setIncludes(assignedCalendar.getIncludes());
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][%s][%s][WorkingDayCalendar=%s][includes]%s", startupMode, method, controllerId,
                                dailyPlanDate, schedule.getPath(), assignedCalendar.getCalendarName(), SOSString.toString(restrictions)));
                    }

                    int plannedOrdersCount = 0;
                    List<String> dates = new FrequencyResolver().resolveRestrictions(calendar, restrictions, actDateAsString, nextDateAsString)
                            .getDates();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][%s][%s][calendar=%s][FrequencyResolver]dates=%s", startupMode, method, controllerId,
                                dailyPlanDate, schedule.getPath(), assignedCalendar.getCalendarName(), String.join(",", dates)));
                    }
                    if (dates.size() > 0) {
                        PeriodResolver periodResolver = new PeriodResolver(settings);
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
                                LOGGER.trace(String.format("[%s][%s][%s][%s][%s][calendar=%s]date=%s", startupMode, method, controllerId,
                                        dailyPlanDate, schedule.getPath(), assignedCalendar.getCalendarName(), d));
                            }
                            if (nonWorkingDays != null && nonWorkingDays.get(d) != null) {
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("[%s][%s][%s][%s][%s][calendar=%s][date=%s][skip]date is a non working day",
                                            startupMode, method, controllerId, dailyPlanDate, schedule.getPath(), assignedCalendar.getCalendarName(),
                                            d));
                                }
                            } else {
                                Map<Long, Period> startTimes = periodResolver.getStartTimes(d, dailyPlanDateAsString, assignedCalendar.getTimeZone());

                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format(
                                            "[%s][%s][%s][%s][%s][calendar=%s][timeZone=%s][date=%s][periodResolver]startTimes size=%s", startupMode,
                                            method, controllerId, dailyPlanDate, schedule.getPath(), assignedCalendar.getCalendarName(),
                                            assignedCalendar.getTimeZone(), d, startTimes.size()));
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
                                        for (DailyPlanScheduleWorkflow sw : dailyPlanSchedule.getWorkflows()) {

                                            FreshOrder freshOrder = buildFreshOrder(dailyPlanDate, dailyPlanSchedule.getSchedule(), sw, variableSet,
                                                    periodEntry.getKey(), startMode);

                                            if (!fromService) {
                                                schedule.setSubmitOrderToControllerWhenPlanned(settings.isSubmit());
                                            }

                                            if (synchronizer.getSubmission() == null) {
                                                synchronizer.setSubmission(addDailyPlanSubmission(controllerId, date));
                                            }

                                            PlannedOrder plannedOrder = new PlannedOrder(controllerId, freshOrder, dailyPlanSchedule, sw, calendar
                                                    .getId());
                                            plannedOrder.setPeriod(periodEntry.getValue());
                                            plannedOrder.setSubmissionHistoryId(synchronizer.getSubmission().getId());
                                            plannedOrder.setOrderName(DailyPlanHelper.getOrderName(schedule, variableSet));
                                            synchronizer.add(startupMode, plannedOrder, controllerId, dailyPlanDate);
                                            plannedOrdersCount++;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (plannedOrdersCount == 0) {
                        LOGGER.info(String.format("[%s][%s][%s][%s][skip][schedule=%s][calendar=%s][timeZone=%s]0 planned orders", startupMode,
                                method, controllerId, dailyPlanDate, schedule.getPath(), assignedCalendar.getCalendarName(), assignedCalendar
                                        .getTimeZone()));

                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][%s][%s][%s][calendar=%s]%s planned orders", startupMode, method, controllerId,
                                    dailyPlanDate, schedule.getPath(), assignedCalendar.getCalendarName(), plannedOrdersCount));
                        }
                    }
                }
            }
        }
        return synchronizer;
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

}