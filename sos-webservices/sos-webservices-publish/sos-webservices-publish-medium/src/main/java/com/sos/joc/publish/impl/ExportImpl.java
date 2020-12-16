package com.sos.joc.publish.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.jobclass.JobClass;
import com.sos.jobscheduler.model.junction.Junction;
import com.sos.jobscheduler.model.lock.Lock;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ExportAudit;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.calendar.Calendar;
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
import com.sos.webservices.order.initiator.model.Schedule;

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
            ExportForBackup forBackup = filter.getForBackup();
            
            Set<JSObject> deployables = null;
            Set<ConfigurationObject> releasables = null;
            final Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
            String commitId = null;
            String controllerId = null;
            if (forSigning != null) {
                commitId = UUID.randomUUID().toString();
                controllerId = forSigning.getControllerId();
                deployables = getDeployableObjectsFromDB(forSigning, dbLayer, commitId);
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
                deployables = getDeployableObjectsFromDB(forBackup, dbLayer);
                releasables = getReleasableObjectsFromDB(forBackup, dbLayer);
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
                        String.format("objects exported for controller <%1$s> to file <%2$s> with profile <%3$s>."
                                , controllerId, filter.getExportFile().getFilename(), account));
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

    private Set<JSObject> getDeployableObjectsFromDB(ExportForSigning filter, DBLayerDeploy dbLayer, String commitId) 
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, 
            IOException, SOSHibernateException {
        Set<JSObject> allObjects = new HashSet<JSObject>();
        if (filter.getDeployables() != null) {
            if (filter.getDeployables().getDeployConfigurations() != null && !filter.getDeployables().getDeployConfigurations().isEmpty()) {
                List<DBItemDeploymentHistory> deploymentDbItems = dbLayer.getFilteredDeployments(filter);
                for (DBItemDeploymentHistory deployment : deploymentDbItems) {
                    if (commitId != null) {
                        dbLayer.storeCommitIdForLaterUsage(deployment, commitId);
                    }
                    allObjects.add(getJSObjectFromDBItem(deployment, commitId));
                }
            }
            if (filter.getDeployables().getDraftConfigurations() != null && !filter.getDeployables().getDraftConfigurations().isEmpty()) {
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredDeployableConfigurations(filter);
                for (DBItemInventoryConfiguration configuration : configurationDbItems) {
                    if (commitId != null) {
                        dbLayer.storeCommitIdForLaterUsage(configuration, commitId);
                    }
                    allObjects.add(mapInvConfigToJSObject(configuration, commitId));
                }
            } 
        }
        return allObjects;
    }
    
    private Set<JSObject> getDeployableObjectsFromDB(ExportForBackup filter, DBLayerDeploy dbLayer) 
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, 
            IOException, SOSHibernateException {
        Set<JSObject> allObjects = new HashSet<JSObject>();
        if (filter.getDeployables() != null) {
            if (filter.getDeployables().getDeployConfigurations() != null && !filter.getDeployables().getDeployConfigurations().isEmpty()) {
                List<DBItemDeploymentHistory> deploymentDbItems = dbLayer.getFilteredDeployments(filter);
                for (DBItemDeploymentHistory deployment : deploymentDbItems) {
                    allObjects.add(getJSObjectFromDBItem(deployment));
                }
            }
            if (filter.getDeployables().getDraftConfigurations() != null && !filter.getDeployables().getDraftConfigurations().isEmpty()) {
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredDeployableConfigurations(filter);
                for (DBItemInventoryConfiguration configuration : configurationDbItems) {
                    allObjects.add(mapInvConfigToJSObject(configuration));
                }
            } 
        }
        return allObjects;
    }
    
    private Set<ConfigurationObject> getReleasableObjectsFromDB(ExportForBackup filter, DBLayerDeploy dbLayer) 
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, 
            IOException, SOSHibernateException {
        Set<ConfigurationObject> allObjects = new HashSet<ConfigurationObject>();
        if (filter.getReleasables() != null) {
            if (filter.getReleasables().getReleasedConfigurations() != null && !filter.getReleasables().getReleasedConfigurations().isEmpty()) {
                List<DBItemInventoryReleasedConfiguration> configurationDbItems = dbLayer.getFilteredReleasedConfigurations(filter);
                for (DBItemInventoryReleasedConfiguration configuration : configurationDbItems) {
                    allObjects.add(getConfigurationObjectFromDBItem(configuration));
                }
            }
            if (filter.getReleasables().getDraftConfigurations() != null && !filter.getReleasables().getDraftConfigurations().isEmpty()) {
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredReleasableConfigurations(filter);
                for (DBItemInventoryConfiguration configuration : configurationDbItems) {
                    allObjects.add(getConfigurationObjectFromDBItem(configuration));
                }
            } 
        }
        return allObjects;
    }
    
    private JSObject mapInvConfigToJSObject (DBItemInventoryConfiguration item) throws JsonParseException, 
    JsonMappingException, IOException {
        return mapInvConfigToJSObject(item, null);
    }
    
    private JSObject mapInvConfigToJSObject (DBItemInventoryConfiguration item, String commitId) throws JsonParseException, 
            JsonMappingException, IOException {
        JSObject jsObject = new JSObject();
        jsObject.setId(item.getId());
        jsObject.setPath(item.getPath());
        jsObject.setObjectType(PublishUtils.mapConfigurationType(ConfigurationType.fromValue(item.getType())));
        switch (jsObject.getObjectType()) {
        case WORKFLOW:
            Workflow workflow = om.readValue(item.getContent().getBytes(), Workflow.class);
            if (commitId != null) {
                workflow.setVersionId(commitId);
            }
            jsObject.setContent(workflow);
            break;
        case LOCK:
            Lock lock = om.readValue(item.getContent().getBytes(), Lock.class);
            if (commitId != null) {
                lock.setVersionId(commitId);
            }
            jsObject.setContent(lock);
            break;
        case JUNCTION:
            Junction junction = om.readValue(item.getContent().getBytes(), Junction.class);
            if (commitId != null) {
                junction.setVersionId(commitId);
            }
            jsObject.setContent(junction);
            break;
        case JOBCLASS:
            JobClass jobClass = om.readValue(item.getContent().getBytes(), JobClass.class);
            if (commitId != null) {
                jobClass.setVersionId(commitId);
            }
            jsObject.setContent(jobClass);
            break;
        }
        jsObject.setAccount(Globals.defaultProfileAccount);
        // TODO: setVersion
//        jsObject.setVersion(item.getVersion());
        jsObject.setModified(item.getModified());
        return jsObject;
    }

    private JSObject getJSObjectFromDBItem (DBItemDeploymentHistory item)
            throws JsonParseException, JsonMappingException, IOException {
        return getJSObjectFromDBItem(item, null);
    }

    private JSObject getJSObjectFromDBItem (DBItemDeploymentHistory item, String commitId)
            throws JsonParseException, JsonMappingException, IOException {
        JSObject jsObject = new JSObject();
        jsObject.setId(item.getId());
        jsObject.setPath(item.getPath());
        jsObject.setObjectType(DeployType.fromValue(item.getType()));
        switch (jsObject.getObjectType()) {
        case WORKFLOW:
            Workflow workflow = om.readValue(item.getInvContent().getBytes(), Workflow.class);
            if (commitId != null) {
                workflow.setVersionId(commitId);
            }
            jsObject.setContent(workflow);
            break;
        case JOBCLASS:
            JobClass jobClass = om.readValue(item.getInvContent().getBytes(), JobClass.class);
            if (commitId != null) {
                jobClass.setVersionId(commitId);
            }
            jsObject.setContent(jobClass);
            break;
        case LOCK:
            Lock lock = om.readValue(item.getInvContent().getBytes(), Lock.class);
            if (commitId != null) {
                lock.setVersionId(commitId);
            }
            jsObject.setContent(lock);
            break;
        case JUNCTION:
            Junction junction = om.readValue(item.getInvContent().getBytes(), Junction.class);
            if (commitId != null) {
                junction.setVersionId(commitId);
            }
            jsObject.setContent(junction);
            break;
        }
        jsObject.setVersion(item.getVersion());
        jsObject.setAccount(Globals.defaultProfileAccount);
        return jsObject;
    }
    
    private ConfigurationObject getConfigurationObjectFromDBItem(DBItemInventoryConfiguration item)
            throws JsonParseException, JsonMappingException, IOException {
        ConfigurationObject configuration = new ConfigurationObject();
        configuration.setId(item.getId());
        configuration.setPath(item.getPath());
        configuration.setObjectType(ConfigurationType.fromValue(item.getType()));
        switch (configuration.getObjectType()) {
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            Calendar calendar = om.readValue(item.getContent().getBytes(), Calendar.class);
            configuration.setConfiguration(calendar);
            break;
        case SCHEDULE:
            Schedule schedule = om.readValue(item.getContent(), Schedule.class);
            configuration.setConfiguration(schedule);
            break;
        default:
            break;
        }
        return configuration;
    }

    private ConfigurationObject getConfigurationObjectFromDBItem(DBItemInventoryReleasedConfiguration item)
            throws JsonParseException, JsonMappingException, IOException {
        ConfigurationObject configuration = new ConfigurationObject();
        configuration.setId(item.getId());
        configuration.setPath(item.getPath());
        configuration.setObjectType(ConfigurationType.fromValue(item.getType()));
        switch (configuration.getObjectType()) {
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            Calendar calendar = om.readValue(item.getContent().getBytes(), Calendar.class);
            configuration.setConfiguration(calendar);
            break;
        case SCHEDULE:
            Schedule schedule = om.readValue(item.getContent(), Schedule.class);
            configuration.setConfiguration(schedule);
            break;
        default:
            break;
        }
        return configuration;
    }

}
