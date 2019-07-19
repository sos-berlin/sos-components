package com.sos.webservices.order.initiator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;

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
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.jobscheduler.db.calendar.DBItemCalendar;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.orders.DBItemDaysPlanned;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.db.calendars.CalendarsDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.UnknownJobSchedulerMasterException;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.webservices.order.initiator.classes.OrderInitiatorGlobals;
import com.sos.webservices.order.initiator.classes.PlannedOrder;
import com.sos.webservices.order.initiator.db.DBLayerDaysPlanned;
import com.sos.webservices.order.initiator.db.FilterDaysPlanned;
import com.sos.webservices.order.initiator.model.AssignedCalendars;
import com.sos.webservices.order.initiator.model.AssignedNonWorkingCalendars;
import com.sos.webservices.order.initiator.model.NameValuePair;
import com.sos.webservices.order.initiator.model.OrderTemplate;
import com.sos.webservices.order.initiator.model.Period;

public class OrderInitiatorRunner extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorRunner.class);
    private List<OrderTemplate> listOfOrderTemplates;
    private Map<String, String> listOfNonWorkingDays;
    private SOSHibernateFactory sosHibernateFactory;
    private OrderListSynchronizer orderListSynchronizer;

    public OrderInitiatorRunner(OrderInitiatorSettings orderInitiatorSettings) {
        OrderInitiatorGlobals.orderInitiatorSettings = orderInitiatorSettings;
    }

    public void run() {

        try {
            readTemplates();
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR);
            int year = calendar.get(java.util.Calendar.YEAR);
            
            // TODO check when dayofyear + year is in the next year
            // TODO actDay must be in the interval of the calendar.
            for (int day = dayOfYear; day < dayOfYear + OrderInitiatorGlobals.orderInitiatorSettings.getDayOffset(); day++) {
                orderListSynchronizer = calculateStartTimes(year, day);
                if (orderListSynchronizer.getListOfPlannedOrders().size() > 0) {
                    orderListSynchronizer.addPlannedOrderToMasterAndDB();
                }
            }
		} catch (IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException
				| ParseException | UnknownJobSchedulerMasterException | SOSException | URISyntaxException
				| JocConfigurationException | DBOpenSessionException e) {
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
        LOGGER.debug("... readTemplates");

        OrderTemplateSource orderTemplateSource = new OrderTemplateSourceFile(OrderInitiatorGlobals.orderInitiatorSettings.getOrderTemplatesDirectory());
        OrderTemplates orderTemplates = new OrderTemplates();
        orderTemplates.fillListOfOrderTemplates(orderTemplateSource);
        listOfOrderTemplates = orderTemplates.getListOfOrderTemplates();
    }

	private Calendar getCalendar(String masterId, String calendarName) throws DBMissingDataException,
			JsonParseException, JsonMappingException, IOException, DBConnectionRefusedException, DBInvalidDataException,
			SOSHibernateOpenSessionException, SOSHibernateConfigurationException, SOSHibernateFactoryBuildException,
			JocConfigurationException, DBOpenSessionException {

		SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");

		try {
			CalendarsDBLayer dbLayer = new CalendarsDBLayer(sosHibernateSession);
			DBItemCalendar calendarItem = null;
			calendarName = Globals.normalizePath(calendarName);
			calendarItem = dbLayer.getCalendar(masterId, calendarName);
			if (calendarItem == null) {
				throw new DBMissingDataException(
						String.format("calendar '%1$s' not found for instance %s", calendarName, masterId));
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
        freshOrder.setScheduledFor(startTime);
        freshOrder.setArguments(variables);
        freshOrder.setWorkflowPath(o.getWorkflowPath());
        return freshOrder;
    }

    private String dayAsString(int year, int dayOfYear) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.DAY_OF_YEAR, dayOfYear);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateS = formatter.format(calendar.getTime());
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        return dateS;
    }

   

	private void generateNonWorkingDays(String date, OrderTemplate o, String masterId)
			throws SOSMissingDataException, SOSInvalidDataException, JsonParseException, JsonMappingException,
			DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException, IOException, ParseException,
			SOSHibernateOpenSessionException, SOSHibernateConfigurationException, SOSHibernateFactoryBuildException,
			JocConfigurationException, DBOpenSessionException {
	    if (o.getNonWorkingCalendars() != null) {
            FrequencyResolver fr = new FrequencyResolver();
            for (AssignedNonWorkingCalendars assignedNonWorkingCalendars : o.getNonWorkingCalendars()) {
                LOGGER.debug("Generate non working dates for:" + assignedNonWorkingCalendars.getCalendarPath());
                listOfNonWorkingDays = new HashMap<String, String>();
                Calendar calendar = getCalendar(masterId, assignedNonWorkingCalendars.getCalendarPath());
                CalendarDatesFilter calendarFilter = new CalendarDatesFilter();

                calendarFilter.setDateFrom(date);
                calendarFilter.setDateTo(date);
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

	private OrderListSynchronizer calculateStartTimes(int year, int dayOfYear)
			throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException,
			DBMissingDataException, IOException, SOSMissingDataException, SOSInvalidDataException, ParseException,
			UnknownJobSchedulerMasterException, JocConfigurationException, SOSHibernateException,
			DBOpenSessionException {

		LOGGER.debug(String.format("... calculateStartTimes for year %s day %s", year, dayOfYear));
        
//TODO:         
        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties("/order_configuration.properties");
        }
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");

        try {
            InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(sosHibernateSession);
            // CalendarDatesFilter calendarFilter = new CalendarDatesFilter();

            OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer();
            DBItemInventoryInstance dbItemInventoryInstance = null;
            for (OrderTemplate o : listOfOrderTemplates) {
            	/* comment out wegen Compile-Fehler nach Aenderung der Instances-Tabelle 
                if (dbItemInventoryInstance == null || (!dbItemInventoryInstance.getHostname().equals(o.getHostName()) && !dbItemInventoryInstance
                        .getPort().equals(o.getPort()) && !dbItemInventoryInstance.getSchedulerId().equals(o.getMasterId()))) {
                    dbItemInventoryInstance = dbLayer.getInventoryInstanceByHostPort(o.getHostName(), o.getPort(), o.getMasterId());
                }
                */

                String masterId = dbItemInventoryInstance.getSchedulerId();
                if (planExist(sosHibernateSession, masterId, year, dayOfYear)) {
                    LOGGER.debug(String.format("... Plan for year %s day %s has been already created for master %s", year, dayOfYear, masterId));
                    return new OrderListSynchronizer();
                }

                String actDate = dayAsString(year, dayOfYear);
                DBItemDaysPlanned dbItemDaysPlanned = addPlan(sosHibernateSession, masterId, year, dayOfYear);

                generateNonWorkingDays(actDate, o, masterId);

                for (AssignedCalendars assignedCalendars : o.getCalendars()) {

                    FrequencyResolver fr = new FrequencyResolver();
                    LOGGER.debug("Generate dates for:" + assignedCalendars.getCalendarPath());
                    Calendar calendar = getCalendar(masterId, assignedCalendars.getCalendarPath());
                    Calendar restrictions = new Calendar();
                    //TODO consider calendars date from/to
                    //Maybe not neccessary in JS2
                    calendar.setFrom(actDate);
                    calendar.setTo(actDate);
                    String calendarJson = new ObjectMapper().writeValueAsString(calendar);
                    restrictions.setIncludes(assignedCalendars.getIncludes());

                    fr.resolveRestrictions(calendarJson, calendarJson, actDate, actDate);
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
                            for (Entry<Long, Period> startTime : periodResolver.getStartTimes(d).entrySet()) {
                                FreshOrder freshOrder = buildFreshOrder(o, startTime.getKey());
                                PlannedOrder plannedOrder = new PlannedOrder();
                                plannedOrder.setFreshOrder(freshOrder);
                                plannedOrder.setCalendarId(calendar.getId());
                                plannedOrder.setPeriod(startTime.getValue());
                                plannedOrder.setPlanId(dbItemDaysPlanned.getId());
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

    private DBItemDaysPlanned addPlan(SOSHibernateSession sosHibernateSession, String masterId, int year, int dayOfYear)
            throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException {
        DBLayerDaysPlanned dbLayer = new DBLayerDaysPlanned(sosHibernateSession);
        FilterDaysPlanned filter = new FilterDaysPlanned();
        filter.setDay(dayOfYear);
        filter.setYear(year);
        filter.setMasterId(masterId);
        Globals.beginTransaction(sosHibernateSession);
        DBItemDaysPlanned dbItemDaysPlanned = dbLayer.storePlan(filter);
        Globals.commit(sosHibernateSession);
        return dbItemDaysPlanned;
    }

    private boolean planExist(SOSHibernateSession sosHibernateSession, String masterId, int year, int dayOfYear) throws JocConfigurationException,
            DBConnectionRefusedException, SOSHibernateException {
        DBLayerDaysPlanned dbLayer = new DBLayerDaysPlanned(sosHibernateSession);
        FilterDaysPlanned filter = new FilterDaysPlanned();
        filter.setDay(dayOfYear);
        filter.setYear(year);
        filter.setMasterId(masterId);
        return dbLayer.getPlannedDay(filter) != null;
    }

}