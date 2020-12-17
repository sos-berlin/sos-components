package com.sos.joc.publish.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ExportAudit;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.ExportForBackup;
import com.sos.joc.model.publish.ExportForSigning;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.joc.publish.resource.IExportResource;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@Path("inventory")
public class ExportImpl extends JOCResourceImpl implements IExportResource {

    private static final String API_CALL = "./inventory/export";
    private ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
    
    @Override
    public JOCDefaultResponse getExportConfiguration(String xAccessToken, String accessToken, String exportFilter)
            throws Exception {
        return postExportConfiguration(getAccessToken(xAccessToken, accessToken), exportFilter.getBytes());
    }
        
	@Override
	public JOCDefaultResponse postExportConfiguration(String xAccessToken, byte[] exportFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, exportFilter, xAccessToken);
            JsonValidator.validate(exportFilter, ExportFilter.class);
            ExportFilter filter = Globals.objectMapper.readValue(exportFilter, ExportFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
            		getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isExport());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = Globals.getDefaultProfileUserAccount();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);

            ExportForSigning forSigning = filter.getForSigning();
            ExportForBackup forBackup = filter.getForBackup();
            
            Set<JSObject> deployables = null;
            Set<ConfigurationObject> releasables = null;
            final Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
            String commitId = null;
            String controllerId = null;
            if (forSigning != null) {
                commitId = UUID.randomUUID().toString();
                controllerId = forSigning.getControllerId();
                deployables = PublishUtils.getDeployableObjectsFromDB(forSigning.getDeployables(), dbLayer, commitId);
                final String controllerIdUsed = controllerId;
                deployables.stream()
                .forEach(deployable -> {
                    if (DeployType.WORKFLOW.equals(deployable.getObjectType())) {
                        Workflow workflow = (Workflow)deployable.getContent();
                        try {
                            updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(om.writeValueAsString(workflow), 
                                    ConfigurationType.WORKFLOW, controllerIdUsed, dbLayer));
                        } catch (JsonProcessingException e) {}   
                    }
                });
            } else {
                deployables = PublishUtils.getDeployableObjectsFromDB(forBackup.getDeployables(), dbLayer);
                releasables = PublishUtils.getReleasableObjectsFromDB(forBackup.getReleasables(), dbLayer);
            }
            // TODO: create time restricted token to export, too
            // TODO: get JOC Version and Schema Version for later appliance of transformation rules (import)
            StreamingOutput stream = null;
            if (filter.getExportFile().getFormat().equals(ArchiveFormat.TAR_GZ)) {
                stream = PublishUtils.writeTarGzipFile(deployables, releasables, updateableAgentNames, commitId, controllerId, dbLayer);
            } else {
                stream = PublishUtils.writeZipFile(deployables, releasables, updateableAgentNames, commitId, controllerId, dbLayer);
            }
            ExportAudit audit = null;
            if (controllerId != null) {
                audit = new ExportAudit(filter, 
                        String.format("objects exported for controller <%1$s> to file <%2$s> with profile <%3$s>.", 
                                controllerId, filter.getExportFile().getFilename(), account));
            } else {
                audit = new ExportAudit(filter, 
                        String.format("objects exported to file <%1$s> with profile <%2$s>.", filter.getExportFile().getFilename(), account));
            }
            logAuditMessage(audit);
            storeAuditLogEntry(audit);
            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(stream, filter.getExportFile().getFilename());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
	}

}
