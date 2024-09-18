package com.sos.joc.inventory.impl.common;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.JocAuditObjectsLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.dailyplan.impl.DailyPlanCancelOrderImpl;
import com.sos.joc.dailyplan.impl.DailyPlanDeleteOrdersImpl;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.deploy.DeployHistoryFileOrdersSourceEvent;
import com.sos.joc.event.bean.deploy.DeployHistoryWorkflowEvent;
import com.sos.joc.inventory.impl.ReleaseResourceImpl;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.delete.RequestFilters;
import com.sos.joc.model.inventory.delete.RequestFolder;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.util.DeleteDeployments;

import io.vavr.control.Either;
import js7.base.problem.Problem;

public abstract class ADeleteConfiguration extends JOCResourceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ADeleteConfiguration.class);

    public JOCDefaultResponse remove(String accessToken, RequestFilters in, String request) throws Exception {
        SOSHibernateSession session = null;
        try {
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(request);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            Globals.beginTransaction(session);
            
            Predicate<RequestFilter> isFolder = r -> JocInventory.isFolder(r.getObjectType());
//            if (in.getObjects().stream().parallel().anyMatch(isFolder)) {
//                //throw new 
//            }
            Set<DBItemDeploymentHistory> allDeployments = new HashSet<>();
            DBLayerDeploy deployDbLayer = new DBLayerDeploy(session);
            Set<String> foldersForEvent = new HashSet<>();
            List<Long> workflowInvIds = new ArrayList<>();
            JocAuditObjectsLog auditLogObjectsLogging = new JocAuditObjectsLog(dbAuditLog.getId());
            
            for (RequestFilter r : in.getObjects().stream().filter(isFolder.negate()).collect(Collectors.toSet())) {
                DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, r, folderPermissions);
                final ConfigurationType type = config.getTypeAsEnum();

                if (JocInventory.isReleasable(type)) {
                    cancelOrders(accessToken, config, in.getCancelOrdersDateFrom(), dbLayer);
                    ReleaseResourceImpl.delete(config, dbLayer, dbAuditLog, false, auditLogObjectsLogging, false);
                    foldersForEvent.add(config.getFolder());
                    auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()), dbLayer
                            .getSession(), dbAuditLog));

                } else if (JocInventory.isDeployable(type)) {
                    // TODO restrict to allowed Controllers
                    List<DBItemDeploymentHistory> allDeploymentsPerObject = deployDbLayer.getDeployedConfigurations(config.getName(), config.getType());
                    Set<DBItemDeploymentHistory> deployments = null;
                    if (allDeploymentsPerObject != null) {
                        deployments = allDeploymentsPerObject.stream().filter(d -> OperationType.UPDATE.value() == d.getOperation()).filter(
                                d -> DeploymentState.DEPLOYED.value() == d.getState()).collect(Collectors.groupingBy(
                                        DBItemDeploymentHistory::getControllerId, Collectors.maxBy(Comparator.comparingLong(
                                                DBItemDeploymentHistory::getId)))).values().stream().filter(Optional::isPresent).map(Optional::get)
                                .collect(Collectors.toSet());
                    }
                    if (deployments == null || deployments.isEmpty()) {
                        JocInventory.deleteInventoryConfigurationAndPutToTrash(config, dbLayer, ConfigurationType.FOLDER);
                        auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()),
                                dbLayer.getSession(), dbAuditLog));
                        foldersForEvent.add(config.getFolder());
                        if (JocInventory.isWorkflow(config.getType())) {
                            workflowInvIds.add(config.getId());
                        }
                    } else {
                        allDeployments.addAll(deployments);
                    }
                } else { 
                    // deployment descriptors (not releaseable and not deployable)
                    JocInventory.deleteInventoryConfigurationAndPutToTrash(config, dbLayer, ConfigurationType.DESCRIPTORFOLDER);
                }
            }
            if (allDeployments != null && !allDeployments.isEmpty()) {
                String account = JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel()) ? ClusterSettings.getDefaultProfileAccount(Globals
                        .getConfigurationGlobalsJoc()) : getAccount();
                Set<DBItemInventoryConfiguration> deployedDeployables = DeleteDeployments.delete(allDeployments, deployDbLayer, account, accessToken,
                        getJocError(), dbAuditLog.getId(), true, false, in.getCancelOrdersDateFrom());
                workflowInvIds.addAll(deployedDeployables.stream().filter(i -> JocInventory.isWorkflow(i.getType())).map(
                        DBItemInventoryConfiguration::getId).collect(Collectors.toList()));
            }
            Globals.commit(session);
            auditLogObjectsLogging.log();
            // post events
            for (String folder: foldersForEvent) {
                JocInventory.postEvent(folder);
                JocInventory.postTrashEvent(folder);
            }
            // post event: InventoryTaggingUpdated
            if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
                InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
                dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
            }
            // post events for updating workflows and fileordersources
            if (allDeployments != null) {
                allDeployments.stream().filter(dbItem -> DeployType.FILEORDERSOURCE.intValue() == dbItem.getType()).map(
                        DBItemDeploymentHistory::getControllerId).distinct().map(controllerId -> new DeployHistoryFileOrdersSourceEvent(controllerId))
                        .forEach(evt -> EventBus.getInstance().post(evt));
                allDeployments.stream().filter(dbItem -> DeployType.WORKFLOW.intValue() == dbItem.getType()).map(
                        DBItemDeploymentHistory::getControllerId).distinct().map(controllerId -> new DeployHistoryWorkflowEvent(controllerId))
                        .forEach(evt -> EventBus.getInstance().post(evt));
            }
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public JOCDefaultResponse removeFolder(String accessToken, RequestFolder in, String request) throws Exception {
        return removeFolder(accessToken, in, false, request);
    }

    public JOCDefaultResponse removeFolder(String accessToken, RequestFolder in, boolean forDescriptors, String request) throws Exception {
        SOSHibernateSession session = null;
        try {
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(request);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();
            
            List<Long> workflowInvIds = null;
            DBItemInventoryConfiguration folder = null;
            if(forDescriptors) {
                folder = JocInventory.getConfiguration(dbLayer, null, in.getPath(), ConfigurationType.DESCRIPTORFOLDER, folderPermissions);
            } else {
                folder = JocInventory.getConfiguration(dbLayer, null, in.getPath(), ConfigurationType.FOLDER, folderPermissions);
            }
            JocAuditObjectsLog auditLogObjectsLogging = new JocAuditObjectsLog(dbAuditLog.getId());
            
            if (!forDescriptors) {
                cancelOrders(accessToken, folder, in.getCancelOrdersDateFrom(),dbLayer);
                ReleaseResourceImpl.delete(folder, dbLayer, dbAuditLog, false, auditLogObjectsLogging, false);
                // TODO restrict to allowed Controllers
                List<DBItemInventoryConfiguration> deployables = dbLayer.getFolderContent(folder.getPath(), true, JocInventory.getDeployableTypes(),
                        forDescriptors);
                if (deployables != null && !deployables.isEmpty()) {
                    String account = JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel()) ? ClusterSettings.getDefaultProfileAccount(Globals
                            .getConfigurationGlobalsJoc()) : getAccount();
                    Set<DBItemInventoryConfiguration> deployedDeployables = DeleteDeployments.deleteFolder(request, folder.getPath(), true, Proxies
                            .getControllerDbInstances().keySet(), new DBLayerDeploy(session), account, accessToken, getJocError(), dbAuditLog.getId(),
                            true, false, in.getCancelOrdersDateFrom());
                    
                    workflowInvIds = deployables.stream().filter(i -> JocInventory.isWorkflow(i.getType())).map(
                            DBItemInventoryConfiguration::getId).collect(Collectors.toList());
                    deployables.removeAll(deployedDeployables); // deployables that never were deployed
                    for (DBItemInventoryConfiguration deployable : deployables) {
                        JocInventory.deleteInventoryConfigurationAndPutToTrash(deployable, dbLayer, ConfigurationType.FOLDER);
                    }
                }
            } else {
                List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(folder.getPath(), true, Collections.singleton(
                        ConfigurationType.DEPLOYMENTDESCRIPTOR.intValue()), forDescriptors);
                for (DBItemInventoryConfiguration descriptor : folderContent) {
                    JocInventory.deleteInventoryConfigurationAndPutToTrash(descriptor, dbLayer, ConfigurationType.DESCRIPTORFOLDER);
                }

            }
            JocInventory.deleteEmptyFolders(dbLayer, folder.getPath(), forDescriptors);
            
            Globals.commit(session);
            auditLogObjectsLogging.log();
            
            JocInventory.postFolderEvent(folder.getFolder());
            JocInventory.postTrashFolderEvent(folder.getFolder());
            JocInventory.postTrashEvent(folder.getPath());
            // Tagging events
            if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
                InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
                dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public JOCDefaultResponse delete(String accessToken, RequestFilters in, String request) throws Exception {
        SOSHibernateSession session = null;
        try {
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(request);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
            InventoryJobTagDBLayer dbJobTagLayer = new InventoryJobTagDBLayer(session);
            session.beginTransaction();
            
            Predicate<RequestFilter> isFolder = r -> 
                (ConfigurationType.FOLDER.equals(r.getObjectType()) || ConfigurationType.DESCRIPTORFOLDER.equals(r.getObjectType()));
            if (in.getObjects().stream().parallel().anyMatch(isFolder)) {
                //throw new 
            }
            Set<String> foldersForEvent = new HashSet<>();
            JocAuditObjectsLog auditLogObjectsLogging = new JocAuditObjectsLog(dbAuditLog.getId());
            for (RequestFilter r : in.getObjects().stream().filter(isFolder.negate()).collect(Collectors.toSet())) {
                DBItemInventoryConfigurationTrash config = JocInventory.getTrashConfiguration(dbLayer, r, folderPermissions);
                deleteTaggings(config.getName(), config.getTypeAsEnum(), dbLayer, dbTagLayer, dbJobTagLayer);
                session.delete(config);
                foldersForEvent.add(config.getFolder());
                auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()), session,
                        dbAuditLog));
            }
            
            Globals.commit(session);
            auditLogObjectsLogging.log();
            // post events
            for (String folder: foldersForEvent) {
                JocInventory.postTrashEvent(folder);
            }
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public JOCDefaultResponse deleteFolder(String accessToken, RequestFolder in, String request) throws Exception {
        return deleteFolder(accessToken, in, false, request);
    }
    
    public JOCDefaultResponse deleteFolder(String accessToken, RequestFolder in, boolean forDescriptors, String request) throws Exception {
        SOSHibernateSession session = null;
        try {
            JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            session = Globals.createSosHibernateStatelessConnection(request);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
            InventoryJobTagDBLayer dbJobTagLayer = new InventoryJobTagDBLayer(session);
            session.beginTransaction();
            DBItemInventoryConfigurationTrash config = null;
            if(forDescriptors) {
                config = JocInventory.getTrashConfiguration(dbLayer, null, in.getPath(), ConfigurationType.DESCRIPTORFOLDER, folderPermissions);
            } else {
                config = JocInventory.getTrashConfiguration(dbLayer, null, in.getPath(), ConfigurationType.FOLDER, folderPermissions);
            }
            deleteTaggingsOfFolder(config, dbLayer, dbTagLayer, dbJobTagLayer);
            dbLayer.deleteTrashFolder(config.getPath());
            Globals.commit(session);
            JocInventory.postTrashEvent(config.getFolder());
            JocInventory.postTrashFolderEvent(config.getPath());
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private int deleteTaggings(String name, ConfigurationType type, InventoryDBLayer dbLayer, InventoryTagDBLayer dbTagLayer,
            InventoryJobTagDBLayer dbJobTagLayer) {
        int i = 0;
        if (ConfigurationType.WORKFLOW.equals(type) && dbLayer.getConfigurationByName(name, type.intValue()).isEmpty()) {
            if (dbTagLayer.hasTaggings(name, type.intValue())) {
                i = i + dbTagLayer.delete(name, type.intValue());
            }
            if (dbJobTagLayer.hasTaggings(name)) {
                i = i + dbJobTagLayer.delete(name);
            }
        }
        
        return i;
    }
    
    private int deleteTaggingsOfFolder(DBItemInventoryConfigurationTrash config, InventoryDBLayer dbLayer, InventoryTagDBLayer dbTagLayer,
            InventoryJobTagDBLayer dbJobTagLayer) throws SOSHibernateException {
        int i = 0;
        List<DBItemInventoryConfigurationTrash> workflows = dbLayer.getTrashFolderContent(config.getPath(), true, Collections.singleton(
                ConfigurationType.WORKFLOW.intValue()), false);
        for (DBItemInventoryConfigurationTrash conf : workflows) {
            i += deleteTaggings(conf.getName(), conf.getTypeAsEnum(), dbLayer, dbTagLayer, dbJobTagLayer);
        }
        return i;
    }
    
    private void cancelOrders(String xAccessToken, DBItemInventoryConfiguration config, String cancelOrdersDate, InventoryDBLayer dbLayer)
            throws SOSHibernateException {
        if(cancelOrdersDate != null) {
            if(ConfigurationType.FOLDER.equals(config.getTypeAsEnum())) {
                Set<Integer> confTypes = new HashSet<Integer>();
                confTypes.add(ConfigurationType.SCHEDULE.intValue());
                confTypes.add(ConfigurationType.WORKINGDAYSCALENDAR.intValue());
                confTypes.add(ConfigurationType.NONWORKINGDAYSCALENDAR.intValue());
                List<DBItemInventoryConfiguration> folderContent = 
                        dbLayer.getFolderContent(config.getPath(), true, confTypes, false);
                if(!folderContent.isEmpty()) {
                    folderContent.stream().forEach(configuration -> 
                        cancelOrders2(xAccessToken, configuration, cancelOrdersDate, dbLayer));
                }
            } else if (ConfigurationType.SCHEDULE.equals(config.getTypeAsEnum())
                    || ConfigurationType.WORKINGDAYSCALENDAR.equals(config.getTypeAsEnum()) 
                    || ConfigurationType.NONWORKINGDAYSCALENDAR.equals(config.getTypeAsEnum())) {
                cancelOrders2(xAccessToken, config, cancelOrdersDate, dbLayer);
            }
        }
    }

    private void cancelOrders2(String xAccessToken, DBItemInventoryConfiguration config, String cancelOrdersDate, InventoryDBLayer dbLayer) {
        Set<String> schedulePaths = new HashSet<String>();
        if(ConfigurationType.SCHEDULE.equals(config.getTypeAsEnum())) {
            schedulePaths.add(config.getPath());
        } else if (ConfigurationType.WORKINGDAYSCALENDAR.equals(config.getTypeAsEnum()) 
                || ConfigurationType.NONWORKINGDAYSCALENDAR.equals(config.getTypeAsEnum())) {
            try {
                List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByCalendarName(config.getName());
                if(schedules != null) {
                    schedulePaths = schedules.stream().map(DBItemInventoryConfiguration::getPath).collect(Collectors.toSet());
                    for (DBItemInventoryConfiguration schedule : schedules) {
                        schedule.setReleased(false);
                        schedule.setValid(false);
                        dbLayer.getSession().update(schedule);
                        DBItemInventoryReleasedConfiguration releasedSchedule = dbLayer.getReleasedItemByConfigurationId(schedule.getId());
                        if (releasedSchedule != null) {
                            try {
                                Schedule scheduleObject = Globals.objectMapper.readValue(releasedSchedule.getContent(), Schedule.class);
                                scheduleObject.setPlanOrderAutomatically(false);
                                scheduleObject.setSubmitOrderToControllerWhenPlanned(false);
                                releasedSchedule.setContent(Globals.objectMapper.writeValueAsString(scheduleObject));
                                dbLayer.getSession().update(releasedSchedule);
                            } catch (JsonProcessingException e) {
                                getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                                LOGGER.warn(e.getMessage());
                            } 
                        } 
                    }
                }
            } catch (SOSHibernateException e) {
                getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                LOGGER.warn(e.getMessage());
            }
        }
        if(!schedulePaths.isEmpty()) {
            DailyPlanCancelOrderImpl cancelOrderImpl = new DailyPlanCancelOrderImpl();
            DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
            DailyPlanOrderFilterDef orderFilter = new DailyPlanOrderFilterDef();
            if("now".equals(cancelOrdersDate.toLowerCase())) {
                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
                orderFilter.setDailyPlanDateFrom(sdf.format(Date.from(Instant.now())));
            } else {
                orderFilter.setDailyPlanDateFrom(cancelOrdersDate);
            }
            orderFilter.setSchedulePaths(new ArrayList<String>(schedulePaths));
            
            try {
                Map<String, List<DBItemDailyPlanOrder>> ordersPerController = 
                        cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilter, xAccessToken, false, false);
                Map<String, CompletableFuture<Either<Problem, Void>>> cancelOrderResponsePerController = 
                        cancelOrderImpl.cancelOrders(ordersPerController, xAccessToken, null, false, false);

                for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
                    if(!cancelOrderResponsePerController.containsKey(controllerId)) {
                        cancelOrderResponsePerController.put(controllerId, CompletableFuture.supplyAsync(() -> Either.right(null)));
                    }
                    cancelOrderResponsePerController.get(controllerId).thenAccept(
                            either -> {
                                if (either.isRight()) {
                                    DailyPlanOrderFilterDef localOrderFilter = new DailyPlanOrderFilterDef();
                                    localOrderFilter.setControllerIds(Collections.singletonList(controllerId));
                                    localOrderFilter.setDailyPlanDateFrom(orderFilter.getDailyPlanDateFrom());
                                    localOrderFilter.setSchedulePaths(orderFilter.getSchedulePaths());
                                    try {
                                        boolean successful = deleteOrdersImpl.deleteOrders(localOrderFilter, xAccessToken, false, false, false);
                                        if (!successful) {
                                            getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                                            LOGGER.warn("Order delete failed due to missing permission.");
                                        }
                                    } catch (SOSHibernateException e) {
                                        getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                                        LOGGER.warn("Order delete failed due to: ", e.getMessage());
                                    }
                                } else {
                                    getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                                    LOGGER.warn("Order cancel failed due to missing permission.");
                                }
                            });
                }
            } catch (Exception e) {
                getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                LOGGER.warn(e.getMessage());
            }
        }
    }

}
