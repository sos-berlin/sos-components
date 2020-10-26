package com.sos.joc.publish.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
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

@Path("publish")
public class ExportImpl extends JOCResourceImpl implements IExportResource {

    private static final String API_CALL = "./publish/export";
    private ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
    
    @Override
    public JOCDefaultResponse getExportConfiguration(String xAccessToken, String accessToken, String filename, String configurations,
            String deployments) throws Exception {
        ExportFilter filter = new ExportFilter();
        List<Long> configs = new ArrayList<Long>();
        String[] configsAsString = configurations.split(",");
        for (int i=0; i < configsAsString.length; i++) {
            configs.add(Long.valueOf(configsAsString[i]));
        }
        filter.setConfigurations(configs);
        List<Long> deploys = new ArrayList<Long>();
        String[] deploysAsString = deployments.split(",");
        for (int i=0; i < deploysAsString.length; i++) {
            deploys.add(Long.valueOf(deploysAsString[i]));
        }
        filter.setDeployments(deploys);
        byte[] filterBytes = Globals.objectMapper.writeValueAsBytes(filter);
        return postExportConfiguration(getAccessToken(xAccessToken, accessToken), filename, filterBytes);
    }
        
	@Override
	public JOCDefaultResponse postExportConfiguration(String xAccessToken, String filename, byte[] exportFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, exportFilter, xAccessToken);
            JsonValidator.validateFailFast(exportFilter, ExportFilter.class);
            ExportFilter filter = Globals.objectMapper.readValue(exportFilter, ExportFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
            		getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isExport());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String versionId = UUID.randomUUID().toString();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            final Set<JSObject> jsObjects = getObjectsFromDB(filter, hibernateSession, versionId);
            if (filename.endsWith("tar.gz") || filename.endsWith("gzip")) {
                return JOCDefaultResponse.responseOctetStreamDownloadStatus200(PublishUtils.writeTarGzipFile(jsObjects, versionId), filename);
            } else {
                return JOCDefaultResponse.responseOctetStreamDownloadStatus200(PublishUtils.writeZipFile(jsObjects, versionId), filename);
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
        
        if (filter.getDeployments() != null) {
            List<DBItemDeploymentHistory> deploymentDbItems = dbLayer.getFilteredDeployments(filter);
            for (DBItemDeploymentHistory deployment : deploymentDbItems) {
                dbLayer.storeCommitIdForLaterUsage(deployment, versionId);
                allObjects.add(mapDepHistoryToJSObject(deployment, versionId));
            } 
        }
        if (filter.getConfigurations() != null) {
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
            case AGENTREF:
                AgentRef agentRef = om.readValue(item.getContent().getBytes(), AgentRef.class);
                agentRef.setVersionId(versionId);
                jsObject.setContent(agentRef);
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
            case AGENTREF:
                AgentRef agentRef = om.readValue(item.getContent().getBytes(), AgentRef.class);
                agentRef.setVersionId(versionId);
                jsObject.setContent(agentRef);
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
//        jsObject.setSignedContent(item.getSignedContent());
//        jsObject.setVersion(item.getVersion());
        jsObject.setAccount(Globals.defaultProfileAccount);
        return jsObject;
    }
    
}
