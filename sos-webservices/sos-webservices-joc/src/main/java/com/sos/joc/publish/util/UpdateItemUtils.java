package com.sos.joc.publish.util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.SOSKeyConstants;
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

import io.vavr.control.Either;
import js7.base.crypt.SignedString;
import js7.base.crypt.SignerId;
import js7.base.problem.Problem;
import js7.data.board.BoardPath;
import js7.data.item.VersionId;
import js7.data.job.JobResourcePath;
import js7.data.lock.LockPath;
import js7.data.orderwatch.OrderWatchPath;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.item.JUpdateItemOperation;
import reactor.core.publisher.Flux;

public class UpdateItemUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateItemUtils.class);

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdatePGP(String commitId,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId) throws SOSException,
            IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateItemOperations(alreadyDeployed, SOSKeyConstants.PGP_ALGORITHM_NAME, null, null)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509Certificate(String commitId,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, String signatureAlgorithm, String certificate)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateItemOperations(alreadyDeployed, signatureAlgorithm, certificate, null)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }
    
    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509SignerDN(String commitId,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, String signatureAlgorithm, String signerDN)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateItemOperations(alreadyDeployed, signatureAlgorithm, null, signerDN)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }
    
    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdatePGPFromImport(String commitId,
            Map<ControllerObject, DBItemDepSignatures> alreadyDeployed, String controllerId) throws SOSException,
            IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateItemOperationsFromImport(alreadyDeployed, SOSKeyConstants.PGP_ALGORITHM_NAME, null, null)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509CertificateFromImport(String commitId,
            Map<ControllerObject, DBItemDepSignatures> drafts, String controllerId, String signatureAlgorithm, String certificate)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateItemOperationsFromImport(drafts, signatureAlgorithm, certificate, null)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509SignerDNFromImport(String commitId,
            Map<ControllerObject, DBItemDepSignatures> drafts, String controllerId, String signatureAlgorithm, String signerDN)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateItemOperationsFromImport(drafts, signatureAlgorithm, null, signerDN)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }
    
    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrDeletePGP(String commitId,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, Set<DBItemDeploymentHistory> toDelete, String controllerId) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateAndDeleteItemOperations(alreadyDeployed, toDelete, SOSKeyConstants.PGP_ALGORITHM_NAME, null, null)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }
    
    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrDeleteX509Certificate(String commitId,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, Set<DBItemDeploymentHistory> toDelete, String controllerId,
            String signatureAlgorithm, String certificate) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateAndDeleteItemOperations(alreadyDeployed, toDelete, signatureAlgorithm, certificate, null)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }
    
    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrDeleteX509SignerDN(String commitId,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, Set<DBItemDeploymentHistory> toDelete, String controllerId,
            String signatureAlgorithm, String signerDN) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateAndDeleteItemOperations(alreadyDeployed, toDelete, signatureAlgorithm, null, signerDN)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }
    
    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrDeletePGPFromImport(String commitId,
            Map<ControllerObject, DBItemDepSignatures> alreadyDeployed, Set<DBItemDeploymentHistory> toDelete, String controllerId) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateAndDeleteItemOperationsFromImport(alreadyDeployed, toDelete, SOSKeyConstants.PGP_ALGORITHM_NAME, null,
                        null)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }
    
    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrDeleteX509CertificateFromImport(String commitId,
            Map<ControllerObject, DBItemDepSignatures> alreadyDeployed, Set<DBItemDeploymentHistory> toDelete, String controllerId,
            String signatureAlgorithm, String certificate) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateAndDeleteItemOperationsFromImport(alreadyDeployed, toDelete, signatureAlgorithm, certificate, null)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }
    
    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrDeleteX509SignerDNFromImport(String commitId,
            Map<ControllerObject, DBItemDepSignatures> alreadyDeployed, Set<DBItemDeploymentHistory> toDelete, String controllerId,
            String signatureAlgorithm, String signerDN) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(createUpdateAndDeleteItemOperationsFromImport(alreadyDeployed, toDelete, signatureAlgorithm, null, signerDN)), 
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }
    public static CompletableFuture<Either<Problem, Void>> updateItemsDelete(String commitId, List<DBItemDeploymentHistory> toDelete,
            String controllerId) {
        Set<JUpdateItemOperation> updateItemOperations = new HashSet<JUpdateItemOperation>();
        if (toDelete != null) {
            updateItemOperations.addAll(toDelete.stream().map(item -> {
                switch(item.getTypeAsEnum()) {
                case WORKFLOW:
                    return JUpdateItemOperation.deleteVersioned(WorkflowPath.of(item.getName()));
                case JOBRESOURCE:
                    return JUpdateItemOperation.deleteSimple(JobResourcePath.of(item.getName()));
                case FILEORDERSOURCE:
                    return JUpdateItemOperation.deleteSimple(OrderWatchPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case LOCK:
                    return JUpdateItemOperation.deleteSimple(LockPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case NOTICEBOARD:
                    return JUpdateItemOperation.deleteSimple(BoardPath.of(Paths.get(item.getPath()).getFileName().toString()));
                default:
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.fromIterable(updateItemOperations), Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId)))));
    }
    
    public static <T extends DBItem> Set<DBItemDeploymentHistory> checkRenamingForUpdate(Set<T> verifiedObjects,
            String controllerId, DBLayerDeploy dbLayer)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        // check first if a deploymentHistory item related to the configuration item exist
        Set<DBItemDeploymentHistory> renamedOriginalHistoryEntries = new HashSet<DBItemDeploymentHistory>();
        DBItemDeploymentHistory latestDepHistory = null;
        DBItemDeploymentHistory depHistory = null;
        DBItemInventoryConfiguration invConf = null;
        for (T object : verifiedObjects) {
            if (DBItemInventoryConfiguration.class.isInstance(object)) {
                invConf = (DBItemInventoryConfiguration) object;
                latestDepHistory = dbLayer.getLatestActiveDepHistoryItem(invConf.getId(), controllerId);
                // if operation of latest history item was 'delete', no need to delete again
                // if so, check if the paths of both are the same
                if (latestDepHistory != null && (latestDepHistory.getState() == 0 && latestDepHistory.getOperation() != 1) && invConf != null 
                        && !invConf.getName().equals(latestDepHistory.getName())) {
                    // if not, delete the old deployed item via updateRepo before deploy of the new configuration
                    renamedOriginalHistoryEntries.add(latestDepHistory);
                }
            } else {
                depHistory = (DBItemDeploymentHistory) object;
                latestDepHistory = dbLayer.getLatestActiveDepHistoryItem(depHistory.getInventoryConfigurationId(), controllerId);
                // if so, check if the paths of both are the same
                if (depHistory != null && latestDepHistory != null && (latestDepHistory.getState() == 0 && latestDepHistory.getOperation() != 1) 
                        && !depHistory.getName().equals(latestDepHistory.getName())) {
                    // if not, delete the old deployed item via updateRepo before deploy of the new configuration
                    renamedOriginalHistoryEntries.add(latestDepHistory);
                }
            }
        }
        return renamedOriginalHistoryEntries;
    }

    private static Set<JUpdateItemOperation> createUpdateItemOperations(Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed,
            String signatureAlgorithm, String certificate, String signerDN)
            throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<JUpdateItemOperation>();
        if (alreadyDeployed != null) {
            updateRepoOperations.addAll(alreadyDeployed.entrySet().stream().map(item -> {
                IDeployObject content = item.getKey().readUpdateableContent();
                try {
                    switch (item.getKey().getTypeAsEnum()) {
                    case WORKFLOW: 
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
                        Board board = (Board) content;
                        if (board.getPath() == null) {
                            board.setPath(Paths.get(item.getKey().getPath()).getFileName().toString());
                        }
                        return JUpdateItemOperation.addOrChangeSimple(PublishUtils.getJBoard(board));
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
        return updateRepoOperations;
    }
    
    private static Set<JUpdateItemOperation> createUpdateItemOperationsFromImport(Map<ControllerObject, DBItemDepSignatures> alreadyDeployed,
            String signatureAlgorithm, String certificate, String signerDN)
            throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<JUpdateItemOperation>();
        if (alreadyDeployed != null) {
            updateRepoOperations.addAll(alreadyDeployed.entrySet().stream().map(item -> {
                try {
                    switch (item.getKey().getObjectType()) {
                    case WORKFLOW: 
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
                        Board board = (Board) item.getKey().getContent();
                        if (board.getPath() == null) {
                            board.setPath(Paths.get(item.getKey().getPath()).getFileName().toString());
                        }
                        return JUpdateItemOperation.addOrChangeSimple(PublishUtils.getJBoard(board));
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
        return updateRepoOperations;
    }
    
    private static Set<JUpdateItemOperation> createUpdateAndDeleteItemOperations(Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed,
            Set<DBItemDeploymentHistory> toDelete, String signatureAlgorithm, String certificate, String signerDN)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<JUpdateItemOperation>();
        if (alreadyDeployed != null) {
            updateRepoOperations.addAll(alreadyDeployed.entrySet().stream().map(item -> {
                IDeployObject content = item.getKey().readUpdateableContent();
                try {
                    switch (item.getKey().getTypeAsEnum()) {
                    case WORKFLOW: 
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
                        Board board = (Board) content;
                        if (board.getPath() == null) {
                            board.setPath(Paths.get(item.getKey().getPath()).getFileName().toString());
                        }
                        return JUpdateItemOperation.addOrChangeSimple(PublishUtils.getJBoard(board));
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
            updateRepoOperations.addAll(toDelete.stream().map(item -> {
                switch(item.getTypeAsEnum()) {
                case WORKFLOW:
                    return JUpdateItemOperation.deleteVersioned(WorkflowPath.of(item.getName()));
                case JOBRESOURCE:
                    return JUpdateItemOperation.deleteSimple(JobResourcePath.of(item.getName()));
                case FILEORDERSOURCE:
                    return JUpdateItemOperation.deleteSimple(OrderWatchPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case LOCK:
                    return JUpdateItemOperation.deleteSimple(LockPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case NOTICEBOARD:
                    return JUpdateItemOperation.deleteSimple(BoardPath.of(Paths.get(item.getPath()).getFileName().toString()));
                default:
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        return updateRepoOperations;
    }
    
    private static Set<JUpdateItemOperation> createUpdateAndDeleteItemOperationsFromImport(Map<ControllerObject, DBItemDepSignatures> alreadyDeployed,
            Set<DBItemDeploymentHistory> toDeleteForRename, String signatureAlgorithm, String certificate, String signerDN)
            throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<JUpdateItemOperation>();
        if (alreadyDeployed != null) {
            updateRepoOperations.addAll(alreadyDeployed.entrySet().stream().map(item -> {
                try {
                    switch (item.getKey().getObjectType()) {
                    case WORKFLOW: 
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
                        Board board = (Board) item.getKey().getContent();
                        if (board.getPath() == null) {
                            board.setPath(Paths.get(item.getKey().getPath()).getFileName().toString());
                        }
                        return JUpdateItemOperation.addOrChangeSimple(PublishUtils.getJBoard(board));
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
            updateRepoOperations.addAll(toDeleteForRename.stream().map(item -> {
                switch(item.getTypeAsEnum()) {
                case WORKFLOW:
                    return JUpdateItemOperation.deleteVersioned(WorkflowPath.of(item.getName()));
                case JOBRESOURCE:
                    return JUpdateItemOperation.deleteSimple(JobResourcePath.of(item.getName()));
                case FILEORDERSOURCE:
                    return JUpdateItemOperation.deleteSimple(OrderWatchPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case LOCK:
                    return JUpdateItemOperation.deleteSimple(LockPath.of(Paths.get(item.getPath()).getFileName().toString()));
                case NOTICEBOARD:
                    return JUpdateItemOperation.deleteSimple(BoardPath.of(Paths.get(item.getPath()).getFileName().toString()));
                default:
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
            
        }

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

}