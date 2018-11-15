package com.sos.webservices.order.initiator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.jobscheduler.db.calendar.DBItemInventoryClusterCalendar;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.db.calendars.CalendarsDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.UnknownJobSchedulerMasterException;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.calendar.Frequencies;
import com.sos.webservices.order.initiator.classes.Globals;
import com.sos.webservices.order.initiator.classes.PlannedOrder;
import com.sos.webservices.order.initiator.model.AssignedCalendars;
import com.sos.webservices.order.initiator.model.AssignedNonWorkingCalendars;
import com.sos.webservices.order.initiator.model.NameValuePair;
import com.sos.webservices.order.initiator.model.OrderTemplate;
import com.sos.webservices.order.initiator.model.Period;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderInitiatorRunner extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorRunner.class);
    private List<OrderTemplate> listOfOrderTemplates;
    private Map<String, String> listOfNonWorkingDays;
    private SOSHibernateFactory sosHibernateFactory;
    private int dayOffset = 2;

    public OrderInitiatorRunner(OrderInitiatorSettings orderInitiatorSettings) {
        Globals.orderInitiatorSettings = orderInitiatorSettings;
    }

    public void run() {

        try {
            readTemplates();
            OrderListSynchronizer orderListSynchronizer = calculateStartTimes();
            orderListSynchronizer.removeAllOrdersFromMaster();
            orderListSynchronizer.addOrdersToMaster();

        } catch (IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException | ParseException
                | UnknownJobSchedulerMasterException | SOSException | URISyntaxException | JocConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void exit() {
        if (sosHibernateFactory != null) {
            sosHibernateFactory.close();
        }
    }

    private void readTemplates() throws IOException {
        // TODO OrderTemplateSourceDB implementieren.
        OrderTemplateSource orderTemplateSource = new OrderTemplateSourceFile("src/test/resources/orderTemplates");
        OrderTemplates orderTemplates = new OrderTemplates();
        orderTemplates.fillListOfOrderTemplates(orderTemplateSource);
        listOfOrderTemplates = orderTemplates.getListOfOrderTemplates();
    }

    private Calendar getCalendar(String masterId, String calendarName) throws DBMissingDataException, JsonParseException, JsonMappingException,
            IOException, DBConnectionRefusedException, DBInvalidDataException, SOSHibernateOpenSessionException, SOSHibernateConfigurationException,
            SOSHibernateFactoryBuildException, JocConfigurationException {

        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");

        try {
            CalendarsDBLayer dbLayer = new CalendarsDBLayer(sosHibernateSession);
            DBItemInventoryClusterCalendar calendarItem = null;
            calendarName = Globals.normalizePath(calendarName);
            calendarItem = dbLayer.getCalendar(masterId, calendarName);
            if (calendarItem == null) {
                throw new DBMissingDataException(String.format("calendar '%1$s' not found for instance %s", calendarName, masterId));
            }

            Calendar calendar = new ObjectMapper().readValue(calendarItem.getConfiguration(), Calendar.class);
            calendar.setId(calendarItem.getId());
            calendar.setPath(calendarItem.getName());
            calendar.setName(calendarItem.getBaseName());
            return calendar;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private FreshOrder buildFreshOrder(OrderTemplate o, Long startTime) {
        Variables variables = new Variables();
        for (NameValuePair param : o.getVariables()) {
            variables.setAdditionalProperty(param.getName(), param.getValue());
        }
        FreshOrder freshOrder = new FreshOrder();
        freshOrder.setId(o.getOrderName() + "_" + startTime);
        freshOrder.setScheduledAt(startTime);
        freshOrder.setVariables(variables);
        freshOrder.setWorkflowPath(o.getWorkflowPath());
        return freshOrder;
    }

    private String todayAsString() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone(DateTimeZone.getDefault().getID()));
        Date now = new Date();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String froms = formatter.format(now);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        return froms;
    }

    private String toDateAsString() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone(DateTimeZone.getDefault().getID()));
        Date now = new Date();
        if (dayOffset > 0) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(now);
            calendar.add(GregorianCalendar.DAY_OF_MONTH, dayOffset);
            now = calendar.getTime();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String tos = formatter.format(now);

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        return tos;
    }

    private void generateNonWorkingDays(OrderTemplate o, String masterId) throws SOSMissingDataException, SOSInvalidDataException, JsonParseException,
            JsonMappingException, DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException, IOException, ParseException,
            SOSHibernateOpenSessionException, SOSHibernateConfigurationException, SOSHibernateFactoryBuildException, JocConfigurationException {
        if (o.getNonWorkingCalendars() != null) {
            FrequencyResolver fr = new FrequencyResolver();
            for (AssignedNonWorkingCalendars assignedNonWorkingCalendars : o.getNonWorkingCalendars()) {
                LOGGER.debug("Generate non working dates for:" + assignedNonWorkingCalendars.getCalendarPath());
                listOfNonWorkingDays = new HashMap<String, String>();
                Calendar calendar = getCalendar(masterId, assignedNonWorkingCalendars.getCalendarPath());
                CalendarDatesFilter calendarFilter = new CalendarDatesFilter();

                calendarFilter.setDateFrom(todayAsString());
                calendarFilter.setDateTo(toDateAsString());
                calendarFilter.setCalendar(calendar);
                fr.resolve(calendarFilter);
            }
            Set<String> s = fr.getDates().keySet();
            for (String d : s) {
                LOGGER.trace("Non working date: " + d);
                listOfNonWorkingDays.put(d, masterId);
            }
        }
    }

    private OrderListSynchronizer calculateStartTimes() throws SOSHibernateOpenSessionException, SOSHibernateConfigurationException,
            SOSHibernateFactoryBuildException, JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException,
            DBMissingDataException, IOException, SOSMissingDataException, SOSInvalidDataException, ParseException, UnknownJobSchedulerMasterException,
            JocConfigurationException {

        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");

        try {
            InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(sosHibernateSession);
            //CalendarDatesFilter calendarFilter = new CalendarDatesFilter();

            OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer();
            DBItemInventoryInstance dbItemInventoryInstance = null;
            for (OrderTemplate o : listOfOrderTemplates) {
                if (dbItemInventoryInstance == null || (!dbItemInventoryInstance.getHostname().equals(o.getHostName()) && !dbItemInventoryInstance
                        .getPort().equals(o.getPort()) && !dbItemInventoryInstance.getSchedulerId().equals(o.getMasterId()))) {
                    dbItemInventoryInstance = dbLayer.getInventoryInstanceByHostPort(o.getHostName(), o.getPort(), o.getMasterId());
                }

                generateNonWorkingDays(o, dbItemInventoryInstance.getSchedulerId());

                for (AssignedCalendars assignedCalendars : o.getCalendars()) {
                    
                    FrequencyResolver fr = new FrequencyResolver();
                    LOGGER.debug("Generate dates for:" + assignedCalendars.getCalendarPath());
                    Calendar calendar = getCalendar(dbItemInventoryInstance.getSchedulerId(), assignedCalendars.getCalendarPath());
                    Calendar restrictions = new Calendar();
                    String calendarJson = new ObjectMapper().writeValueAsString(calendar);
                    restrictions.setIncludes(assignedCalendars.getIncludes());
                    calendar.setFrom(todayAsString());
                    calendar.setTo(toDateAsString());

                    fr.resolveRestrictions(calendarJson, calendarJson, todayAsString(), toDateAsString());
                    Set<String> s = fr.getDates().keySet();
                    PeriodResolver periodResolver = new PeriodResolver();
                    for (Period p : assignedCalendars.getPeriods()) {
                        periodResolver.addStartTimes(p);
                    }

                    for (String d : s) {
                        LOGGER.trace("Date: " + d);
                        if (listOfNonWorkingDays != null && listOfNonWorkingDays.get(d) != null) {
                            LOGGER.trace(d + "will be ignored as it is a non working day");
                        } else {
                            for (Long startTime : periodResolver.getStartTimes(d)) {
                                FreshOrder freshOrder = buildFreshOrder(o, startTime);
                                PlannedOrder plannedOrder = new PlannedOrder();
                                plannedOrder.setFreshOrder(freshOrder);
                                plannedOrder.setCalendarId(calendar.getId());
                                plannedOrder.setOrderTemplate(o);
                                orderListSynchronizer.add(plannedOrder);
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

}