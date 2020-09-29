package com.sos.js7.order.initiator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.orders.DBItemDailyPlanSubmissionHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;
import com.sos.js7.order.initiator.classes.PlannedOrder;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanSubmissionHistory;
import com.sos.webservices.order.initiator.model.AssignedNonWorkingCalendars;
import com.sos.webservices.order.initiator.model.NameValuePair;
import com.sos.webservices.order.initiator.model.OrderTemplate;
import com.sos.webservices.order.initiator.model.Period;

public class OrderInitiatorRunner extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorRunner.class);
    private List<OrderTemplate> listOfOrderTemplates;
    private boolean fromService = true;

    public List<OrderTemplate> getListOfOrderTemplates() {
        return listOfOrderTemplates;
    }

    private Map<String, String> listOfNonWorkingDays;
    private SOSHibernateFactory sosHibernateFactory;
    private OrderListSynchronizer orderListSynchronizer;

    public OrderInitiatorRunner(OrderInitiatorSettings orderInitiatorSettings, boolean fromService) {
        OrderInitiatorGlobals.orderInitiatorSettings = orderInitiatorSettings;
        this.fromService = fromService;
    }

    public void generateDailyPlan(String dailyPlanDate) throws JsonParseException, JsonMappingException, DBConnectionRefusedException,
            DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, IOException, ParseException,
            SOSException, URISyntaxException, JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, InterruptedException,
            ExecutionException, TimeoutException {

        orderListSynchronizer = calculateStartTimes(dailyPlanDate);
        if (orderListSynchronizer.getListOfPlannedOrders().size() > 0) {
            orderListSynchronizer.addPlannedOrderToControllerAndDB();
        }
    }

    public void run() {

        try {

            OrderTemplateSource orderTemplateSource = new OrderTemplateSourceDB(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId());
            readTemplates(orderTemplateSource);
            java.util.Calendar calendar = java.util.Calendar.getInstance();

            for (int day = 0; day < OrderInitiatorGlobals.orderInitiatorSettings.getDayOffset(); day++) {
                // generateDailyPlan(calendar);
                calendar.add(java.util.Calendar.DATE, 1);
            }

        } catch (IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException | JocConfigurationException
                | DBOpenSessionException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (SOSHibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void exit() {
        if (sosHibernateFactory != null) {
            sosHibernateFactory.close();
        }
    }

    public void readTemplates(OrderTemplateSource orderTemplateSource) throws IOException, SOSHibernateException {
        LOGGER.debug("... readTemplates " + orderTemplateSource.fromSource());
        OrderTemplates orderTemplates = new OrderTemplates();
        orderTemplates.fillListOfOrderTemplates(orderTemplateSource);
        listOfOrderTemplates = orderTemplates.getListOfOrderTemplates();
    }

    private Calendar getCalendar(String controllerId, String calendarName) throws DBMissingDataException, JsonParseException, JsonMappingException,
            IOException, DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException, DBOpenSessionException,
            SOSHibernateException {

        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
        InventoryDBLayer dbLayer = new InventoryDBLayer(sosHibernateSession);

        try {

            calendarName = Globals.normalizePath(calendarName);
            // TODO getConfiguration with list of types
            DBItemInventoryConfiguration config = dbLayer.getConfiguration(calendarName, Arrays.asList(ConfigurationType.WORKINGDAYSCALENDAR
                    .intValue(), ConfigurationType.NONWORKINGDAYSCALENDAR.intValue()));
            if (config == null) {
                throw new DBMissingDataException(String.format("calendar '%s' not found for controller instance %s", calendarName, controllerId));
            }

            Calendar calendar = new ObjectMapper().readValue(config.getContent(), Calendar.class);
            calendar.setId(config.getId());
            calendar.setPath(config.getName());
            calendar.setName(config.getName());
            return calendar;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private String getDailyPlanDate(Long startTime) {
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(new Date(startTime));
        return this.dayAsString(calendar);
    }

    private String buildOrderKey(OrderTemplate o, Long startTime) {
        Path path = Paths.get(o.getPath());
        String shortOrderTemplateName = path.getFileName().toString().substring(0, 30);
        return this.getDailyPlanDate(startTime) + "#P" + "<id" + startTime + ">-" + shortOrderTemplateName;
    }

    private FreshOrder buildFreshOrder(OrderTemplate o, Long startTime) {
        Variables variables = new Variables();
        for (NameValuePair param : o.getVariables()) {
            variables.setAdditionalProperty(param.getName(), param.getValue());
        }
        FreshOrder freshOrder = new FreshOrder();
        freshOrder.setId(this.buildOrderKey(o, startTime));
        freshOrder.setScheduledFor(startTime);
        freshOrder.setArguments(variables);
        freshOrder.setWorkflowPath(o.getWorkflowPath());
        return freshOrder;
    }

    private String dayAsString(java.util.Calendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateS = formatter.format(calendar.getTime());
        return dateS;
    }

    private void generateNonWorkingDays(String dailyPlanDate, OrderTemplate o, String controllerId) throws SOSMissingDataException,
            SOSInvalidDataException, JsonParseException, JsonMappingException, DBMissingDataException, DBConnectionRefusedException,
            DBInvalidDataException, IOException, ParseException, JocConfigurationException, DBOpenSessionException, SOSHibernateException {

        String nextDate = this.getNextDay(dailyPlanDate);

        if (o.getNonWorkingCalendars() != null) {
            FrequencyResolver fr = new FrequencyResolver();
            for (AssignedNonWorkingCalendars assignedNonWorkingCalendars : o.getNonWorkingCalendars()) {
                LOGGER.debug("Generate non working dates for:" + assignedNonWorkingCalendars.getCalendarPath());
                listOfNonWorkingDays = new HashMap<String, String>();
                Calendar calendar = getCalendar(controllerId, assignedNonWorkingCalendars.getCalendarPath());
                CalendarDatesFilter calendarFilter = new CalendarDatesFilter();

                calendarFilter.setDateFrom(dailyPlanDate);
                calendarFilter.setDateTo(nextDate);
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

    private String getNextDay(String dailyPlanDate) throws ParseException {
        TimeZone timeZone = TimeZone.getTimeZone(OrderInitiatorGlobals.orderInitiatorSettings.getTimeZone());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dateForPlan = formatter.parse(dailyPlanDate);

        java.util.Calendar calendar = java.util.Calendar.getInstance(timeZone);

        calendar.setTime(dateForPlan);
        calendar.add(java.util.Calendar.DATE, 1);
        return this.dayAsString(calendar);
    }

    private OrderListSynchronizer calculateStartTimes(String dailyPlanDate) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, IOException, SOSMissingDataException,
            SOSInvalidDataException, ParseException, JocConfigurationException, SOSHibernateException, DBOpenSessionException {

        LOGGER.debug(String.format("... calculateStartTimes for %s", dailyPlanDate));

        // TODO:
        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties("/order_configuration.properties");
        }
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
        String actDate = dailyPlanDate;
        String nextDate = this.getNextDay(dailyPlanDate);

        try {
            OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer();
            for (OrderTemplate orderTemplate : listOfOrderTemplates) {
                if (fromService && !orderTemplate.getPlanOrderAutomatically()) {
                    LOGGER.debug(String.format("... orderTemplate %s  will not be planned automatically", orderTemplate.getPath()));
                } else {
                    String controllerId = orderTemplate.getControllerId();

                    DBItemDailyPlanSubmissionHistory dbItemDailyPlanSubmissionHistory = addDailyPlanSubmission(sosHibernateSession, controllerId,
                            dailyPlanDate);

                    generateNonWorkingDays(dailyPlanDate, orderTemplate, controllerId);

                    for (com.sos.webservices.order.initiator.model.AssignedCalendars assignedCalendar : orderTemplate.getCalendars()) {

                        FrequencyResolver fr = new FrequencyResolver();
                        LOGGER.debug("Generate dates for:" + assignedCalendar.getCalendarPath());
                        Calendar calendar = getCalendar(controllerId, assignedCalendar.getCalendarPath());
                        Calendar restrictions = new Calendar();

                        calendar.setFrom(actDate);
                        calendar.setTo(nextDate);
                        String calendarJson = new ObjectMapper().writeValueAsString(calendar);
                        restrictions.setIncludes(assignedCalendar.getIncludes());

                        fr.resolveRestrictions(calendarJson, calendarJson, actDate, nextDate);
                        Set<String> s = fr.getDates().keySet();
                        PeriodResolver periodResolver = new PeriodResolver();
                        for (Period p : assignedCalendar.getPeriods()) {
                            periodResolver.addStartTimes(p, dailyPlanDate, assignedCalendar.getTimeZone());
                        }

                        for (String d : s) {
                            LOGGER.trace("Date: " + d);
                            if (listOfNonWorkingDays != null && listOfNonWorkingDays.get(d) != null) {
                                LOGGER.trace(d + "will be ignored as it is a non working day");
                            } else {
                                for (Entry<Long, Period> startTime : periodResolver.getStartTimes(d, dailyPlanDate).entrySet()) {
                                    FreshOrder freshOrder = buildFreshOrder(orderTemplate, startTime.getKey());
                                    PlannedOrder plannedOrder = new PlannedOrder();
                                    plannedOrder.setFreshOrder(freshOrder);
                                    plannedOrder.setCalendarId(calendar.getId());
                                    plannedOrder.setPeriod(startTime.getValue());
                                    plannedOrder.setSubmissionHistoryId(dbItemDailyPlanSubmissionHistory.getId());
                                    if (!fromService) {
                                        orderTemplate.setSubmitOrderToControllerWhenPlanned(OrderInitiatorGlobals.orderInitiatorSettings.isSubmit());
                                    }
                                    plannedOrder.setOrderTemplate(orderTemplate);
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

    private DBItemDailyPlanSubmissionHistory addDailyPlanSubmission(SOSHibernateSession sosHibernateSession, String controllerId,
            String dailyPlanDate) throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException, ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dateForPlan = formatter.parse(dailyPlanDate);

        DBLayerDailyPlanSubmissionHistory dbLayer = new DBLayerDailyPlanSubmissionHistory(sosHibernateSession);

        DBItemDailyPlanSubmissionHistory dbItemDailyPlanSubmissionHistory = new DBItemDailyPlanSubmissionHistory();
        dbItemDailyPlanSubmissionHistory.setControllerId(controllerId);
        dbItemDailyPlanSubmissionHistory.setSubmissionForDate(dateForPlan);
        dbItemDailyPlanSubmissionHistory.setUserAccount(OrderInitiatorGlobals.orderInitiatorSettings.getUserAccount());

        Globals.beginTransaction(sosHibernateSession);
        dbLayer.storePlan(dbItemDailyPlanSubmissionHistory);
        Globals.commit(sosHibernateSession);
        return dbItemDailyPlanSubmissionHistory;
    }

}