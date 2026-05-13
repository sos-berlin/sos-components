package com.sos.joc.inventory.impl.common;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.JocAuditObjectsLog;
import com.sos.joc.classes.controller.ControllerCommandResponse;
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
import com.sos.joc.db.inventory.dependencies.DBLayerDependencies;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.problem.ProblemEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocReleaseException;
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
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            Predicate<RequestFilter> isFolder = r -> JocInventory.isFolder(r.getObjectType());

            DBLayerDeploy deployDbLayer = new DBLayerDeploy(session);
            Set<String> foldersForEvent = new HashSet<>();
            List<Long> workflowInvIds = new ArrayList<>();
            JocAuditObjectsLog auditLogObjectsLogging = new JocAuditObjectsLog(dbAuditLog.getId());
            
            // JOC-2224
            List<DBItemInventoryReleasedConfiguration> released = getReleased(in.getObjects(), session);
            Set<DBItemDeploymentHistory> deployments = getDeployments(in.getObjects(), session);
            List<CompletableFuture<ControllerCommandResponse>> futures = cancelOrders(released, deployments, accessToken, session);
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                Map<Boolean, List<ControllerCommandResponse>> mappedFutures = futures.stream().map(CompletableFuture::join)
                        .collect(Collectors.groupingBy(ControllerCommandResponse::hasException));
                mappedFutures.putIfAbsent(true, Collections.emptyList());
                mappedFutures.putIfAbsent(false, Collections.emptyList());
                if(!mappedFutures.get(true).isEmpty()) {
                    // contains futures with errors
                    String message = mappedFutures.get(true).stream().peek(controllerCommandResult -> {
                        getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                        LOGGER.error(controllerCommandResult.getControllerId(), controllerCommandResult.getException().get());
                    }).map(c -> c.getControllerId() + ": " + c.getException().get().toString()).collect(Collectors.joining(System.lineSeparator()));
                    EventBus.getInstance().post(new ProblemEvent(accessToken, null, message));
                }
                if (!mappedFutures.get(false).isEmpty() || (mappedFutures.get(false).isEmpty() && mappedFutures.get(true).isEmpty())){
                    SOSHibernateSession futureSession = null;
                    try {
                        futureSession = Globals.createSosHibernateStatelessConnection(request + " - afterOrderDeletion");
                        InventoryDBLayer futureDbLayer = new InventoryDBLayer(futureSession);
                        futureDbLayer.deleteReleasedItemsAndNotesByInventoryId(released.stream().map(DBItemInventoryReleasedConfiguration::getCid)
                                .collect(Collectors.toList()));
                        for(DBItemInventoryReleasedConfiguration releasedItem : released) {
                            DBItemInventoryConfiguration config = futureSession.get(DBItemInventoryConfiguration.class, releasedItem.getCid());
                            // ReleaseResourceImpl.delete is transactional, was replaced by below code
                            config.setAuditLogId(dbAuditLog.getId());
                            JocInventory.deleteInventoryConfigurationAndPutToTrash(config, futureDbLayer, ConfigurationType.FOLDER);
                            DBLayerDependencies dependenciesDbLayer = new DBLayerDependencies(futureDbLayer.getSession());
                            dependenciesDbLayer.deleteDependencies(config);
                            auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()), 
                                    futureSession, dbAuditLog));
                            foldersForEvent.add(config.getFolder());
                        }
                        String account = JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel()) ? ClusterSettings.getDefaultProfileAccount(Globals
                                .getConfigurationGlobalsJoc()) : getAccount();
                        deployments.stream().map(DBItemDeploymentHistory::getFolder).forEach(folder -> foldersForEvent.add(folder));
                        Set<DBItemInventoryConfiguration> deployedDeployables = DeleteDeployments.delete(deployments, new DBLayerDeploy(futureSession),
                                account, accessToken, getJocError(), dbAuditLog.getId(), true, false, in.getCancelOrdersDateFrom());
                        // events
                        auditLogObjectsLogging.log();
                        workflowInvIds.addAll(deployedDeployables.stream().filter(i -> JocInventory.isWorkflow(i.getType())).map(
                                DBItemInventoryConfiguration::getId).collect(Collectors.toList()));
                        // post event: InventoryTaggingUpdated
                        for (String folder: foldersForEvent) {
                            JocInventory.postEvent(folder);
                            JocInventory.postTrashEvent(folder);
                        }
                        if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
                            InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(futureSession);
                            dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
                            // TODO post JocInventory::postJobTaggingEvent
                        }
                        // post events for updating workflows and fileordersources cunsomed by WorkflowRefs
                        if (deployments != null) {
                            JocInventory.postDeployHistoryEventWhenDeleted(deployments);
                        }
                    } catch (SOSHibernateException | ExecutionException e) {
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, getJocError(), null);
                    } finally {
                        Globals.disconnect(futureSession);
                    }
                }
            });
            
//            
//            for (RequestFilter r : in.getObjects().stream().filter(isFolder.negate()).collect(Collectors.toSet())) {
//                DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, r, folderPermissions);
//                final ConfigurationType type = config.getTypeAsEnum();
//                
//                if (JocInventory.isReleasable(type)) {
//                    cancelOrders(accessToken, config, in.getCancelOrdersDateFrom(), dbLayer);
//                    ReleaseResourceImpl.delete(config, dbLayer, dbAuditLog, false, auditLogObjectsLogging, false);
//                    foldersForEvent.add(config.getFolder());
//                    auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()), dbLayer
//                            .getSession(), dbAuditLog));
//
//                } else if (JocInventory.isDeployable(type)) {
//                    // TODO restrict to allowed Controllers
//                    List<DBItemDeploymentHistory> allDeploymentsPerObject = deployDbLayer.getDeployedConfigurations(config.getName(), config.getType());
//                    Set<DBItemDeploymentHistory> deployments = null;
//                    if (allDeploymentsPerObject != null) {
//                        deployments = allDeploymentsPerObject.stream().filter(d -> OperationType.UPDATE.value() == d.getOperation()).filter(
//                                d -> DeploymentState.DEPLOYED.value() == d.getState()).collect(Collectors.groupingBy(
//                                        DBItemDeploymentHistory::getControllerId, Collectors.maxBy(Comparator.comparing(
//                                                DBItemDeploymentHistory::getDeploymentDate)))).values().stream().filter(Optional::isPresent).map(
//                                                        Optional::get).collect(Collectors.toSet());
//                    }
//                    if (deployments == null || deployments.isEmpty()) {
//                        JocInventory.deleteInventoryConfigurationAndPutToTrash(config, dbLayer, ConfigurationType.FOLDER);
//                        auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()),
//                                dbLayer.getSession(), dbAuditLog));
//                        foldersForEvent.add(config.getFolder());
//                        if (JocInventory.isWorkflow(config.getType())) {
//                            workflowInvIds.add(config.getId());
//                        }
//                    } else {
//                        allDeployments.addAll(deployments);
//                        deployments.stream().map(DBItemDeploymentHistory::getFolder).forEach(folder -> foldersForEvent.add(folder));
//                    }
//                } else { 
//                    // deployment descriptors (not releaseable and not deployable)
//                    JocInventory.deleteInventoryConfigurationAndPutToTrash(config, dbLayer, ConfigurationType.DESCRIPTORFOLDER);
//                }
//            }
//            if (allDeployments != null && !allDeployments.isEmpty()) {
//                String account = JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel()) ? ClusterSettings.getDefaultProfileAccount(Globals
//                        .getConfigurationGlobalsJoc()) : getAccount();
//                // JOC-2158: cancelOrder was missing here!
//                List<DBItemInventoryConfiguration> usedSchedules = dbLayer.getUsedSchedulesByWorkflowNames(
//                        allDeployments.stream().filter(item -> DeployType.WORKFLOW.equals(item.getTypeAsEnum()))
//                        .map(workflow -> workflow.getName()).collect(Collectors.toList()));
//                if(!usedSchedules.isEmpty()) {
//                    usedSchedules.stream().forEach(schedule -> {
//                        try {
//                            cancelOrders(accessToken, schedule, in.getCancelOrdersDateFrom(), dbLayer);
//                        } catch (SOSHibernateException e) {
//                            throw new JocSosHibernateException(e);
//                        }
//                    });
//                }
//                // JOC-2158: fix ends
//                Set<DBItemInventoryConfiguration> deployedDeployables = DeleteDeployments.delete(allDeployments, deployDbLayer, account, accessToken,
//                        getJocError(), dbAuditLog.getId(), true, false, in.getCancelOrdersDateFrom());
//                workflowInvIds.addAll(deployedDeployables.stream().filter(i -> JocInventory.isWorkflow(i.getType())).map(
//                        DBItemInventoryConfiguration::getId).collect(Collectors.toList()));
//            }
//            Globals.commit(session);
//            auditLogObjectsLogging.log();
//            // post events
//            for (String folder: foldersForEvent) {
//                JocInventory.postEvent(folder);
//                JocInventory.postTrashEvent(folder);
//            }
//            // post event: InventoryTaggingUpdated
//            if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
//                InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
//                dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
//                // TODO post JocInventory::postJobTaggingEvent
//            }
//            // post events for updating workflows and fileordersources cunsomed by WorkflowRefs
//            if (allDeployments != null) {
//                JocInventory.postDeployHistoryEventWhenDeleted(allDeployments);
//            }
            
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
//            Globals.rollback(session);
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
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            List<Long> workflowInvIds = new ArrayList<Long>();
            DBItemInventoryConfiguration folder = getFolder(dbLayer, in.getPath(), forDescriptors);
            JocAuditObjectsLog auditLogObjectsLogging = new JocAuditObjectsLog(dbAuditLog.getId());
            
            if (!forDescriptors) {
                List<CompletableFuture<Void>> futures = cancelOrders(accessToken, folder, in.getCancelOrdersDateFrom(),dbLayer);
                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                    SOSHibernateSession futureSession = null;
                    try {
                        futureSession = Globals.createSosHibernateStatelessConnection(request);
                        futureSession.setAutoCommit(false);
                        InventoryDBLayer futureDbLayer = new InventoryDBLayer(futureSession);
                        Globals.beginTransaction(futureSession);
                        ReleaseResourceImpl.delete(folder, futureDbLayer, dbAuditLog, false, auditLogObjectsLogging, false);
                        // TODO restrict to allowed Controllers
                        List<DBItemInventoryConfiguration> deployables = futureDbLayer.getFolderContent(folder.getPath(), true, 
                                JocInventory.getDeployableTypes(), forDescriptors);
                        if (deployables != null && !deployables.isEmpty()) {
                            String account = JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel()) ? ClusterSettings.getDefaultProfileAccount(Globals
                                    .getConfigurationGlobalsJoc()) : getAccount();
                            Set<DBItemInventoryConfiguration> deployedDeployables = DeleteDeployments.deleteFolder(request, folder.getPath(), true,
                                    Proxies.getControllerDbInstances().keySet(), new DBLayerDeploy(futureSession), account, accessToken, getJocError(),
                                    dbAuditLog.getId(), true, false, in.getCancelOrdersDateFrom());
                            
                            workflowInvIds.addAll(deployables.stream().filter(i -> JocInventory.isWorkflow(i.getType())).map(
                                    DBItemInventoryConfiguration::getId).collect(Collectors.toList()));
                            deployables.removeAll(deployedDeployables); // deployables that never were deployed
                            for (DBItemInventoryConfiguration deployable : deployables) {
                                JocInventory.deleteInventoryConfigurationAndPutToTrash(deployable, futureDbLayer, ConfigurationType.FOLDER);
                            }
                        }
                        Globals.commit(futureSession);
                        futureSession.setAutoCommit(true);
                        JocInventory.deleteEmptyFolders(dbLayer, folder.getPath(), forDescriptors);
                        auditLogObjectsLogging.log();
                        postEvents(folder, workflowInvIds, futureSession);
                    } catch (Exception e) {
                        Globals.rollback(futureSession);
                        throw new JocException(e);
                    } finally {
                        Globals.disconnect(futureSession);
                    }
                });
            } else {
                List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(folder.getPath(), true, Collections.singleton(
                        ConfigurationType.DEPLOYMENTDESCRIPTOR.intValue()), forDescriptors);
                for (DBItemInventoryConfiguration descriptor : folderContent) {
                    JocInventory.deleteInventoryConfigurationAndPutToTrash(descriptor, dbLayer, ConfigurationType.DESCRIPTORFOLDER);
                }
                JocInventory.deleteEmptyFolders(dbLayer, folder.getPath(), forDescriptors);
                auditLogObjectsLogging.log();
                postEvents(folder, workflowInvIds, session);
            }
            return responseStatusJSOk(Date.from(Instant.now()));
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private void postEvents(DBItemInventoryConfiguration folder, List <Long> workflowInvIds, SOSHibernateSession session) {
        JocInventory.postFolderEvent(folder.getFolder());
        JocInventory.postTrashFolderEvent(folder.getFolder());
        JocInventory.postTrashEvent(folder.getPath());
        // Tagging events
        if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
            InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
            dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
            // TODO post JocInventory::postJobTaggingEvent
        }
    }
    
    private DBItemInventoryConfiguration getFolder (InventoryDBLayer dbLayer, String path, boolean forDescriptors) throws Exception {
        if(forDescriptors) {
            return JocInventory.getConfiguration(dbLayer, null, path, ConfigurationType.DESCRIPTORFOLDER, folderPermissions);
        } else {
            return JocInventory.getConfiguration(dbLayer, null, path, ConfigurationType.FOLDER, folderPermissions);
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
            
            return responseStatusJSOk(Date.from(Instant.now()));
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
            return responseStatusJSOk(Date.from(Instant.now()));
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
    
    private List<CompletableFuture<Void>>  cancelOrders(String xAccessToken, DBItemInventoryConfiguration config, String cancelOrdersDate, InventoryDBLayer dbLayer)
            throws SOSHibernateException {
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
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
                        futures.addAll(cancelOrders2(xAccessToken, configuration, cancelOrdersDate, dbLayer)));
                }
            } else if (ConfigurationType.SCHEDULE.equals(config.getTypeAsEnum())
                    || ConfigurationType.WORKINGDAYSCALENDAR.equals(config.getTypeAsEnum()) 
                    || ConfigurationType.NONWORKINGDAYSCALENDAR.equals(config.getTypeAsEnum())) {
                futures.addAll(cancelOrders2(xAccessToken, config, cancelOrdersDate, dbLayer));
            }
        }
        return futures;
    }

    private List<CompletableFuture<Void>> cancelOrders2(String xAccessToken, DBItemInventoryConfiguration config, String cancelOrdersDate, InventoryDBLayer dbLayer) {
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
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
                        cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilter, xAccessToken);
                Map<String, CompletableFuture<Either<Problem, Void>>> cancelOrderResponsePerController = Collections.emptyMap();
                        //cancelOrderImpl.cancelOrders(ordersPerController, xAccessToken, null, false, false);

                for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
                    if(!cancelOrderResponsePerController.containsKey(controllerId)) {
                        cancelOrderResponsePerController.put(controllerId, CompletableFuture.completedFuture(Either.right(null)));
                    }
                    futures.add(cancelOrderResponsePerController.get(controllerId)
                        .thenAccept(
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
                                        LOGGER.warn("Order delete failed due to: ", e);
                                    }
                                } else {
                                    getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                                    LOGGER.warn(ProblemHelper.getErrorMessage(either.getLeft()));
                                }
                            }
                        )
                    );
                }
            } catch (Exception e) {
                getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                LOGGER.warn(e.getMessage());
            }
        }
        return futures;
    }
    
    // JOC-2224
    private Set<DBItemDeploymentHistory> getDeployments(Set<RequestFilter> filters, SOSHibernateSession session) throws SOSHibernateException {
        DBLayerDeploy deployDbLayer = new DBLayerDeploy(session);
        Set<Long> inventoryDeployableIds = filters.stream().filter(filter -> JocInventory.isDeployable(filter.getObjectType()))
                .map(RequestFilter::getId).collect(Collectors.toSet());
        List<DBItemDeploymentHistory> allDeploymentsPerObject = deployDbLayer.getDeployedConfigurations(inventoryDeployableIds);
        Set<DBItemDeploymentHistory> deployments = Collections.emptySet();
        if (allDeploymentsPerObject != null) {
            deployments = allDeploymentsPerObject.stream().filter(d -> OperationType.UPDATE.value() == d.getOperation()).filter(
                    d -> DeploymentState.DEPLOYED.value() == d.getState()).collect(Collectors.groupingBy(
                            DBItemDeploymentHistory::getControllerId, Collectors.maxBy(Comparator.comparing(
                                    DBItemDeploymentHistory::getDeploymentDate)))).values().stream().filter(Optional::isPresent).map(
                                            Optional::get).collect(Collectors.toSet());
        }
        return deployments;
    }
    
    private List<DBItemInventoryReleasedConfiguration> getReleased(Set<RequestFilter> filters, SOSHibernateSession session) throws SOSHibernateException {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        Set<Long> inventoryReleaseableIds = filters.stream().filter(filter -> JocInventory.isReleasable(filter.getObjectType()))
                .map(RequestFilter::getId).collect(Collectors.toSet());
        return dbLayer.getReleasedItemByConfigurationIds(inventoryReleaseableIds);
    }
    
    private List<CompletableFuture<ControllerCommandResponse>> cancelOrders(List<DBItemInventoryReleasedConfiguration> released, 
            Set<DBItemDeploymentHistory> deployments, String xAccessToken, SOSHibernateSession session) throws SOSHibernateException,
                ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, 
                DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {
        List<CompletableFuture<ControllerCommandResponse>> futures = new ArrayList<>();

        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        
        List<String> controllerIds = Collections.emptyList();
        

        DailyPlanOrderFilterDef orderFilterDeployed = new DailyPlanOrderFilterDef();
        DailyPlanOrderFilterDef orderFilterReleased = new DailyPlanOrderFilterDef();
        orderFilterDeployed.setControllerIds(deployments.stream().map(DBItemDeploymentHistory::getControllerId).collect(Collectors.toList()));
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
        String dateFormatted = sdf.format(Date.from(Instant.now()));
        orderFilterDeployed.setDailyPlanDateFrom(dateFormatted);
        orderFilterReleased.setDailyPlanDateFrom(dateFormatted);
        Map<String, List<DBItemDailyPlanOrder>> ordersPerController = new HashMap<String, List<DBItemDailyPlanOrder>>();
        Map<String, List<DBItemDailyPlanOrder>> ordersPerControllerReleased = new HashMap<String, List<DBItemDailyPlanOrder>>();
        Map<String, List<DBItemDailyPlanOrder>> ordersPerControllerDeployed = new HashMap<String, List<DBItemDailyPlanOrder>>();
        DailyPlanCancelOrderImpl cancelOrderImpl = new DailyPlanCancelOrderImpl();
        if(!deployments.isEmpty()) {
            orderFilterDeployed.setWorkflowPaths(deployments.stream().filter(item -> item.getTypeAsEnum().equals(DeployType.WORKFLOW))
                    .map(workflow -> workflow.getName()).collect(Collectors.toList()));
            if(!orderFilterDeployed.getWorkflowPaths().isEmpty()) {
                ordersPerControllerDeployed.putAll(cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilterDeployed, xAccessToken));
            }
        }
        if(!released.isEmpty()) {
            List<String> scheduleNames = released.stream().filter(item -> ConfigurationType.SCHEDULE.equals(item.getTypeAsEnum()))
                    .map(DBItemInventoryReleasedConfiguration::getName).collect(Collectors.toList());
            orderFilterReleased.setSchedulePaths(scheduleNames);
            Set<String> calendarNames = released.stream().filter(item -> ConfigurationType.WORKINGDAYSCALENDAR.equals(item.getTypeAsEnum()) ||
                    ConfigurationType.NONWORKINGDAYSCALENDAR.equals(item.getTypeAsEnum())).map(DBItemInventoryReleasedConfiguration::getName)
                    .collect(Collectors.toSet());
            if(!calendarNames.isEmpty()) {
                for(String name : calendarNames) {
                    List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByCalendarName(name);
                    if(!schedules.isEmpty()) {
                        orderFilterReleased.getSchedulePaths().addAll(schedules.stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toList()));
                    }
                }
            }
            if(!orderFilterReleased.getSchedulePaths().isEmpty()) {
                ordersPerControllerReleased.putAll(cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilterReleased, xAccessToken));
            }
        }
        ordersPerControllerReleased.keySet().forEach(k -> ordersPerControllerDeployed.putIfAbsent(k, Collections.emptyList()));
        ordersPerControllerDeployed.forEach((k, v) -> ordersPerController.put(k, Stream.concat(v.stream(), ordersPerControllerReleased.getOrDefault(k,
                Collections.emptyList()).stream()).distinct().toList()));
        ordersPerController.values().removeIf(v -> v.isEmpty());
        
        DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
        Map<String, CompletableFuture<ControllerCommandResponse>> cancelOrderResponsePerController = cancelOrderImpl.cancelOrders(
                ordersPerController, xAccessToken);
        for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
            if (!cancelOrderResponsePerController.containsKey(controllerId)) {
                cancelOrderResponsePerController.put(controllerId, CompletableFuture.completedFuture(new ControllerCommandResponse(
                        controllerId)));
            }
            futures.add(cancelOrderResponsePerController.get(controllerId).thenApply(ccr -> {
                
                if (ccr.getException().isEmpty()) {
//                    DailyPlanOrderFilterDef localOrderFilter = new DailyPlanOrderFilterDef();
//                    localOrderFilter.setControllerIds(Collections.singletonList(controllerId));
//                    localOrderFilter.setDailyPlanDateFrom(orderFilter.getDailyPlanDateFrom());
//                    localOrderFilter.setSchedulePaths(orderFilter.getSchedulePaths());
                    
                    try {
                        // TODO create Method to transfer a set of order objects to delete instead of a filter
                        boolean successful = deleteOrdersImpl.deleteOrders(orderFilterReleased, xAccessToken, false, false);
                        if (!successful) {
                            return new ControllerCommandResponse(controllerId, Optional.of(new JocReleaseException(
                                    "Order delete failed due to missing permission.")));
                        }
                        successful = deleteOrdersImpl.deleteOrders(orderFilterDeployed, xAccessToken, false, false);
                        if (!successful) {
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
}
