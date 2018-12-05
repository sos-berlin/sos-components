package com.sos.webservices.order.initiator;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.util.SOSDuration;
import com.sos.commons.util.SOSDurations;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.webservices.order.initiator.classes.Globals;
import com.sos.webservices.order.initiator.classes.PlannedOrder;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderListSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListSynchronizer.class);
    private List<PlannedOrder> listOfOrders;
    private Map<String, Long> listOfDurations;

    public OrderListSynchronizer() {
        super();
        listOfOrders = new ArrayList<PlannedOrder>();
    }

    public void add(PlannedOrder o) {
        listOfOrders.add(o);
    }

    public void removeAllOrdersFromMaster() {
    }

    private void calculateDuration(PlannedOrder plannedOrder) throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException {

        if (listOfDurations.get(plannedOrder.orderkey()) == null) {

            SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("calculateDurations");
            try {

                DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
                Globals.beginTransaction(sosHibernateSession);
                dbLayerDailyPlan.getFilter().setMasterId(plannedOrder.getOrderTemplate().getMasterId());
                dbLayerDailyPlan.getFilter().setWorkflow(plannedOrder.getOrderTemplate().getWorkflowPath());
                dbLayerDailyPlan.getFilter().setOrderName(plannedOrder.getOrderTemplate().getOrderName());
                List<DBItemDailyPlanWithHistory> listOfPlannedOrders = dbLayerDailyPlan.getDailyPlanWithHistoryList(0);
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
                listOfDurations.put(plannedOrder.orderkey(), sosDurations.average());
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        }
    }

    private void calculateDurations() throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException {
        listOfDurations = new HashMap<String, Long>();
        for (PlannedOrder plannedOrder : listOfOrders) {
            calculateDuration(plannedOrder);
        }
    }

    public void addOrdersToMaster() throws JsonProcessingException, SOSException, URISyntaxException, JocConfigurationException,
            DBConnectionRefusedException, ParseException {
        SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
        sosRestApiClient.addHeader("Content-Type", "application/json");
        sosRestApiClient.addHeader("Accept", "application/json");

        String postBody = "";
        String answer = "";
        calculateDurations();
        for (PlannedOrder plannedOrder : listOfOrders) {
            if (!plannedOrder.orderExist()) {
                plannedOrder.setAverageDuration(listOfDurations.get(plannedOrder.orderkey()));
                plannedOrder.store();
                postBody = new ObjectMapper().writeValueAsString(plannedOrder.getFreshOrder());
                answer = sosRestApiClient.postRestService(new URI(Globals.orderInitiatorSettings.getJocUrl() + "/order"), postBody);
                LOGGER.debug(answer);
            }

        }

    }

}
