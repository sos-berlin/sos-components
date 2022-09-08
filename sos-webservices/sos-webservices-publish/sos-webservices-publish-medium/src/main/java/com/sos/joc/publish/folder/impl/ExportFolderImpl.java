package com.sos.joc.publish.folder.impl;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.StreamingOutput;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.Version;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.folder.ExportFolderFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpdateableFileOrderSourceAgentName;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.joc.publish.resource.IExportFolderResource;
import com.sos.joc.publish.util.ExportUtils;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@Path("inventory")
public class ExportFolderImpl extends JOCResourceImpl implements IExportFolderResource {

    private static final String API_CALL = "./inventory/export/folder";
    
    @Override
    public JOCDefaultResponse getExportFolder(String xAccessToken, String accessToken, String exportFilter)
            throws Exception {
        return postExportFolder(getAccessToken(xAccessToken, accessToken), exportFilter.getBytes());
    }
        
	@Override
	public JOCDefaultResponse postExportFolder(String xAccessToken, byte[] exportFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, exportFilter, xAccessToken);
            JsonValidator.validate(exportFilter, ExportFolderFilter.class);
            ExportFolderFilter filter = Globals.objectMapper.readValue(exportFilter, ExportFolderFilter.class);
            
            boolean permitted = false;
            if (filter.getForSigning() != null) {
                permitted = getJocPermissions(xAccessToken).getInventory().getManage();
            } else {
                permitted = getJocPermissions(xAccessToken).getInventory().getView();
            }
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            DBItemJocAuditLog dbAudit = storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
            String account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            
            Set<ControllerObject> deployablesForSigning = null;
            Set<ConfigurationObject> configurationsForShallowCopy = null;
            final Set<UpdateableWorkflowJobAgentName> updateableWorkflowJobsAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
            final Set<UpdateableFileOrderSourceAgentName> updateableFileOrderSourceAgentNames = new HashSet<UpdateableFileOrderSourceAgentName>();
            
            String commitId = null;
            String controllerId = null;
            if (filter.getForSigning() != null) {
                commitId = UUID.randomUUID().toString();
                controllerId = filter.getForSigning().getControllerId();
                folderPermissions.setSchedulerId(controllerId);
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                deployablesForSigning = ExportUtils.getFolderControllerObjectsForSigning(filter, account, dbLayer, commitId);
                deployablesForSigning = deployablesForSigning.stream()
                		.filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
                final String controllerIdUsed = controllerId;

                InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(dbLayer.getSession());
                Set<String> controllerIds = new HashSet<String>();
                controllerIds.add(controllerIdUsed);
                Map<String, Map<String, Set<String>>> agentsWithAliasesByControllerId = agentDbLayer.getAgentWithAliasesByControllerIds(controllerIds);

                deployablesForSigning.stream()
                .forEach(deployable -> {
                    if (DeployType.WORKFLOW.equals(deployable.getObjectType())) {
                        updateableWorkflowJobsAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(agentsWithAliasesByControllerId, 
                                deployable.getPath(), deployable.getContent(), DeployType.WORKFLOW.intValue(), controllerIdUsed));
                    } else if (DeployType.FILEORDERSOURCE.equals(deployable.getObjectType())) {
                        updateableFileOrderSourceAgentNames.add(PublishUtils.getUpdateableAgentRefInFileOrderSource(agentsWithAliasesByControllerId,
                                deployable.getPath(), deployable.getContent(), controllerIdUsed));
                    }
                });
                final Stream<ControllerObject> stream = deployablesForSigning.stream();
                CompletableFuture.runAsync(() -> JocAuditLog.storeAuditLogDetails(stream.map(
                        i -> new AuditLogDetail(i.getPath(), i.getObjectType().intValue())), dbAudit.getId(), dbAudit.getCreated()));
            } else { // shallow copy
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                configurationsForShallowCopy = ExportUtils.getFolderConfigurationObjectsForShallowCopy(filter, account, dbLayer);
                configurationsForShallowCopy = configurationsForShallowCopy.stream()
                		.filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
                final Stream<ConfigurationObject> stream = configurationsForShallowCopy.stream();
                CompletableFuture.runAsync(() -> JocAuditLog.storeAuditLogDetails(stream.map(i -> new AuditLogDetail(i.getPath(), i.getObjectType().intValue())),
                        dbAudit.getId(), dbAudit.getCreated()));
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
                if (filter.getForSigning() != null) {
                    stream = ExportUtils.writeTarGzipFileForSigning(deployablesForSigning, updateableWorkflowJobsAgentNames, 
                    		updateableFileOrderSourceAgentNames, commitId, controllerId, dbLayer, jocVersion, apiVersion, inventoryVersion);
                } else { // shallow copy
                    stream = ExportUtils.writeTarGzipFileShallow(configurationsForShallowCopy, dbLayer, jocVersion, apiVersion, inventoryVersion);
                }
            } else {
                if (filter.getForSigning() != null) {
                    stream = ExportUtils.writeZipFileForSigning(deployablesForSigning, updateableWorkflowJobsAgentNames, 
                    		updateableFileOrderSourceAgentNames, commitId, controllerId, dbLayer, jocVersion, apiVersion, inventoryVersion);
                } else { // shallow copy
                    stream = ExportUtils.writeZipFileShallow(configurationsForShallowCopy, dbLayer, jocVersion, apiVersion, inventoryVersion);
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
