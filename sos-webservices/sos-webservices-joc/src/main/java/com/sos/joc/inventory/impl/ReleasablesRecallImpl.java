package com.sos.joc.inventory.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.controller.ControllerCommandResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.PublishSemaphore;
import com.sos.joc.classes.inventory.ReleaseDeploySemaphore;
import com.sos.joc.classes.inventory.RemoveSemaphore;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.problem.ProblemEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.inventory.resource.IReleasablesRecall;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFolder;
import com.sos.joc.model.inventory.release.ReleasableRecallFilter;
import com.sos.joc.model.inventory.release.ReleasableRecallFolderFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.impl.CancelOrdersPublishHelper;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("inventory")
public class ReleasablesRecallImpl extends JOCResourceImpl implements IReleasablesRecall {

    private static final String API_CALL = "./inventory/releasables/recall";
    private static final String API_CALL_FOLDER = "./inventory/releasables/recall/folder";
    private static final String SEMAPHORE_ID = "RECALL";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReleasablesRecallImpl.class);

    @Override
    public JOCDefaultResponse postRecall(String accessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL, filter, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(filter, ReleasableRecallFilter.class, true);
            ReleasableRecallFilter recallFilter = Globals.objectMapper.readValue(filter, ReleasableRecallFilter.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response != null) {
                return response;
            }
            if (RemoveSemaphore.availablePermits(accessToken) == 1) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            if(!recallFilter.getKeepOrders()) {
                LOGGER.debug("acquire semaphore from recall with AT " + accessToken);
                RemoveSemaphore.tryAcquire(accessToken, SEMAPHORE_ID);
            }
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            Optional<JocBadRequestException> optException = recallFilter.getReleasables().stream().filter(released -> !JocInventory.isReleasable(
                    released.getObjectType())).findAny().map(r -> new JocBadRequestException(String.format(
                            "The object '%s' of type '%s' is not a releasable object.", r.getPath(), r.getObjectType().value().toLowerCase())));
            if (optException.isPresent()) {
                throw optException.get();
            }
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), recallFilter.getAuditLog());
            Long dbAuditLogId = dbAuditLog.getId();

            Set<DBItemInventoryReleasedConfiguration> releasedItems = recallFilter.getReleasables().stream()
                    .map(r -> dbLayer.getReleasedConfiguration(JocInventory.pathToName(r.getPath()), r.getObjectType()))
                    .filter(Objects::nonNull).collect(Collectors.toSet());

            recallItems(releasedItems, hibernateSession, recallFilter.getKeepOrders(), accessToken, dbLayer, dbAuditLogId);
            return responseStatusJSOk(new Date());
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
    @Override
    public JOCDefaultResponse postRecallByFolder(String accessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL_FOLDER, filter, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(filter, RequestFolder.class, true);
            ReleasableRecallFolderFilter recallFilter = Globals.objectMapper.readValue(filter, ReleasableRecallFolderFilter.class);
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response != null) {
                return response;
            }
            
            InventoryDBLayer dbInvLayer = new InventoryDBLayer(hibernateSession);
            DBLayerDeploy dbDepLayer = new DBLayerDeploy(hibernateSession);
            
            if (!folderPermissions.isPermittedForFolder(recallFilter.getPath())) {
                throw new JocFolderPermissionsException("Access denied: " + recallFilter.getPath());
            }
            
            if (RemoveSemaphore.availablePermits(accessToken) == 1) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            if(!recallFilter.getKeepOrders()) {
                LOGGER.debug("acquire semaphore from recall with AT " + accessToken);
                RemoveSemaphore.tryAcquire(accessToken, SEMAPHORE_ID);
            }
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), recallFilter.getAuditLog());
            Long dbAuditLogId = dbAuditLog.getId();
            
            Folder folder = new Folder();
            folder.setFolder(recallFilter.getPath());
            folder.setRecursive(recallFilter.getRecursive());
            Stream<ConfigurationType> objectTypes = JocInventory.getReleasableTypesStream(recallFilter.getObjectTypes());
            List<DBItemInventoryReleasedConfiguration> releasables = dbInvLayer.getReleasedConfigurationsByFolder(Collections.singleton(folder),
                    objectTypes.collect(Collectors.toSet()));

            recallItems(releasables, hibernateSession, recallFilter.getKeepOrders(), accessToken, dbDepLayer, dbAuditLogId);
            return responseStatusJSOk(new Date());
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    public void recallItems (Collection<DBItemInventoryReleasedConfiguration> releasedItems, SOSHibernateSession session, boolean keepOrders, 
            String accessToken, DBLayerDeploy dbLayer, Long dbAuditLogId) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
                DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, 
                SOSHibernateException, ExecutionException {
        List<AuditLogDetail> auditLogDetails = new ArrayList<>();
        Set<String> events = new HashSet<>();
        if(!keepOrders) {
            DailyPlanOrderFilterDef orderFilter = CancelOrdersPublishHelper.getDailyPlanOrderFilter(releasedItems, new InventoryDBLayer(session));
            List<CompletableFuture<ControllerCommandResponse>> cancelOrderResponse = 
                    CancelOrdersPublishHelper.getCancelOrderFutures(accessToken, orderFilter);

            CompletableFuture.allOf(cancelOrderResponse.toArray(CompletableFuture[]::new)).thenRun(() -> {
                Map<Boolean, List<ControllerCommandResponse>> mappedFutures = cancelOrderResponse.stream().map(CompletableFuture::join)
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
                    removeSemapohoreFinally(accessToken);
                } else {
                    SOSHibernateSession futureSession = null;
                    try {
                        futureSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                        DBLayerDeploy futureDbLayer = new DBLayerDeploy(futureSession);
                        releasedItems.stream().forEach(released -> {
                            if(futureDbLayer.recallReleasedConfiguration(released, dbAuditLogId)) {
                                auditLogDetails.add(new AuditLogDetail(released.getPath(), released.getType()));
                                events.add(released.getFolder());
                            }
                        });
                        JocAuditLog.storeAuditLogDetails(auditLogDetails, futureDbLayer.getSession(), dbAuditLogId);
                        events.stream().forEach(JocInventory::postEvent);
                    } finally {
                        Globals.disconnect(futureSession);
                        removeSemapohoreFinally(accessToken);
                    }
                }
            });
        } else {
            releasedItems.stream().forEach(released -> {
                if(dbLayer.recallReleasedConfiguration(released, dbAuditLogId)) {
                    auditLogDetails.add(new AuditLogDetail(released.getPath(), released.getType()));
                    events.add(released.getFolder());
                }
            });
            JocAuditLog.storeAuditLogDetails(auditLogDetails, dbLayer.getSession(), dbAuditLogId);
            events.stream().forEach(JocInventory::postEvent);
        }
    }

    private static void removeSemapohoreFinally(String accessToken) {
        RemoveSemaphore.release(accessToken);
        if (PublishSemaphore.getInstance().getSemaphore(accessToken).map(ReleaseDeploySemaphore::getInitialCaller).filter(str -> str.equals(
                SEMAPHORE_ID)).isPresent()) {
            PublishSemaphore.remove(accessToken);
        }
    }

}
