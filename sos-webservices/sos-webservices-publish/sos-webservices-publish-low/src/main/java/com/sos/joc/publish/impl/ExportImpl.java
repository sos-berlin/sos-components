package com.sos.joc.publish.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ExportAudit;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;
import com.sos.joc.publish.resource.IExportResource;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@Path("inventory")
public class ExportImpl extends JOCResourceImpl implements IExportResource {

    private static final String API_CALL = "./inventory/export";
    private ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
    
    @Override
    public JOCDefaultResponse getExportConfiguration(String xAccessToken, String accessToken, String filename, String exportFilter)
            throws Exception {
        return postExportConfiguration(getAccessToken(xAccessToken, accessToken), filename, exportFilter.getBytes());
    }
        
	@Override
	public JOCDefaultResponse postExportConfiguration(String xAccessToken, String filename, byte[] exportFilter) throws Exception {
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
            String versionId = UUID.randomUUID().toString();
            String account = Globals.getDefaultProfileUserAccount();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            final Set<JSObject> jsObjects = getObjectsFromDB(filter, hibernateSession, versionId);
            Set<ExportAudit> audits = jsObjects.stream().map(item ->  new ExportAudit(filter, 
                    String.format("Object with path: %1$s exported to file %2$s with profile %3$s", item.getPath(), filename, account)))
                    .collect(Collectors.toSet());
            StreamingOutput stream = null;
            if (filename.endsWith("tar.gz") || filename.endsWith("gzip")) {
                stream = PublishUtils.writeTarGzipFile(jsObjects, versionId);
                audits.stream().forEach(audit -> logAuditMessage(audit));
                audits.stream().forEach(audit -> storeAuditLogEntry(audit));
                return JOCDefaultResponse.responseOctetStreamDownloadStatus200(stream, filename);
            } else {
                stream = PublishUtils.writeZipFile(jsObjects, versionId);
                audits.stream().forEach(audit -> logAuditMessage(audit));
                audits.stream().forEach(audit -> storeAuditLogEntry(audit));
                return JOCDefaultResponse.responseOctetStreamDownloadStatus200(stream, filename);
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
	}

    private Set<JSObject> getObjectsFromDB(ExportFilter filter, SOSHibernateSession connection, String versionId) throws DBConnectionRefusedException,
            DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException, SOSHibernateException {
        DBLayerDeploy dbLayer = new DBLayerDeploy(connection);
        Set<JSObject> allObjects = new HashSet<JSObject>();
        
        if (filter.getDeployConfigurations() != null) {
            List<DBItemDeploymentHistory> deploymentDbItems = dbLayer.getFilteredDeployments(filter);
            for (DBItemDeploymentHistory deployment : deploymentDbItems) {
                dbLayer.storeCommitIdForLaterUsage(deployment, versionId);
                allObjects.add(mapDepHistoryToJSObject(deployment, versionId));
            } 
        }
        if (filter.getDraftConfigurations() != null) {
            List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredConfigurations(filter);
            for (DBItemInventoryConfiguration configuration : configurationDbItems) {
                dbLayer.storeCommitIdForLaterUsage(configuration, versionId);
                allObjects.add(mapInvConfigToJSObject(configuration, versionId));
            } 
        }
        return allObjects;
    }
    
    private JSObject mapInvConfigToJSObject (DBItemInventoryConfiguration item, String versionId) throws JsonParseException, JsonMappingException,
            IOException {
        JSObject jsObject = new JSObject();
        jsObject.setId(item.getId());
        jsObject.setPath(item.getPath());
        jsObject.setObjectType(PublishUtils.mapInventoryMetaConfigurationType(ConfigurationType.fromValue(item.getType())));
        switch (jsObject.getObjectType()) {
            case WORKFLOW:
                Workflow workflow = om.readValue(item.getContent().getBytes(), Workflow.class);
                workflow.setVersionId(versionId);
                jsObject.setContent(workflow);
                break;
            case LOCK:
                // TODO: 
                break;
            case JUNCTION:
                // TODO: 
                break;
            default:
                break;
        }
        jsObject.setAccount(Globals.defaultProfileAccount);
        // TODO: setVersion
//        jsObject.setVersion(item.getVersion());
        jsObject.setModified(item.getModified());
        return jsObject;
    }

    private JSObject mapDepHistoryToJSObject (DBItemDeploymentHistory item, String versionId) throws JsonParseException, JsonMappingException,
            IOException {
        JSObject jsObject = new JSObject();
        jsObject.setId(item.getId());
        jsObject.setPath(item.getPath());
        jsObject.setObjectType(DeployType.fromValue(item.getType()));
        switch (jsObject.getObjectType()) {
            case WORKFLOW:
                Workflow workflow = om.readValue(item.getContent().getBytes(), Workflow.class);
                workflow.setVersionId(versionId);
                jsObject.setContent(workflow);
                break;
            case JOBCLASS:
                // TODO: 
                break;
            case LOCK:
                // TODO: 
                break;
            case JUNCTION:
                // TODO: 
                break;
        }
//        jsObject.setVersion(item.getVersion());
        jsObject.setAccount(Globals.defaultProfileAccount);
        return jsObject;
    }
    
}
