package com.sos.js7.order.initiator;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.util.SOSDuration;
import com.sos.commons.util.SOSDurations;
import com.sos.joc.db.orders.DBItemDailyPlan;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;
import com.sos.js7.order.initiator.classes.PlannedOrder;
import com.sos.js7.order.initiator.classes.PlannedOrderKey;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;

public class OrderListSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListSynchronizer.class);
    private Map<PlannedOrderKey, PlannedOrder> listOfPlannedOrders;
    private Map<PlannedOrderKey, Long> listOfDurations;

    public OrderListSynchronizer() {
        listOfPlannedOrders = new HashMap<PlannedOrderKey, PlannedOrder>();
    }

    public OrderListSynchronizer(OrderInitiatorSettings orderInitiatorSettings) {
        listOfPlannedOrders = new HashMap<PlannedOrderKey, PlannedOrder>();
        OrderInitiatorGlobals.orderInitiatorSettings = orderInitiatorSettings;
    }

    public void add(PlannedOrder o) {
        listOfPlannedOrders.put(o.uniqueOrderkey(), o);
    }

    private void calculateDuration(PlannedOrder plannedOrder) throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException,
            DBOpenSessionException {

        if (listOfDurations.get(plannedOrder.uniqueOrderkey()) == null) {

            SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("calculateDurations");
            try {

                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                DBLayerDailyPlannedOrders dbLayerDailyPlan = new DBLayerDailyPlannedOrders(sosHibernateSession);
                Globals.beginTransaction(sosHibernateSession);
                filter.setJobSchedulerId(plannedOrder.getOrderTemplate().getJobschedulerId());
                filter.setWorkflow(plannedOrder.getOrderTemplate().getWorkflowPath());
                filter.setTemplateId(plannedOrder.getOrderTemplate().getTemplateId());
                List<DBItemDailyPlanWithHistory> listOfPlannedOrders = dbLayerDailyPlan.getDailyPlanWithHistoryList(filter, 0);
                SOSDurations sosDurations = new SOSDurations();
                for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {
                    if (dbItemDailyPlanWithHistory.getDbItemOrder() != null) {
                        SOSDuration sosDuration = new SOSDuration();
                        Date startTime = dbItemDailyPlanWithHistory.getDbItemOrder().getStartTime();
                        Date endTime = dbItemDailyPlanWithHistory.getDbItemOrder().getEndTime();
                        sosDuration.setStartTime(startTime);
                        sosDuration.setEndTime(endTime);
                        sosDurations.add(sosDuration);
                    }
                }
                listOfDurations.put(plannedOrder.uniqueOrderkey(), sosDurations.average());
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        }
    }

    private void calculateDurations() throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException, DBOpenSessionException {
        LOGGER.debug("... calculateDurations");
        listOfDurations = new HashMap<PlannedOrderKey, Long>();

        for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
            calculateDuration(plannedOrder);
        }
    }

    private void addOrderToController(PlannedOrder plannedOrder) throws JsonProcessingException, SOSException, URISyntaxException,
            JocConfigurationException, DBConnectionRefusedException, ParseException {
        if (!plannedOrder.getOrderTemplate().getSubmit_order_to_controller_when_planned()) {
            LOGGER.debug(String.format("Planned order %s for orderTemplate %s will not be submitted to the controller", plannedOrder.getFreshOrder()
                    .getId(), plannedOrder.getOrderTemplate().getOrderTemplateName()));
        } else {
            SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
            sosRestApiClient.addHeader("Content-Type", "application/json");
            sosRestApiClient.addHeader("Accept", "application/json");

            String postBody = "";
            String answer = "";
            postBody = new ObjectMapper().writeValueAsString(plannedOrder.getFreshOrder());
            LOGGER.debug("controller Url: " + OrderInitiatorGlobals.orderInitiatorSettings.getJobschedulerUrl());
            answer = sosRestApiClient.postRestService(new URI(OrderInitiatorGlobals.orderInitiatorSettings.getJobschedulerUrl()
                    + "/controller/api/order"), postBody);
            LOGGER.debug(answer);
        }
    }

    public void addPlannedOrderToControllerAndDB() throws JsonProcessingException, SOSException, URISyntaxException, JocConfigurationException,
            DBConnectionRefusedException, ParseException, DBOpenSessionException {
        LOGGER.debug("... addPlannedOrderToControllerAndDB");

        calculateDurations();
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("synchronizePlannedOrderWithDB");
        DBLayerDailyPlannedOrders dbLayerDailyPlan = new DBLayerDailyPlannedOrders(sosHibernateSession);
        Globals.beginTransaction(sosHibernateSession);
        try {

            for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
                DBItemDailyPlan dbItemDailyPlan = dbLayerDailyPlan.getUniqueDailyPlan(plannedOrder);
                if (dbItemDailyPlan == null) {
                    LOGGER.debug("snchronizer: adding planned order: " + plannedOrder.uniqueOrderkey());
                    plannedOrder.setAverageDuration(listOfDurations.get(plannedOrder.uniqueOrderkey()));
                    dbLayerDailyPlan.store(plannedOrder);
                    addOrderToController(plannedOrder);
                }
            }
            Globals.commit(sosHibernateSession);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    public Map<PlannedOrderKey, PlannedOrder> getListOfPlannedOrders() {
        return listOfPlannedOrders;
    }
}
