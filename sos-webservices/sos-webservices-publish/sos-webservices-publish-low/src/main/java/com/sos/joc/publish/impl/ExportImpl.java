package com.sos.joc.publish.impl;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.Version;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.ExportForSigning;
import com.sos.joc.model.publish.ExportShallowCopy;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpdateableFileOrderSourceAgentName;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.joc.publish.resource.IExportResource;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@Path("inventory")
public class ExportImpl extends JOCResourceImpl implements IExportResource {

    private static final String API_CALL = "./inventory/export";
    
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
            ExportForSigning forSigning = filter.getForSigning();
            
            boolean permitted = false;
            if (forSigning != null) {
                permitted = getJocPermissions(xAccessToken).getInventory().getManage();
            } else {
                permitted = getJocPermissions(xAccessToken).getInventory().getView();
            }
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            DBItemJocAuditLog dbAudit = storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
            String account = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);

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
                folderPermissions.setSchedulerId(controllerId);
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                deployablesForSigning = PublishUtils.getDeployableControllerObjectsFromDB(forSigning.getDeployables(), dbLayer, commitId);
                deployablesForSigning = deployablesForSigning.stream()
                		.filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
                final String controllerIdUsed = controllerId;
                deployablesForSigning.stream()
                .forEach(deployable -> {
                    if (DeployType.WORKFLOW.equals(deployable.getObjectType())) {
                        updateableWorkflowJobsAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(deployable.getPath(),
                        		deployable.getContent(), DeployType.WORKFLOW.intValue(), controllerIdUsed, dbLayer));
                    } else if (DeployType.FILEORDERSOURCE.equals(deployable.getObjectType())) {
                        updateableFileOrderSourceAgentNames.add(PublishUtils.getUpdateableAgentRefInFileOrderSource(deployable.getPath(),
                                deployable.getContent(), controllerIdUsed, dbLayer));
                    }
                });
                JocAuditLog.storeAuditLogDetails(deployablesForSigning.stream().map(i -> new AuditLogDetail(i.getPath(), i.getObjectType()
                        .intValue())), hibernateSession, dbAudit.getId(), dbAudit.getCreated());
            } else { // shallow copy
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                deployablesForShallowCopy = PublishUtils.getDeployableConfigurationObjectsFromDB(shallowCopy.getDeployables(), dbLayer);
                deployablesForShallowCopy = deployablesForShallowCopy.stream()
                		.filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
                releasables = PublishUtils.getReleasableObjectsFromDB(shallowCopy.getReleasables(), dbLayer);
                releasables = releasables.stream().filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
                
                JocAuditLog.storeAuditLogDetails(deployablesForShallowCopy.stream().map(i -> new AuditLogDetail(i.getPath(), i.getObjectType()
                        .intValue())), hibernateSession, dbAudit.getId(), dbAudit.getCreated());
                JocAuditLog.storeAuditLogDetails(releasables.stream().map(i -> new AuditLogDetail(i.getPath(), i.getObjectType()
                        .intValue())), hibernateSession, dbAudit.getId(), dbAudit.getCreated());
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
                    stream = PublishUtils.writeTarGzipFileForSigning(deployablesForSigning, releasables, updateableWorkflowJobsAgentNames, 
                    		updateableFileOrderSourceAgentNames, commitId, controllerId, dbLayer, jocVersion, apiVersion, inventoryVersion);
                } else { // shallow copy
                    stream = PublishUtils.writeTarGzipFileShallow(deployablesForShallowCopy, releasables, dbLayer, jocVersion, apiVersion, inventoryVersion);
                }
            } else {
                if (forSigning != null) {
                    stream = PublishUtils.writeZipFileForSigning(deployablesForSigning, releasables, updateableWorkflowJobsAgentNames, 
                    		updateableFileOrderSourceAgentNames, commitId, controllerId, dbLayer, jocVersion, apiVersion, inventoryVersion);
                } else { // shallow copy
                    stream = PublishUtils.writeZipFileShallow(deployablesForShallowCopy, releasables, dbLayer, jocVersion, apiVersion, inventoryVersion);
                }
            }
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
