package com.sos.joc.publish.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.jobclass.JobClass;
import com.sos.inventory.model.junction.Junction;
import com.sos.inventory.model.lock.Lock;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.ImportDeployAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.jobclass.JobClassPublish;
import com.sos.joc.model.inventory.junction.JunctionPublish;
import com.sos.joc.model.inventory.lock.LockPublish;
import com.sos.joc.model.inventory.workflow.WorkflowPublish;
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.ImportDeployFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.SignaturePath;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IImportDeploy;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("inventory/deployment")
public class ImportDeployImpl extends JOCResourceImpl implements IImportDeploy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportDeployImpl.class);
    private static final String API_CALL = "./inventory/deployment/import_deploy";
    private DBLayerDeploy dbLayer = null;

    @Override
    public JOCDefaultResponse postImportDeploy(String xAccessToken,
            FormDataBodyPart body, 
            String controllerId, 
            String signatureAlgorithm,
            String format, 
            String timeSpent, 
            String ticketLink, 
            String comment) throws Exception {
        AuditParams auditParams = new AuditParams();
        auditParams.setComment(comment);
        auditParams.setTicketLink(ticketLink);
        try {
            auditParams.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {}
        ImportDeployFilter filter = new ImportDeployFilter();
        filter.setAuditLog(auditParams);
        filter.setControllerId(controllerId);
        filter.setFormat(ArchiveFormat.fromValue(format));
        filter.setSignatureAlgorithm(signatureAlgorithm);
        return postImportDeploy(xAccessToken, body, Globals.objectMapper.writeValueAsBytes(filter));
    }

	private JOCDefaultResponse postImportDeploy(String xAccessToken, FormDataBodyPart body, byte[] importDeployFilter) throws Exception {
        InputStream stream = null;
        String uploadFileName = null;
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, importDeployFilter, xAccessToken);
            JsonValidator.validateFailFast(importDeployFilter, ImportDeployFilter.class);
            ImportDeployFilter filter = Globals.objectMapper.readValue(importDeployFilter, ImportDeployFilter.class);
            // copy&paste Permission, has to be changed to the correct permission for upload 
            JOCDefaultResponse jocDefaultResponse = initPermissions("",
                   getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isImport() &&
                   getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            stream = body.getEntityAs(InputStream.class);
            Map<ControllerObject, SignaturePath> objectsWithSignature = new HashMap<ControllerObject, SignaturePath>();
            JocMetaInfo jocMetaInfo = new JocMetaInfo();
            
            // process uploaded archive
            if (ArchiveFormat.ZIP.equals(filter.getFormat())) {
                objectsWithSignature = PublishUtils.readZipFileContentWithSignatures(stream, jocMetaInfo);
            } else if (ArchiveFormat.TAR_GZ.equals(filter.getFormat())) {
                objectsWithSignature = PublishUtils.readTarGzipFileContentWithSignatures(stream, jocMetaInfo);
            } else {
            	throw new JocUnsupportedFileTypeException(
            	        String.format("The file %1$s to be uploaded must have one of the formats .zip or .tar.gz!", uploadFileName)); 
            }
            if(!PublishUtils.isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                // TODO: process transformation rules 
                LOGGER.info(String.format("Imported from JS7 JOC Cockpit version: %1$s", jocMetaInfo.getJocVersion()));
                LOGGER.info(String.format("  with inventory schema version: %1$s", jocMetaInfo.getInventorySchemaVersion()));
                LOGGER.info(String.format("  and API version: %1$s", jocMetaInfo.getApiVersion()));
            }
            // process signature verification and save or update objects
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            List<DBItemInventoryCertificate> caCertificates = dbLayer.getCaCertificates();
            Map<ControllerObject, DBItemDepSignatures> importedObjects = 
                    new HashMap<ControllerObject, DBItemDepSignatures>();
            String commitId = null;
            if (objectsWithSignature != null && !objectsWithSignature.isEmpty()) {
                ControllerObject config = objectsWithSignature.keySet().stream().findFirst().get();
                switch (config.getObjectType()) {
                case WORKFLOW:
                    commitId = ((Workflow)config.getContent()).getVersionId();
                    break;
                case LOCK:
                    break;
                case JUNCTION:
                    commitId = ((Junction)config.getContent()).getVersionId();
                    break;
                case JOBCLASS:
                    break;
                default:
                    commitId = ((Workflow)config.getContent()).getVersionId();
                }
            }
            ImportDeployAudit mainAudit = new ImportDeployAudit(filter,
                    String.format("%1$d object(s) imported with profile %2$s", objectsWithSignature.size(), account));
            logAuditMessage(mainAudit);
            DBItemJocAuditLog dbItemAuditLog = storeAuditLogEntry(mainAudit);
            Set<java.nio.file.Path> folders = new HashSet<java.nio.file.Path>();
            folders = objectsWithSignature.keySet().stream().map(config -> config.getPath()).map(path -> Paths.get(path).getParent()).collect(Collectors.toSet());
            Set<DBItemInventoryConfiguration> objectsToCheckPathRenaming = new HashSet<DBItemInventoryConfiguration>();
            for (ControllerObject config : objectsWithSignature.keySet()) {
                SignaturePath signaturePath = objectsWithSignature.get(config);
                switch(config.getObjectType()) {
                case WORKFLOW:
                    WorkflowPublish workflowPublish = new WorkflowPublish();
                    workflowPublish.setContent((Workflow)config.getContent());
                    workflowPublish.setSignedContent(signaturePath.getSignature().getSignatureString());
                    DBItemInventoryConfiguration workflowDbItem = dbLayer.getConfigurationByPath(config.getPath(), ConfigurationType.WORKFLOW);
                    objectsToCheckPathRenaming.add(workflowDbItem);
                    DBItemDepSignatures workflowDbItemSignature = dbLayer.saveOrUpdateSignature(
                            workflowDbItem.getId(), workflowPublish, account, DeployType.WORKFLOW);
                    workflowPublish.setObjectType(DeployType.WORKFLOW);
                    importedObjects.put(workflowPublish, workflowDbItemSignature);
                    break;
                case LOCK:
                    LockPublish lockPublish = new LockPublish();
                    lockPublish.setContent((Lock)config.getContent());
//                    DBItemInventoryConfiguration lockDbItem = dbLayer.getConfiguration(config.getPath(), ConfigurationType.LOCK);
//                    objectsToCheckPathRenaming.add(lockDbItem);
//                    lockPublish.setObjectType(DeployType.LOCK);
//                    importedObjects.put(lockPublish, null);
                    break;
                case JUNCTION:
                    JunctionPublish junctionPublish = new JunctionPublish();
                    junctionPublish.setContent((Junction)config.getContent());
//                    DBItemInventoryConfiguration junctionDbItem = dbLayer.getConfiguration(config.getPath(), ConfigurationType.LOCK);
//                    objectsToCheckPathRenaming.add(junctionDbItem);
//                    junctionPublish.setObjectType(DeployType.JUNCTION);
//                    importedObjects.put(junctionPublish, null);
                    break;
                case JOBCLASS:
                    JobClassPublish jobClassPublish = new JobClassPublish();
                    jobClassPublish.setContent((JobClass)config.getContent());
//                    DBItemInventoryConfiguration jobClassDbItem = dbLayer.getConfiguration(config.getPath(), ConfigurationType.LOCK);
//                    objectsToCheckPathRenaming.add(jobClassDbItem);
//                    jobClassPublish.setObjectType(DeployType.JOBCLASS);
//                    importedObjects.put(jobClassPublish, null);
                    break;
                default:
                    break;
                }
            }
            dbLayer.createInvConfigurationsDBItemsForFoldersIfNotExists(
                    PublishUtils.updateSetOfPathsWithParents(folders), dbItemAuditLog.getId());
            // Deploy
            final Date deploymentDate = Date.from(Instant.now());
            // call UpdateRepo for all provided Controllers
            String controllerId = filter.getControllerId();
            final String commitIdForUpdate = commitId;
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
            if (keyPair == null) {
                throw new JocMissingKeyException(
                        "No public key or X.509 Certificate found for signature verification! - "
                        + "Please check your key from the key management section in your profile.");
            }
            List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(
                    objectsToCheckPathRenaming, controllerId, dbLayer, keyPair.getKeyAlgorithm());
            // and subsequently call delete for the object with the previous path before committing the update 
            if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
                // clone list as it has to be final now for processing in CompleteableFuture.thenAccept method
                final List<DBItemDeploymentHistory> toDelete = toDeleteForRename;
                // set new versionId for second round (delete items)
                final String commitIdForDeleteRenamed = UUID.randomUUID().toString();
                    // call updateRepo command via Proxy of given controllers
                    PublishUtils.updateItemsDelete(commitIdForDeleteRenamed, toDelete, controllerId, dbLayer, 
                            keyPair.getKeyAlgorithm()).thenAccept(either -> {
                            processAfterDelete(either, toDelete, controllerId, account, commitIdForDeleteRenamed, null);
                    }).get();
            }
            boolean verified = false;
            String signerDN = null;
            X509Certificate cert = null;
            switch(keyPair.getKeyAlgorithm()) {
            case SOSKeyConstants.PGP_ALGORITHM_NAME:
                PublishUtils.updateItemsAddOrUpdatePGP2(commitIdForUpdate, importedObjects, null, controllerId, dbLayer)
                    .thenAccept(either -> {
                        processAfterAdd(either, importedObjects, null, account, commitIdForUpdate, controllerId, deploymentDate, filter);
                });
                break;
            case SOSKeyConstants.RSA_ALGORITHM_NAME:
                cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                if (verified) {
                    PublishUtils.updateItemsAddOrUpdateWithX509CertificateFromImport(commitIdForUpdate, importedObjects, null, controllerId, dbLayer,
                            SOSKeyConstants.RSA_SIGNER_ALGORITHM, keyPair.getCertificate()).thenAccept(either -> {
                                processAfterAdd(either, importedObjects, null, account, commitIdForUpdate, controllerId, deploymentDate, filter);
                    });
                } else {
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateItemsAddOrUpdateWithX509SignerDNFromImport(commitIdForUpdate, importedObjects, null, controllerId, dbLayer,
                            SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                processAfterAdd(either, importedObjects, null, account, commitIdForUpdate, controllerId, deploymentDate, filter);
                    });
                }
                break;
            case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                if (verified) {
                    PublishUtils.updateItemsAddOrUpdateWithX509CertificateFromImport(commitIdForUpdate, importedObjects, null, controllerId, dbLayer,
                            SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, keyPair.getCertificate()).thenAccept(either -> {
                                processAfterAdd(either, importedObjects, null, account, commitIdForUpdate, controllerId, deploymentDate, filter);
                    });
                } else {
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateItemsAddOrUpdateWithX509SignerDNFromImport(commitIdForUpdate, importedObjects, null, controllerId, dbLayer,
                            SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                processAfterAdd(either, importedObjects, null, account, commitIdForUpdate, controllerId, deploymentDate, filter);
                    });
                }
                break;
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {}
        }
	}

    private void processAfterAdd (
            Either<Problem, Void> either, 
            Map<ControllerObject, DBItemDepSignatures> verifiedConfigurations,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables,
            String account,
            String versionIdForUpdate,
            String controllerId,
            Date deploymentDate, 
            ImportDeployFilter filter) {
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            if (either.isRight()) {
                // no error occurred
                Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();
                 if (verifiedConfigurations != null && !verifiedConfigurations.isEmpty()) {
                    deployedObjects.addAll(PublishUtils.cloneInvConfigurationsToDepHistoryItems(
                        verifiedConfigurations, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate));
                    PublishUtils.prepareNextInvConfigGeneration(verifiedConfigurations.keySet(), controllerId, dbLayer);
                    // cleanup stored signatures
                    dbLayer.cleanupSignatures(verifiedConfigurations.keySet().stream()
                            .map(item -> verifiedConfigurations.get(item)).filter(Objects::nonNull).collect(Collectors.toSet()));
                    // cleanup stored commitIds
                    deployedObjects.stream().forEach(item -> dbLayer.cleanupCommitIds(item.getCommitId()));
                }
                if (verifiedReDeployables != null && !verifiedReDeployables.isEmpty()) {
                    Set<DBItemDeploymentHistory> cloned = PublishUtils.cloneDepHistoryItemsToRedeployed(
                            verifiedReDeployables, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate);
                    deployedObjects.addAll(cloned);
                    dbLayer.cleanupSignatures(verifiedReDeployables.keySet().stream()
                            .map(item -> verifiedReDeployables.get(item)).filter(Objects::nonNull).collect(Collectors.toSet()));
                    cloned.stream().forEach(item -> dbLayer.cleanupCommitIds(item.getCommitId()));
                }
                if (!deployedObjects.isEmpty()) {
                    LOGGER.info(String.format("Update command send to Controller \"%1$s\".", controllerId));
                    JocInventory.handleWorkflowSearch(newHibernateSession, deployedObjects, false);
                }
            } else if (either.isLeft()) {
                // an error occurred
                String message = String.format(
                        "Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.error(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployUpdateItems = dbLayer.updateFailedDeploymentForImportDeploy(
                        verifiedConfigurations, verifiedReDeployables, controllerId, account, versionIdForUpdate, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(failedDeployUpdateItems);
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), getAccessToken(), getJocError(), null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
    private void processAfterDelete (
            Either<Problem, Void> either, 
            List<DBItemDeploymentHistory> itemsToDelete, 
            String controllerId, 
            String account, 
            String versionIdForDelete,
            ImportDeployFilter filter) {
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            final DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            final InventoryDBLayer invDbLayer = new InventoryDBLayer(newHibernateSession);
            if (either.isRight()) {
                Set<DBItemInventoryConfiguration> configurationsToDelete = itemsToDelete.stream()
                        .map(item -> dbLayer.getInventoryConfigurationByNameAndType(item.getName(), item.getType()))
                        .collect(Collectors.toSet());
                Set<DBItemDeploymentHistory> deletedDeployItems = PublishUtils.updateDeletedDepHistory(itemsToDelete, dbLayer);
                configurationsToDelete.stream().forEach(item -> JocInventory.deleteInventoryConfigurationAndPutToTrash(item, invDbLayer));
//                JocInventory.deleteConfigurations(configurationsToDelete);
                JocInventory.handleWorkflowSearch(newHibernateSession, deletedDeployItems, true);
            } else if (either.isLeft()) {
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(
                        itemsToDelete, controllerId, account, versionIdForDelete, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(failedDeployDeleteItems);
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), getAccessToken(), getJocError(), null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
}
