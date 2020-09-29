package com.sos.joc.publish.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.agent.AgentRefPublish;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.jobscheduler.model.workflow.WorkflowPublish;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
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
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.publish.Controller;
import com.sos.joc.model.publish.ImportDeployFilter;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.model.publish.SignaturePath;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;
import com.sos.joc.publish.resource.IImportDeploy;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@Path("publish")
public class ImportDeployImpl extends JOCResourceImpl implements IImportDeploy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportDeployImpl.class);
    private static final String API_CALL = "./publish/import";
    private SOSHibernateSession connection = null;
    private Set<Workflow> workflows = new HashSet<Workflow>();
    private Set<AgentRef> agentRefs = new HashSet<AgentRef>();
    private Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
    private ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
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
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {}
		return postImportDeploy(xAccessToken, body, auditLog, importDeployFilter);
	}

	private JOCDefaultResponse postImportDeploy(String xAccessToken, FormDataBodyPart body,
			AuditParams auditLog, String importDeployFilter) throws Exception {
        JsonValidator.validateFailFast(importDeployFilter.getBytes(StandardCharsets.UTF_8), ImportDeployFilter.class);
        ImportDeployFilter filter = Globals.objectMapper.readValue(importDeployFilter, ImportDeployFilter.class);
	    
        InputStream stream = null;
        String uploadFileName = null;
        SOSHibernateSession hibernateSession = null;
        try {
            filter.setAuditLog(auditLog);
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
             // copy&paste Permission, has to be changed to the correct permission for upload 
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, "",
            		getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isImport());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            stream = body.getEntityAs(InputStream.class);
            final String mediaSubType = body.getMediaType().getSubtype().replaceFirst("^x-", "");
            ImportDeployAudit importAudit = new ImportDeployAudit(filter);
            logAuditMessage(importAudit);
            // process uploaded archive
            if (mediaSubType.contains("zip") && !mediaSubType.contains("gzip")) {
                PublishUtils.readZipFileContent(stream, workflows, agentRefs);
            } else if (mediaSubType.contains("tgz") || mediaSubType.contains("tar.gz") || mediaSubType.contains("gzip")) {
                PublishUtils.readTarGzipFileContent(stream, workflows, agentRefs);
            } else {
            	throw new JocUnsupportedFileTypeException(String.format("The file %1$s to be uploaded must have the format zip!", uploadFileName)); 
            }
            // process signature verification and save or update objects
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBItemJocAuditLog dbItemAuditLog = storeAuditLogEntry(importAudit);
            dbLayer = new DBLayerDeploy(hibernateSession);
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> importedObjects = new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            String versionId = null;
            if (workflows != null && !workflows.isEmpty()) {
                versionId = workflows.stream().findFirst().get().getVersionId();
            }
            for (Workflow workflow : workflows) {
                WorkflowPublish wfEdit = new WorkflowPublish();
                wfEdit.setContent(workflow);
                if (!signaturePaths.isEmpty()) {
                    Signature signature = PublishUtils.verifyWorkflows(hibernateSession, signaturePaths, workflow, account);
                    if (signature != null) {
                        wfEdit.setSignedContent(signature.getSignatureString());
                    } 
                }
                DBItemInventoryConfiguration dbItem = 
                        dbLayer.saveOrUpdateInventoryConfiguration(workflow.getPath(), wfEdit, workflow.getTYPE(), account, dbItemAuditLog.getId());
                DBItemDepSignatures dbItemSignature = dbLayer.saveOrUpdateSignature(dbItem.getId(), wfEdit, account, workflow.getTYPE());
                importedObjects.put(dbItem, dbItemSignature);
            }
            for (AgentRef agentRef : agentRefs) {
                AgentRefPublish arEdit = new AgentRefPublish();
                arEdit.setContent(agentRef);
                if (!signaturePaths.isEmpty()) {
                    Signature signature = PublishUtils.verifyAgentRefs(hibernateSession, signaturePaths, agentRef, account);
                    if (signature != null) {
                        arEdit.setSignedContent(signature.getSignatureString());
                    } 
                }
                DBItemInventoryConfiguration dbItem = 
                        dbLayer.saveOrUpdateInventoryConfiguration(agentRef.getPath(), arEdit, agentRef.getTYPE(), account, dbItemAuditLog.getId());
                DBItemDepSignatures dbItemSignature = dbLayer.saveOrUpdateSignature(dbItem.getId(), arEdit, account, agentRef.getTYPE());
                importedObjects.put(dbItem, dbItemSignature);
            }
            // Deploy
            final Date deploymentDate = Date.from(Instant.now());
            // call UpdateRepo for all provided Controllers
            List<Controller> controllers = filter.getControllers();
            final String versionIdForUpdate = versionId;
            for (Controller controller : controllers) {
                // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename
                // and subsequently call delete for the object with the previous path before committing the update
                PublishUtils.checkPathRenamingForUpdate(importedObjects.keySet(), controller.getController(), dbLayer);

                // call updateRepo command via Proxy of given controllers
                PublishUtils.updateRepoAddOrUpdate(versionId, importedObjects, null, controller.getController(), dbLayer).thenAccept(either -> {
                    if (either.isRight()) {
                        Set<DBItemDeploymentHistory> deployedObjects = PublishUtils.cloneInvConfigurationsToDepHistoryItems(importedObjects,
                                account, dbLayer, versionIdForUpdate, controller.getController(), deploymentDate);
                        PublishUtils.prepareNextInvConfigGeneration(importedObjects.keySet(), dbLayer.getSession());
                        LOGGER.info(String.format("Deploy to Controller \"%1$s\" was successful!", controller.getController()));
                    } else if (either.isLeft()) {
                        // an error occurred
                        String message = String.format("Response from Controller \"%1$s:\": %2$s", controller.getController(), either.getLeft().message());
                        LOGGER.warn(message);
                        // updateRepo command is atomic, therefore all items are rejected
                        List<DBItemDeploymentHistory> failedDeployUpdateItems = dbLayer.updateFailedDeploymentForUpdate(importedObjects,
                                null, controller.getController(), account, versionIdForUpdate, either.getLeft().message());
                        // if not successful the objects and the related controllerId have to be stored
                        // in a submissions table for reprocessing
                        dbLayer.cloneFailedDeployment(failedDeployUpdateItems);
                        hasErrors = true;
                        if (either.getLeft().codeOrNull() != null) {
                            listOfErrors.add(new BulkError().get(new JocError(either.getLeft().message()), "/"));
                        } else {
                            listOfErrors.add(new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), either.getLeft().message()),
                                    "/"));
                        }
                    }
                }).get();
            }
            storeAuditLogEntry(importAudit);
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
            Globals.disconnect(connection);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {}
        }
	}

}
