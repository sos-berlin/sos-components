package com.sos.joc.inventory.impl.common;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.JocAuditObjectsLog;
import com.sos.joc.classes.controller.ControllerCommandResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.PublishSemaphore;
import com.sos.joc.classes.inventory.ReleaseDeploySemaphore;
import com.sos.joc.classes.inventory.RemoveSemaphore;
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

public abstract class ADeleteConfiguration extends JOCResourceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ADeleteConfiguration.class);
    private static final String SEMAPHORE_ID = "REMOVE";
    
    public JOCDefaultResponse remove(String accessToken, RequestFilters in, String request) throws Exception {
        SOSHibernateSession session = null;
        try {
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            RemoveSemaphore.tryAcquire(accessToken, SEMAPHORE_ID);

            session = Globals.createSosHibernateStatelessConnection(request);

            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            DBLayerDeploy deployDbLayer = new DBLayerDeploy(session);

            Predicate<RequestFilter> isFolder = r -> JocInventory.isFolder(r.getObjectType());
            
            Set<DBItemInventoryConfiguration> configs = new HashSet<>();
            Map<Long, DBItemInventoryReleasedConfiguration> released = new HashMap<>();
            Set<String> scheduleNames = new HashSet<>();
            Set<DBItemDeploymentHistory> deployments = new HashSet<>();
            List<Long> workflowInvIds = new ArrayList<>();
                    
            for (RequestFilter r : in.getObjects().stream().filter(isFolder.negate()).collect(Collectors.toSet())) {
                DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, r, folderPermissions);
                configs.add(config);
                
                if (JocInventory.isReleasable(config.getTypeAsEnum())) {
                    
                    DBItemInventoryReleasedConfiguration releasedItem = dbLayer.getReleasedItemByConfigurationId(config.getId());
                    if (releasedItem != null) {
                        released.put(config.getId(), releasedItem);
                        addScheduleNames(releasedItem, dbLayer).ifPresent(scheduleNames::addAll);
                    }
                } else if (JocInventory.isDeployable(config.getTypeAsEnum())) {
                    List<DBItemDeploymentHistory> allDeploymentsPerObject = deployDbLayer.getDeployedConfigurations(config.getId());
                    Set<DBItemDeploymentHistory> deploymentsPerObject = null;
                    if (allDeploymentsPerObject != null) {
                        deploymentsPerObject = allDeploymentsPerObject.stream().filter(d -> OperationType.UPDATE.value() == d.getOperation()).filter(
                                d -> DeploymentState.DEPLOYED.value() == d.getState()).collect(Collectors.groupingBy(
                                        DBItemDeploymentHistory::getControllerId, Collectors.maxBy(Comparator.comparing(
                                                DBItemDeploymentHistory::getDeploymentDate)))).values().stream().filter(Optional::isPresent).map(
                                                        Optional::get).collect(Collectors.toSet());
                    }
                    if (deploymentsPerObject == null || deploymentsPerObject.isEmpty()) {
                        if (JocInventory.isWorkflow(config.getType())) {
                            workflowInvIds.add(config.getId());
                        }
                    } else {
                        deployments.addAll(deploymentsPerObject);
                    }
                } else { 
                    // deployment descriptors (not releaseable and not deployable)
                    JocInventory.deleteInventoryConfigurationAndPutToTrash(config, dbLayer, ConfigurationType.DESCRIPTORFOLDER);
                }
            }
            
            JocAuditObjectsLog auditLogObjectsLogging = new JocAuditObjectsLog(dbAuditLog.getId());
            
            if (request.contains("descriptor")) {
                auditLogObjectsLogging.log();
                return responseStatusJSOk(Date.from(Instant.now()));
            }

            deleteAfterCancel(request, accessToken, session, configs, null, released, scheduleNames, deployments, workflowInvIds, dbAuditLog,
                    auditLogObjectsLogging, in.getCancelOrdersDateFrom());

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
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
            
            DBItemInventoryConfiguration folder = getFolder(dbLayer, in.getPath(), forDescriptors);
            List<Long> workflowInvIds = new ArrayList<>();
            JocAuditObjectsLog auditLogObjectsLogging = new JocAuditObjectsLog(dbAuditLog.getId());
            
            if (!forDescriptors) {
                DBLayerDeploy deployDbLayer = new DBLayerDeploy(session);
                List<DBItemInventoryConfiguration> configs = dbLayer.getFolderContent(folder.getPath(), true, null, false);
                Map<Long, DBItemInventoryReleasedConfiguration> released = new HashMap<>();
                Set<String> scheduleNames = new HashSet<>();
                Set<DBItemDeploymentHistory> deployments = new HashSet<>();
                
                for (DBItemInventoryConfiguration config : configs) {
                    if (JocInventory.isReleasable(config.getTypeAsEnum())) {
                        
                        DBItemInventoryReleasedConfiguration releasedItem = dbLayer.getReleasedItemByConfigurationId(config.getId());
                        if (releasedItem != null) {
                            released.put(config.getId(), releasedItem);
                            addScheduleNames(releasedItem, dbLayer).ifPresent(scheduleNames::addAll);
                        }
                    } else if (JocInventory.isDeployable(config.getTypeAsEnum())) {
                        List<DBItemDeploymentHistory> allDeploymentsPerObject = deployDbLayer.getDeployedConfigurations(config.getId());
                        Set<DBItemDeploymentHistory> deploymentsPerObject = null;
                        if (allDeploymentsPerObject != null) {
                            deploymentsPerObject = allDeploymentsPerObject.stream().filter(d -> OperationType.UPDATE.value() == d.getOperation()).filter(
                                    d -> DeploymentState.DEPLOYED.value() == d.getState()).collect(Collectors.groupingBy(
                                            DBItemDeploymentHistory::getControllerId, Collectors.maxBy(Comparator.comparing(
                                                    DBItemDeploymentHistory::getDeploymentDate)))).values().stream().filter(Optional::isPresent).map(
                                                            Optional::get).collect(Collectors.toSet());
                        }
                        if (deploymentsPerObject == null || deploymentsPerObject.isEmpty()) {
                            if (JocInventory.isWorkflow(config.getType())) {
                                workflowInvIds.add(config.getId());
                            }
                        } else {
                            deployments.addAll(deploymentsPerObject);
                        }
                    }
                }
                
                deleteAfterCancel(request, accessToken, session, configs, folder.getPath(), released, scheduleNames, deployments, workflowInvIds,
                        dbAuditLog, auditLogObjectsLogging, in.getCancelOrdersDateFrom());

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
    
    private void deleteAfterCancel(String request, String accessToken, SOSHibernateSession session, Collection<DBItemInventoryConfiguration> configs,
            String folder, Map<Long, DBItemInventoryReleasedConfiguration> released, Set<String> scheduleNames, Set<DBItemDeploymentHistory> deployments,
            List<Long> workflowInvIds, DBItemJocAuditLog dbAuditLog, JocAuditObjectsLog auditLogObjectsLogging, String cancelOrdersDateFrom)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, SOSHibernateException, ExecutionException {
        
        String dateFormatted = cancelOrdersDateFrom;
        
        if (cancelOrdersDateFrom == null || cancelOrdersDateFrom.isBlank() || "now".equals(cancelOrdersDateFrom.toLowerCase())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateFormatted = sdf.format(Date.from(Instant.now()));
        }

        DailyPlanOrderFilterDef orderFilterDeployed = new DailyPlanOrderFilterDef();
        orderFilterDeployed.setDailyPlanDateFrom(dateFormatted);
        orderFilterDeployed.setControllerIds(deployments.stream().map(DBItemDeploymentHistory::getControllerId).distinct().toList());
        orderFilterDeployed.setWorkflowPaths(deployments.stream().filter(item -> item.getTypeAsEnum().equals(DeployType.WORKFLOW))
                .map(DBItemDeploymentHistory::getName).collect(Collectors.toList()));
        
        DailyPlanOrderFilterDef orderFilterReleased = new DailyPlanOrderFilterDef();
        orderFilterReleased.setDailyPlanDateFrom(dateFormatted);
        orderFilterReleased.setSchedulePaths(scheduleNames.stream().toList());
        
        List<CompletableFuture<ControllerCommandResponse>> futures = cancelOrders(orderFilterReleased, orderFilterDeployed, accessToken, session);
        
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
                releaseAndReaquireSemaphore(accessToken);
            }
            if (!mappedFutures.get(false).isEmpty() || (mappedFutures.get(false).isEmpty() && mappedFutures.get(true).isEmpty())){
                SOSHibernateSession futureSession = null;
                Set<String> foldersForEvent = new HashSet<>();
                try {
                    futureSession = Globals.createSosHibernateStatelessConnection(request + " - afterOrderDeletion");
                    InventoryDBLayer futureDbLayer = new InventoryDBLayer(futureSession);
                    
                    String account = JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel()) ? ClusterSettings.getDefaultProfileAccount(Globals
                            .getConfigurationGlobalsJoc()) : getAccount();
                    
                    deployments.stream().map(DBItemDeploymentHistory::getFolder).forEach(foldersForEvent::add);
                    Set<DBItemInventoryConfiguration> deployedDeployables = DeleteDeployments.delete(deployments, new DBLayerDeploy(futureSession),
                            account, accessToken, getJocError(), dbAuditLog.getId(), true, false, cancelOrdersDateFrom);
                    
                    Set<Long> deployedInvIds = deployments.stream().map(DBItemDeploymentHistory::getInventoryConfigurationId).collect(Collectors
                            .toSet());
                    // events
                    workflowInvIds.addAll(deployedDeployables.stream().filter(i -> JocInventory.isWorkflow(i.getType())).map(
                            DBItemInventoryConfiguration::getId).collect(Collectors.toList()));

                    for (DBItemInventoryConfiguration config : configs) {
                        foldersForEvent.add(config.getFolder());
                        auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()),
                                futureDbLayer.getSession(), dbAuditLog));

                        if (JocInventory.isReleasable(config.getTypeAsEnum())) {
                            deleteObject(config, released.get(config.getId()), dbAuditLog, futureDbLayer);
                        }
                        
                        if (JocInventory.isDeployable(config.getTypeAsEnum()) && !deployedInvIds.contains(config.getId())) {
                            JocInventory.deleteInventoryConfigurationAndPutToTrash(config, futureDbLayer, ConfigurationType.FOLDER);
                        }
                    }
                    
                    if (folder != null) {
                        JocInventory.deleteEmptyFolders(futureDbLayer, folder, false);
                    }
                    auditLogObjectsLogging.log();
                    // post event: InventoryTaggingUpdated
                    foldersForEvent.forEach(fld -> {
                        JocInventory.postEvent(fld);
                        JocInventory.postTrashEvent(fld);
                    });
                    if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
                        InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(futureSession);
                        dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
                        // TODO post JocInventory::postJobTaggingEvent
                    }
                    // post events for updating workflows and fileordersources consumed by WorkflowRefs
                    JocInventory.postDeployHistoryEventWhenDeleted(deployments);
                    releaseAndReaquireSemaphore(accessToken);
                } catch (Exception e) {
                    ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, getJocError(), null);
                } finally {
                    Globals.disconnect(futureSession);
                    RemoveSemaphore.release(accessToken);
                    if (PublishSemaphore.getInstance().getSemaphore(accessToken).map(ReleaseDeploySemaphore::getInitialCaller).filter(str -> str
                            .equals(SEMAPHORE_ID)).isPresent()) {
                        PublishSemaphore.remove(accessToken);
                        LOGGER.debug("final remove semaphore from remove with AT " + accessToken);
                    }

                }
            }
        });
    }
    
    public static Optional<List<String>> addScheduleNames(DBItemInventoryReleasedConfiguration config, InventoryDBLayer dbLayer)
            throws SOSHibernateException {
        if (ConfigurationType.SCHEDULE.intValue().equals(config.getType())) {
            return Optional.of(Collections.singletonList(config.getName()));
        } else if (JocInventory.isCalendar(config.getType())) {
            List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByCalendarName(config.getName());
            if (schedules != null) {
                return Optional.of(schedules.stream().map(DBItemInventoryConfiguration::getName).toList());
            }
        }
        return Optional.empty();
    }
    
    private static void deleteObject(DBItemInventoryConfiguration conf, DBItemInventoryReleasedConfiguration released, DBItemJocAuditLog dbAuditLog,
            InventoryDBLayer dbLayer) throws SOSHibernateException {
        conf.setAuditLogId(dbAuditLog.getId());
        if (released != null) {
            dbLayer.getSession().delete(released);
        }
        JocInventory.deleteInventoryConfigurationAndPutToTrash(conf, dbLayer, ConfigurationType.FOLDER);
        DBLayerDependencies dependenciesDbLayer = new DBLayerDependencies(dbLayer.getSession());
        dependenciesDbLayer.deleteDependencies(conf);
    }
    
    private List<CompletableFuture<ControllerCommandResponse>> cancelOrders(DailyPlanOrderFilterDef orderFilterReleased, 
            DailyPlanOrderFilterDef orderFilterDeployed, String xAccessToken, SOSHibernateSession session) throws SOSHibernateException,
                ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, 
                DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {
        List<CompletableFuture<ControllerCommandResponse>> futures = new ArrayList<>();

        Map<String, List<DBItemDailyPlanOrder>> ordersPerController = new HashMap<>();
        Map<String, List<DBItemDailyPlanOrder>> ordersPerControllerReleased = new HashMap<>();
        Map<String, List<DBItemDailyPlanOrder>> ordersPerControllerDeployed = new HashMap<>();
        DailyPlanCancelOrderImpl cancelOrderImpl = new DailyPlanCancelOrderImpl();
        
        if(!orderFilterDeployed.getWorkflowPaths().isEmpty()) {
            ordersPerControllerDeployed.putAll(cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilterDeployed, xAccessToken));
        }

        if (!orderFilterReleased.getSchedulePaths().isEmpty()) {
            ordersPerControllerReleased.putAll(cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilterReleased, xAccessToken));
        }

        ordersPerControllerReleased.keySet().forEach(k -> ordersPerControllerDeployed.putIfAbsent(k, Collections.emptyList()));
        ordersPerControllerDeployed.forEach((k, v) -> ordersPerController.put(k, Stream.concat(v.stream(), ordersPerControllerReleased.getOrDefault(k,
                Collections.emptyList()).stream()).distinct().toList()));
        ordersPerController.values().removeIf(v -> v.isEmpty());
        
        Map<String, CompletableFuture<ControllerCommandResponse>> cancelOrderResponsePerController = cancelOrderImpl.cancelOrders(
                ordersPerController, xAccessToken);

        DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
        for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
            cancelOrderResponsePerController.putIfAbsent(controllerId, CompletableFuture.completedFuture(new ControllerCommandResponse(
                    controllerId)));

            futures.add(cancelOrderResponsePerController.get(controllerId).thenApply(ccr -> {
                
                if (ccr.getException().isEmpty()) {
                    DailyPlanOrderFilterDef localOrderFilterReleased = new DailyPlanOrderFilterDef();
                    localOrderFilterReleased.setControllerIds(Collections.singletonList(controllerId));
                    localOrderFilterReleased.setDailyPlanDateFrom(orderFilterReleased.getDailyPlanDateFrom());
                    localOrderFilterReleased.setSchedulePaths(orderFilterReleased.getSchedulePaths());
                    
                    DailyPlanOrderFilterDef localOrderFilterDeployed = new DailyPlanOrderFilterDef();
                    localOrderFilterDeployed.setControllerIds(Collections.singletonList(controllerId));
                    localOrderFilterDeployed.setDailyPlanDateFrom(orderFilterDeployed.getDailyPlanDateFrom());
                    localOrderFilterDeployed.setWorkflowPaths(orderFilterDeployed.getWorkflowPaths());
                    
                    boolean successful1 = true;
                    boolean successful2 = true;
                    try {
                        // TODO create Method to transfer a set of order objects to delete instead of a filter
                        if (!localOrderFilterReleased.getSchedulePaths().isEmpty()) {
                            successful1 = deleteOrdersImpl.deleteOrders(localOrderFilterReleased, xAccessToken, false, false); 
                        }
                        if (!localOrderFilterDeployed.getWorkflowPaths().isEmpty()) {
                            successful2 = deleteOrdersImpl.deleteOrders(localOrderFilterDeployed, xAccessToken, false, false);
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

    private static void releaseAndReaquireSemaphore(String accessToken) {
        try {
            RemoveSemaphore.release(accessToken);
            LOGGER.debug("release semaphore from remove with AT " + accessToken);
        } catch (Exception e) {
            // DO NOTHING if semaphore release failed
        }
        if (RemoveSemaphore.availablePermits(accessToken) == 1) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        try {
            RemoveSemaphore.tryAcquire(accessToken, SEMAPHORE_ID);
        } catch (InterruptedException e) {
            throw new JocException(e);
        }
        LOGGER.debug("acquire again semaphore from remove with AT " + accessToken);
    }

}