package com.sos.joc.classes.publish.listener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBLayerDailyPlan;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.event.bean.deploy.DeploymentHistoryMoveEvent;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;

public class DeploymentHistoryMoveListener {

    private static DeploymentHistoryMoveListener instance;

    private DeploymentHistoryMoveListener() {
        EventBus.getInstance().register(this);
    }

    public static DeploymentHistoryMoveListener getInstance() {
        if (instance == null) {
            instance = new DeploymentHistoryMoveListener();
        }
        return instance;
    }

    @Subscribe({ DeploymentHistoryMoveEvent.class })
    public void updateHistory(DeploymentHistoryMoveEvent evt) throws Throwable {
        String folder = evt.getFolder();
        Long inventoryId = evt.getInventoryId();
        Long auditLogId = evt.getAuditLogId();
        ConfigurationType objectType = ConfigurationType.fromValue(evt.getObjectType());
        
        SOSHibernateSession session = null;
        // TODO: get latest successful from History
        //       check if latest entry was an update operation
        //       clone entry and store with updated folder
        try {
            session = Globals.createSosHibernateStatelessConnection(DeploymentHistoryMoveListener.class.getSimpleName());
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            List<DBItemDeploymentHistory> allDeploymentsPerObject = dbLayer.getDeployedConfigurations(inventoryId);
            Set<DBItemDeploymentHistory> deployments = null;
            if (allDeploymentsPerObject != null) {
                deployments = allDeploymentsPerObject.stream().filter(d -> OperationType.UPDATE.value() == d.getOperation())
                        .filter(d -> DeploymentState.DEPLOYED.value() == d.getState()).collect(
                                Collectors.groupingBy(DBItemDeploymentHistory::getControllerId, 
                                        Collectors.maxBy(Comparator.comparingLong(DBItemDeploymentHistory::getId))))
                        .values().stream().filter(Optional::isPresent).map(Optional::get)
                        .map(toClone -> clone(folder, auditLogId, toClone)).collect(Collectors.toSet());
            }
            for(DBItemDeploymentHistory latest : deployments) {
                session.save(latest);
            }
            deployments.stream().forEach(item -> JocInventory.postDeployHistoryEvent(item));
            List<DBItemDailyPlanOrder> ordersToUpdate = new ArrayList<DBItemDailyPlanOrder>();
            DBItemInventoryReleasedConfiguration released = null;
            DBLayerDailyPlan dbLayerDP = new DBLayerDailyPlan(session);
            if(ConfigurationType.WORKFLOW.equals(objectType)) {
                // TODO: Workflow update DPL_ORDERS
                ordersToUpdate = dbLayerDP.getOrdersByWorkflowName(evt.getName());
            } else if(ConfigurationType.SCHEDULE.equals(objectType)) {
                // TODO: Schedule update INV_RELEASED_CONFIGURATIONS and DPL_ORDERS
                ordersToUpdate = dbLayerDP.getOrdersByScheduleName(evt.getName());
                released = dbLayer.getReleasedItemByConfigurationId(inventoryId);
            }
            for(DBItemDailyPlanOrder order : ordersToUpdate) {
                if(ConfigurationType.WORKFLOW.equals(objectType)) {
                    order.setWorkflowFolder(folder);
                    order.setWorkflowPath(folder.concat("/").concat(order.getWorkflowName()));
                } else if (ConfigurationType.SCHEDULE.equals(objectType)) {
                    order.setScheduleFolder(folder);
                    order.setSchedulePath(folder.concat("/").concat(order.getScheduleName()));
                }
                session.update(order);
            }
            if(released != null) {
                released.setFolder(folder);
                released.setPath(folder.concat("/").concat(released.getName()));
                session.update(released);
            }
            if(!ordersToUpdate.isEmpty()) {
                EventBus.getInstance().post(new DailyPlanEvent(null, null));
            }
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
        
    }
    
    private DBItemDeploymentHistory clone(String newFolder, Long auditLogId, DBItemDeploymentHistory toClone) {
        DBItemDeploymentHistory cloned = new DBItemDeploymentHistory();
        cloned.setAccount(toClone.getAccount());
        cloned.setAuditlogId(auditLogId);
        cloned.setCommitId(toClone.getCommitId());
        cloned.setContent(toClone.getContent());
        cloned.setControllerId(toClone.getControllerId());
        cloned.setControllerInstanceId(toClone.getControllerInstanceId());
        cloned.setDeleteDate(toClone.getDeleteDate());
        cloned.setDeploymentDate(toClone.getDeploymentDate());
        cloned.setErrorMessage(toClone.getErrorMessage());
        cloned.setFolder(newFolder);
        cloned.setInvContent(toClone.getInvContent());
        cloned.setInventoryConfigurationId(toClone.getInventoryConfigurationId());
        cloned.setName(toClone.getName());
        cloned.setOperation(toClone.getOperation());
        cloned.setPath(newFolder.concat("/").concat(toClone.getName()));
        cloned.setSignedContent(toClone.getSignedContent());
        cloned.setState(toClone.getState());
        cloned.setTitle(toClone.getTitle());
        cloned.setType(toClone.getType());
        cloned.setVersion(toClone.getVersion());
        return cloned;
    }
}
