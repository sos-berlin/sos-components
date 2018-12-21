package com.sos.webservices.order.initiator;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
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
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.webservices.order.initiator.classes.Globals;
import com.sos.webservices.order.initiator.classes.PlannedOrder;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlan;
import com.sos.webservices.order.initiator.db.FilterDailyPlan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderListSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListSynchronizer.class);
    private Map<String, PlannedOrder> listOfPlannedOrders;
    private Map<String, Long> listOfDurations;

    public OrderListSynchronizer() {
        super();
        listOfPlannedOrders = new HashMap<String, PlannedOrder>();
    }

    public void add(PlannedOrder o) {
        listOfPlannedOrders.put(o.uniqueOrderkey(), o);
    }

    private void calculateDuration(PlannedOrder plannedOrder) throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException {

        if (listOfDurations.get(plannedOrder.orderkey()) == null) {

            SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("calculateDurations");
            try {

                FilterDailyPlan filter = new FilterDailyPlan();
                DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
                Globals.beginTransaction(sosHibernateSession);
                filter.setMasterId(plannedOrder.getOrderTemplate().getMasterId());
                filter.setWorkflow(plannedOrder.getOrderTemplate().getWorkflowPath());
                filter.setOrderName(plannedOrder.getOrderTemplate().getOrderName());
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
                listOfDurations.put(plannedOrder.orderkey(), sosDurations.average());
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        }
    }

    private void calculateDurations() throws SOSHibernateException, JocConfigurationException, DBConnectionRefusedException {
        LOGGER.debug("... calculateDurations");
        listOfDurations = new HashMap<String, Long>();

        for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
            calculateDuration(plannedOrder);
        }
    }

    private void addOrderToMaster(PlannedOrder plannedOrder) throws JsonProcessingException, SOSException, URISyntaxException,
            JocConfigurationException, DBConnectionRefusedException, ParseException {
        SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
        sosRestApiClient.addHeader("Content-Type", "application/json");
        sosRestApiClient.addHeader("Accept", "application/json");

        String postBody = "";
        String answer = "";
        postBody = new ObjectMapper().writeValueAsString(plannedOrder.getFreshOrder());
        answer = sosRestApiClient.postRestService(new URI(Globals.orderInitiatorSettings.getJocUrl() + "/order"), postBody);
        LOGGER.debug(answer);
    }

    public void addPlannedOrderToMasterAndDB() throws JsonProcessingException, SOSException, URISyntaxException, JocConfigurationException,
            DBConnectionRefusedException, ParseException {
        LOGGER.debug("... addPlannedOrderToMasterAndDB");

        calculateDurations();

        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("synchronizePlannedOrderWithDB");
        DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
        Globals.beginTransaction(sosHibernateSession);
        try {
            /*
             * FilterDailyPlan filter = dbLayerDailyPlan.resetFilter(); List<DBItemDailyPlan> listOfNotSubmittedOrders =
             * dbLayerDailyPlan.getDailyPlanList(filter, 0); for (DBItemDailyPlan dbItemDailyPlan : listOfNotSubmittedOrders) { PlannedOrder plannedOrder = new
             * PlannedOrder(dbItemDailyPlan); if (listOfPlannedOrders.get(plannedOrder.uniqueOrderkey()) == null) {
             * LOGGER.debug("snchronizer: remove planned order: " + plannedOrder.uniqueOrderkey() + "  from plan as no longer in plan");
             * dbLayerDailyPlan.deleteVariables(dbItemDailyPlan.getId()); dbLayerDailyPlan.delete(filter); } }
             */
            for (PlannedOrder plannedOrder : listOfPlannedOrders.values()) {
                DBItemDailyPlan dbItemDailyPlan = dbLayerDailyPlan.getUniqueDailyPlan(plannedOrder);
                if (dbItemDailyPlan == null) {
                    LOGGER.debug("snchronizer: adding planned order: " + plannedOrder.uniqueOrderkey());
                    plannedOrder.setAverageDuration(listOfDurations.get(plannedOrder.orderkey()));
                    dbLayerDailyPlan.store(plannedOrder);
                    addOrderToMaster(plannedOrder);
                } else {
                    /*
                     * LOGGER.debug("snchronizer: renew planned order: " + plannedOrder.uniqueOrderkey());
                     * dbLayerDailyPlan.deleteVariables(dbItemDailyPlan.getId()); dbLayerDailyPlan.storeVariables(plannedOrder, dbItemDailyPlan.getId());
                     */
                }
            }
        } finally {
            Globals.commit(sosHibernateSession);
            Globals.disconnect(sosHibernateSession);
        }

    }

    public Map<String, PlannedOrder> getListOfPlannedOrders() {
        return listOfPlannedOrders;
    }
}
