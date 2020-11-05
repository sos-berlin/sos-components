package com.sos.joc.publish.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.agent.AgentRefPublish;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.jobscheduler.model.workflow.WorkflowPublish;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.DeployAudit;
import com.sos.joc.classes.audit.ImportDeployAudit;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.Controller;
import com.sos.joc.model.publish.ImportDeployFilter;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.model.publish.SignaturePath;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IImportDeploy;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("publish")
public class ImportDeployImpl extends JOCResourceImpl implements IImportDeploy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportDeployImpl.class);
    private static final String API_CALL = "./publish/import";
    private DBLayerDeploy dbLayer = null;
    private boolean hasErrors = false;
    private List<Err419> listOfErrors = new ArrayList<Err419>();

    @Override
	public JOCDefaultResponse postImportDeploy(String xAccessToken, 
			FormDataBodyPart body, 
			String timeSpent,
			String ticketLink,
			String comment,
			String importDeployFilter) throws Exception {
        AuditParams auditParams = new AuditParams();
        auditParams.setComment(comment);
        auditParams.setTicketLink(ticketLink);
        try {
            auditParams.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {}
		return postImportDeploy(xAccessToken, body, auditParams, importDeployFilter);
	}

	private JOCDefaultResponse postImportDeploy(String xAccessToken, FormDataBodyPart body,
			AuditParams auditParams, String importDeployFilter) throws Exception {
        InputStream stream = null;
        String uploadFileName = null;
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, importDeployFilter.getBytes(StandardCharsets.UTF_8), xAccessToken);
            JsonValidator.validateFailFast(importDeployFilter.getBytes(StandardCharsets.UTF_8), ImportDeployFilter.class);
            ImportDeployFilter filter = Globals.objectMapper.readValue(importDeployFilter, ImportDeployFilter.class);
            filter.setAuditLog(auditParams);
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
            final String mediaSubType = body.getMediaType().getSubtype().replaceFirst("^x-", "");

            Set<Workflow> workflows = new HashSet<Workflow>();
            Set<AgentRef> agentRefs = new HashSet<AgentRef>();
            Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
            
            // process uploaded archive
            if (mediaSubType.contains("zip") && !mediaSubType.contains("gzip")) {
                PublishUtils.readZipFileContent(stream, workflows, agentRefs);
            } else if (mediaSubType.contains("tgz") || mediaSubType.contains("tar.gz") || mediaSubType.contains("gzip")) {
                PublishUtils.readTarGzipFileContent(stream, workflows, agentRefs);
            } else {
            	throw new JocUnsupportedFileTypeException(
            	        String.format("The file %1$s to be uploaded must have the format zip!", uploadFileName)); 
            }
            // process signature verification and save or update objects
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> importedObjects = 
                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            String versionId = null;
            if (workflows != null && !workflows.isEmpty()) {
                versionId = workflows.stream().findFirst().get().getVersionId();
            }
            ImportDeployAudit mainAudit = new ImportDeployAudit(filter,
                    String.format("%1$d workflow(s) and %2$d agentRef(s) imported with profile %3$s", workflows.size(), agentRefs.size(),
                            account));
            logAuditMessage(mainAudit);
            DBItemJocAuditLog dbItemAuditLog = storeAuditLogEntry(mainAudit);
            Set<java.nio.file.Path> folders = new HashSet<java.nio.file.Path>();
            folders = workflows.stream().map(wf -> wf.getPath()).map(path -> Paths.get(path).getParent()).collect(Collectors.toSet());
            for (Workflow workflow : workflows) {
                WorkflowPublish wfEdit = new WorkflowPublish();
                wfEdit.setContent(workflow);
                if (!signaturePaths.isEmpty()) {
                    Signature signature = PublishUtils.verifyWorkflows(hibernateSession, signaturePaths, workflow, account);
                    if (signature != null) {
                        wfEdit.setSignedContent(signature.getSignatureString());
                    } 
                }
                DBItemInventoryConfiguration dbItem = dbLayer.saveOrUpdateInventoryConfiguration(
                        workflow.getPath(), wfEdit, workflow.getTYPE(), account, dbItemAuditLog.getId());
                DBItemDepSignatures dbItemSignature = dbLayer.saveOrUpdateSignature(dbItem.getId(), wfEdit, account, workflow.getTYPE());
                importedObjects.put(dbItem, dbItemSignature);
            }
            folders.addAll(agentRefs.stream().map(aRef -> aRef.getPath()).map(path -> Paths.get(path)).collect(Collectors.toSet()));
            for (AgentRef agentRef : agentRefs) {
                AgentRefPublish arEdit = new AgentRefPublish();
                arEdit.setContent(agentRef);
                if (!signaturePaths.isEmpty()) {
                    Signature signature = PublishUtils.verifyAgentRefs(hibernateSession, signaturePaths, agentRef, account);
                    if (signature != null) {
                        arEdit.setSignedContent(signature.getSignatureString());
                    } 
                }
                DBItemInventoryConfiguration dbItem = dbLayer.saveOrUpdateInventoryConfiguration(
                        agentRef.getPath(), arEdit, agentRef.getTYPE(), account, dbItemAuditLog.getId());
                DBItemDepSignatures dbItemSignature = dbLayer.saveOrUpdateSignature(dbItem.getId(), arEdit, account, agentRef.getTYPE());
                importedObjects.put(dbItem, dbItemSignature);
            }
            dbLayer.createInvConfigurationsDBItemsForFoldersIfNotExists(
                    PublishUtils.updateSetOfPathsWithParents(folders), dbItemAuditLog.getId());
            // Deploy
            final Date deploymentDate = Date.from(Instant.now());
            // call UpdateRepo for all provided Controllers
            List<Controller> controllers = filter.getControllers();
            final String versionIdForUpdate = versionId;
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
            for (Controller controller : controllers) {
                List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(
                        importedObjects.keySet(), controller.getController(), dbLayer, keyPair.getKeyAlgorithm());
                // and subsequently call delete for the object with the previous path before committing the update 
                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
                    // clone list as it has to be final now for processing in CompleteableFuture.thenAccept method
                    final List<DBItemDeploymentHistory> toDelete = toDeleteForRename;
                    // set new versionId for second round (delete items)
                    final String versionIdForDeleteRenamed = UUID.randomUUID().toString();
                        // call updateRepo command via Proxy of given controllers
                        PublishUtils.updateRepoDelete(versionIdForDeleteRenamed, toDelete, controller.getController(), dbLayer, 
                                keyPair.getKeyAlgorithm()).thenAccept(either -> {
                                processAfterDelete(either, toDelete, controller.getController(), account, versionIdForDeleteRenamed, null);
                        }).get();
                }
            }
            for (Controller controller : controllers) {
                // call updateRepo command via ControllerApi for given controllers
                String signerDN = null;
                X509Certificate cert = null;
                switch(keyPair.getKeyAlgorithm()) {
                case SOSKeyConstants.PGP_ALGORITHM_NAME:
                    PublishUtils.updateRepoAddOrUpdatePGP(versionIdForUpdate, importedObjects, null, controller.getController(), dbLayer)
                        .thenAccept(either -> {
                            processAfterAdd(either, importedObjects, null, account, versionIdForUpdate, controller.getController(),
                                    deploymentDate, filter);
                    }).get();
                    break;
                case SOSKeyConstants.RSA_ALGORITHM_NAME:
                    cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateRepoAddOrUpdateWithX509(versionIdForUpdate, importedObjects, null, controller.getController(), dbLayer,
                            SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                processAfterAdd(either, importedObjects, null, account, versionIdForUpdate, controller.getController(),
                                    deploymentDate, filter);
                    }).get();
                    break;
                case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                    cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateRepoAddOrUpdateWithX509(versionIdForUpdate, importedObjects, null, controller.getController(), dbLayer,
                            SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                processAfterAdd(either, importedObjects, null, account, versionIdForUpdate, controller.getController(),
                                    deploymentDate, filter);
                    }).get();
                    break;
                }
            }
            if (hasErrors) {
                return JOCDefaultResponse.responseStatus419(listOfErrors);
            } else {
                if (importedObjects != null && !importedObjects.isEmpty()) {
                    dbLayer.cleanupSignaturesForConfigurations(importedObjects.keySet());
                    dbLayer.cleanupCommitIdsForConfigurations(versionId);
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
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {}
        }
	}

    private void processAfterAdd (
            Either<Problem, Void> either, 
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables,
            String account,
            String versionIdForUpdate,
            String controllerId,
            Date deploymentDate, 
            ImportDeployFilter filter) {
        if (either.isRight()) {
            // no error occurred
            Set<DBItemDeploymentHistory> deployedObjects = PublishUtils.cloneInvConfigurationsToDepHistoryItems(
                    verifiedConfigurations, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate);
            deployedObjects.addAll(PublishUtils.cloneDepHistoryItemsToRedeployed(
                    verifiedReDeployables, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate));
            createAuditLogFor(deployedObjects, filter, controllerId, true, versionIdForUpdate);
            PublishUtils.prepareNextInvConfigGeneration(verifiedConfigurations.keySet(), dbLayer.getSession());
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
            ImportDeployFilter filter) {
        if (either.isRight()) {
            Set<DBItemDeploymentHistory> deletedDeployItems = 
                    PublishUtils.updateDeletedDepHistory(depHistoryDBItemsToDeployDelete, dbLayer);
            if (filter != null) {
                createAuditLogFor(deletedDeployItems, filter, controller, false, versionIdForDelete);
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
    
    private void createAuditLogFor(Collection<DBItemDeploymentHistory> depHistoryEntries, ImportDeployFilter filter, String controllerId,
            boolean update, String commitId) {
        Set<ImportDeployAudit> audits = depHistoryEntries.stream().map(item -> { 
            if (update) {
                return new ImportDeployAudit(filter, update, controllerId, commitId, item.getId(),
                        item.getPath(), String.format("object %1$s updated on controller %2$s", item.getPath(), controllerId));
            } else {
                return new ImportDeployAudit(filter, update, controllerId, commitId, item.getId(),
                        item.getPath(), String.format("object %1$s deleted from controller %2$s", item.getPath(), controllerId));
            }
        }).collect(Collectors.toSet());
        audits.stream().forEach(audit -> logAuditMessage(audit));
        audits.stream().forEach(audit -> storeAuditLogEntry(audit));
    }
    
}
