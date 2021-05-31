package com.sos.joc.publish.util;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonSerializer;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.SignedItemsSpec;
import com.sos.sign.model.fileordersource.FileOrderSource;
import com.sos.sign.model.jobclass.JobClass;
import com.sos.sign.model.jobresource.JobResource;
import com.sos.sign.model.junction.Junction;
import com.sos.sign.model.lock.Lock;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import js7.base.problem.Problem;

public class StoreDeployments {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreDeployments.class);
    
    public static final Map<Integer, Class<?>> CLASS_MAPPING = Collections.unmodifiableMap(new HashMap<Integer, Class<?>>() {

        private static final long serialVersionUID = 1L;

        {
            put(DeployType.JOBCLASS.intValue(), JobClass.class);
            put(DeployType.JOBRESOURCE.intValue(), JobResource.class);
            put(DeployType.JUNCTION.intValue(), Junction.class);
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
                for (Map.Entry<DBItemDeploymentHistory, DBItemDepSignatures> entry : signedItemsSpec.getVerifiedDeployables().entrySet()) {
                	DBItemDeploymentHistory item = entry.getKey();
            		if (item.getId() == null) {
                    	// first id == null 
                		item.setContent(JsonSerializer.serializeAsString(entry.getKey().readUpdateableContent()));
                		item.setSignedContent(entry.getValue().getSignature());
                		item.setDeploymentDate(deploymentDate);
                		item.setOperation(OperationType.UPDATE.value());
                		item.setState(DeploymentState.DEPLOYED.value());
                		item.setAuditlogId(signedItemsSpec.getAuditlogId());
                		dbLayer.getSession().save(item);
                        PublishUtils.postDeployHistoryWorkflowEvent(item);
                        DBItemDepSignatures signature = entry.getValue();
                        if (signature != null) {
                            signature.setDepHistoryId(item.getId());
                            dbLayer.getSession().update(signature);
                        }
                        deployedObjects.add(item);
                        DBItemInventoryConfiguration toUpdate = dbLayer.getSession().get(DBItemInventoryConfiguration.class, item.getInventoryConfigurationId());
                        toUpdate.setDeployed(true);
                        toUpdate.setModified(Date.from(Instant.now()));
                        dbLayer.getSession().update(toUpdate);
                        JocInventory.postEvent(item.getFolder());
                	} else {
                    	// second id != null 
                    	DBItemDeploymentHistory cloned = PublishUtils.cloneDepHistoryItemsToNewEntry(item, entry.getValue(), account, dbLayer, commitId,
                    			controllerId, deploymentDate, signedItemsSpec.getAuditlogId());
                        deployedObjects.add(cloned);
                	}
                }
            }
            if (!deployedObjects.isEmpty()) {
                long countWorkflows = deployedObjects.stream().filter(item -> ConfigurationType.WORKFLOW.intValue() == item.getType()).count();
                long countLocks = deployedObjects.stream().filter(item -> ConfigurationType.LOCK.intValue() == item.getType()).count();
                long countFileOrderSources = deployedObjects.stream().filter(item -> ConfigurationType.FILEORDERSOURCE.intValue() == item.getType()).count();
                long countJobResources = deployedObjects.stream().filter(item -> ConfigurationType.JOBRESOURCE.intValue() == item.getType()).count();
                LOGGER.info(String.format(
                        "Update command send to Controller \"%1$s\" containing %2$d Workflow(s), %3$d Lock(s), %4$d FileOrderSource(s) and %5$d JobResource(s).", 
                        controllerId, countWorkflows, countLocks, countFileOrderSources, countJobResources));
                 JocInventory.handleWorkflowSearch(dbLayer.getSession(), deployedObjects, false);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
        } 
    }
    
    public static void processAfterAdd(Either<Problem, Void> either, String account, String commitId, String controllerId, String accessToken, 
            JocError jocError, String wsIdentifier) {
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
            } else  if (either.isLeft()) {
                // an error occurred
                String message = String.format(
                        "Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.error(message);
                // updateRepo command is atomic, therefore all items are rejected
                
                // get all already optimistically stored entries for the commit
                List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(commitId);
                // update all previously optimistically stored entries with the error message and change the state
                for(DBItemDeploymentHistory optimistic : optimisticEntries) {
                    optimistic.setErrorMessage(either.getLeft().message());
                    optimistic.setState(DeploymentState.NOT_DEPLOYED.value());
                    dbLayer.getSession().update(optimistic);
                    // update related inventory configuration to deployed=false 
                    DBItemInventoryConfiguration cfg = dbLayer.getConfiguration(optimistic.getInventoryConfigurationId());
                    cfg.setDeployed(false);
                    dbLayer.getSession().update(cfg);
                }
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(optimisticEntries);
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
    public static void callUpdateItemsFor(DBLayerDeploy dbLayer,
            SignedItemsSpec signedItemsSpec,
            String account, String commitId, String controllerId, String accessToken, JocError jocError, String wsIdentifier) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException, CertificateException {
        
        if (signedItemsSpec.getVerifiedDeployables() != null && !signedItemsSpec.getVerifiedDeployables().isEmpty()) {
            
            // store new history entries and update inventory for update operation optimistically
            storeNewDepHistoryEntries(signedItemsSpec, account, commitId, controllerId, accessToken, jocError, dbLayer);

            List<DBItemInventoryCertificate> caCertificates = dbLayer.getCaCertificates();
            boolean verified = false;
            String signerDN = null;
            X509Certificate cert = null;
            // call updateItems command via ControllerApi for the given controller
            switch(signedItemsSpec.getKeyPair().getKeyAlgorithm()) {
            case SOSKeyConstants.PGP_ALGORITHM_NAME:
                PublishUtils.updateItemsAddOrUpdatePGP(commitId, signedItemsSpec.getVerifiedDeployables(), controllerId, dbLayer).thenAccept(either -> 
                            processAfterAdd(either, account, commitId, controllerId, accessToken, jocError, wsIdentifier));
                break;
            case SOSKeyConstants.RSA_ALGORITHM_NAME:
                cert = KeyUtil.getX509Certificate(signedItemsSpec.getKeyPair().getCertificate());
                verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                if (verified) {
                    PublishUtils.updateItemsAddOrUpdateWithX509Certificate(commitId, signedItemsSpec.getVerifiedDeployables(), controllerId, dbLayer, 
                    		SOSKeyConstants.RSA_SIGNER_ALGORITHM, signedItemsSpec.getKeyPair().getCertificate()).thenAccept(either -> 
                                processAfterAdd(either, account, commitId, controllerId, accessToken, jocError, wsIdentifier));
                } else {
                  signerDN = cert.getSubjectDN().getName();
                  PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(commitId, signedItemsSpec.getVerifiedDeployables(), controllerId, dbLayer, 
                		  SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN)
                  .thenAccept(either -> processAfterAdd(either, account, commitId, controllerId, accessToken, jocError, wsIdentifier));
                }
                break;
            case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                cert = KeyUtil.getX509Certificate(signedItemsSpec.getKeyPair().getCertificate());
                verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                if (verified) {
                    PublishUtils.updateItemsAddOrUpdateWithX509Certificate(commitId,  
                            signedItemsSpec.getVerifiedDeployables(), controllerId, dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, 
                            signedItemsSpec.getKeyPair().getCertificate()).thenAccept(either -> 
                            processAfterAdd(either, account, commitId, controllerId, accessToken, jocError, wsIdentifier));
                } else {
                  signerDN = cert.getSubjectDN().getName();
                  PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(commitId,  
                          signedItemsSpec.getVerifiedDeployables(), controllerId, dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN)
                      .thenAccept(either -> processAfterAdd(either, account, commitId, controllerId, accessToken, jocError, wsIdentifier));
                }
                break;
            }
        }
    }

}
