package com.sos.webservices.order.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.OrderHelper;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.VariableType;
import com.sos.joc.model.dailyplan.DailyPlanModifyOrder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.DBLayerOrderVariables;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterOrderVariables;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.resource.IDailyPlanModifyOrder;

@Path("daily_plan")
public class DailyPlanModifyOrderImpl extends JOCOrderResourceImpl implements IDailyPlanModifyOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanModifyOrderImpl.class);
    private static final String API_CALL_MODIFY_ORDER = "./daily_plan/orders/modify";

    @Override
    public JOCDefaultResponse postModifyOrder(String accessToken, byte[] filterBytes) throws JocException {

        LOGGER.debug("Change start time for orders from the daily plan");

        try {
            initLogging(API_CALL_MODIFY_ORDER, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanModifyOrder.class);
            DailyPlanModifyOrder dailyplanModifyOrder = Globals.objectMapper.readValue(filterBytes, DailyPlanModifyOrder.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(dailyplanModifyOrder.getControllerId(), getJocPermissions(accessToken)
                    .getDailyPlan().getView());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            // TODO this check is not necessary if schema specifies orderIds as required and with minItems:1
            // uniqueItems should also better
            this.checkRequiredParameter("orderIds", dailyplanModifyOrder.getOrderIds());
            if (dailyplanModifyOrder.getScheduledFor() == null && dailyplanModifyOrder.getRemoveVariables() == null && dailyplanModifyOrder
                    .getVariables() == null) {
                throw new JocMissingRequiredParameterException("variables, removeVariables or scheduledFor missing");
            }

            List<String> orderIds = dailyplanModifyOrder.getOrderIds();
            Set<String> temporaryOrderIds = orderIds.stream().filter(id -> id.matches(".*#T[0-9]+-.*")).collect(Collectors.toSet());
            orderIds.removeAll(temporaryOrderIds);

            CategoryType category = CategoryType.DAILYPLAN;
            if (orderIds.isEmpty()) {
                category = CategoryType.CONTROLLER;
            }
            DBItemJocAuditLog dbAuditlog = storeAuditLog(dailyplanModifyOrder.getAuditLog(), dailyplanModifyOrder.getControllerId(), category);

            List<Err419> errors = OrdersHelper.cancelAndAddFreshOrder(temporaryOrderIds, dailyplanModifyOrder, accessToken, getJocError(), dbAuditlog
                    .getId(), folderPermissions);

            if (!orderIds.isEmpty()) {
                setSettings();

                List<String> listOfOrderIds = new ArrayList<String>();
                for (String orderId : orderIds) {
                    listOfOrderIds.add(orderId);
                }

                for (String orderId : orderIds) {
                    addCyclicOrderIds(listOfOrderIds, orderId, dailyplanModifyOrder.getControllerId());
                }

                for (String orderId : listOfOrderIds) {
                    modifyOrder(orderId,accessToken, dailyplanModifyOrder, dbAuditlog);
                }
            }

            if (!errors.isEmpty()) {
                return JOCDefaultResponse.responseStatus419(errors);
            }

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }


    private void cancelOrdersFromController(FilterDailyPlannedOrders filter, List<DBItemDailyPlanWithHistory> listOfPlannedOrdersWithHistory)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, JsonProcessingException, InterruptedException,
            ExecutionException {

        try {
            OrderHelper.removeFromJobSchedulerControllerWithHistory(filter.getControllerId(), listOfPlannedOrdersWithHistory);
        } catch (ControllerObjectNotExistException e) {
            LOGGER.warn("Order unknown in JS7 Controller");
        }
    }

    private void submitOrdersToController(List<DBItemDailyPlanOrders> listOfPlannedOrders) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {

        if (listOfPlannedOrders.size() > 0) {

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            orderInitiatorSettings.setOverwrite(false);
            orderInitiatorSettings.setSubmit(true);

            orderInitiatorSettings.setTimeZone(settings.getTimeZone());
            orderInitiatorSettings.setPeriodBegin(settings.getPeriodBegin());
            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

            orderInitiatorRunner.submitOrders(listOfPlannedOrders.get(0).getControllerId(), getJocError(), getAccessToken(), listOfPlannedOrders);
        }
    }

    private void updateVariables(DailyPlanModifyOrder dailyplanModifyOrder, DBItemDailyPlanOrders dbItemDailyPlanOrder) throws SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);
            FilterOrderVariables filter = new FilterOrderVariables();
            filter.setPlannedOrderId(dbItemDailyPlanOrder.getId());

            if (dailyplanModifyOrder.getVariables() != null) {

                Map<String, Object> mapOfvariables = new HashMap<String, Object>();
                List<DBItemDailyPlanVariables> listOfVariables = dbLayerOrderVariables.getOrderVariables(filter, 0);
                for (DBItemDailyPlanVariables dbItemDailyPlanVariables : listOfVariables) {
                    mapOfvariables.put(dbItemDailyPlanVariables.getVariableName(), dbItemDailyPlanVariables.getVariableValue());
                }

                for (DBItemDailyPlanVariables dbItemDailyPlanVariables : listOfVariables) {
                    Object value = dailyplanModifyOrder.getVariables().getAdditionalProperties().get(dbItemDailyPlanVariables.getVariableName());
                    if (value != null) {
                        dbItemDailyPlanVariables.setVariableValue(value.toString());
                        sosHibernateSession.update(dbItemDailyPlanVariables);
                    }
                }

                for (Map.Entry<String, Object> variable : dailyplanModifyOrder.getVariables().getAdditionalProperties().entrySet()) {

                    String varName = variable.getKey().toString();

                    if (mapOfvariables.get(varName) == null) {

                        DBItemDailyPlanVariables dbItemDailyPlanVariables = new DBItemDailyPlanVariables();
                        dbItemDailyPlanVariables.setCreated(new Date());
                        dbItemDailyPlanVariables.setModified(new Date());
                        dbItemDailyPlanVariables.setPlannedOrderId(dbItemDailyPlanOrder.getId());
                        dbItemDailyPlanVariables.setVariableName(varName);

                        dbItemDailyPlanVariables.setVariableType(VariableType.valueOf(variable.getValue().getClass().getSimpleName().toUpperCase())
                                .value());
                        dbItemDailyPlanVariables.setVariableValue(variable.getValue().toString());
                        sosHibernateSession.save(dbItemDailyPlanVariables);
                    }
                }
            }

            if (dailyplanModifyOrder.getRemoveVariables() != null) {
                for (Map.Entry<String, Object> variable : dailyplanModifyOrder.getRemoveVariables().getAdditionalProperties().entrySet()) {
                    filter.setVariableName(variable.getKey().toString());
                    dbLayerOrderVariables.delete(filter);
                }
            }

            Globals.commit(sosHibernateSession);

        } finally

        {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void modifyOrder(String orderId, String accessToken, DailyPlanModifyOrder dailyplanModifyOrder, DBItemJocAuditLog dbAuditlog)
            throws JocConfigurationException, DBConnectionRefusedException, ControllerInvalidResponseDataException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, DBInvalidDataException, SOSException,
            URISyntaxException, InterruptedException, ExecutionException, IOException, ParseException, TimeoutException {

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

                if (dailyplanModifyOrder.getScheduledFor() != null) {
                    Date scheduledFor = Date.from(JobSchedulerDate.getScheduledForInUTC(dailyplanModifyOrder.getScheduledFor(), dailyplanModifyOrder
                            .getTimeZone()).get());

                    Long expectedDuration = dbItemDailyPlanOrder.getExpectedEnd().getTime() - dbItemDailyPlanOrder.getPlannedStart().getTime();
                    dbItemDailyPlanOrder.setExpectedEnd(new Date(expectedDuration + scheduledFor.getTime()));
                    dbItemDailyPlanOrder.setPlannedStart(scheduledFor);
                    sosHibernateSession.update(dbItemDailyPlanOrder);
                }
                if ((dailyplanModifyOrder.getVariables() != null && dailyplanModifyOrder.getVariables() != null) || (dailyplanModifyOrder
                        .getRemoveVariables() != null && dailyplanModifyOrder.getRemoveVariables() != null)) {
                    updateVariables(dailyplanModifyOrder, dbItemDailyPlanOrder);
                }

                if (dbItemDailyPlanOrder.getSubmitted()) {
                    filter.addState(OrderStateText.PENDING); //@Uwe: Why addState?? Where is it used??
                    filter.addState(OrderStateText.SCHEDULED); //Why addState?? Where is it used??
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
                

                List<AuditLogDetail> auditLogDetails = new ArrayList<>();
                auditLogDetails.add(new AuditLogDetail(dbItemDailyPlanOrder.getWorkflowPath(), dbItemDailyPlanOrder.getOrderId()));
                OrdersHelper.storeAuditLogDetails(auditLogDetails, dbAuditlog.getId()).thenAccept(either -> ProblemHelper
                        .postExceptionEventIfExist(either, accessToken, getJocError(), dailyplanModifyOrder.getControllerId()));

            } else {
                LOGGER.warn("Expected one record for order-id " + filter.getOrderId() + " found: " + listOfPlannedOrders.size());
                throw new DBMissingDataException("Expected one record for order-id " + filter.getOrderId() + " found: " + listOfPlannedOrders.size());
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
