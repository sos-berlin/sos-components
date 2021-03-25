package com.sos.joc.publish.impl;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ExportAudit;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.Version;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.ExportForSigning;
import com.sos.joc.model.publish.ExportShallowCopy;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;
import com.sos.joc.publish.mapper.UpdateableFileOrderSourceAgentName;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.joc.publish.resource.IExportResource;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;
import com.sos.sign.model.fileordersource.FileOrderSource;

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
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);

            ExportForSigning forSigning = filter.getForSigning();
            ExportShallowCopy shallowCopy = filter.getShallowCopy();
            
            Set<ControllerObject> deployablesForSigning = null;
            Set<ConfigurationObject> deployablesForShallowCopy = null;
            Set<ConfigurationObject> releasables = null;
            final Set<UpdateableWorkflowJobAgentName> updateableWorkflowJobsAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
            final Set<UpdateableFileOrderSourceAgentName> updateableFileOrderSourceAgentNames = new HashSet<UpdateableFileOrderSourceAgentName>();
            
            String commitId = null;
            String controllerId = null;
            if (forSigning != null) {
                commitId = UUID.randomUUID().toString();
                controllerId = forSigning.getControllerId();
                deployablesForSigning = PublishUtils.getDeployableControllerObjectsFromDB(forSigning.getDeployables(), dbLayer, commitId);
                final String controllerIdUsed = controllerId;
                deployablesForSigning.stream()
                .forEach(deployable -> {
                    if (DeployType.WORKFLOW.equals(deployable.getObjectType())) {
                        try {
                            Workflow workflow = (Workflow)deployable.getContent();
                            updateableWorkflowJobsAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(deployable.getPath(),
                                    om.writeValueAsString(workflow), ConfigurationType.WORKFLOW, controllerIdUsed, dbLayer));
                        } catch (JsonProcessingException e) {}   
                    } else if (DeployType.FILEORDERSOURCE.equals(deployable.getObjectType())) {
                        try {
                            FileOrderSource fileOrderSource = (FileOrderSource)deployable.getContent();
                            updateableFileOrderSourceAgentNames.add(PublishUtils.getUpdateableAgentRefInFileOrderSource(fileOrderSource.getId(),
                                    om.writeValueAsString(fileOrderSource), controllerIdUsed, dbLayer));
                        } catch (JsonProcessingException e) {}
                    }
                });
            } else { // shallow copy
                deployablesForShallowCopy = PublishUtils.getDeployableConfigurationObjectsFromDB(shallowCopy.getDeployables(), dbLayer);
                releasables = PublishUtils.getReleasableObjectsFromDB(shallowCopy.getReleasables(), dbLayer);
            }
            // TODO: create time restricted token to export, too
            // TODO: get JOC Version and Schema Version for later appliance of transformation rules (import)
            InputStream jocVersionStream = null;
            InputStream apiVersionStream = null;
            InputStream inventoryVersionStream = null;
            jocVersionStream = this.getClass().getClassLoader().getResourceAsStream("/version.json");
            if (jocVersionStream == null) {
                jocVersionStream = this.getClass().getResourceAsStream("/version.json");
            }
            apiVersionStream = this.getClass().getClassLoader().getResourceAsStream("/api-schema-version.json");
            if (apiVersionStream == null) {
                apiVersionStream = this.getClass().getResourceAsStream("/api-schema-version.json");
            }
            inventoryVersionStream = this.getClass().getClassLoader().getResourceAsStream("/inventory-schema-version.json");
            if (inventoryVersionStream == null) {
                inventoryVersionStream = this.getClass().getResourceAsStream("/inventory-schema-version.json");
            }
            Version jocVersion = PublishUtils.readVersion(jocVersionStream, "/version.json");
            Version apiVersion = PublishUtils.readVersion(apiVersionStream, "/api-schema-version.json");
            Version inventoryVersion = PublishUtils.readVersion(inventoryVersionStream, "/inventory-schema-version.json");

            StreamingOutput stream = null;
            if (filter.getExportFile().getFormat().equals(ArchiveFormat.TAR_GZ)) {
                if (forSigning != null) {
                    stream = PublishUtils.writeTarGzipFileForSigning(deployablesForSigning, releasables, 
                            updateableWorkflowJobsAgentNames, updateableFileOrderSourceAgentNames,
                            commitId, controllerId, dbLayer, jocVersion, apiVersion, inventoryVersion);
                } else { // shallow copy
                    stream = PublishUtils.writeTarGzipFileShallow(deployablesForShallowCopy, releasables, 
                            dbLayer, jocVersion, apiVersion, inventoryVersion);
                }
            } else {
                if (forSigning != null) {
                    stream = PublishUtils.writeZipFileForSigning(deployablesForSigning, releasables,
                            updateableWorkflowJobsAgentNames, updateableFileOrderSourceAgentNames,
                            commitId, controllerId, dbLayer, jocVersion, apiVersion, inventoryVersion);
                } else { // shallow copy
                    stream = PublishUtils.writeZipFileShallow(deployablesForShallowCopy, releasables,
                            dbLayer, jocVersion, apiVersion, inventoryVersion);
                }
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
