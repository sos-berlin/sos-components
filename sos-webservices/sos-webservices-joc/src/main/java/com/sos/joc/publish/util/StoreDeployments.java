package com.sos.joc.publish.util;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonSerializer;
import com.sos.joc.dailyplan.impl.DailyPlanOrdersGenerateImpl;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.common.IDeployObject;
import com.sos.joc.model.dailyplan.generate.GenerateRequest;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.SignedItemsSpec;
import com.sos.sign.model.board.Board;
import com.sos.sign.model.fileordersource.FileOrderSource;
import com.sos.sign.model.jobclass.JobClass;
import com.sos.sign.model.jobresource.JobResource;
import com.sos.sign.model.lock.Lock;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import js7.base.problem.Problem;

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

    public static void storeNewDepHistoryEntriesForRedeploy(SignedItemsSpec signedItemsSpec,
            String account, String commitId, String controllerId, String accessToken, JocError jocError, DBLayerDeploy dbLayer) {
        storeNewDepHistoryEntries(signedItemsSpec, account, commitId, controllerId, accessToken, jocError, dbLayer);
    }
    
    public static void storeNewDepHistoryEntries(SignedItemsSpec signedItemsSpec,
            String account, String commitId, String controllerId, String accessToken, JocError jocError, DBLayerDeploy dbLayer) {
        try {
            final Date deploymentDate = Date.from(Instant.now());
            // no error occurred
            Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();

            if (signedItemsSpec.getVerifiedDeployables() != null && !signedItemsSpec.getVerifiedDeployables().isEmpty()) {
                Set<String> folders = signedItemsSpec.getVerifiedDeployables().keySet().stream().map(DBItemDeploymentHistory::getFolder).collect(Collectors.toSet());
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
                		dbLayer.getSession().save(item);
                        PublishUtils.postDeployHistoryEvent(item);
                        if (signature != null) {
                            signature.setDepHistoryId(item.getId());
                            dbLayer.getSession().save(signature);
                        }
                        deployedObjects.add(item);
                        DBItemInventoryConfiguration toUpdate = dbLayer.getSession().get(DBItemInventoryConfiguration.class, item.getInventoryConfigurationId());
                        toUpdate.setDeployed(true);
                        toUpdate.setModified(Date.from(Instant.now()));
                        dbLayer.getSession().update(toUpdate);
                	} else {
                    	// second id != null 
                    	DBItemDeploymentHistory cloned = PublishUtils.cloneDepHistoryItemsToNewEntry(item, entry.getValue(), account, dbLayer, commitId,
                    			controllerId, deploymentDate, signedItemsSpec.getAuditlogId());
                        deployedObjects.add(cloned);
                	}
                }
                folders.forEach(folder -> JocInventory.postEvent(folder));
            }
            if (!deployedObjects.isEmpty()) {
                long countWorkflows = deployedObjects.stream().filter(item -> ConfigurationType.WORKFLOW.intValue() == item.getType()).count();
                long countLocks = deployedObjects.stream().filter(item -> ConfigurationType.LOCK.intValue() == item.getType()).count();
                long countFileOrderSources = deployedObjects.stream().filter(item -> ConfigurationType.FILEORDERSOURCE.intValue() == item.getType()).count();
                long countJobResources = deployedObjects.stream().filter(item -> ConfigurationType.JOBRESOURCE.intValue() == item.getType()).count();
                long countBoards = deployedObjects.stream().filter(item -> ConfigurationType.NOTICEBOARD.intValue() == item.getType()).count();
                LOGGER.info(String.format(
                        "Update command send to Controller \"%1$s\" containing %2$d Workflow(s), %3$d Lock(s), %4$d FileOrderSource(s), %5$d JobResource(s) and %6$d Board(s).", 
                        controllerId, countWorkflows, countLocks, countFileOrderSources, countJobResources, countBoards));
                 JocInventory.handleWorkflowSearch(dbLayer.getSession(), deployedObjects, false);
            }
        } catch (Exception e) {
            //LOGGER.error(e.getMessage(), e);
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
        } 
    }
    
    public static void processAfterAdd(Either<Problem, Void> either, String account, String commitId, String controllerId, String accessToken,
            JocError jocError, String wsIdentifier, String dailyPlanDate) {
        // asynchronous processing:  this method is called from a CompletableFuture and therefore 
        // creates a new db session as the session of the caller may already be closed
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(wsIdentifier);
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            if(either.isRight()) {
                // cleanup stored signatures
                dbLayer.cleanupSignatures(commitId, controllerId);
                // cleanup stored commitIds
                dbLayer.cleanupCommitIds(commitId);
                // TODO: create new (daily) planned orders
                if(dailyPlanDate != null) {
                    DailyPlanOrdersGenerateImpl ordersGenerate = new DailyPlanOrdersGenerateImpl();
                    List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(commitId);
                    List<String> schedulePathsWithSubmit = new ArrayList<String>();
                    List<String> schedulePathsWithoutSubmit = new ArrayList<String>();
                    InventoryDBLayer invDbLayer = new InventoryDBLayer(newHibernateSession);
                    List<String> workflowNames = optimisticEntries.stream().filter(item -> item.getTypeAsEnum().equals(DeployType.WORKFLOW))
                    .map(workflow -> workflow.getName()).collect(Collectors.toList());
                    // get the schedules referencing these workflows 
                    for (String workflowName : workflowNames) {
                        List<DBItemInventoryReleasedConfiguration> scheduleDbItems = invDbLayer.getUsedReleasedSchedulesByWorkflowName(workflowName);
                        for (DBItemInventoryReleasedConfiguration scheduleDbItem : scheduleDbItems) {
                            Schedule schedule = Globals.objectMapper.readValue(scheduleDbItem.getContent(), Schedule.class);
                            // check planOrderAutomatically of the schedule first
                            if (schedule.getPlanOrderAutomatically()) {
                                if(schedule.getSubmitOrderToControllerWhenPlanned()) {
                                    schedulePathsWithSubmit.add(schedule.getPath());
                                } else {
                                    schedulePathsWithoutSubmit.add(schedule.getPath());
                                }
                            }
                        }
                    }
                    List<GenerateRequest> requests = new ArrayList<GenerateRequest>();
                    if(!schedulePathsWithSubmit.isEmpty()) {
                        requests.addAll(ordersGenerate.getGenerateRequests(dailyPlanDate, null, schedulePathsWithSubmit, controllerId, true));
                    }
                    if(!schedulePathsWithoutSubmit.isEmpty()) {
                        requests.addAll(ordersGenerate.getGenerateRequests(dailyPlanDate, null, schedulePathsWithoutSubmit, controllerId, false));
                    }
                    if(!requests.isEmpty()) {
                        boolean successful = ordersGenerate.generateOrders(requests, accessToken, false);
                        if (!successful) {
                            LOGGER.warn("generate orders failed due to missing permission.");
                        }
                    }
//                    if(!schedulePaths.isEmpty()) {
//                        List<GenerateRequest> requests =  ordersGenerate.getGenerateRequests(dailyPlanDate, null, schedulePaths, controllerId);
//                        boolean successful = ordersGenerate.generateOrders(requests, accessToken, false);
//                        if (!successful) {
//                            LOGGER.warn("generate orders failed due to missing permission.");
//                        }
//                    }
                }
            } else  if (either.isLeft()) {
                // an error occurred
//                String message = String.format(
//                        "Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
//                LOGGER.error(message);
                // updateRepo command is atomic, therefore all items are rejected
                
                // get all already optimistically stored entries for the commit
                List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(commitId);
                // update all previously optimistically stored entries with the error message and change the state
                LOGGER.trace("JSON(s) rejected from controller: ");
                
                optimisticEntries.stream().filter(item -> item.getType() == 1 || item.getType() == 10)
                    .forEach(item -> LOGGER.trace(item.getContent()));
                updateOptimisticEntriesIfFailed(optimisticEntries, either.getLeft().message(), dbLayer);
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(optimisticEntries);
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, null);
            }
        } catch (Exception e) {
            //LOGGER.error(e.getMessage(), e);
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
    public static void updateOptimisticEntriesIfFailed (Collection<DBItemDeploymentHistory> optimisticEntries, String message,
            DBLayerDeploy dbLayer) throws SOSHibernateException {
        for(DBItemDeploymentHistory optimistic : optimisticEntries) {
            optimistic.setErrorMessage(message);
            optimistic.setState(DeploymentState.NOT_DEPLOYED.value());
            dbLayer.getSession().update(optimistic);
            PublishUtils.postDeployHistoryEventWhenDeleted(optimistic);
            // update related inventory configuration to deployed=false 
            DBItemInventoryConfiguration cfg = dbLayer.getConfiguration(optimistic.getInventoryConfigurationId());
            if (cfg != null) {
                cfg.setDeployed(false);
                dbLayer.getSession().update(cfg);
            }
        }
    }

    public static void callUpdateItemsFor(DBLayerDeploy dbLayer, SignedItemsSpec signedItemsSpec, Set<DBItemDeploymentHistory> renamedToDelete,
            String account, String commitId, String controllerId, String accessToken, JocError jocError, String wsIdentifier) throws SOSException,
            IOException, InterruptedException, ExecutionException, TimeoutException, CertificateException {
        callUpdateItemsFor(dbLayer, signedItemsSpec, renamedToDelete, account, commitId, controllerId, accessToken, jocError, wsIdentifier, null);
    }
    public static void callUpdateItemsFor(DBLayerDeploy dbLayer, SignedItemsSpec signedItemsSpec, Set<DBItemDeploymentHistory> renamedToDelete,
            String account, String commitId, String controllerId, String accessToken, JocError jocError, String wsIdentifier, String dailyPlanDate) throws SOSException,
            IOException, InterruptedException, ExecutionException, TimeoutException, CertificateException {

        if (signedItemsSpec.getVerifiedDeployables() != null && !signedItemsSpec.getVerifiedDeployables().isEmpty()) {

            // store new history entries and update inventory for update operation optimistically
            storeNewDepHistoryEntries(signedItemsSpec, account, commitId, controllerId, accessToken, jocError, dbLayer);
            DeleteDeployments.storeNewDepHistoryEntries(dbLayer, new ArrayList<>(renamedToDelete), commitId);
            
            List<DBItemInventoryCertificate> caCertificates = dbLayer.getCaCertificates();
            boolean verified = false;
            String signerDN = null;
            X509Certificate cert = null;
            // call updateItems command via ControllerApi for the given controller

            switch(signedItemsSpec.getKeyPair().getKeyAlgorithm()) {
            case SOSKeyConstants.PGP_ALGORITHM_NAME:
                UpdateItemUtils.updateItemsAddOrDeletePGP(commitId, signedItemsSpec.getVerifiedDeployables(), renamedToDelete, controllerId)
                    .thenAccept(either -> processAfterAdd(either, account, commitId, controllerId, accessToken, jocError, wsIdentifier, dailyPlanDate));
                break;
            case SOSKeyConstants.RSA_ALGORITHM_NAME:
                if (signedItemsSpec.getKeyPair().getCertificate() != null && !signedItemsSpec.getKeyPair().getCertificate().isEmpty()) {
                    cert = KeyUtil.getX509Certificate(signedItemsSpec.getKeyPair().getCertificate());
                }
                if (cert != null) {
                    verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                    if (verified) {
                        UpdateItemUtils.updateItemsAddOrDeleteX509Certificate(commitId, signedItemsSpec.getVerifiedDeployables(), renamedToDelete,
                                controllerId, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signedItemsSpec.getKeyPair().getCertificate())
                            .thenAccept(either -> 
                                    processAfterAdd(either, account, commitId, controllerId, accessToken, jocError, wsIdentifier, dailyPlanDate));
                    } else {
                      signerDN = cert.getSubjectDN().getName();
                      UpdateItemUtils.updateItemsAddOrDeleteX509SignerDN(commitId, signedItemsSpec.getVerifiedDeployables(),
                              renamedToDelete, controllerId, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> 
                              processAfterAdd(either, account, commitId, controllerId, accessToken, jocError, wsIdentifier, dailyPlanDate));
                    }
                } else {
                    String message = "No certificate present! Items could not be deployed to controller.";
                    updateOptimisticEntriesIfFailed(signedItemsSpec.getVerifiedDeployables().keySet(), message, dbLayer);
                    throw new JocDeployException(message);
                }
                break;
            case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                cert = KeyUtil.getX509Certificate(signedItemsSpec.getKeyPair().getCertificate());
                if (cert != null) {
                    verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                    if (verified) {
                        UpdateItemUtils.updateItemsAddOrDeleteX509Certificate(commitId, signedItemsSpec.getVerifiedDeployables(), renamedToDelete,
                                controllerId, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signedItemsSpec.getKeyPair().getCertificate())
                            .thenAccept(either -> 
                                    processAfterAdd(either, account, commitId, controllerId, accessToken, jocError, wsIdentifier, dailyPlanDate));
                    } else {
                      signerDN = cert.getSubjectDN().getName();
                      UpdateItemUtils.updateItemsAddOrDeleteX509SignerDN(commitId, signedItemsSpec.getVerifiedDeployables(), renamedToDelete,
                              controllerId, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> 
                                  processAfterAdd(either, account, commitId, controllerId, accessToken, jocError, wsIdentifier, dailyPlanDate));
                    }
                } else {
                    String message = "No certificate present! Items could not be deployed to controller.";
                    updateOptimisticEntriesIfFailed(signedItemsSpec.getVerifiedDeployables().keySet(), message, dbLayer);
                    throw new JocDeployException(message);
                }
                break;
            }
        }
    }

}