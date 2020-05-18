package com.sos.joc.publish.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.inventory.DBItemJSCfgToJSMapping;
import com.sos.jobscheduler.db.inventory.DBItemJSConfiguration;
import com.sos.jobscheduler.db.inventory.DBItemJSConfigurationMapping;
import com.sos.jobscheduler.db.inventory.DBItemJSDraftObject;
import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.jobscheduler.model.agent.AgentRefEdit;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.WorkflowEdit;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.JSConfigurationState;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.model.publish.SetVersionFilter;
import com.sos.joc.publish.common.JSObjectFileExtension;

public class DBLayerDeploy {

	private SOSHibernateSession session;

    public DBLayerDeploy(SOSHibernateSession connection) {
    	session = connection;
    }
    
    public SOSHibernateSession getSession() {
    	return session;
    }

    public DBItemJSConfiguration getConfiguration(String masterId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select cfg from ");
        hql.append(DBLayer.DBITEM_JS_CONFIGURATION).append(" as cfg");
        hql.append(", ").append(DBLayer.DBITEM_JS_CONFIG_TO_SCHEDULER_MAPPING).append(" as cfgToJs");
        hql.append(" where cfgToJs.jobschedulerId = :jobschedulerId");
        hql.append(" and cfg.id = cfgToJs.configurationId");
        Query<DBItemJSConfiguration> query = session.createQuery(hql.toString());
        query.setParameter("jobschedulerId", masterId);
        return session.getSingleResult(query);
    }
        
    public DBItemJSConfiguration getLatestSuccessfulConfiguration(String masterId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select cfg from ");
        hql.append(DBLayer.DBITEM_JS_CONFIGURATION).append(" as cfg");
        hql.append(", ").append(DBLayer.DBITEM_JS_CONFIG_TO_SCHEDULER_MAPPING).append(" as cfgToJs");
        hql.append(" where cfgToJs.jobschedulerId = :jobschedulerId");
        hql.append(" and cfgToJs.configurationId = cfg.id");
        hql.append(" and cfg.state like 'DEPLOYED_SUCCESSFULLY'");
        Query<DBItemJSConfiguration> query = session.createQuery(hql.toString());
        query.setParameter("jobschedulerId", masterId);
        List<DBItemJSConfiguration> configurations = session.getResultList(query);
        DBItemJSConfiguration configuration = null;
        if (configurations != null && !configurations.isEmpty()) {
            if (configurations.size() > 1) {
                for (int i = 1; i < configurations.size(); i++) {
                    if (configurations.get(i -1).getId() > configurations.get(i).getId()) {
                        configuration = configurations.get(i -1);
                    } else {
                        configuration = configurations.get(i);
                    }
                }
            } else {
                configuration = configurations.get(0);
            }
        }
        return configuration;
    }
        
    public List<DBItemJSObject> getJobSchedulerObjects(Long configurationId)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_JS_CONFIGURATION_MAPPING);
            sql.append(" configurationId = :configurationId");
            Query<DBItemJSObject> query = session.createQuery(sql.toString());
            query.setParameter("configurationId", configurationId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemJSObject> getJobSchedulerObjectsByConfiguration(String schedulerId)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select mapping from ").append(DBLayer.DBITEM_JS_CONFIGURATION).append("as conf, ");
            sql.append(DBLayer.DBITEM_JS_CONFIGURATION_MAPPING).append("as mapping");
            sql.append(" where conf.jobschedulerId = :jobschedulerId");
            sql.append(" and mapping.configurationId = conf.id");
            Query<DBItemJSObject> query = session.createQuery(sql.toString());
            query.setParameter("jobschedulerId", schedulerId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemJSDraftObject> getAllJobSchedulerDraftObjects()
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_JS_DRAFT_OBJECTS);
            Query<DBItemJSDraftObject> query = session.createQuery(sql.toString());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemJSDraftObject> getFilteredJobSchedulerDraftObjectsForExport(ExportFilter filter)
            throws DBConnectionRefusedException, DBInvalidDataException {
        return getFilteredJobSchedulerDraftObjects(filter.getJsObjectPaths());
    }

    public List<DBItemJSDraftObject> getFilteredJobSchedulerDraftObjectsForSetVersion(SetVersionFilter filter)
            throws DBConnectionRefusedException, DBInvalidDataException {
        return getFilteredJobSchedulerDraftObjects(filter.getJsObjects());
    }

    public List<DBItemJSDraftObject> getFilteredJobSchedulerDraftObjects(List<String> paths)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (paths != null && ! paths.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append("from ").append(DBLayer.DBITEM_JS_DRAFT_OBJECTS);
                sql.append(" where path in :paths");
                Query<DBItemJSDraftObject> query = session.createQuery(sql.toString());
                query.setParameter("paths", paths);
                return session.getResultList(query);
            } else {
                return new ArrayList<DBItemJSDraftObject>();
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemJSObject> getAllJobSchedulerDeployedObjects()
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_JS_OBJECTS);
            Query<DBItemJSObject> query = session.createQuery(sql.toString());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemJSObject> getFilteredJobSchedulerDeployedObjects(ExportFilter filter)
            throws DBConnectionRefusedException, DBInvalidDataException {
        return getFilteredJobSchedulerDeployedObjects(filter.getJsObjectPaths());
    }

    public List<DBItemJSObject> getFilteredJobSchedulerDeployedObjects(SetVersionFilter filter)
            throws DBConnectionRefusedException, DBInvalidDataException {
        return getFilteredJobSchedulerDeployedObjects(filter.getJsObjects());
    }

    public List<DBItemJSObject> getFilteredJobSchedulerDeployedObjects(List<String> paths)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (paths != null && ! paths.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append("from ").append(DBLayer.DBITEM_JS_OBJECTS);
                sql.append(" where path in :paths");
                Query<DBItemJSObject> query = session.createQuery(sql.toString());
                query.setParameter("paths", paths);
                return session.getResultList(query);
            } else {
                return new ArrayList<DBItemJSObject>();
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemJSObject getJSObject(String path, String objectType)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_JS_OBJECTS);
            sql.append(" where path = :path");
            sql.append(" and objectType = :objectType");
            Query<DBItemJSObject> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
            query.setParameter("objectType", objectType);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void saveOrUpdateJSDraftObject(String path, JSObject jsObject, String type, String account)
            throws SOSHibernateException, JsonProcessingException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_JS_DRAFT_OBJECTS);
        hql.append(" where path = :path");
        Query<DBItemJSDraftObject> query = session.createQuery(hql.toString());
        query.setParameter("path", path);
        DBItemJSDraftObject existingJsObject =  session.getSingleResult(query);
        Path folderPath = null;
        if (existingJsObject != null) {
            existingJsObject.setEditAccount(account);
//            existingJsObject.setOldPath(jsObject.getOldPath());
//            existingJsObject.setUri(jsObject.getUri());
            existingJsObject.setState("");
            existingJsObject.setComment(jsObject.getComment());
            existingJsObject.setModified(Date.from(Instant.now()));
            switch (DeployType.fromValue(type)) {
            case WORKFLOW:
                existingJsObject.setContent(Globals.objectMapper.writeValueAsString(((WorkflowEdit)jsObject).getContent()));
                folderPath = Paths.get(((WorkflowEdit)jsObject).getContent().getPath() + JSObjectFileExtension.WORKFLOW_FILE_EXTENSION).getParent();
                existingJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                existingJsObject.setPath(((WorkflowEdit)jsObject).getContent().getPath());
                existingJsObject.setSignedContent(((WorkflowEdit)jsObject).getSignedContent());
                existingJsObject.setVersion(((WorkflowEdit)jsObject).getVersion());
                existingJsObject.setParentVersion(((WorkflowEdit)jsObject).getParentVersion());
//                existingJsObject.setOperation(((WorkflowEdit)jsObject).getOperation());
//                existingJsObject.setVersionId(((WorkflowEdit)jsObject).getVersionId());
                break;
            case AGENT_REF:
                existingJsObject.setContent(Globals.objectMapper.writeValueAsString(((AgentRefEdit)jsObject).getContent()));
                folderPath = Paths.get(((AgentRefEdit)jsObject).getContent().getPath() + JSObjectFileExtension.AGENT_REF_FILE_EXTENSION).getParent();
                existingJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                existingJsObject.setPath(((AgentRefEdit)jsObject).getContent().getPath());
                existingJsObject.setSignedContent(((AgentRefEdit)jsObject).getSignedContent());
                existingJsObject.setVersion(((AgentRefEdit)jsObject).getVersion());
                existingJsObject.setParentVersion(((AgentRefEdit)jsObject).getParentVersion());
                existingJsObject.setUri(((AgentRefEdit)jsObject).getContent().getUri());
//                existingJsObject.setOperation(((AgentRefEdit)jsObject).getOperation());
//                existingJsObject.setVersionId(((AgentRefEdit)jsObject).getVersionId());
                break;
            case LOCK:
//                existingJsObject.setContent(Globals.objectMapper.writeValueAsString(((LockEdit)jsObject).getContent()));
                break;
            }
            session.update(existingJsObject);
        } else {
            DBItemJSDraftObject newJsObject = new DBItemJSDraftObject();
            newJsObject.setObjectType(type);
            newJsObject.setEditAccount(account);
//            newJsObject.setOldPath(null);
            newJsObject.setComment(jsObject.getComment());
            newJsObject.setState("");
            newJsObject.setModified(Date.from(Instant.now()));
            switch (DeployType.fromValue(type)) {
            case WORKFLOW:
                newJsObject.setContent(Globals.objectMapper.writeValueAsString(((WorkflowEdit)jsObject).getContent()));
                folderPath = Paths.get(((WorkflowEdit)jsObject).getContent().getPath() + JSObjectFileExtension.WORKFLOW_FILE_EXTENSION).getParent();
                newJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                newJsObject.setPath(((WorkflowEdit)jsObject).getContent().getPath());
                newJsObject.setSignedContent(((WorkflowEdit)jsObject).getSignedContent());
                newJsObject.setVersion(((WorkflowEdit)jsObject).getVersion());
                newJsObject.setParentVersion(((WorkflowEdit)jsObject).getParentVersion());
//                newJsObject.setUri(((WorkflowEdit)jsObject).getContent().getUri());
//                newJsObject.setOperation(((WorkflowEdit)jsObject).getOperation());
//                newJsObject.setVersionId(((WorkflowEdit)jsObject).getVersionId());
               break;
            case AGENT_REF:
                newJsObject.setContent(Globals.objectMapper.writeValueAsString(((AgentRefEdit)jsObject).getContent()));
                folderPath = Paths.get(((AgentRefEdit)jsObject).getContent().getPath() + JSObjectFileExtension.AGENT_REF_FILE_EXTENSION).getParent();
                newJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                newJsObject.setPath(((AgentRefEdit)jsObject).getContent().getPath());
                newJsObject.setSignedContent(((AgentRefEdit)jsObject).getSignedContent());
                newJsObject.setVersion(((AgentRefEdit)jsObject).getVersion());
                newJsObject.setParentVersion(((AgentRefEdit)jsObject).getParentVersion());
                newJsObject.setUri(((AgentRefEdit)jsObject).getContent().getUri());
//                  newJsObject.setOperation(((AgentRefEdit)jsObject).getOperation());
//                  newJsObject.setVersionId(((AgentRefEdit)jsObject).getVersionId());
                break;
            case LOCK:
//                newJsObject.setContent(Globals.objectMapper.writeValueAsString(((LockEdit)jsObject).getContent()));
                break;
            }
            session.save(newJsObject);
        }
    }

    public List<DBItemInventoryInstance> getMasters(List<String> masterIds) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_INVENTORY_INSTANCES);
        hql.append(" where schedulerId in :schedulerId");
        Query<DBItemInventoryInstance> query = session.createQuery(hql.toString());
        query.setParameter("schedulerId", masterIds);
        return session.getResultList(query);
    }
    
    public List<DBItemJSConfigurationMapping> getConfigurationMappings(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_JS_CONFIGURATION_MAPPING);
        hql.append(" where configurationId = :id");
        Query<DBItemJSConfigurationMapping> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        return session.getResultList(query);
    }
    
    public void updateJSMasterConfiguration(String masterId, String account, DBItemJSConfiguration latestConfiguration,
            Set<DBItemJSObject> deployedObjects, List<DBItemJSDraftObject> deletedDrafts, JSConfigurationState state) throws SOSHibernateException {
        List<DBItemJSConfigurationMapping> latestConfigurationMappings = null;
        DBItemJSConfiguration cloneConfiguration = null;
        DBItemJSConfiguration newConfiguration = null;
        if (latestConfiguration == null) {
            // create new configuration if not already exists
            newConfiguration = new DBItemJSConfiguration();
            // Version of the configuration 
            newConfiguration.setVersion(UUID.randomUUID().toString().substring(0, 19));
            newConfiguration.setParentVersion(null);
            newConfiguration.setState(state.toString());
            newConfiguration.setAccount(account);
            newConfiguration.setComment(String.format("new configuration for master %1$s", masterId));
            newConfiguration.setModified(Date.from(Instant.now()));
            session.save(newConfiguration);
        } else {
            // clone new configuration from latest existing one
            cloneConfiguration = new DBItemJSConfiguration();
            cloneConfiguration.setVersion(UUID.randomUUID().toString().substring(0, 19));
            cloneConfiguration.setParentVersion(latestConfiguration.getVersion());
            cloneConfiguration.setState(state.toString());
            cloneConfiguration.setAccount(account);
            cloneConfiguration.setComment(String.format("updated configuration for master %1$s", masterId));
            cloneConfiguration.setModified(Date.from(Instant.now()));
            session.save(cloneConfiguration);
            latestConfigurationMappings = getConfigurationMappings(latestConfiguration.getId());
        }
        // Clone mapping for existing items
        // get all drafts from parent configuration if exists
        if (latestConfigurationMappings != null && !latestConfigurationMappings.isEmpty()) {
            for (DBItemJSConfigurationMapping mapping : latestConfigurationMappings) {
                DBItemJSConfigurationMapping newMapping = new DBItemJSConfigurationMapping();
                DBItemJSObject deployedObject = session.get(DBItemJSObject.class, mapping.getObjectId());
                if (deployedObject != null) {
                    DBItemJSDraftObject deletedDraft = deletedDrafts.stream().filter(draft -> draft.getPath().equals(deployedObject.getPath())).findFirst().get();
                    if (deletedDraft != null) {
                        // do nothing if draft is marked for deletion
                        continue;
                    } else if(!deployedObjects.contains(deployedObject)) {
                        // do nothing if draft is marked for update, updates will be processed afterwards
                        if (cloneConfiguration != null) {
                            newMapping.setConfigurationId(cloneConfiguration.getId());
                        } else {
                            newMapping.setConfigurationId(newConfiguration.getId());
                        }
                        newMapping.setObjectId(deployedObject.getId());
                        session.save(newMapping);
                    }
                }
            }
        }
        // updated items
        for(DBItemJSObject updatedObject : deployedObjects) {
            DBItemJSConfigurationMapping newMapping = new DBItemJSConfigurationMapping();
            if (cloneConfiguration != null) {
                newMapping.setConfigurationId(cloneConfiguration.getId());
            } else {
                newMapping.setConfigurationId(newConfiguration.getId());
            }
            newMapping.setObjectId(updatedObject.getId());
            session.save(newMapping);
        }
        // get scheduler to configuration mapping and save or update
        DBItemJSCfgToJSMapping cfgToJsMapping = getCfgToJsMapping(masterId);
        if (cfgToJsMapping == null) {
            cfgToJsMapping = new DBItemJSCfgToJSMapping();
            cfgToJsMapping.setJobschedulerId(masterId);
            if (cloneConfiguration != null) {
                cfgToJsMapping.setConfigurationId(cloneConfiguration.getId());
            } else {
                cfgToJsMapping.setConfigurationId(newConfiguration.getId());
            }
            session.save(cfgToJsMapping);
        } else {
            if (cloneConfiguration != null) {
                cfgToJsMapping.setConfigurationId(cloneConfiguration.getId());
            } else {
                cfgToJsMapping.setConfigurationId(newConfiguration.getId());
            }
            session.update(cfgToJsMapping);
            session.commit();
        }
    }

    public DBItemJSCfgToJSMapping getCfgToJsMapping(String jsMasterId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_JS_CONFIG_TO_SCHEDULER_MAPPING);
        hql.append(" where jobschedulerId = :jsMasterId");
        Query<DBItemJSCfgToJSMapping> query = session.createQuery(hql.toString());
        query.setParameter("jsMasterId", jsMasterId);
        return session.getSingleResult(query);
    }
    
}