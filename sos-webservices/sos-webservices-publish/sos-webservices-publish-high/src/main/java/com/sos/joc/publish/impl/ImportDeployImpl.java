package com.sos.joc.publish.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
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
import com.sos.joc.publish.util.ImportUtils;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.joc.publish.util.StoreDeployments;
import com.sos.joc.publish.util.UpdateItemUtils;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

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
            
            String account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
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
                LOGGER.info(String.format("  and API schema version: %1$s", jocMetaInfo.getApiVersion()));
            }
            // process signature verification and save or update objects
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            List<DBItemInventoryCertificate> caCertificates = dbLayer.getCaCertificates();
            Map<ControllerObject, DBItemDepSignatures> importedObjects = new HashMap<ControllerObject, DBItemDepSignatures>();
            String controllerId = filter.getControllerId();
            String commitId = null;
            ControllerObject foundWorkflow = null;
            if (objectsWithSignature != null && !objectsWithSignature.isEmpty()) {
                for(ControllerObject config : objectsWithSignature.keySet()) {
                    switch (config.getObjectType()) {
                    case WORKFLOW:
                        commitId = getCommitId(config);
                        foundWorkflow = config;
                        break;
                    default:
                        break; // commitId = getCommitId(config);
                    }
                    if(commitId != null) {
                        break;
                    }
                }
                if(foundWorkflow != null && commitId == null) {
                    LOGGER.warn(String.format("Could not determine versionId of configuration from archive with path %1$s.", foundWorkflow.getPath()));
                }
            }
            if(foundWorkflow != null) {
                checkCommitId(dbLayer, commitId, controllerId);
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
                    break;
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
            // call UpdateRepo for all provided Controllers
            final String commitIdForUpdate = commitId;
            if(commitIdForUpdate == null || commitIdForUpdate.isEmpty()) {
                throw new JocDeployException("versionId could not be determinated, deployment not executed.");
            }
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
            if (keyPair == null) {
                throw new JocMissingKeyException("No public key or X.509 Certificate found for signature verification! - "
                        + "Please check your key from the key management section in your profile.");
            }
            Set<DBItemDeploymentHistory> toDeleteForRename = UpdateItemUtils.checkRenamingForUpdate(objectsToCheckPathRenaming, controllerId,
                    dbLayer);
            deployItems(importedObjects, toDeleteForRename, account, commitIdForUpdate, controllerId, auditLogId, keyPair, caCertificates, filter);
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

    private String getCommitId (ControllerObject config) {
        JsonReader jsonReader = null;
        String commitId = null;
        try {
            jsonReader = Json.createReader(new StringReader(config.getSignedContent()));
            JsonObject json = jsonReader.readObject();
            commitId = json.getString("versionId", "");
        } catch(Exception e) {
//            LOGGER.warn(String.format("Could not determine versionId of configuration from archive with path %1$s.", config.getPath()));
        } finally {
            jsonReader.close();
        }
        return commitId;
        
    }
    
    private static final void checkCommitId(DBLayerDeploy dbLayer, String commitId, String controllerId) {
        if (commitId == null || commitId.isEmpty()) {
            throw new JocDeployException("No versionId found in configuration. Deployment will not be processed.");
        }else {
            Boolean alreadyExists = false;
            try {
                alreadyExists = dbLayer.checkCommitIdAlreadyExists(commitId, controllerId);
            } catch (SOSHibernateException e) {
                throw new JocDeployException("Could not determine if versionId already exists in deployment history. Deployment will not be processed.");
            }
            if (alreadyExists) {
                throw new JocDeployException("versionId already used at previous deployment to this controller. Deployment will not be processed."); 
            }
        }
    }

    private void deployItems(Map<ControllerObject, DBItemDepSignatures> importedObjects,
            Set<DBItemDeploymentHistory> toDeleteForRename, String account, String commitIdForUpdate, String controllerId, Long auditLogId,
            JocKeyPair keyPair, List<DBItemInventoryCertificate> caCertificates, ImportDeployFilter filter) throws JsonParseException,
            JsonMappingException, IOException, SOSException, InterruptedException, ExecutionException, TimeoutException, CertificateException {
        final Date deploymentDate = Date.from(Instant.now());
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
            long countBoards = deployedObjects.stream().filter(item -> ConfigurationType.NOTICEBOARD.intValue() == item.getType()).count();
            LOGGER.info(String.format(
                    "Update command send to Controller \"%1$s\" containing %2$d Workflow(s), %3$d Lock(s), %4$d FileOrderSource(s), %5$d JobResource(s) and %6$d Board(s).",
                    controllerId, countWorkflows, countLocks, countFileOrderSources, countJobResources, countBoards));
            JocInventory.handleWorkflowSearch(dbLayer.getSession(), deployedObjects, false);
        }
        boolean verified = false;
        String signerDN = null;
        X509Certificate cert = null;
        switch (keyPair.getKeyAlgorithm()) {
        case SOSKeyConstants.PGP_ALGORITHM_NAME:
            UpdateItemUtils.updateItemsAddOrDeletePGPFromImport(commitIdForUpdate, importedObjects, toDeleteForRename, controllerId)
                .thenAccept(either -> 
                    StoreDeployments.processAfterAdd(either, account, commitIdForUpdate, controllerId, getAccessToken(),getJocError(),
                                    API_CALL, null));
            break;
        case SOSKeyConstants.RSA_ALGORITHM_NAME:
            cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
            verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
            if (verified) {
                UpdateItemUtils.updateItemsAddOrDeleteX509CertificateFromImport(commitIdForUpdate, importedObjects, toDeleteForRename, controllerId, 
                        filter.getSignatureAlgorithm() != null ? filter.getSignatureAlgorithm() : SOSKeyConstants.RSA_SIGNER_ALGORITHM,
                        keyPair.getCertificate()).thenAccept(either -> 
                            StoreDeployments.processAfterAdd(either, account, commitIdForUpdate, controllerId, getAccessToken(), getJocError(),
                                    API_CALL, null));
            } else {
                signerDN = cert.getSubjectDN().getName();
                UpdateItemUtils.updateItemsAddOrDeleteX509SignerDNFromImport(commitIdForUpdate, importedObjects, toDeleteForRename, controllerId,
                        filter.getSignatureAlgorithm() != null ? filter.getSignatureAlgorithm() : SOSKeyConstants.RSA_SIGNER_ALGORITHM,
                        signerDN).thenAccept(either -> 
                            StoreDeployments.processAfterAdd(either, account, commitIdForUpdate, controllerId, getAccessToken(), getJocError(),
                                    API_CALL, null));
            }
            break;
        case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
            cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
            verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
            if (verified) {
                UpdateItemUtils.updateItemsAddOrDeleteX509CertificateFromImport(commitIdForUpdate, importedObjects, toDeleteForRename, controllerId,
                        filter.getSignatureAlgorithm() != null ? filter.getSignatureAlgorithm() : SOSKeyConstants.ECDSA_SIGNER_ALGORITHM,
                        keyPair.getCertificate()).thenAccept(either -> 
                            StoreDeployments.processAfterAdd(either, account, commitIdForUpdate, controllerId, getAccessToken(), getJocError(),
                                    API_CALL, null));
            } else {
                signerDN = cert.getSubjectDN().getName();
                UpdateItemUtils.updateItemsAddOrDeleteX509SignerDNFromImport(commitIdForUpdate, importedObjects, toDeleteForRename, controllerId,
                        filter.getSignatureAlgorithm() != null ? filter.getSignatureAlgorithm() : SOSKeyConstants.ECDSA_SIGNER_ALGORITHM,
                        signerDN).thenAccept(either -> 
                            StoreDeployments.processAfterAdd(either, account, commitIdForUpdate, controllerId, getAccessToken(), getJocError(),
                                    API_CALL, null));
            }
            break;
        }
    }

}
