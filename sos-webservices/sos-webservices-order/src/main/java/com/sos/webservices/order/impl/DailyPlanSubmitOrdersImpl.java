package com.sos.webservices.order.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.FolderPermissionEvaluator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.resource.IDailyPlanSubmitOrderResource;

@Path("daily_plan")
public class DailyPlanSubmitOrdersImpl extends JOCOrderResourceImpl implements IDailyPlanSubmitOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSubmitOrdersImpl.class);
    private static final String API_CALL = "./daily_plan/orders/submit";

    private void submitOrdersToController(DailyPlanOrderFilter dailyPlanOrderFilter, Collection<String> allowedControllers, String accessToken)
            throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, ControllerConnectionResetException, ControllerConnectionRefusedException, IOException,
            ParseException, SOSException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        setSettings();

        OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
        orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
        orderInitiatorSettings.setOverwrite(false);
        orderInitiatorSettings.setSubmit(true);

        orderInitiatorSettings.setTimeZone(settings.getTimeZone());
        orderInitiatorSettings.setPeriodBegin(settings.getPeriodBegin());
        OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);

            if (dailyPlanOrderFilter.getFilter() == null) {
                dailyPlanOrderFilter.setFilter(new DailyPlanOrderFilterDef());
            }

            Globals.beginTransaction(sosHibernateSession);

            FolderPermissionEvaluator folderPermissionEvaluator = new FolderPermissionEvaluator();
            folderPermissionEvaluator.setListOfWorkflowFolders(dailyPlanOrderFilter.getFilter().getWorkflowFolders());
            folderPermissionEvaluator.setListOfScheduleFolders(dailyPlanOrderFilter.getFilter().getScheduleFolders());
            folderPermissionEvaluator.setListOfSchedulePaths(dailyPlanOrderFilter.getFilter().getSchedulePaths());
            folderPermissionEvaluator.setListOfWorkflowPaths(dailyPlanOrderFilter.getFilter().getWorkflowPaths());

            for (String controllerId : allowedControllers) {

                DBItemJocAuditLog dbItemJocAuditLog = storeAuditLog(dailyPlanOrderFilter.getAuditLog(), controllerId, CategoryType.DAILYPLAN);
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();

                folderPermissions.setSchedulerId(controllerId);
                folderPermissionEvaluator.getPermittedNames(folderPermissions, controllerId, filter);

                if (folderPermissionEvaluator.isHasPermission()) {

                    List<String> orderIds = new ArrayList<String>();

                    if (dailyPlanOrderFilter.getFilter().getOrderIds() != null) {
                        orderIds.addAll(dailyPlanOrderFilter.getFilter().getOrderIds());

                        for (String orderId : orderIds) {
                            dbLayerDailyPlannedOrders.addCyclicOrderIds(dailyPlanOrderFilter.getFilter().getOrderIds(), orderId, controllerId,
                                    orderInitiatorSettings.getTimeZone(), orderInitiatorSettings.getPeriodBegin());
                        }
                    }

                    filter.setListOfOrders(dailyPlanOrderFilter.getFilter().getOrderIds());
                    filter.setSubmitted(false);
                    filter.setDailyPlanDate(dailyPlanOrderFilter.getFilter().getDailyPlanDate(), orderInitiatorSettings.getTimeZone(),
                            orderInitiatorSettings.getPeriodBegin());
                    filter.setListOfSubmissionIds(dailyPlanOrderFilter.getFilter().getSubmissionHistoryIds());

                    filter.setListOfWorkflowNames(folderPermissionEvaluator.getListOfPermittedWorkflowNames());
                    filter.setListOfScheduleNames(folderPermissionEvaluator.getListOfPermittedScheduleNames());
                    filter.setControllerId(controllerId);

                    List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);

                    Globals.commit(sosHibernateSession);
                    orderInitiatorRunner.submitOrders(controllerId, getJocError(), getAccessToken(), listOfPlannedOrders);

                    EventBus.getInstance().post(new DailyPlanEvent(dailyPlanOrderFilter.getFilter().getDailyPlanDate()));

                    List<AuditLogDetail> auditLogDetails = new ArrayList<>();

                    for (DBItemDailyPlanOrders dbItemDailyPlanOrders : listOfPlannedOrders) {
                        auditLogDetails.add(new AuditLogDetail(dbItemDailyPlanOrders.getWorkflowPath(), dbItemDailyPlanOrders.getControllerId()));
                    }

                    OrdersHelper.storeAuditLogDetails(auditLogDetails, dbItemJocAuditLog.getId()).thenAccept(either -> ProblemHelper
                            .postExceptionEventIfExist(either, accessToken, getJocError(), controllerId));

                }
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postSubmitOrders(String accessToken, byte[] filterBytes) throws JocException {
        LOGGER.debug("Submit orders to JS7 controller");
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanSubmissionsFilter.class);
            DailyPlanOrderFilter dailyPlanOrderFilter = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilter.class);

            if (dailyPlanOrderFilter.getFilter().getControllerIds() == null) {
                dailyPlanOrderFilter.getFilter().setControllerIds(new ArrayList<String>());
                dailyPlanOrderFilter.getFilter().getControllerIds().add(dailyPlanOrderFilter.getControllerId());
            } else {
                if (!dailyPlanOrderFilter.getFilter().getControllerIds().contains(dailyPlanOrderFilter.getControllerId())) {
                    dailyPlanOrderFilter.getFilter().getControllerIds().add(dailyPlanOrderFilter.getControllerId());
                }
            }

            Set<String> allowedControllers = getAllowedControllersOrdersView(dailyPlanOrderFilter.getControllerId(), dailyPlanOrderFilter.getFilter()
                    .getControllerIds(), accessToken).stream().filter(availableController -> getControllerPermissions(availableController,
                            accessToken).getOrders().getCreate()).collect(Collectors.toSet());

            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            submitOrdersToController(dailyPlanOrderFilter, allowedControllers, accessToken);
            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
