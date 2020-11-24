package com.sos.joc.publish.impl;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.DeployAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.ControllerId;
import com.sos.joc.model.publish.DeployConfig;
import com.sos.joc.model.publish.DeployConfiguration;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DraftConfig;
import com.sos.joc.model.publish.DraftConfiguration;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IDeploy;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("inventory/deployment")
public class DeployImpl extends JOCResourceImpl implements IDeploy {

    private static final String API_CALL = "./inventory/deployment/deploy";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployImpl.class);
    private DBLayerDeploy dbLayer = null;
    private boolean hasErrors = false;
    private List<Err419> listOfErrors = new ArrayList<Err419>();

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validateFailFast(filter, DeployFilter.class);
            DeployFilter deployFilter = Globals.objectMapper.readValue(filter, DeployFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            // get all available controller instances
            Map<String, List<DBItemInventoryJSInstance>> allControllers = dbLayer.getAllControllers().stream().collect(Collectors.groupingBy(
                    DBItemInventoryJSInstance::getControllerId));
            // process filter
            Set<String> controllerIds = getControllerIdsFromFilter(deployFilter);
            List<DraftConfiguration> draftConfigsToStore = getDraftConfigurationsToStoreFromFilter(deployFilter);
            /* TODO: 
             * - check for configurationIds with -marked-for-delete- set
             * - get all deployments from history related to the given configurationId
             * - get all controllers from those deployments
             * - delete all those existing deployments from all determined controllers
            **/
            List<DeployConfiguration> deployConfigsToStoreAgain = getDeployConfigurationsToStoreFromFilter(deployFilter);
            List<DeployConfiguration> deployConfigsToDelete = getDeployConfigurationsToDeleteFromFilter(deployFilter);
            
//            Set<Long> deploymentIdsToDeleteFromConfigIds = getDeploymentIdsToDeleteByConfigurationIdsFromFilter(deployFilter, allControllers);
//            Set<Long> configurationIdsToDelete = getDeplozConfigurationsToDeleteFromFilter(deployFilter);

            // read all objects provided in the filter from the database
            List<DBItemInventoryConfiguration> configurationDBItemsToDeploy = null;
            if (draftConfigsToStore != null) {
                configurationDBItemsToDeploy = dbLayer.getFilteredInventoryConfiguration(draftConfigsToStore);
            }
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeploy = null;
            if (deployConfigsToStoreAgain != null) {
                depHistoryDBItemsToDeploy = dbLayer.getFilteredDeploymentHistory(deployConfigsToStoreAgain);
            }
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = null;
            if (deployConfigsToDelete != null) {
                depHistoryDBItemsToDeployDelete = dbLayer.getFilteredDeploymentHistory(deployConfigsToDelete);
            }

            Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts = 
                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> signedDeployments = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();

            for (DBItemInventoryConfiguration update : configurationDBItemsToDeploy) {
                DBItemDepSignatures signature = dbLayer.getSignature(update.getId());
                if (signature != null) {
                    signedDrafts.put(update, signature);
                }
            }
            for (DBItemDeploymentHistory depHistory : depHistoryDBItemsToDeploy) {
                DBItemDepSignatures signature = dbLayer.getSignature(depHistory.getId());
                if (signature != null) {
                    signedDeployments.put(depHistory, signature);
                }
            }
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations =
                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables = 
                    new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
            String versionId = null;
            final Date deploymentDate = Date.from(Instant.now());
            // only signed objects will be processed
            // existing signatures of objects are verified
            for (DBItemInventoryConfiguration draft : signedDrafts.keySet()) {
                if (versionId == null) {
                    versionId = dbLayer.getVersionId(draft);
                }
                verifiedConfigurations.put(PublishUtils.verifySignature(account, draft, signedDrafts.get(draft), hibernateSession,
                        JocSecurityLevel.HIGH), signedDrafts.get(draft));
            }
            for (DBItemDeploymentHistory deployed : signedDeployments.keySet()) {
                verifiedReDeployables.put(PublishUtils.verifySignature(account, deployed, signedDeployments.get(deployed), hibernateSession,
                        JocSecurityLevel.HIGH), signedDeployments.get(deployed));
            }
            final String versionIdForUpdate = versionId;
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
            // call UpdateRepo for all provided Controllers
            // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename
            for (String controller : allControllers.keySet()) {
                List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(verifiedConfigurations.keySet(),
                        controller, dbLayer, keyPair.getKeyAlgorithm());
                if (toDeleteForRename != null) {
                    toDeleteForRename.addAll(PublishUtils.checkPathRenamingForUpdate(verifiedReDeployables.keySet(), controller, dbLayer, 
                            keyPair.getKeyAlgorithm()));
                } else {
                    toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(verifiedReDeployables.keySet(), controller, dbLayer, 
                            keyPair.getKeyAlgorithm());
                }
                // and subsequently call delete for the object with the previous path before committing the update
                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
                    // clone list as it has to be final now for processing in CompleteableFuture.thenAccept method
                    final List<DBItemDeploymentHistory> toDelete = toDeleteForRename;
                    // set new versionId for second round (delete items)
                    final String versionIdForDeleteRenamed = UUID.randomUUID().toString();
                    // call updateRepo command via Proxy of given controllers
                    PublishUtils.updateRepoDelete(versionIdForDeleteRenamed, toDelete, controller, dbLayer, keyPair.getKeyAlgorithm())
                    .thenAccept(either -> {
                                processAfterDelete(either, toDelete, controller, account, versionIdForDeleteRenamed, null);
                            }).get();
                }
            }
            for (String controllerId : controllerIds) {
                // call updateRepo command via ControllerApi for given controllers
                String signerDN = null;
                X509Certificate cert = null;
                switch (keyPair.getKeyAlgorithm()) {
                case SOSKeyConstants.PGP_ALGORITHM_NAME:
                    PublishUtils.updateRepoAddOrUpdatePGP(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId, 
                            dbLayer).thenAccept(either -> {
                                processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, versionIdForUpdate, controllerId,
                                        deploymentDate, deployFilter);
                            }).get();
                    break;
                case SOSKeyConstants.RSA_ALGORITHM_NAME:
                    cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateRepoAddOrUpdateWithX509(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                            dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, versionIdForUpdate, controllerId,
                                        deploymentDate, deployFilter);
                            }).get();
                    break;
                case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                    cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateRepoAddOrUpdateWithX509(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                            dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, versionIdForUpdate, controllerId,
                                        deploymentDate, deployFilter);
                            }).get();
                    break;
                }
            }
            if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                // set new versionId for second round (delete items)
                final String versionIdForDelete = UUID.randomUUID().toString();
                for (String controller : allControllers.keySet()) {
                    // call updateRepo command via Proxy of given controllers
                    final List<DBItemDeploymentHistory> toDelete = depHistoryDBItemsToDeployDelete;
                    PublishUtils.updateRepoDelete(versionIdForDelete, toDelete, controller, dbLayer, 
                            keyPair.getKeyAlgorithm()).thenAccept(either -> {
                                processAfterDelete(either, toDelete, controller, account, versionIdForDelete,
                                        deployFilter);
                            }).get();
                    Set<Long> draftConfigsToDelete = depHistoryDBItemsToDeployDelete.stream()
                            .map(DBItemDeploymentHistory::getInventoryConfigurationId).collect(Collectors.toSet());
                    JocInventory.deleteConfigurations(draftConfigsToDelete);
                }
            }
            if (hasErrors) {
                return JOCDefaultResponse.responseStatus419(listOfErrors);
            } else {
                if (verifiedConfigurations != null && !verifiedConfigurations.isEmpty()) {
                    dbLayer.cleanupSignaturesForConfigurations(verifiedConfigurations.keySet());
                    dbLayer.cleanupCommitIdsForConfigurations(verifiedConfigurations.keySet());
                }
                if (verifiedReDeployables != null && !verifiedReDeployables.isEmpty()) {
                    dbLayer.cleanupSignaturesForRedeployments(verifiedReDeployables.keySet());
                    dbLayer.cleanupCommitIdsForRedeployments(verifiedReDeployables.keySet());
                }
                return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private Set<String> getControllerIdsFromFilter(DeployFilter deployFilter) {
        return deployFilter.getControllerIds().stream().map(ControllerId::getControllerId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private List<DraftConfiguration> getDraftConfigurationsToStoreFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDraftConfigurations().stream()
                    .map(DraftConfig::getDraftConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<DeployConfiguration> getDeployConfigurationsToStoreFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDeployConfigurations().stream()
                    .map(DeployConfig::getDeployConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<DeployConfiguration> getDeployConfigurationsToDeleteFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getDelete() != null) {
            return deployFilter.getDelete().getDeployConfigurations().stream()
                    .map(DeployConfig::getDeployConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return null;
        }
    }

//    private Set<Long> getDeploymentIdsToDeleteByConfigurationIdsFromFilter(DeployFilter deployFilter,
//            Map<String, List<DBItemInventoryJSInstance>> controllerInstances) throws SOSHibernateException {
//        Set<Long> configurationIds = deployFilter.getDelete().stream().map(DeployDelete::getConfigurationId).filter(Objects::nonNull).collect(
//                Collectors.toSet());
//        Set<Long> deploymentIdsToDelete = new HashSet<Long>();
//        for (String controller : controllerInstances.keySet()) {
//            List<Long> deploymentIds = dbLayer.getLatestDeploymentFromConfigurationId(configurationIds, controller);
//            deploymentIdsToDelete.addAll(deploymentIds);
//        }
//        return deploymentIdsToDelete;
//    }

    private void processAfterAdd (
            Either<Problem, Void> either, 
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables,
            String account,
            String versionIdForUpdate,
            String controllerId,
            Date deploymentDate,
            DeployFilter deployFilter) {
        if (either.isRight()) {
            // no error occurred
            Set<DBItemDeploymentHistory> deployedObjects = PublishUtils.cloneInvConfigurationsToDepHistoryItems(
                    verifiedConfigurations, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate);
            deployedObjects.addAll(PublishUtils.cloneDepHistoryItemsToRedeployed(
                    verifiedReDeployables, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate));
            createAuditLogFor(deployedObjects, deployFilter, controllerId, true, versionIdForUpdate);
            PublishUtils.prepareNextInvConfigGeneration(verifiedConfigurations.keySet(), null, dbLayer.getSession());
            LOGGER.info(String.format("Deploy to Controller \"%1$s\" was successful!", controllerId));
        } else if (either.isLeft()) {
            // an error occurred
            String message = String.format(
                    "Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
            LOGGER.error(message);
            // updateRepo command is atomic, therefore all items are rejected
            List<DBItemDeploymentHistory> failedDeployUpdateItems = dbLayer.updateFailedDeploymentForUpdate(
                    verifiedConfigurations, verifiedReDeployables, controllerId, account, versionIdForUpdate, either.getLeft().message());
            // if not successful the objects and the related controllerId have to be stored 
            // in a submissions table for reprocessing
            dbLayer.createSubmissionForFailedDeployments(failedDeployUpdateItems);
            hasErrors = true;
            if (either.getLeft().codeOrNull() != null) {
                listOfErrors.add(
                        new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), either.getLeft().message()), "/"));
            } else {
                listOfErrors.add(new BulkError().get(new JocError(either.getLeft().message()), "/"));
            }
        }
    }
    
    private void processAfterDelete (
            Either<Problem, Void> either, 
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete, 
            String controller, 
            String account, 
            String versionIdForDelete,
            DeployFilter deployFilter) {
        if (either.isRight()) {
            Set<DBItemDeploymentHistory> deletedDeployItems = 
                    PublishUtils.updateDeletedDepHistory(depHistoryDBItemsToDeployDelete, dbLayer);
            if (deployFilter != null) {
                createAuditLogFor(deletedDeployItems, deployFilter, controller, false, versionIdForDelete);
            }
        } else if (either.isLeft()) {
            String message = String.format("Response from Controller \"%1$s:\": %2$s", controller, either.getLeft().message());
            LOGGER.warn(message);
            // updateRepo command is atomic, therefore all items are rejected
            List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(
                    depHistoryDBItemsToDeployDelete, controller, account, versionIdForDelete, either.getLeft().message());
            // if not successful the objects and the related controllerId have to be stored 
            // in a submissions table for reprocessing
            dbLayer.createSubmissionForFailedDeployments(failedDeployDeleteItems);
            hasErrors = true;
            if (either.getLeft().codeOrNull() != null) {
                listOfErrors.add(
                        new BulkError().get(new JocError(either.getLeft().message()), "/"));
            } else {
                listOfErrors.add(
                        new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), 
                                either.getLeft().message()), "/"));
            }
        }
    }
    
    private void createAuditLogFor(Collection<DBItemDeploymentHistory> depHistoryEntries, DeployFilter deployFilter, String controllerId,
            boolean update, String commitId) {
        Set<DeployAudit> audits = depHistoryEntries.stream().map(item -> { 
            if (update) {
                return new DeployAudit(deployFilter, update, controllerId, commitId, item.getId(),
                        item.getPath(), String.format("object %1$s updated on controller %2$s", item.getPath(), controllerId));
            } else {
                return new DeployAudit(deployFilter, update, controllerId, commitId, item.getId(),
                        item.getPath(), String.format("object %1$s deleted from controller %2$s", item.getPath(), controllerId));
            }
        }).collect(Collectors.toSet());
        audits.stream().forEach(audit -> logAuditMessage(audit));
        audits.stream().forEach(audit -> storeAuditLogEntry(audit));
    }
    
}