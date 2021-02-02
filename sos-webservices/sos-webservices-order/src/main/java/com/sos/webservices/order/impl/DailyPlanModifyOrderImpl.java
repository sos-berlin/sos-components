package com.sos.webservices.order.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrderHelper;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JobSchedulerObjectNotExistException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.dailyplan.DailyPlanModifyOrder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.DBLayerOrderVariables;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterOrderVariables;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.initiator.model.NameValuePair;
import com.sos.webservices.order.resource.IDailyPlanModifyOrder;

@Path("daily_plan")
public class DailyPlanModifyOrderImpl extends JOCResourceImpl implements IDailyPlanModifyOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanModifyOrderImpl.class);
    private static final String API_CALL_MODIFY_ORDER = "./daily_plan/orders/modify";

    @Override
    public JOCDefaultResponse postModifyOrder(String accessToken,  byte[] filterBytes) throws JocException {
           
        LOGGER.debug("Change start time for orders from the daily plan");

        try {
            initLogging(API_CALL_MODIFY_ORDER, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanModifyOrder.class);
            DailyPlanModifyOrder dailyplanModifyOrder = Globals.objectMapper.readValue(filterBytes, DailyPlanModifyOrder.class);


            JOCDefaultResponse jocDefaultResponse = init(API_CALL_MODIFY_ORDER, dailyplanModifyOrder, accessToken, dailyplanModifyOrder
                    .getControllerId(), getPermissonsJocCockpit(getControllerId(accessToken, dailyplanModifyOrder.getControllerId()), accessToken)
                            .getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("orderIds", dailyplanModifyOrder.getOrderIds());
            if (dailyplanModifyOrder.getStartTime() == null && dailyplanModifyOrder.getVariables() == null) {
                throw new JocMissingRequiredParameterException("variables or startTime missing");
            }

            for (String orderId : dailyplanModifyOrder.getOrderIds()) {
                modifyOrder(orderId, dailyplanModifyOrder);
            }

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

    private void cancelOrdersFromController(FilterDailyPlannedOrders filter, List<DBItemDailyPlanWithHistory> listOfPlannedOrdersWithHistory)
            throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, JsonProcessingException, InterruptedException,
            ExecutionException {

        try {
            OrderHelper.removeFromJobSchedulerControllerWithHistory(filter.getControllerId(), listOfPlannedOrdersWithHistory);
        } catch (JobSchedulerObjectNotExistException e) {
            LOGGER.warn("Order unknown in JS7 Controller");
        }
    }

    private void submitOrdersToController(List<DBItemDailyPlanOrders> listOfPlannedOrders) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, IOException, ParseException, SOSException,
            URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        if (listOfPlannedOrders.size() > 0) {

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            orderInitiatorSettings.setControllerId(listOfPlannedOrders.get(0).getControllerId());
            orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            orderInitiatorSettings.setOverwrite(false);
            orderInitiatorSettings.setSubmit(true);

            orderInitiatorSettings.setTimeZone(Globals.sosCockpitProperties.getProperty("daily_plan_timezone", Globals.DEFAULT_TIMEZONE_DAILY_PLAN));
            orderInitiatorSettings.setPeriodBegin(Globals.sosCockpitProperties.getProperty("daily_plan_period_begin",
                    Globals.DEFAULT_PERIOD_DAILY_PLAN));
            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

            orderInitiatorRunner.submitOrders(listOfPlannedOrders);
        }
    }

    private void updateVariables(DailyPlanModifyOrder dailyplanModifyOrder, DBItemDailyPlanOrders dbItemDailyPlanOrder) throws SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;

        try {
            Map<String, NameValuePair> varibales2Modify = new HashMap<String, NameValuePair>();
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);
            FilterOrderVariables filter = new FilterOrderVariables();
            filter.setPlannedOrderId(dbItemDailyPlanOrder.getId());
            dbLayerOrderVariables.delete(filter);
            for (NameValuePair variable : dailyplanModifyOrder.getVariables()) {
                varibales2Modify.put(variable.getName(), variable);
            }
            for (NameValuePair variable : varibales2Modify.values()) {
                DBItemDailyPlanVariables dbItemDailyPlanVariables = new DBItemDailyPlanVariables();
                dbItemDailyPlanVariables.setCreated(new Date());
                dbItemDailyPlanVariables.setModified(new Date());
                dbItemDailyPlanVariables.setPlannedOrderId(dbItemDailyPlanOrder.getId());
                dbItemDailyPlanVariables.setVariableName(variable.getName());
                dbItemDailyPlanVariables.setVariableValue(variable.getValue());
                sosHibernateSession.save(dbItemDailyPlanVariables);
            }
            Globals.commit(sosHibernateSession);

        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void modifyOrder(String orderId, DailyPlanModifyOrder dailyplanModifyOrder) throws JocConfigurationException,
            DBConnectionRefusedException, JobSchedulerInvalidResponseDataException, DBOpenSessionException, JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, DBInvalidDataException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, IOException, ParseException, TimeoutException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(dailyplanModifyOrder.getControllerId());
            filter.setOrderId(orderId);

            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
            if (listOfPlannedOrders.size() == 1) {
                DBItemDailyPlanOrders dbItemDailyPlanOrder = listOfPlannedOrders.get(0);
                dbItemDailyPlanOrder.setModified(new Date());
                if (dailyplanModifyOrder.getStartTime() != null) {
                    dbItemDailyPlanOrder.setPlannedStart(dailyplanModifyOrder.getStartTime());
                    sosHibernateSession.update(dbItemDailyPlanOrder);
                }
                if (dailyplanModifyOrder.getVariables() != null) {
                    updateVariables(dailyplanModifyOrder, dbItemDailyPlanOrder);
                }

                if (dbItemDailyPlanOrder.getSubmitted()) {
                    filter.addState(OrderStateText.PENDING);
                    List<DBItemDailyPlanWithHistory> listOfDailyPlanItems = new ArrayList<DBItemDailyPlanWithHistory>();
                    DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory = new DBItemDailyPlanWithHistory();
                    dbItemDailyPlanWithHistory.setOrderId(dbItemDailyPlanOrder.getOrderId());
                    dbItemDailyPlanWithHistory.setPlannedOrderId(dbItemDailyPlanOrder.getId());
                    listOfDailyPlanItems.add(dbItemDailyPlanWithHistory);
                    cancelOrdersFromController(filter, listOfDailyPlanItems);

                    FilterDailyPlannedOrders filterDailyPlannedOrders = new FilterDailyPlannedOrders();
                    filterDailyPlannedOrders.setPlannedOrderId(dbItemDailyPlanOrder.getId());
                    dbLayerDailyPlannedOrders.delete(filterDailyPlannedOrders);

                    DBItemDailyPlanOrders dbItemDailyPlanOrders = dbLayerDailyPlannedOrders.insertFrom(dbItemDailyPlanOrder);
                    dbLayerOrderVariables.update(dbItemDailyPlanWithHistory.getPlannedOrderId(), dbItemDailyPlanOrders.getId());
                    listOfPlannedOrders.clear();
                    listOfPlannedOrders.add(dbItemDailyPlanOrders);
                    Globals.commit(sosHibernateSession);
                    submitOrdersToController(listOfPlannedOrders);
                } else {
                    Globals.commit(sosHibernateSession);
                }

            } else {
                LOGGER.warn("Expected one record for order-id " + filter.getOrderId());
                throw new DBMissingDataException("Expected one record for order-id " + filter.getOrderId());
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
