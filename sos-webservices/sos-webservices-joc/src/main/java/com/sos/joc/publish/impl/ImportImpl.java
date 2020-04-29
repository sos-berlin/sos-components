package com.sos.joc.publish.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.agent.AgentRefEdit;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.jobscheduler.model.workflow.WorkflowEdit;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ImportAudit;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.ImportFilter;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IImportResource;
import com.sos.joc.publish.util.PublishUtils;

@Path("publish")
public class ImportImpl extends JOCResourceImpl implements IImportResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportImpl.class);
    private static final String API_CALL = "./publish/import";
    private static final List<String> SUPPORTED_SUBTYPES = new ArrayList<String>(Arrays.asList(
    		JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(),
    		JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.value(),
    		JSObjectFileExtension.LOCK_FILE_EXTENSION.value()));
    private SOSHibernateSession connection = null;

    @Override
	public JOCDefaultResponse postImportConfiguration(String xAccessToken, 
			FormDataBodyPart body, 
			String timeSpent,
			String ticketLink,
			boolean updateRepo,
			String comment) throws Exception {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {}
		return postImportConfiguration(xAccessToken, body, auditLog);
	}

	private JOCDefaultResponse postImportConfiguration(String xAccessToken, FormDataBodyPart body,
			AuditParams auditLog) throws Exception {
        InputStream stream = null;
        String uploadFileName = null;
        SOSHibernateSession hibernateSession = null;
        try {
            ImportFilter filter = new ImportFilter();
            filter.setAuditLog(auditLog);
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
             // copy&paste Permission, has to be changed to the correct permission for upload 
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, "",
            		/*getPermissonsJocCockpit(null, xAccessToken).getDeploy().isImport()*/
            		true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            stream = body.getEntityAs(InputStream.class);
            String extension = PublishUtils.getExtensionFromFilename(uploadFileName);
            final String mediaSubType = body.getMediaType().getSubtype().replaceFirst("^x-", "");
            Optional<String> supportedSubType = SUPPORTED_SUBTYPES.stream().filter(s -> mediaSubType.contains(s)).findFirst();
            ImportAudit importAudit = new ImportAudit(filter);
            logAuditMessage(importAudit);

            Set<Workflow> workflows = new HashSet<Workflow>();
            Set<AgentRef> agentRefs = new HashSet<AgentRef>();
//            Set<Lock> locks = new HashSet<Lock>();
            
            if (mediaSubType.contains("zip") && !mediaSubType.contains("gzip")) {
                readZipFileContent(stream, filter, workflows, agentRefs);
            } else if ((mediaSubType.contains("gz") || mediaSubType.contains("gzip")) && !mediaSubType.contains("tar.gz")) {
                // TODO:
//                readGzipFileContent(stream, filter);
            } else if (mediaSubType.contains("tgz") || mediaSubType.contains("tar.gz")) {
                // TODO:                
//                readTarGzipFileContent(stream, filter);
            } else {
            	throw new JocUnsupportedFileTypeException(String.format("The file %1$s to be uploaded must have the format zip!", uploadFileName)); 
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            if (workflows.size() > 0) {
                for (Workflow workflow : workflows) {
                    WorkflowEdit wfEdit = new WorkflowEdit();
                    wfEdit.setContent(workflow);
                    dbLayer.saveOrUpdateJSDraftObject(workflow.getPath(), wfEdit, workflow.getTYPE().toString(), account);
                }
            }
            if (agentRefs.size() > 0) {
                for (AgentRef agentRef : agentRefs) {
                    AgentRefEdit arEdit = new AgentRefEdit();
                    arEdit.setContent(agentRef);
                    dbLayer.saveOrUpdateJSDraftObject(agentRef.getPath(), arEdit, agentRef.getTYPE().toString(), account);
                }
                
            }
            storeAuditLogEntry(importAudit);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
            } catch (Exception e) {
            }
        }
	}

    private void readZipFileContent(InputStream inputStream, ImportFilter filter, Set<Workflow> workflows, Set<AgentRef> agentRefs/*, Set<Lock> locks*/) throws DBConnectionRefusedException, DBInvalidDataException,
            SOSHibernateException, IOException, JocUnsupportedFileTypeException, JocConfigurationException, DBOpenSessionException {
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(inputStream);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                byte[] binBuffer = new byte[8192];
                int binRead = 0;
                while ((binRead = zipStream.read(binBuffer, 0, 8192)) >= 0) {
                    outBuffer.write(binBuffer, 0, binRead);
                }
//                byte[] bytes = outBuffer.toByteArray();
                // TODO: read files from zip file an do something with it
                if (("/"+entryName).endsWith(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                    workflows.add(Globals.objectMapper.readValue(outBuffer.toString(), Workflow.class));
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.value())) {
                    agentRefs.add(Globals.objectMapper.readValue(outBuffer.toString(), AgentRef.class));
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
                    // TODO: add processing for Locks, when Locks are ready
                }
            }
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
        }
        
    }

}
