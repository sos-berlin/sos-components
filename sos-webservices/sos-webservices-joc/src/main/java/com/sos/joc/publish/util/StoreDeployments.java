package com.sos.joc.publish.util;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.board.BoardConverter;
import com.sos.joc.classes.controller.ControllerCommandResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonSerializer;
import com.sos.joc.classes.inventory.PublishSemaphore;
import com.sos.joc.classes.inventory.ReleaseDeploySemaphore;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.publish.record.DeployTransportRecord;
import com.sos.joc.dailyplan.impl.DailyPlanOrdersGenerateImpl;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.common.IDeployObject;
import com.sos.joc.model.dailyplan.generate.GenerateRequest;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.impl.ADeploy;
import com.sos.joc.publish.mapper.SignedItemsSpec;
import com.sos.sign.model.board.Board;
import com.sos.sign.model.fileordersource.FileOrderSource;
import com.sos.sign.model.jobclass.JobClass;
import com.sos.sign.model.jobresource.JobResource;
import com.sos.sign.model.lock.Lock;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.proxy.javaapi.JControllerProxy;

public class StoreDeployments {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreDeployments.class);

    public static final Map<Integer, Class<? extends IDeployObject>> CLASS_MAPPING = Collections.unmodifiableMap(
            new HashMap<Integer, Class<? extends IDeployObject>>() {

                private static final long serialVersionUID = 1L;
                {
                    put(DeployType.JOBCLASS.intValue(), JobClass.class);
                    put(DeployType.JOBRESOURCE.intValue(), JobResource.class);
                    put(DeployType.NOTICEBOARD.intValue(), Board.class);
                    put(DeployType.LOCK.intValue(), Lock.class);
                    put(DeployType.FILEORDERSOURCE.intValue(), FileOrderSource.class);
                    put(DeployType.WORKFLOW.intValue(), Workflow.class);
                }
            });
    public static final String API_CALL_REDEPLOY = "./inventory/deployment/redeploy";
    public static final String API_CALL_SYNC = "./inventory/deployment/synchronize";

    public static void storeNewDepHistoryEntriesForRedeploy(SignedItemsSpec signedItemsSpec, String account, String commitId, String controllerId,
            String accessToken, JocError jocError, DBLayerDeploy dbLayer) {
        storeNewDepHistoryEntries(signedItemsSpec, account, commitId, controllerId, accessToken, jocError, dbLayer, true);
    }

    public static void storeNewDepHistoryEntries(SignedItemsSpec signedItemsSpec, String account, String commitId, String controllerId,
            String accessToken, JocError jocError, DBLayerDeploy dbLayer, boolean redeploy) {
        try {
            final Date deploymentDate = Date.from(Instant.now());
            // no error occurred
            Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();

            if (signedItemsSpec.getVerifiedDeployables() != null && !signedItemsSpec.getVerifiedDeployables().isEmpty()) {
                Set<String> folders = signedItemsSpec.getVerifiedDeployables().keySet().stream().map(DBItemDeploymentHistory::getFolder).collect(
                        Collectors.toSet());
                List<Long> workflowInvIds = new ArrayList<>();
                for (Map.Entry<DBItemDeploymentHistory, DBItemDepSignatures> entry : signedItemsSpec.getVerifiedDeployables().entrySet()) {
                    DBItemDeploymentHistory item = entry.getKey();
                    if (item.getId() == null) {
                        // first id == null
                        item.setContent(JsonSerializer.serializeAsString(entry.getKey().readUpdateableContent()));
                        DBItemDepSignatures signature = entry.getValue();
                        if (signature != null && signature.getSignature() != null && !signature.getSignature().isEmpty()) {
                            item.setSignedContent(signature.getSignature());
                        } else {
                            item.setSignedContent(".");
                        }
                        item.setCommitId(commitId);
                        item.setDeploymentDate(deploymentDate);
                        item.setOperation(OperationType.UPDATE.value());
                        item.setState(DeploymentState.DEPLOYED.value());
                        item.setAuditlogId(signedItemsSpec.getAuditlogId());
                        item.setControllerId(controllerId);
                        dbLayer.getSession().save(item);
                        if (signature != null) {
                            signature.setDepHistoryId(item.getId());
                            dbLayer.getSession().save(signature);
                        }
                        deployedObjects.add(item);
                        DBItemInventoryConfiguration toUpdate = dbLayer.getSession().get(DBItemInventoryConfiguration.class, item
                                .getInventoryConfigurationId());
                        if (toUpdate != null) {
                            if (JocInventory.isWorkflow(toUpdate.getType())) {
                                workflowInvIds.add(toUpdate.getId());
                            }
                            if(!redeploy) {
                                toUpdate.setDeployed(true);
                            }
                            toUpdate.setModified(Date.from(Instant.now()));
                            dbLayer.getSession().update(toUpdate);
                        }
                    } else {
                        // second id != null
                        DBItemDeploymentHistory cloned = PublishUtils.cloneDepHistoryItemsToNewEntry(item, entry.getValue(), account, dbLayer,
                                commitId, controllerId, deploymentDate, signedItemsSpec.getAuditlogId());
                        deployedObjects.add(cloned);
                    }
                }
                folders.forEach(folder -> JocInventory.postEvent(folder));
                // post event: InventoryTaggingUpdated
                if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
                    InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(dbLayer.getSession());
                    dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
                }
            }
            if (!deployedObjects.isEmpty()) {
                long countWorkflows = deployedObjects.stream().filter(item -> ConfigurationType.WORKFLOW.intValue() == item.getType()).count();
                long countLocks = deployedObjects.stream().filter(item -> ConfigurationType.LOCK.intValue() == item.getType()).count();
                long countFileOrderSources = deployedObjects.stream().filter(item -> ConfigurationType.FILEORDERSOURCE.intValue() == item.getType())
                        .count();
                long countJobResources = deployedObjects.stream().filter(item -> ConfigurationType.JOBRESOURCE.intValue() == item.getType()).count();
                long countBoards = deployedObjects.stream().filter(item -> ConfigurationType.NOTICEBOARD.intValue() == item.getType()).count();
                LOGGER.info(String.format(
                        "Update command send to Controller \"%1$s\" containing %2$d Workflow(s), %3$d Lock(s), %4$d FileOrderSource(s), %5$d JobResource(s) and %6$d Board(s).",
                        controllerId, countWorkflows, countLocks, countFileOrderSources, countJobResources, countBoards));
                JocInventory.handleWorkflowSearch(dbLayer.getSession(), deployedObjects, false);
                JocInventory.postDeployHistoryEvent(deployedObjects);
            }
        } catch (Exception e) {
            // LOGGER.error(e.getMessage(), e);
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
        }
    }

    public static CompletableFuture<ControllerCommandResponse> processAfterAdd(Exception exception, String controllerId, DeployTransportRecord record) {
        // asynchronous processing: this method is called from a CompletableFuture and therefore
        // creates a new db session as the session of the caller may already be closed
        SOSHibernateSession newHibernateSession = null;
        String accessToken = record.impl().getAccessToken();
        JocError jocError = record.impl().getJocError();
        ControllerCommandResponse ccr = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(record.wsIdentifier());
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            if (exception == null) {
                // cleanup stored signatures
                dbLayer.cleanupSignatures(record.commitId(), controllerId);
                // cleanup stored commitIds
                dbLayer.cleanupCommitIds(record.commitId());
                // create new (daily) planned orders
                List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(record.commitId());
                if (record.dailyPlanDate() != null) {
                    DailyPlanOrdersGenerateImpl ordersGenerate = new DailyPlanOrdersGenerateImpl();
                    ordersGenerate.setCurrentAccount(record.impl());
                    InventoryDBLayer invDbLayer = new InventoryDBLayer(newHibernateSession);
                    List<String> workflowNames = optimisticEntries.stream().filter(item -> item.getTypeAsEnum().equals(DeployType.WORKFLOW)).map(
                            workflow -> workflow.getName()).collect(Collectors.toList());
                    PublishSemaphore.getInstance().getSemaphore(accessToken).map(ReleaseDeploySemaphore::getWorkflowNames)
                            .ifPresent(set -> workflowNames.removeAll(set));
                    LOGGER.debug("DEPLOY: already processed workflows from Semaphore information removed");
                    // get the schedules referencing these workflows
                    List<String> workflowsWithSubmit = new ArrayList<String>();
                    List<String> workflowsWithoutSubmit = new ArrayList<String>();
                    
                    for (String workflowName : workflowNames) {
                        List<DBItemInventoryReleasedConfiguration> scheduleDbItems = invDbLayer.getUsedReleasedSchedulesByWorkflowName(workflowName);
                        for (DBItemInventoryReleasedConfiguration scheduleDbItem : scheduleDbItems) {
                            Schedule schedule = Globals.objectMapper.readValue(scheduleDbItem.getContent(), Schedule.class);
                            // check planOrderAutomatically of the schedule first
                            if (schedule.getPlanOrderAutomatically()) {
                                if (schedule.getSubmitOrderToControllerWhenPlanned()) {
                                    workflowsWithSubmit.add(workflowName);
                                } else {
                                    workflowsWithoutSubmit.add(workflowName);
                                }
                            }
                        }
                    }
                    List<GenerateRequest> requests = new ArrayList<GenerateRequest>();
                    List<String> allowedDailyPlanDates = ordersGenerate.getAllowedDailyPlanDates(newHibernateSession, controllerId);
                    if (!workflowsWithSubmit.isEmpty()) {
                        PublishSemaphore.getInstance().getSemaphore(record.transactionId()).map(ReleaseDeploySemaphore::getWorkflowNames)
                                .ifPresent(set -> workflowsWithSubmit.removeAll(set));
                        requests.addAll(ordersGenerate.getGenerateRequests(record.dailyPlanDate(), workflowsWithSubmit, null, controllerId,
                                true, true, allowedDailyPlanDates));
                    }
                    if (!workflowsWithoutSubmit.isEmpty()) {
                        PublishSemaphore.getInstance().getSemaphore(record.transactionId()).map(ReleaseDeploySemaphore::getWorkflowNames)
                                .ifPresent(set -> workflowsWithoutSubmit.removeAll(set));
                        requests.addAll(ordersGenerate.getGenerateRequests(record.dailyPlanDate(), workflowsWithoutSubmit, null, controllerId, 
                                false, true, allowedDailyPlanDates));
                    }
                    if (!requests.isEmpty()) {
                        boolean successful = ordersGenerate.generateOrders(requests, accessToken, false, record.includeLate(), ADeploy.API_CALL);
                        if (!successful) {
                            LOGGER.warn("generate orders failed due to missing permission.");
                        }
                    }
                }
                // after successful deployment, set enforce flag to false for related dependencies 
                PublishUtils.resetDependenciesEnforcementAfterPublish(
                        optimisticEntries.stream().map(entry -> entry.getInventoryConfigurationId())
                            .collect(Collectors.toSet()),
                        newHibernateSession);
                ccr = new ControllerCommandResponse(controllerId, Optional.empty());
            } else {
                // an error occurred
                // updateRepo command is atomic, therefore all items are rejected
                // get all already optimistically stored entries for the commit
                // update all previously optimistically stored entries with the error message and change the state
                List<DBItemDeploymentHistory> optimisticEntries = updateOptimisticEntriesIfFailed(record.commitId(), exception.getMessage(), dbLayer, 
                        record.wsIdentifier());
//                ProblemHelper.postProblemEventIfExist(either.getLeft(), accessToken, jocError, null);
                ccr = new ControllerCommandResponse(controllerId, Optional.of(new JocDeployException(exception.getCause())));
            }
            return CompletableFuture.completedFuture(ccr);
        } catch (JocDeployException e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
            ccr = new ControllerCommandResponse(controllerId, Optional.of(e));
            return CompletableFuture.completedFuture(ccr);
        } catch (Exception e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
            ccr = new ControllerCommandResponse(controllerId, Optional.of(e));
            return CompletableFuture.completedFuture(ccr);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }

    public static List<DBItemDeploymentHistory> updateOptimisticEntriesIfFailed(String commitId, String message, DBLayerDeploy dbLayer,
            String wsIdentifier) throws SOSHibernateException {
        List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(commitId);
        LOGGER.trace("JSON(s) rejected from controller: ");
        optimisticEntries.stream().filter(item -> item.getType() == 1 || item.getType() == 10).forEach(item -> LOGGER.trace(item
                .getContent()));
        for (DBItemDeploymentHistory optimistic : optimisticEntries) {
            optimistic.setErrorMessage(message);
            optimistic.setState(DeploymentState.NOT_DEPLOYED.value());
            dbLayer.getSession().update(optimistic);
            // update related inventory configuration to deployed=false
            if(!API_CALL_REDEPLOY.equals(wsIdentifier) && !API_CALL_SYNC.equals(wsIdentifier)) {
                DBItemInventoryConfiguration cfg = dbLayer.getConfiguration(optimistic.getInventoryConfigurationId());
                if (cfg != null) {
                    cfg.setDeployed(false);
                    dbLayer.getSession().update(cfg);
                }
            }
        }
        JocInventory.postDeployHistoryEventWhenDeleted(optimisticEntries);
        return optimisticEntries;
    }

//    public static CompletableFuture<ControllerCommandResponse> callUpdateItemsFor(DBLayerDeploy dbLayer, SignedItemsSpec signedItemsSpec, Set<DBItemDeploymentHistory> renamedToDelete,
//            String account, String commitId, String controllerId, JOCResourceImpl impl, String wsIdentifier)
//                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException, CertificateException {
//        return callUpdateItemsFor(dbLayer, signedItemsSpec, renamedToDelete, account, commitId, controllerId, impl, wsIdentifier, null,
//                false, null);
//    }
//
//    public static CompletableFuture<ControllerCommandResponse> callUpdateItemsFor(DBLayerDeploy dbLayer, SignedItemsSpec signedItemsSpec,
//            Set<DBItemDeploymentHistory> renamedToDelete, String controllerId, DeployResponseRecord record)
//                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException, CertificateException {
//        return callUpdateItemsFor(dbLayer, signedItemsSpec, renamedToDelete, controllerId, record);
//    }

    public static CompletableFuture<ControllerCommandResponse> callUpdateItemsFor(DBLayerDeploy dbLayer, SignedItemsSpec signedItemsSpec, 
            Set<DBItemDeploymentHistory> renamedToDelete, String controllerId, DeployTransportRecord record) throws SOSException, IOException, 
            InterruptedException, ExecutionException, TimeoutException, CertificateException {
        CompletableFuture<ControllerCommandResponse> future = null;
        if (signedItemsSpec.getVerifiedDeployables() != null && !signedItemsSpec.getVerifiedDeployables().isEmpty()) {

            // store new history entries and update inventory for update operation optimistically
            DeleteDeployments.storeNewDepHistoryEntries(dbLayer, renamedToDelete, record.commitId(), null, record.account(), 
                    signedItemsSpec.getAuditlogId());
            if (!API_CALL_REDEPLOY.equals(record.wsIdentifier()) && !API_CALL_SYNC.equals(record.wsIdentifier())) {
                storeNewDepHistoryEntries(signedItemsSpec, record.account(), record.commitId(), controllerId, record.impl().getAccessToken(), 
                        record.impl().getJocError(), dbLayer, false);
            }

            boolean selfIssued = false;
            String signerDN = null;
            X509Certificate cert = null;
            JControllerProxy proxy = Proxy.of(controllerId);
            // call updateItems command via ControllerApi for the given controller
            switch (signedItemsSpec.getKeyPair().getKeyAlgorithm()) {
            case SOSKeyConstants.PGP_ALGORITHM_NAME:
                Set<JUpdateItemOperation> itemOperations1 = UpdateItemUtils.createUpdateAndDeleteItemOperations(signedItemsSpec
                        .getVerifiedDeployables(), renamedToDelete, SOSKeyConstants.PGP_ALGORITHM_NAME, null, null, proxy);
                future = getUpdateItemsFuture(proxy, signedItemsSpec, controllerId, record, itemOperations1); 
            case SOSKeyConstants.RSA_ALGORITHM_NAME:
                if (signedItemsSpec.getKeyPair().getCertificate() != null && !signedItemsSpec.getKeyPair().getCertificate().isEmpty()) {
                    cert = KeyUtil.getX509Certificate(signedItemsSpec.getKeyPair().getCertificate());
                }
                if (cert != null) {
                    selfIssued = PublishUtils.checkCertificateIsSelfIssued(cert);
                    if (!selfIssued) {
                        Set<JUpdateItemOperation> itemOperations2 = UpdateItemUtils.createUpdateAndDeleteItemOperations(signedItemsSpec
                                .getVerifiedDeployables(), renamedToDelete, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signedItemsSpec.getKeyPair()
                                        .getCertificate(), null, proxy);
                        future = getUpdateItemsFuture(proxy, signedItemsSpec, controllerId, record, itemOperations2); 
                    } else {
                        signerDN = cert.getSubjectX500Principal().getName();
                        Set<JUpdateItemOperation> itemOperations3 = UpdateItemUtils.createUpdateAndDeleteItemOperations(signedItemsSpec
                                .getVerifiedDeployables(), renamedToDelete, SOSKeyConstants.RSA_SIGNER_ALGORITHM, null, signerDN, proxy);
                        future = getUpdateItemsFuture(proxy, signedItemsSpec, controllerId, record, itemOperations3); 
                    }
                } else {
                    String message = "No certificate present! Items could not be deployed to controller.";
                    updateOptimisticEntriesIfFailed(record.commitId(), message, dbLayer, record.wsIdentifier());
                    throw new JocDeployException(message);
                }
                break;
            case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                cert = KeyUtil.getX509Certificate(signedItemsSpec.getKeyPair().getCertificate());
                if (cert != null) {
                    selfIssued = PublishUtils.checkCertificateIsSelfIssued(cert);
                    if (!selfIssued) {
                        Set<JUpdateItemOperation> itemOperations4 = UpdateItemUtils.createUpdateAndDeleteItemOperations(signedItemsSpec
                                .getVerifiedDeployables(), renamedToDelete, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signedItemsSpec.getKeyPair()
                                        .getCertificate(), null, proxy);
                        future = getUpdateItemsFuture(proxy, signedItemsSpec, controllerId, record, itemOperations4); 
                    } else {
                        signerDN = cert.getSubjectX500Principal().getName();
                        Set<JUpdateItemOperation> itemOperations5 = UpdateItemUtils.createUpdateAndDeleteItemOperations(signedItemsSpec
                                .getVerifiedDeployables(), renamedToDelete, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, null, signerDN, proxy);
                        future = getUpdateItemsFuture(proxy, signedItemsSpec, controllerId, record, itemOperations5); 
                    }
                } else {
                    String message = "No certificate present! Items could not be deployed to controller.";
                    updateOptimisticEntriesIfFailed(record.commitId(), message, dbLayer, record.wsIdentifier());
                    throw new JocDeployException(message);
                }
                break;
            }
        }
        return future;
    }

    private static CompletableFuture<ControllerCommandResponse> getUpdateItemsFuture(JControllerProxy proxy, SignedItemsSpec signedItemsSpec,
            String controllerId, DeployTransportRecord record, Set<JUpdateItemOperation> itemOperations) {
        return BoardConverter.convertFromDepItems(proxy, signedItemsSpec.getVerifiedDeployables().keySet()).thenCompose(e -> {
            if (e.isRight()) {
                return UpdateItemUtils.updateItems(proxy.api(), record.commitId(), itemOperations).thenCompose(either -> {
                    if(either.isRight()) {
                        return CompletableFuture.completedFuture(new ControllerCommandResponse(controllerId, Optional.empty(), Optional.of(record)));
                    } else {
                        return CompletableFuture.completedFuture(new ControllerCommandResponse(controllerId, Optional.of(new JocDeployException(
                                ProblemHelper.getErrorMessage(either.getLeft()))), Optional.of(record)));
                    }
                });
            } else {
                return CompletableFuture.completedFuture(new ControllerCommandResponse(controllerId, Optional.of(new JocDeployException(
                        ProblemHelper.getErrorMessage(e.getLeft()))), Optional.of(record)));
            }
        });
    }
}