package com.sos.webservices.order.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.joc.DBItemJocAuditLog;
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
import com.sos.js7.order.initiator.DailyPlanRunner;
import com.sos.js7.order.initiator.DailyPlanSettings;
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

    @Override
    public JOCDefaultResponse postSubmitOrders(String accessToken, byte[] filterBytes) throws JocException {
        LOGGER.debug("Submit orders to JS7 controller");
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanSubmissionsFilter.class);
            DailyPlanOrderFilter in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilter.class);

            if (in.getFilter().getControllerIds() == null) {
                in.getFilter().setControllerIds(new ArrayList<String>());
                in.getFilter().getControllerIds().add(in.getControllerId());
            } else {
                if (!in.getFilter().getControllerIds().contains(in.getControllerId())) {
                    in.getFilter().getControllerIds().add(in.getControllerId());
                }
            }

            Set<String> allowedControllers = getAllowedControllersOrdersView(in.getControllerId(), in.getFilter().getControllerIds(), accessToken)
                    .stream().filter(availableController -> getControllerPermissions(availableController, accessToken).getOrders().getCreate())
                    .collect(Collectors.toSet());

            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }

            submitOrdersToController(in, allowedControllers, accessToken);
            return JOCDefaultResponse.responseStatusJSOk(new Date());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void submitOrdersToController(DailyPlanOrderFilter in, Collection<String> allowedControllers, String accessToken)
            throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, ControllerConnectionResetException, ControllerConnectionRefusedException, IOException,
            ParseException, SOSException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        DBItemJocAuditLog auditLog = storeAuditLog(in.getAuditLog(), CategoryType.DAILYPLAN);
        setSettings();

        DailyPlanSettings settings = new DailyPlanSettings();
        settings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
        settings.setOverwrite(false);
        settings.setSubmit(true);
        settings.setTimeZone(settings.getTimeZone());
        settings.setPeriodBegin(settings.getPeriodBegin());
        DailyPlanRunner runner = new DailyPlanRunner(settings, false);

        if (in.getFilter() == null) {
            in.setFilter(new DailyPlanOrderFilterDef());
        }

        FolderPermissionEvaluator evaluator = new FolderPermissionEvaluator();
        evaluator.setWorkflowFolders(in.getFilter().getWorkflowFolders());
        evaluator.setScheduleFolders(in.getFilter().getScheduleFolders());
        evaluator.setSchedulePaths(in.getFilter().getSchedulePaths());
        evaluator.setWorkflowPaths(in.getFilter().getWorkflowPaths());

        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(null);
        Set<AuditLogDetail> auditLogDetails = new HashSet<>();

        for (String controllerId : allowedControllers) {
            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            folderPermissions.setSchedulerId(controllerId);
            evaluator.getPermittedNames(folderPermissions, controllerId, filter);

            if (evaluator.isHasPermission()) {
                List<String> orderIds = new ArrayList<String>();
                if (in.getFilter().getOrderIds() != null) {
                    orderIds.addAll(in.getFilter().getOrderIds());

                    for (String orderId : orderIds) {
                        dbLayer.addCyclicOrderIds(in.getFilter().getOrderIds(), orderId, controllerId, settings.getTimeZone(), settings
                                .getPeriodBegin());
                    }
                }

                filter.setOrderIds(in.getFilter().getOrderIds());
                filter.setSubmitted(false);
                filter.setSortMode(null);
                filter.setOrderCriteria(null);
                filter.setDailyPlanDate(in.getFilter().getDailyPlanDate(), settings.getTimeZone(), settings.getPeriodBegin());
                filter.setSubmissionIds(in.getFilter().getSubmissionHistoryIds());

                filter.setWorkflowNames(evaluator.getPermittedWorkflowNames());
                filter.setScheduleNames(evaluator.getPermittedScheduleNames());
                filter.setControllerId(controllerId);

                SOSHibernateSession session = null;
                List<DBItemDailyPlanOrder> items = null;
                try {
                    session = Globals.createSosHibernateStatelessConnection(API_CALL);
                    // sosHibernateSession.setAutoCommit(false);
                    // Globals.beginTransaction(sosHibernateSession);

                    dbLayer.setSession(session);
                    items = dbLayer.getDailyPlanList(filter, 0);
                    // Globals.commit(sosHibernateSession);
                } finally {
                    Globals.disconnect(session);
                }

                runner.submitOrders(controllerId, items, null, getJocError(), getAccessToken());

                EventBus.getInstance().post(new DailyPlanEvent(in.getFilter().getDailyPlanDate()));

                for (DBItemDailyPlanOrder item : items) {
                    auditLogDetails.add(new AuditLogDetail(item.getWorkflowPath(), item.getOrderId(), controllerId));
                }

            }
        }
        OrdersHelper.storeAuditLogDetails(auditLogDetails, auditLog.getId()).thenAccept(either -> ProblemHelper.postExceptionEventIfExist(either,
                accessToken, getJocError(), null));
    }

}
