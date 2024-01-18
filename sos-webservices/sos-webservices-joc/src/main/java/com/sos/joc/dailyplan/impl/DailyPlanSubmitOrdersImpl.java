package com.sos.joc.dailyplan.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.FolderPermissionEvaluator;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanSubmitOrderResource;
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
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanSubmitOrdersImpl extends JOCOrderResourceImpl implements IDailyPlanSubmitOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSubmitOrdersImpl.class);

    @Override
    public JOCDefaultResponse postSubmitOrders(String accessToken, byte[] filterBytes) {
        LOGGER.debug("Submit orders to JS7 controller");
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, "orderManagement/dailyplan/dailyPlanOrdersFilterDef-schema.json");
            DailyPlanOrderFilterDef in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilterDef.class);
            
            boolean noControllerAvailable = Proxies.getControllerDbInstances().isEmpty();
            boolean permitted = true;
            Set<String> allowedControllers = Collections.emptySet();
            if (!noControllerAvailable) {
                Stream<String> controllerIds = Proxies.getControllerDbInstances().keySet().stream();
                if (in.getControllerIds() != null && !in.getControllerIds().isEmpty()) {
                    controllerIds = controllerIds.filter(availableController -> in.getControllerIds().contains(availableController));
                }
                allowedControllers = controllerIds.filter(availableController -> getControllerPermissions(availableController,
                        accessToken).getOrders().getCreate()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
            }

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }
            
            submitOrdersToController(in, allowedControllers, accessToken, true);
            return JOCDefaultResponse.responseStatusJSOk(new Date());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void submitOrdersToController(DailyPlanOrderFilterDef in, Collection<String> allowedControllers, String accessToken, boolean withEvent)
            throws JsonParseException, JsonMappingException, DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, ControllerConnectionResetException, ControllerConnectionRefusedException, IOException,
            ParseException, SOSException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        DBItemJocAuditLog auditLog = storeAuditLog(in.getAuditLog(), CategoryType.DAILYPLAN);
        setSettings();
        
        DailyPlanSettings settings = new DailyPlanSettings();
        settings.setUserAccount(this.getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname());
        settings.setOverwrite(false);
        settings.setSubmit(true);
        settings.setTimeZone(getSettings().getTimeZone());
        settings.setPeriodBegin(getSettings().getPeriodBegin());
        DailyPlanRunner runner = new DailyPlanRunner(settings);

        FolderPermissionEvaluator evaluator = new FolderPermissionEvaluator();
        evaluator.setWorkflowFolders(in.getWorkflowFolders());
        evaluator.setScheduleFolders(in.getScheduleFolders());
        evaluator.setSchedulePaths(in.getSchedulePaths());
        evaluator.setWorkflowPaths(in.getWorkflowPaths());

        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(null);
        Set<AuditLogDetail> auditLogDetails = new HashSet<>();
        
        for (String controllerId : allowedControllers) {
            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            folderPermissions.setSchedulerId(controllerId);
            evaluator.getPermittedNames(folderPermissions, controllerId, filter);

            if (evaluator.isHasPermission()) {
                List<String> orderIds = new ArrayList<String>();
                if (in.getOrderIds() != null) {
                    orderIds.addAll(in.getOrderIds());

                    for (String orderId : orderIds) {
                        dbLayer.addCyclicOrderIds(in.getOrderIds(), orderId, controllerId, settings.getTimeZone(), settings
                                .getPeriodBegin());
                    }
                }

                filter.setOrderIds(in.getOrderIds());
                filter.setSubmitted(false);
                filter.setSortMode(null);
                filter.setOrderCriteria(null);
                // TODO not planned start time is relevant
//                filter.setDailyPlanInterval(in.getDailyPlanDateFrom(), in.getDailyPlanDateTo(), settings.getTimeZone(),
//                        settings.getPeriodBegin());
                // instead join to submissions
                filter.setSubmissionForDateFrom(toUTCDate(in.getDailyPlanDateFrom()));
                filter.setSubmissionForDateTo(toUTCDate(in.getDailyPlanDateTo()));
                filter.setSubmissionIds(in.getSubmissionHistoryIds());

                filter.setWorkflowNames(evaluator.getPermittedWorkflowNames());
                filter.setScheduleNames(evaluator.getPermittedScheduleNames());
                filter.setControllerId(controllerId);

                SOSHibernateSession session = null;
                List<DBItemDailyPlanOrder> items = null;
                try {
                    session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                    dbLayer.setSession(session);
                    items = dbLayer.getDailyPlanList(filter, 0);
                } finally {
                    Globals.disconnect(session);
                }

                runner.submitOrders(StartupMode.manual, controllerId, items, null, getJocError(), accessToken);

                Set<String> days = new HashSet<String>();
                for (DBItemDailyPlanOrder item : items) {
                    if (withEvent) {
                        String date = item.getDailyPlanDate(settings.getTimeZone());
                        if (!days.contains(date)) {
                            days.add(date);
                            EventBus.getInstance().post(new DailyPlanEvent(item.getControllerId(), date));
                        }
                    }
                    auditLogDetails.add(new AuditLogDetail(item.getWorkflowPath(), item.getOrderId(), controllerId));
                }

            }
        }
        OrdersHelper.storeAuditLogDetails(auditLogDetails, auditLog.getId()).thenAccept(either -> ProblemHelper.postExceptionEventIfExist(either,
                accessToken, getJocError(), null));
        
    }

}
