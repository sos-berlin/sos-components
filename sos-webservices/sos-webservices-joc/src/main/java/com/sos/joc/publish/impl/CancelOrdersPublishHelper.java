package com.sos.joc.publish.impl;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.classes.controller.ControllerCommandResponse;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.impl.DailyPlanCancelOrderImpl;
import com.sos.joc.dailyplan.impl.DailyPlanDeleteOrdersImpl;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocReleaseException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.inventory.impl.common.ADeleteConfiguration;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;

public class CancelOrdersPublishHelper {

    public static List<CompletableFuture<ControllerCommandResponse>> getCancelOrderFutures(String xAccessToken, DailyPlanOrderFilterDef orderFilter) 
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, 
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, SOSHibernateException, ExecutionException {
        
        if ((orderFilter.getWorkflowPaths() == null || orderFilter.getWorkflowPaths().isEmpty()) && 
                (orderFilter.getSchedulePaths() == null || orderFilter.getSchedulePaths().isEmpty())) {
            return Collections.emptyList();
        }
        List<CompletableFuture<ControllerCommandResponse>> futures = new ArrayList<>();
        DailyPlanCancelOrderImpl cancelOrderImpl = new DailyPlanCancelOrderImpl();
        DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
        Map<String, List<DBItemDailyPlanOrder>> ordersPerController = cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilter,
                xAccessToken);
        Map<String, CompletableFuture<ControllerCommandResponse>> cancelOrderResponsePerController = cancelOrderImpl.cancelOrders(
                ordersPerController, xAccessToken);
        for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
            if (!cancelOrderResponsePerController.containsKey(controllerId)) {
                cancelOrderResponsePerController.put(controllerId, CompletableFuture.completedFuture(new ControllerCommandResponse(
                        controllerId)));
            }
            futures.add(cancelOrderResponsePerController.get(controllerId).thenApply(ccr -> {
                if (ccr.getException().isEmpty()) {
                    DailyPlanOrderFilterDef localOrderScheduleFilter = new DailyPlanOrderFilterDef();
                    localOrderScheduleFilter.setControllerIds(Collections.singletonList(controllerId));
                    localOrderScheduleFilter.setDailyPlanDateFrom(orderFilter.getDailyPlanDateFrom());
                    localOrderScheduleFilter.setSchedulePaths(orderFilter.getSchedulePaths());
                    
                    DailyPlanOrderFilterDef localOrderWorkflowFilter = new DailyPlanOrderFilterDef();
                    localOrderWorkflowFilter.setControllerIds(Collections.singletonList(controllerId));
                    localOrderWorkflowFilter.setDailyPlanDateFrom(orderFilter.getDailyPlanDateFrom());
                    localOrderWorkflowFilter.setWorkflowPaths(orderFilter.getWorkflowPaths());
                    
                    boolean successful1 = true;
                    boolean successful2 = true;
                    try {
                        // TODO create Method to transfer a set of order objects to delete instead of a filter
                        if (orderFilter.getSchedulePaths() != null && !orderFilter.getSchedulePaths().isEmpty()) {
                            successful1 = deleteOrdersImpl.deleteOrders(localOrderScheduleFilter, xAccessToken, false, false, false); 
                        }
                        if (orderFilter.getWorkflowPaths() != null && !orderFilter.getWorkflowPaths().isEmpty()) {
                            successful2 = deleteOrdersImpl.deleteOrders(localOrderWorkflowFilter, xAccessToken, false, false, false);
                        }
                        if (!successful1 || !successful2) {
                            return new ControllerCommandResponse(controllerId, Optional.of(new JocReleaseException(
                                    "Order delete failed due to missing permission.")));
                        }
                    } catch (Exception e) {
                        return new ControllerCommandResponse(controllerId, Optional.of(e));
                    }
                }
                return ccr;
            }));
        }
        return futures;
    }
    
    // for Deploy and Revoke operations
    public static DailyPlanOrderFilterDef getDailyPlanOrderFilter(Set<DBItemDeploymentHistory> deployed, Optional<Set<DBItemDeploymentHistory>> renamed,
            String cancelOrdersDateFrom, String controllerId) {
        DailyPlanOrderFilterDef orderFilter = new DailyPlanOrderFilterDef();
        orderFilter.setControllerIds(Collections.singletonList(controllerId));
        if("now".equals(cancelOrdersDateFrom.toLowerCase())) {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
            orderFilter.setDailyPlanDateFrom(sdf.format(Date.from(Instant.now())));
        } else {
            orderFilter.setDailyPlanDateFrom(cancelOrdersDateFrom);
        }
        orderFilter.setWorkflowPaths(
                deployed.stream()
                    .filter(item -> item.getTypeAsEnum().equals(DeployType.WORKFLOW))
                    .map(DBItemDeploymentHistory::getName).distinct().collect(Collectors.toList())
            );
        renamed.map(s -> s.stream()
                .filter(item -> item.getTypeAsEnum().equals(DeployType.WORKFLOW))
                .map(DBItemDeploymentHistory::getName).distinct().toList()).ifPresent(s -> orderFilter.getWorkflowPaths().addAll(s)
            );

        return orderFilter;
    }

    // for Recall operations
    public static DailyPlanOrderFilterDef getDailyPlanOrderFilter(Collection<DBItemInventoryReleasedConfiguration> released, InventoryDBLayer dbLayer) {
        DailyPlanOrderFilterDef orderFilter = new DailyPlanOrderFilterDef();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
        orderFilter.setDailyPlanDateFrom(sdf.format(Date.from(Instant.now())));
        if(released != null) {
            orderFilter.setSchedulePaths(released.stream().map(r -> {
                try {
                    return ADeleteConfiguration.addScheduleNames(r, dbLayer);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            }).filter(Optional::isPresent).map(Optional::get).flatMap(List::stream).distinct().toList());
        }
        return orderFilter;
    }
}
