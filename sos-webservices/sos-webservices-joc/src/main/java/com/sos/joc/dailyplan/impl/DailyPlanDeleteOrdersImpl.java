package com.sos.joc.dailyplan.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanDeleteOrderResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanDeleteOrdersImpl extends JOCOrderResourceImpl implements IDailyPlanDeleteOrderResource {


    @Override
    public JOCDefaultResponse postDeleteOrders(String accessToken, byte[] filterBytes) {

        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.DAILYPLAN);
            // validation without required dailyPlanDateFrom 
            JsonValidator.validateFailFast(filterBytes, "orderManagement/dailyplan/dailyPlanOrdersFilterDef-schema.json");
            DailyPlanOrderFilterDef in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilterDef.class);
            
            in.setStates(Collections.singletonList(DailyPlanOrderStateText.PLANNED));
            Map<String, List<DBItemDailyPlanOrder>> ordersPerController = DailyPlanCancelOrderImpl.getOrdersPerController(in);
            
            JOCDefaultResponse response = null;
            for (Map.Entry<String, List<DBItemDailyPlanOrder>> entry : ordersPerController.entrySet()) {
                String controllerId = entry.getKey();
                Set<String> workflows = ordersPerController.getOrDefault(controllerId, Collections.emptyList()).stream().map(
                        DBItemDailyPlanOrder::getWorkflowName).collect(Collectors.toSet());
                response = initWorkflowPermissions(accessToken, getControllerPermissions(controllerId, accessToken).map(p -> p.getOrders()
                        .getCreate()), workflows);
                if (response != null) {
                    return response;
                }
            }
            
            storeAuditLog(in.getAuditLog());
            deleteOrders(ordersPerController);
            return responseStatusJSOk(new Date());

        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    public synchronized void deleteOrders(Map<String, List<DBItemDailyPlanOrder>> ordersPerController) throws SOSHibernateException {
        
        if (!ordersPerController.isEmpty()) {
            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                for (Map.Entry<String, List<DBItemDailyPlanOrder>> entry : ordersPerController.entrySet()) {
                    String controllerId = entry.getKey();
                    final Set<Folder> permittedFolders = folderPermissions.getListOfFolders(controllerId);
                    List<DBItemDailyPlanOrder> permittedOrders = entry.getValue().stream().filter(o -> folderIsPermitted(o.getWorkflowFolder(),
                            permittedFolders)).distinct().toList();
                    deleteOrders(controllerId, permittedOrders, dbLayer);
                    
                    //events
                    permittedOrders.stream().map(DBItemDailyPlanOrder::getOrderId).map(oId -> oId.substring(1, 11)).distinct().map(
                            date -> new DailyPlanEvent(controllerId, date)).forEach(EventBus.getInstance()::post);
                }
                
            } finally {
                Globals.disconnect(session);
            }
        }
    }

    // called by CancelOrdersPublishHelper, ReleaseResourceImpl, ADeleteConfiguration
    // without check of order permissions
    public static synchronized void deleteOrdersOfController(String caller, String controllerId, String dailyPlanDateFrom, List<String> workflows,
            List<String> schedules) throws SOSHibernateException {
        
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setControllerId(controllerId);
        filter.setSubmissionForDateFrom(toUTCDate(dailyPlanDateFrom));
        filter.addState(DailyPlanOrderStateText.PLANNED);
        
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(caller == null ? "deleteOrdersFromPlan" : caller);
            if (schedules != null && !schedules.isEmpty()) {
                filter.setScheduleNames(schedules.stream().map(JocInventory::pathToName).distinct().collect(Collectors.toList()));
                deleteOrders(filter, session);
                filter.setScheduleNames(null);
            }
            if (workflows != null && !workflows.isEmpty()) {
                filter.setWorkflowNames(workflows.stream().map(JocInventory::pathToName).distinct().collect(Collectors.toList()));
                deleteOrders(filter, session);
            }
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static void deleteOrders(FilterDailyPlannedOrders filter, SOSHibernateSession session) throws SOSHibernateException {
        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
        
        List<DBItemDailyPlanOrder> dpOrders = dbLayer.getDailyPlanList(filter, 0);
        deleteOrders(filter.getControllerId(), dpOrders, dbLayer);
    }
    
    private static void deleteOrders(String controllerId, List<DBItemDailyPlanOrder> dpOrders, DBLayerDailyPlannedOrders dbLayer)
            throws SOSHibernateException {

        // without transactions
        for (DBItemDailyPlanOrder dpOrder : dpOrders) {
            dbLayer.getSession().delete(dpOrder);
        }

        dbLayer.executeDeleteVariables(dpOrders.stream().map(DBItemDailyPlanOrder::getOrderId), controllerId);
    }
}
