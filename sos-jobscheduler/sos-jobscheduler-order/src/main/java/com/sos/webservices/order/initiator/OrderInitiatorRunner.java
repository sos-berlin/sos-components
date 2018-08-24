package com.sos.webservices.order.initiator;

import java.io.IOException;
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
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.joc.Globals;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.db.calendars.CalendarsDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.UnknownJobSchedulerMasterException;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.webservices.db.calendar.DBItemCalendar;
import com.sos.webservices.db.inventory.instance.DBItemInventoryInstance;
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
    private Map<String, Long> listOfNonWorkingDays;
    private SOSHibernateSession session;
    private int dayOffset = 10;

    public void run() {

        readSettings();
        try {
            session = getSession("D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/reporting.hibernate.cfg.xml");
            readTemplates();
            OrderListSynchronizer orderListSynchronizer = calculateStartTimes();
            orderListSynchronizer.removeAllOrdersFromMaster();
            orderListSynchronizer.addOrdersToMaster();

        } catch (IOException | SOSHibernateOpenSessionException | SOSHibernateConfigurationException | SOSHibernateFactoryBuildException
                | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException | SOSMissingDataException | SOSInvalidDataException
                | ParseException | UnknownJobSchedulerMasterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        addOrders();
    }

    private void readSettings() {

    }

    private SOSHibernateSession getSession(String confFile) throws SOSHibernateOpenSessionException, SOSHibernateConfigurationException,
            SOSHibernateFactoryBuildException {
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
        sosHibernateFactory.addClassMapping(DBLayer.getOrderInitatorClassMapping());
        sosHibernateFactory.build();
        return sosHibernateFactory.openStatelessSession();
    }

    private void readTemplates() throws IOException {
        // TODO OrderTemplateSourceDB implementieren.
        OrderTemplateSource orderTemplateSource = new OrderTemplateSourceFile("C:/temp/orderTemplates");
        OrderTemplates orderTemplates = new OrderTemplates();
        orderTemplates.fillListOfOrderTemplates(orderTemplateSource);
        listOfOrderTemplates = orderTemplates.getListOfOrderTemplates();
    }

    private Calendar getCalendar(Long instanceId, String calendarName) throws DBMissingDataException, JsonParseException, JsonMappingException,
            IOException, DBConnectionRefusedException, DBInvalidDataException {

        CalendarsDBLayer dbLayer = new CalendarsDBLayer(session);
        DBItemCalendar calendarItem = null;
        calendarName = Globals.normalizePath(calendarName);
        calendarItem = dbLayer.getCalendar(instanceId, calendarName);
        if (calendarItem == null) {
            throw new DBMissingDataException(String.format("calendar '%1$s' not found for instance %s", calendarName, instanceId));
        }

        Calendar calendar = new ObjectMapper().readValue(calendarItem.getConfiguration(), Calendar.class);
        calendar.setId(calendarItem.getId());
        calendar.setPath(calendarItem.getName());
        calendar.setName(calendarItem.getBaseName());
        return calendar;
    }

    private FreshOrder buildFreshOrder(OrderTemplate o, Long startTime) {
        Variables variables = new Variables();
        for (NameValuePair param : o.getVariables()) {
            variables.setAdditionalProperty(param.getName(), param.getValue());
        }
        FreshOrder freshOrder = new FreshOrder();
        freshOrder.setOrderId(o.getOrderKey());
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

    private void generateNonWorkingDays(OrderTemplate o, Long instanceId) throws SOSMissingDataException, SOSInvalidDataException, JsonParseException,
            JsonMappingException, DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException, IOException, ParseException {
        if (o.getNonWorkingCalendars() != null) {
            FrequencyResolver fr = new FrequencyResolver();
            for (AssignedNonWorkingCalendars assignedNonWorkingCalendars : o.getNonWorkingCalendars()) {
                LOGGER.debug("Generate non working dates for:" + assignedNonWorkingCalendars.getCalendarPath());
                listOfNonWorkingDays = new HashMap<String, Long>();
                Calendar calendar = getCalendar(instanceId, assignedNonWorkingCalendars.getCalendarPath());
                CalendarDatesFilter calendarFilter = new CalendarDatesFilter();

                calendarFilter.setDateFrom(todayAsString());
                calendarFilter.setDateTo(toDateAsString());
                calendarFilter.setCalendar(calendar);
                fr.resolve(calendarFilter);
            }
            Set<String> s = fr.getDates().keySet();
            for (String d : s) {
                LOGGER.trace("Non working date: " + d);
                listOfNonWorkingDays.put(d, instanceId);
            }
        }
    }

    private OrderListSynchronizer calculateStartTimes() throws SOSHibernateOpenSessionException, SOSHibernateConfigurationException,
            SOSHibernateFactoryBuildException, JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException,
            DBMissingDataException, IOException, SOSMissingDataException, SOSInvalidDataException, ParseException,
            UnknownJobSchedulerMasterException {

        InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(session);
        CalendarDatesFilter calendarFilter = new CalendarDatesFilter();

        OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer();
        DBItemInventoryInstance dbItemInventoryInstance = null;
        for (OrderTemplate o : listOfOrderTemplates) {
            if (dbItemInventoryInstance == null || (!dbItemInventoryInstance.getHostname().equals(o.getHostName()) && !dbItemInventoryInstance
                    .getPort().equals(o.getPort()) && !dbItemInventoryInstance.getSchedulerId().equals(o.getSchedulerId()))) {
                dbItemInventoryInstance = dbLayer.getInventoryInstanceByHostPort(o.getHostName(), o.getPort(), o.getSchedulerId());
            }

            generateNonWorkingDays(o, dbItemInventoryInstance.getId());

            for (AssignedCalendars assignedCalendars : o.getCalendars()) {
                FrequencyResolver fr = new FrequencyResolver();
                LOGGER.debug("Generate dates for:" + assignedCalendars.getCalendarPath());
                Calendar calendar = getCalendar(dbItemInventoryInstance.getId(), assignedCalendars.getCalendarPath());

                calendarFilter.setDateFrom(todayAsString());
                calendarFilter.setDateTo(toDateAsString());
                calendarFilter.setCalendar(calendar);

                fr.resolve(calendarFilter);
                Set<String> s = fr.getDates().keySet();
                PeriodResolver periodResolver = new PeriodResolver();
                for (Period p : assignedCalendars.getPeriods()) {
                    periodResolver.addStartTimes(p);
                }

                for (String d : s) {
                    LOGGER.trace("Date: " + d);
                    if (listOfNonWorkingDays.get(d) != null) {
                        LOGGER.trace(d + "will be ignored as it is a non working day");
                    } else {
                        for (Long startTime : periodResolver.getStartTimes(d)) {
                            FreshOrder freshOrder = buildFreshOrder(o, startTime);
                            orderListSynchronizer.add(freshOrder);
                        }
                    }
                }
            }
        }
        return orderListSynchronizer;
    }

    private void addOrders() {
        // Aufträge für Intervall im Master löschen
        // Aufträge für Intervall hinzufügen

    }
}