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
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.ImportDeployFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.SignaturePath;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IImportDeploy;
import com.sos.joc.publish.util.DeleteDeployments;
import com.sos.joc.publish.util.ImportUtils;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.joc.publish.util.StoreDeployments;
import com.sos.schema.JsonValidator;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("inventory/deployment")
public class ImportDeployImpl extends JOCResourceImpl implements IImportDeploy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportDeployImpl.class);
    private static final String API_CALL = "./inventory/deployment/import_deploy";
    private DBLayerDeploy dbLayer = null;

    @Override
    public JOCDefaultResponse postImportDeploy(String xAccessToken, FormDataBodyPart body, String controllerId, String signatureAlgorithm,
            String format, String timeSpent, String ticketLink, String comment) throws Exception {
        AuditParams auditParams = new AuditParams();
        auditParams.setComment(comment);
        auditParams.setTicketLink(ticketLink);
        try {
            auditParams.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {
        }
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
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
            
            DBItemJocAuditLog dbAuditItem = storeAuditLog(filter.getAuditLog(), CategoryType.DEPLOYMENT);
            Long auditLogId = dbAuditItem != null ? dbAuditItem.getId() : 0L;
            
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            stream = body.getEntityAs(InputStream.class);
            Map<ControllerObject, SignaturePath> objectsWithSignature = new HashMap<ControllerObject, SignaturePath>();
            JocMetaInfo jocMetaInfo = new JocMetaInfo();

            // process uploaded archive
            if (ArchiveFormat.ZIP.equals(filter.getFormat())) {
                objectsWithSignature = ImportUtils.readZipFileContentWithSignatures(stream, jocMetaInfo);
            } else if (ArchiveFormat.TAR_GZ.equals(filter.getFormat())) {
                objectsWithSignature = ImportUtils.readTarGzipFileContentWithSignatures(stream, jocMetaInfo);
            } else {
                throw new JocUnsupportedFileTypeException(String.format("The file %1$s to be uploaded must have one of the formats .zip or .tar.gz!",
                        uploadFileName));
            }
            if (!ImportUtils.isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                // TODO: process transformation rules
                LOGGER.info(String.format("Imported from JS7 JOC Cockpit version: %1$s", jocMetaInfo.getJocVersion()));
                LOGGER.info(String.format("  with inventory schema version: %1$s", jocMetaInfo.getInventorySchemaVersion()));
                LOGGER.info(String.format("  and API version: %1$s", jocMetaInfo.getApiVersion()));
            }
            // process signature verification and save or update objects
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            List<DBItemInventoryCertificate> caCertificates = dbLayer.getCaCertificates();
            Map<ControllerObject, DBItemDepSignatures> importedObjects = new HashMap<ControllerObject, DBItemDepSignatures>();
            String commitId = null;
            if (objectsWithSignature != null && !objectsWithSignature.isEmpty()) {
                ControllerObject config = objectsWithSignature.keySet().stream().findFirst().get();
                switch (config.getObjectType()) {
                case WORKFLOW:
                    commitId = Globals.objectMapper.readValue(config.getSignedContent(), Workflow.class).getVersionId();
                    break;
                case LOCK:
                    break;
                case NOTICEBOARD:
                    break;
                case JOBCLASS:
                    break;
                default:
                    commitId = Globals.objectMapper.readValue(config.getSignedContent(), Workflow.class).getVersionId();
                }
            }
            Set<java.nio.file.Path> folders = new HashSet<java.nio.file.Path>();
            folders = objectsWithSignature.keySet().stream().map(config -> config.getPath()).map(path -> Paths.get(path).getParent()).collect(
                    Collectors.toSet());
            Set<DBItemInventoryConfiguration> objectsToCheckPathRenaming = new HashSet<DBItemInventoryConfiguration>();
            for (ControllerObject config : objectsWithSignature.keySet()) {
                SignaturePath signaturePath = objectsWithSignature.get(config);
                switch (config.getObjectType()) {
                case WORKFLOW:
                    DBItemInventoryConfiguration workflowDbItem = dbLayer.getConfigurationByPath(config.getPath(), ConfigurationType.WORKFLOW);
                    if (workflowDbItem == null) {
                        throw new JocDeployException(String.format(
                                "The configuration with path %1$s does not exist in the current JOC instance. Deployment is not allowed!", config.getPath()));
                    }
                    objectsToCheckPathRenaming.add(workflowDbItem);
                    DBItemDepSignatures workflowDbItemSignature = dbLayer.saveOrUpdateSignature(workflowDbItem.getId(), signaturePath, account,
                            DeployType.WORKFLOW);
                    importedObjects.put(config, workflowDbItemSignature);
                    break;
                case JOBRESOURCE:
                    DBItemInventoryConfiguration jobResourceDbItem = dbLayer.getConfigurationByPath(config.getPath(), ConfigurationType.JOBRESOURCE);
                    if (jobResourceDbItem == null) {
                        throw new JocDeployException(String.format(
                                "The configuration with path %1$s does not exist in the current JOC instance. Deployment is not allowed!", config.getPath()));
                    }
                    objectsToCheckPathRenaming.add(jobResourceDbItem);
                    DBItemDepSignatures jobResourceDbItemSignature = dbLayer.saveOrUpdateSignature(jobResourceDbItem.getId(), signaturePath, account,
                            DeployType.JOBRESOURCE);
                    importedObjects.put(config, jobResourceDbItemSignature);
                    break;
                case LOCK:
                    DBItemInventoryConfiguration lockDbItem = dbLayer.getConfigurationByPath(config.getPath(), ConfigurationType.LOCK);
                    if (lockDbItem == null) {
                        throw new JocDeployException(String.format(
                                "The configuration with path %1$s does not exist in the current JOC instance. Deployment is not allowed!", config.getPath()));
                    }
                    objectsToCheckPathRenaming.add(lockDbItem);
                    importedObjects.put(config, null);
                    break;
                case FILEORDERSOURCE:
                	DBItemInventoryConfiguration fosDbItem = dbLayer.getConfigurationByPath(config.getPath(), ConfigurationType.FILEORDERSOURCE);
                    if (fosDbItem == null) {
                        throw new JocDeployException(String.format(
                                "The configuration with path %1$s does not exist in the current JOC instance. Deployment is not allowed!", config.getPath()));
                    }
                	objectsToCheckPathRenaming.add(fosDbItem);
                	importedObjects.put(config, null);
                case NOTICEBOARD:
                    DBItemInventoryConfiguration boardDbItem = dbLayer.getConfigurationByPath(config.getPath(), ConfigurationType.NOTICEBOARD);
                    if (boardDbItem == null) {
                        throw new JocDeployException(String.format(
                                "The configuration with path %1$s does not exist in the current JOC instance. Deployment is not allowed!", config.getPath()));
                    }
                    objectsToCheckPathRenaming.add(boardDbItem);
                    importedObjects.put(config, null);
                    break;
                case JOBCLASS:
                    DBItemInventoryConfiguration jobClassDbItem = dbLayer.getConfigurationByPath(config.getPath(), ConfigurationType.JOBCLASS);
                    if (jobClassDbItem == null) {
                        throw new JocDeployException(String.format(
                                "The configuration with path %1$s does not exist in the current JOC instance. Deployment is not allowed!", config.getPath()));
                    }
                    objectsToCheckPathRenaming.add(jobClassDbItem);
                    importedObjects.put(config, null);
                    break;
                default:
                    break;
                }
            }
            dbLayer.createInvConfigurationsDBItemsForFoldersIfNotExists(PublishUtils.updateSetOfPathsWithParents(folders), auditLogId);
            // Deploy
            final Date deploymentDate = Date.from(Instant.now());
            // call UpdateRepo for all provided Controllers
            String controllerId = filter.getControllerId();
            final String commitIdForUpdate = commitId;
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
            if (keyPair == null) {
                throw new JocMissingKeyException("No public key or X.509 Certificate found for signature verification! - "
                        + "Please check your key from the key management section in your profile.");
            }
            List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkRenamingForUpdate(objectsToCheckPathRenaming, controllerId,
                    dbLayer, keyPair.getKeyAlgorithm());
            // and subsequently call delete for the object with the previous path before committing the update
            if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
                // clone list as it has to be final now for processing in CompleteableFuture.thenAccept method
                final List<DBItemDeploymentHistory> toDelete = toDeleteForRename;
                // set new versionId for second round (delete items)
                final String commitIdForDeleteRenamed = UUID.randomUUID().toString();
                // call updateRepo command via Proxy of given controllers
                DeleteDeployments.storeNewDepHistoryEntries(dbLayer, toDelete, commitIdForDeleteRenamed);
                PublishUtils.updateItemsDelete(commitIdForDeleteRenamed, toDelete, controllerId).thenAccept(either -> {
                    processAfterDelete(either, toDelete, controllerId, account, commitIdForDeleteRenamed);
                });
            }
            // Store to db optimistically
            Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();
            if (importedObjects != null && !importedObjects.isEmpty()) {
                deployedObjects.addAll(PublishUtils.cloneInvConfigurationsToDepHistoryItems(importedObjects, account, dbLayer, commitIdForUpdate,
                        controllerId, deploymentDate, auditLogId));
                PublishUtils.prepareNextInvConfigGeneration(importedObjects.keySet(), controllerId, dbLayer);
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
            boolean verified = false;
            String signerDN = null;
            X509Certificate cert = null;
            switch (keyPair.getKeyAlgorithm()) {
            case SOSKeyConstants.PGP_ALGORITHM_NAME:
                PublishUtils.updateItemsAddOrUpdatePGPFromImport(commitIdForUpdate, importedObjects, controllerId, dbLayer).thenAccept(either -> {
                    StoreDeployments.processAfterAdd(either, account, commitIdForUpdate, controllerId, getAccessToken(), getJocError(), API_CALL);
                });
                break;
            case SOSKeyConstants.RSA_ALGORITHM_NAME:
                cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                if (verified) {
                    PublishUtils.updateItemsAddOrUpdateWithX509CertificateFromImport(commitIdForUpdate, importedObjects, controllerId, dbLayer,
                            filter.getSignatureAlgorithm() != null ? filter.getSignatureAlgorithm() : SOSKeyConstants.RSA_SIGNER_ALGORITHM, keyPair.getCertificate())
                        .thenAccept(either -> {
                                StoreDeployments.processAfterAdd(either, account, commitIdForUpdate, controllerId, getAccessToken(), getJocError(), API_CALL);
                            });
                } else {
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateItemsAddOrUpdateWithX509SignerDNFromImport(commitIdForUpdate, importedObjects, controllerId, dbLayer,
                            filter.getSignatureAlgorithm() != null ? filter.getSignatureAlgorithm() : SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN)
                        .thenAccept(either -> {
                                StoreDeployments.processAfterAdd(either, account, commitIdForUpdate, controllerId, getAccessToken(), getJocError(), API_CALL);
                            });
                }
                break;
            case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                if (verified) {
                    PublishUtils.updateItemsAddOrUpdateWithX509CertificateFromImport(commitIdForUpdate, importedObjects, controllerId, dbLayer,
                            filter.getSignatureAlgorithm() != null ? filter.getSignatureAlgorithm() : SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, keyPair.getCertificate())
                        .thenAccept(either -> {
                                StoreDeployments.processAfterAdd(either, account, commitIdForUpdate, controllerId, getAccessToken(), getJocError(), API_CALL);
                            });
                } else {
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateItemsAddOrUpdateWithX509SignerDNFromImport(commitIdForUpdate, importedObjects, controllerId, dbLayer,
                            filter.getSignatureAlgorithm() != null ? filter.getSignatureAlgorithm() : SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN)
                        .thenAccept(either -> {
                                StoreDeployments.processAfterAdd(either, account, commitIdForUpdate, controllerId, getAccessToken(), getJocError(), API_CALL);
                            });
                }
                break;
            }
            // no error occurred
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
            } catch (Exception e) {
            }
        }
    }

    private void processAfterDelete(Either<Problem, Void> either, List<DBItemDeploymentHistory> itemsToDelete, String controllerId, String account,
            String versionIdForDelete) {
        try {
            if (either.isLeft()) {
                String message = String.format("Could not delete renamed object on controller first. Response from Controller \"%1$s:\": %2$s",
                        controllerId, either.getLeft().message());
                LOGGER.warn(message);
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), getAccessToken(), getJocError(), null);
        }
    }

}
