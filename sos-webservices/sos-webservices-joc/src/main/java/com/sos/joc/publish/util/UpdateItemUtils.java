package com.sos.joc.publish.util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.joc.classes.board.BoardConverter;
import com.sos.joc.classes.calendar.ControllerSettings;
import com.sos.joc.classes.inventory.JsonSerializer;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.model.common.IDeployObject;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.sign.model.board.Board;
import com.sos.sign.model.fileordersource.FileOrderSource;
import com.sos.sign.model.lock.Lock;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import js7.base.crypt.SignedString;
import js7.base.crypt.SignerId;
import js7.base.problem.Problem;
import js7.data.board.BoardPath;
import js7.data.calendar.CalendarPath;
import js7.data.item.VersionId;
import js7.data.job.JobResourcePath;
import js7.data.lock.LockPath;
import js7.data.orderwatch.OrderWatchPath;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.JWorkflowId;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

public class UpdateItemUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateItemUtils.class);

    public static CompletableFuture<Either<Problem, Void>> updateItems(JControllerApi controllerApi, String commitId,
            Collection<JUpdateItemOperation> itemOperations) {
        return controllerApi.updateItems(Flux.concat(Flux.fromIterable(itemOperations), Flux.just(JUpdateItemOperation.addVersion(VersionId.of(
                commitId)))));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItems(String controllerId, String commitId,
            Collection<JUpdateItemOperation> itemOperations) {
        return updateItems(ControllerApi.of(controllerId), commitId, itemOperations);
    }
    
    public static CompletableFuture<Either<Problem, Void>> updateItemsDelete(String commitId, List<DBItemDeploymentHistory> toDelete,
            JControllerProxy proxy) {
        Set<JUpdateItemOperation> updateItemOperations = new HashSet<>();
        if (toDelete != null) {
            Set<JUpdateItemOperation> calendars = new HashSet<>();
            Set<CalendarPath> controllerCalendars = proxy.currentState().pathToCalendar().keySet();
            updateItemOperations.addAll(toDelete.stream().filter(Objects::nonNull).map(item -> {
                switch(item.getTypeAsEnum()) {
                case WORKFLOW:
                    CalendarPath cal = CalendarPath.of(ControllerSettings.calendarNamePrefix + item.getName()); 
                    if (controllerCalendars.contains(cal)) {
                        calendars.add(JUpdateItemOperation.deleteSimple(cal));
                    }
                    return JUpdateItemOperation.deleteVersioned(WorkflowPath.of(item.getName()));
                case JOBRESOURCE:
                    return JUpdateItemOperation.deleteSimple(JobResourcePath.of(item.getName()));
                case FILEORDERSOURCE:
                    return JUpdateItemOperation.deleteSimple(OrderWatchPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case LOCK:
                    return JUpdateItemOperation.deleteSimple(LockPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case NOTICEBOARD:
                case PLANNABLEBOARD:
                    return JUpdateItemOperation.deleteSimple(BoardPath.of(Paths.get(item.getPath()).getFileName().toString()));
                default:
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
            updateItemOperations.addAll(calendars);
        }
        return proxy.api().updateItems(Flux.concat(Flux.fromIterable(updateItemOperations), Flux.just(JUpdateItemOperation.addVersion(VersionId.of(
                commitId)))));
    }
    
    public static <T extends DBItem> Set<DBItemDeploymentHistory> checkRenamingForUpdate(Set<T> verifiedObjects,
            String controllerId, DBLayerDeploy dbLayer)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        // check first if a deploymentHistory item related to the configuration item exist
        Set<DBItemDeploymentHistory> renamedOriginalHistoryEntries = new HashSet<DBItemDeploymentHistory>();
        DBItemDeploymentHistory latestDepHistory = null;
        DBItemDeploymentHistory depConf = null;
        DBItemInventoryConfiguration invConf = null;
        for (T object : verifiedObjects) {
            if (object == null) {
                continue;
            }
            if (DBItemInventoryConfiguration.class.isInstance(object)) {
                invConf = (DBItemInventoryConfiguration) object;
                latestDepHistory = dbLayer.getLatestActiveDepHistoryItem(invConf.getId(), controllerId);
                // check if the paths of both are the same
                if (latestDepHistory != null && !invConf.getName().equals(latestDepHistory.getName())) {
                    // if not, delete the old deployed item via updateRepo before deploy of the new configuration
                    renamedOriginalHistoryEntries.add(latestDepHistory);
                }
            } else {
                depConf = (DBItemDeploymentHistory) object;
                latestDepHistory = dbLayer.getLatestActiveDepHistoryItem(depConf.getInventoryConfigurationId(), controllerId);
                // check if the paths of both are the same
                if (latestDepHistory != null && !depConf.getName().equals(latestDepHistory.getName())) {
                    // if not, delete the old deployed item via updateRepo before deploy of the new configuration
                    renamedOriginalHistoryEntries.add(latestDepHistory);
                }
            }
        }
        return renamedOriginalHistoryEntries;
    }

    public static Set<JUpdateItemOperation> createUpdateAndDeleteItemOperations(Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed,
            Set<DBItemDeploymentHistory> toDelete, String signatureAlgorithm, String certificate, String signerDN, JControllerProxy proxy) {
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<>();
        Set<JUpdateItemOperation> jCalendars = new HashSet<>();
        if (alreadyDeployed != null) {
            updateRepoOperations.addAll(alreadyDeployed.entrySet().stream().map(item -> {
                IDeployObject content = item.getKey().readUpdateableContent();
                try {
                    switch (item.getKey().getTypeAsEnum()) {
                    case WORKFLOW:
                        Workflow workflow = (Workflow) content;
                        if (workflow.getCalendarPath() != null && workflow.getCalendarPath().startsWith(ControllerSettings.calendarNamePrefix)) {
                            jCalendars.add(JUpdateItemOperation.addOrChangeSimple(ControllerSettings.getCalendar(workflow.getCalendarPath(), workflow
                                    .getDayOffset())));
                        }
                        // no break!!
                    case JOBRESOURCE:
                        try {
                            String json = JsonSerializer.serializeAsString(content);
                            if(signatureAlgorithm.equals(SOSKeyConstants.PGP_ALGORITHM_NAME)) {
                                return JUpdateItemOperation.addOrChangeSigned(getSignedStringPGP(json, item.getValue().getSignature()));
                            } else if (certificate != null) {
                                return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithCertificate(json, item.getValue().getSignature(),
                                        signatureAlgorithm, certificate));
                            } else { // signerDN != null
                                return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithSignerDN(json, item.getValue().getSignature(),
                                        signatureAlgorithm, signerDN));
                            }
                        } catch (JsonProcessingException e) {
                            return null;
                        }
                    case LOCK:
                        Lock lock = (Lock) content;
                        lock.setPath(Paths.get(item.getKey().getPath()).getFileName().toString());
                        return JUpdateItemOperation.addOrChangeSimple(PublishUtils.getJLock(lock));
                    case FILEORDERSOURCE:
                        FileOrderSource fileOrderSource = (FileOrderSource) content;
                        if (fileOrderSource.getPath() == null) {
                            fileOrderSource.setPath(Paths.get(item.getKey().getPath()).getFileName().toString());
                        }
                        return JUpdateItemOperation.addOrChangeSimple(PublishUtils.getJFileWatch(fileOrderSource));
                    case NOTICEBOARD:
                    case PLANNABLEBOARD:
                        Board board = (Board) content;
                        if (board.getPath() == null) {
                            board.setPath(Paths.get(item.getKey().getPath()).getFileName().toString());
                        }
                        return JUpdateItemOperation.addOrChangeSimple(BoardConverter.getJBoard(board));
                    default:
                        return null;
                    }
                } catch (JocDeployException e) {
                    throw e;
                } catch (Exception e) {
                    throw new JocDeployException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        if(toDelete != null && !toDelete.isEmpty()) {
            Set<CalendarPath> controllerCalendars = proxy.currentState().pathToCalendar().keySet();
            updateRepoOperations.addAll(toDelete.stream().map(item -> {
                switch(item.getTypeAsEnum()) {
                case WORKFLOW:
                    CalendarPath controllerCalendar = CalendarPath.of(ControllerSettings.calendarNamePrefix + item.getName());
                    if (controllerCalendars.contains(controllerCalendar)) {
                        jCalendars.add(JUpdateItemOperation.deleteSimple(controllerCalendar));
                    }
                    return JUpdateItemOperation.deleteVersioned(WorkflowPath.of(item.getName()));
                case JOBRESOURCE:
                    return JUpdateItemOperation.deleteSimple(JobResourcePath.of(item.getName()));
                case FILEORDERSOURCE:
                    return JUpdateItemOperation.deleteSimple(OrderWatchPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case LOCK:
                    return JUpdateItemOperation.deleteSimple(LockPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case NOTICEBOARD:
                case PLANNABLEBOARD:
                    return JUpdateItemOperation.deleteSimple(BoardPath.of(Paths.get(item.getPath()).getFileName().toString()));
                default:
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        updateRepoOperations.addAll(jCalendars);
        return updateRepoOperations;
    }
    
    public static Set<JUpdateItemOperation> createUpdateAndDeleteItemOperationsFromImport(Map<ControllerObject, DBItemDepSignatures> alreadyDeployed,
            Set<DBItemDeploymentHistory> toDeleteForRename, String signatureAlgorithm, String certificate, String signerDN, JControllerProxy proxy)
            throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<>();
        Set<JUpdateItemOperation> jCalendars = new HashSet<>();
        if (alreadyDeployed != null) {
            updateRepoOperations.addAll(alreadyDeployed.entrySet().stream().map(item -> {
                try {
                    switch (item.getKey().getObjectType()) {
                    case WORKFLOW:
                        Workflow workflow = (Workflow) item.getKey().getContent();
                        if (workflow.getCalendarPath() != null && workflow.getCalendarPath().startsWith(ControllerSettings.calendarNamePrefix)) {
                            jCalendars.add(JUpdateItemOperation.addOrChangeSimple(ControllerSettings.getCalendar(workflow.getCalendarPath(), workflow
                                    .getDayOffset())));
                        }
                    case JOBRESOURCE:
                        String json = item.getKey().getSignedContent();
                        if(signatureAlgorithm.equals(SOSKeyConstants.PGP_ALGORITHM_NAME)) {
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringPGP(json, item.getValue().getSignature()));
                        } else if (certificate != null) {
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithCertificate(json, item.getValue().getSignature(), 
                                    signatureAlgorithm, certificate));
                        } else { // signerDN != null
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithSignerDN(json, item.getValue().getSignature(), 
                                    signatureAlgorithm, signerDN));
                        }
                    case LOCK:
                        Lock lock = (Lock) item.getKey().getContent();
                        lock.setPath(Paths.get(item.getKey().getPath()).getFileName().toString());
                        return JUpdateItemOperation.addOrChangeSimple(PublishUtils.getJLock(lock));
                    case FILEORDERSOURCE:
                        FileOrderSource fileOrderSource = (FileOrderSource) item.getKey().getContent();
                        if (fileOrderSource.getPath() == null) {
                            fileOrderSource.setPath(Paths.get(item.getKey().getPath()).getFileName().toString());
                        }
                        return JUpdateItemOperation.addOrChangeSimple(PublishUtils.getJFileWatch(fileOrderSource));
                    case NOTICEBOARD:
                    case PLANNABLEBOARD:
                        Board board = (Board) item.getKey().getContent();
                        if (board.getPath() == null) {
                            board.setPath(Paths.get(item.getKey().getPath()).getFileName().toString());
                        }
                        return JUpdateItemOperation.addOrChangeSimple(BoardConverter.getJBoard(board));
                    default:
                        return null;
                    }
                } catch (JocDeployException e) {
                    throw e;
                } catch (Exception e) {
                    throw new JocDeployException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        if(toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
            Set<CalendarPath> controllerCalendars = proxy.currentState().pathToCalendar().keySet();
            updateRepoOperations.addAll(toDeleteForRename.stream().map(item -> {
                switch(item.getTypeAsEnum()) {
                case WORKFLOW:
                    CalendarPath controllerCalendar = CalendarPath.of(ControllerSettings.calendarNamePrefix + item.getName());
                    if (controllerCalendars.contains(controllerCalendar)) {
                        jCalendars.add(JUpdateItemOperation.deleteSimple(controllerCalendar));
                    }
                    return JUpdateItemOperation.deleteVersioned(WorkflowPath.of(item.getName()));
                case JOBRESOURCE:
                    return JUpdateItemOperation.deleteSimple(JobResourcePath.of(item.getName()));
                case FILEORDERSOURCE:
                    return JUpdateItemOperation.deleteSimple(OrderWatchPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case LOCK:
                    return JUpdateItemOperation.deleteSimple(LockPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case NOTICEBOARD:
                case PLANNABLEBOARD:
                    return JUpdateItemOperation.deleteSimple(BoardPath.of(Paths.get(item.getPath()).getFileName().toString()));
                default:
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
            
        }
        updateRepoOperations.addAll(jCalendars);
        return updateRepoOperations;
    }
    
    private static SignedString getSignedStringPGP(String jsonContent, String signature) {
        LOGGER.trace("JSON send to controller: ");
        LOGGER.trace(jsonContent);
        return SignedString.of(jsonContent, SOSKeyConstants.PGP_ALGORITHM_NAME, signature);
    }

    private static SignedString getSignedStringWithCertificate(String jsonContent, String signature, String signatureAlgorithm, String certificate) {
        LOGGER.trace("JSON send to controller: ");
        LOGGER.trace(jsonContent);
        return SignedString.x509WithCertificate(jsonContent, signature, signatureAlgorithm, certificate);
    }

    private static SignedString getSignedStringWithSignerDN(String jsonContent, String signature, String signatureAlgorithm, String signerDN) {
        LOGGER.trace("JSON send to controller: ");
        LOGGER.trace(jsonContent);
        return SignedString.x509WithSignerId(jsonContent, signature, signatureAlgorithm, SignerId.of(signerDN));
    }
    
    public static Stream<String> getWorkflowNamesWithOrders(JControllerState currentState) {
        return currentState.idToOrder().values().stream().map(JOrder::workflowId).map(JWorkflowId::path).map(WorkflowPath::string).distinct();
    }
}