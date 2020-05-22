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
import com.sos.jobscheduler.db.inventory.DBItemDeployedConfiguration;
import com.sos.jobscheduler.db.inventory.DBItemDeployedConfigurationHistory;
import com.sos.jobscheduler.db.inventory.DBItemInventoryConfiguration;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.inventory.DBItemJoinDepCfgDepCfgHistory;
import com.sos.jobscheduler.db.inventory.DBItemJoinJSDepCfgHistory;
import com.sos.jobscheduler.model.agent.AgentRefEdit;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.WorkflowEdit;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.DeployOperationStatus;
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

    public DBItemDeployedConfigurationHistory getConfigurationHistory(String masterId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select cfg from ");
        hql.append(DBLayer.DBITEM_DEP_CONFIGURATION_HISTORY).append(" as cfg");
        hql.append(", ").append(DBLayer.DBITEM_JOIN_INV_JS_DEP_CFG_HISTORY).append(" as cfgToJs");
        hql.append(" where cfgToJs.jobschedulerId = :jobschedulerId");
        hql.append(" and cfg.id = cfgToJs.configurationId");
        Query<DBItemDeployedConfigurationHistory> query = session.createQuery(hql.toString());
        query.setParameter("jobschedulerId", masterId);
        return session.getSingleResult(query);
    }

    public DBItemDeployedConfigurationHistory getLatestSuccessfulConfigurationHistory(String masterId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select cfg from ");
        hql.append(DBLayer.DBITEM_DEP_CONFIGURATION_HISTORY).append(" as cfg");
        hql.append(", ").append(DBLayer.DBITEM_JOIN_INV_JS_DEP_CFG_HISTORY).append(" as cfgToJs");
        hql.append(" where cfgToJs.jobschedulerId = :jobschedulerId");
        hql.append(" and cfg.id = cfgToJs.configurationId");
        hql.append(" and cfg.state like 'DEPLOYED_SUCCESSFULLY'");
        Query<DBItemDeployedConfigurationHistory> query = session.createQuery(hql.toString());
        query.setParameter("jobschedulerId", masterId);
        List<DBItemDeployedConfigurationHistory> configurations = session.getResultList(query);
        DBItemDeployedConfigurationHistory configuration = null;
        if (configurations != null && !configurations.isEmpty()) {
            if (configurations.size() > 1) {
                for (int i = 1; i < configurations.size(); i++) {
                    if (configurations.get(i - 1).getId() > configurations.get(i).getId()) {
                        configuration = configurations.get(i - 1);
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

    public List<DBItemDeployedConfiguration> getDeployedConfigurations(Long configurationId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            sql.append(" configurationId = :configurationId");
            Query<DBItemDeployedConfiguration> query = session.createQuery(sql.toString());
            query.setParameter("configurationId", configurationId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemDeployedConfiguration getDeployedConfigurationByPath(String path) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            sql.append(" path = :path");
            Query<DBItemDeployedConfiguration> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeployedConfiguration> getDeployedConfigurationsByDeployedConfigurationHistory(String schedulerId)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select mapping from ").append(DBLayer.DBITEM_DEP_CONFIGURATION_HISTORY).append("as conf, ");
            sql.append(DBLayer.DBITEM_JOIN_DEP_CFG_DEP_CFG_HISTORY).append("as mapping");
            sql.append(" where conf.jobschedulerId = :jobschedulerId");
            sql.append(" and mapping.configurationId = conf.id");
            Query<DBItemDeployedConfiguration> query = session.createQuery(sql.toString());
            query.setParameter("jobschedulerId", schedulerId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryConfiguration> getAllInventoryConfigurations() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryConfiguration> getFilteredInventoryConfigurationsForExport(ExportFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getFilteredInventoryConfigurations(filter.getJsObjectPaths());
    }

    public List<DBItemInventoryConfiguration> getFilteredInventoryConfigurationsForSetVersion(SetVersionFilter filter)
            throws DBConnectionRefusedException, DBInvalidDataException {
        return getFilteredInventoryConfigurations(filter.getJsObjects());
    }

    public List<DBItemInventoryConfiguration> getFilteredInventoryConfigurations(List<String> paths) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            if (paths != null && !paths.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
                sql.append(" where path in :paths");
                Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
                query.setParameter("paths", paths);
                return session.getResultList(query);
            } else {
                return new ArrayList<DBItemInventoryConfiguration>();
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeployedConfiguration> getAllDeployedConfigurations() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            Query<DBItemDeployedConfiguration> query = session.createQuery(sql.toString());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeployedConfiguration> getFilteredDeployedConfigurations(ExportFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getFilteredDeployedConfigurations(filter.getJsObjectPaths());
    }

    public List<DBItemDeployedConfiguration> getFilteredDeployedConfigurations(SetVersionFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getFilteredDeployedConfigurations(filter.getJsObjects());
    }

    public List<DBItemDeployedConfiguration> getFilteredDeployedConfigurations(List<String> paths) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            if (paths != null && !paths.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
                sql.append(" where path in :paths");
                Query<DBItemDeployedConfiguration> query = session.createQuery(sql.toString());
                query.setParameter("paths", paths);
                return session.getResultList(query);
            } else {
                return new ArrayList<DBItemDeployedConfiguration>();
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemDeployedConfiguration getDeployedConfiguration(String path, String objectType) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS);
            sql.append(" where path = :path");
            sql.append(" and objectType = :objectType");
            Query<DBItemDeployedConfiguration> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
            query.setParameter("objectType", objectType);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void saveOrUpdateInventoryConfiguration(String path, JSObject jsObject, String type, String account) throws SOSHibernateException,
            JsonProcessingException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where path = :path");
        Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
        query.setParameter("path", path);
        DBItemInventoryConfiguration existingJsObject = session.getSingleResult(query);
        Path folderPath = null;
        if (existingJsObject != null) {
            existingJsObject.setEditAccount(account);
            // existingJsObject.setOldPath(jsObject.getOldPath());
            // existingJsObject.setUri(jsObject.getUri());
            existingJsObject.setState("");
            existingJsObject.setComment(jsObject.getComment());
            existingJsObject.setModified(Date.from(Instant.now()));
            switch (DeployType.fromValue(type)) {
            case WORKFLOW:
                existingJsObject.setContent(Globals.objectMapper.writeValueAsString(((WorkflowEdit) jsObject).getContent()));
                folderPath = Paths.get(((WorkflowEdit) jsObject).getContent().getPath() + JSObjectFileExtension.WORKFLOW_FILE_EXTENSION).getParent();
                existingJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                existingJsObject.setPath(((WorkflowEdit) jsObject).getContent().getPath());
                existingJsObject.setSignedContent(((WorkflowEdit) jsObject).getSignedContent());
                existingJsObject.setVersion(((WorkflowEdit) jsObject).getVersion());
                existingJsObject.setParentVersion(((WorkflowEdit) jsObject).getParentVersion());
                // existingJsObject.setOperation(((WorkflowEdit)jsObject).getOperation());
                // existingJsObject.setVersionId(((WorkflowEdit)jsObject).getVersionId());
                break;
            case AGENT_REF:
                existingJsObject.setContent(Globals.objectMapper.writeValueAsString(((AgentRefEdit) jsObject).getContent()));
                folderPath = Paths.get(((AgentRefEdit) jsObject).getContent().getPath() + JSObjectFileExtension.AGENT_REF_FILE_EXTENSION).getParent();
                existingJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                existingJsObject.setPath(((AgentRefEdit) jsObject).getContent().getPath());
                existingJsObject.setSignedContent(((AgentRefEdit) jsObject).getSignedContent());
                existingJsObject.setVersion(((AgentRefEdit) jsObject).getVersion());
                existingJsObject.setParentVersion(((AgentRefEdit) jsObject).getParentVersion());
                existingJsObject.setUri(((AgentRefEdit) jsObject).getContent().getUri());
                // existingJsObject.setOperation(((AgentRefEdit)jsObject).getOperation());
                // existingJsObject.setVersionId(((AgentRefEdit)jsObject).getVersionId());
                break;
            case LOCK:
                // existingJsObject.setContent(Globals.objectMapper.writeValueAsString(((LockEdit)jsObject).getContent()));
                break;
            }
            session.update(existingJsObject);
        } else {
            DBItemInventoryConfiguration newJsObject = new DBItemInventoryConfiguration();
            newJsObject.setObjectType(type);
            newJsObject.setEditAccount(account);
            // newJsObject.setOldPath(null);
            newJsObject.setComment(jsObject.getComment());
            newJsObject.setState("");
            newJsObject.setModified(Date.from(Instant.now()));
            switch (DeployType.fromValue(type)) {
            case WORKFLOW:
                newJsObject.setContent(Globals.objectMapper.writeValueAsString(((WorkflowEdit) jsObject).getContent()));
                folderPath = Paths.get(((WorkflowEdit) jsObject).getContent().getPath() + JSObjectFileExtension.WORKFLOW_FILE_EXTENSION).getParent();
                newJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                newJsObject.setPath(((WorkflowEdit) jsObject).getContent().getPath());
                newJsObject.setSignedContent(((WorkflowEdit) jsObject).getSignedContent());
                newJsObject.setVersion(((WorkflowEdit) jsObject).getVersion());
                newJsObject.setParentVersion(((WorkflowEdit) jsObject).getParentVersion());
                // newJsObject.setUri(((WorkflowEdit)jsObject).getContent().getUri());
                // newJsObject.setOperation(((WorkflowEdit)jsObject).getOperation());
                // newJsObject.setVersionId(((WorkflowEdit)jsObject).getVersionId());
                break;
            case AGENT_REF:
                newJsObject.setContent(Globals.objectMapper.writeValueAsString(((AgentRefEdit) jsObject).getContent()));
                folderPath = Paths.get(((AgentRefEdit) jsObject).getContent().getPath() + JSObjectFileExtension.AGENT_REF_FILE_EXTENSION).getParent();
                newJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                newJsObject.setPath(((AgentRefEdit) jsObject).getContent().getPath());
                newJsObject.setSignedContent(((AgentRefEdit) jsObject).getSignedContent());
                newJsObject.setVersion(((AgentRefEdit) jsObject).getVersion());
                newJsObject.setParentVersion(((AgentRefEdit) jsObject).getParentVersion());
                newJsObject.setUri(((AgentRefEdit) jsObject).getContent().getUri());
                // newJsObject.setOperation(((AgentRefEdit)jsObject).getOperation());
                // newJsObject.setVersionId(((AgentRefEdit)jsObject).getVersionId());
                break;
            case LOCK:
                // newJsObject.setContent(Globals.objectMapper.writeValueAsString(((LockEdit)jsObject).getContent()));
                break;
            }
            session.save(newJsObject);
        }
    }

    public List<DBItemInventoryInstance> getMasters(List<String> masterIds) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_INV_JS_INSTANCES);
        hql.append(" where schedulerId in :schedulerId");
        Query<DBItemInventoryInstance> query = session.createQuery(hql.toString());
        query.setParameter("schedulerId", masterIds);
        return session.getResultList(query);
    }

    public List<DBItemJoinDepCfgDepCfgHistory> getJoinDepCfgDepCfgHistory(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_JOIN_DEP_CFG_DEP_CFG_HISTORY);
        hql.append(" where configurationId = :id");
        Query<DBItemJoinDepCfgDepCfgHistory> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        return session.getResultList(query);
    }

    public void updateSuccessfulJSMasterConfiguration(String masterId, String account, DBItemDeployedConfigurationHistory latestConfiguration,
            Set<DBItemDeployedConfiguration> deployedObjects, List<DBItemInventoryConfiguration> deletedDrafts, JSConfigurationState state)
            throws SOSHibernateException, DBConnectionRefusedException, DBInvalidDataException {
        List<DBItemJoinDepCfgDepCfgHistory> latestConfigurationMappings = null;
        DBItemDeployedConfigurationHistory cloneConfiguration = null;
        DBItemDeployedConfigurationHistory newConfiguration = null;
        if (latestConfiguration == null) {
            // create new configuration if not already exists
            newConfiguration = new DBItemDeployedConfigurationHistory();
            // Version of the configuration
            newConfiguration.setVersion(UUID.randomUUID().toString());
            newConfiguration.setParentVersion(null);
            newConfiguration.setState(state.toString());
            newConfiguration.setAccount(account);
            newConfiguration.setComment(String.format("new configuration for master %1$s", masterId));
            newConfiguration.setModified(Date.from(Instant.now()));
            session.save(newConfiguration);
        } else {
            // clone new configuration from latest existing one
            cloneConfiguration = new DBItemDeployedConfigurationHistory();
            cloneConfiguration.setVersion(UUID.randomUUID().toString());
            cloneConfiguration.setParentVersion(latestConfiguration.getVersion());
            cloneConfiguration.setState(state.toString());
            cloneConfiguration.setAccount(account);
            cloneConfiguration.setComment(String.format("updated configuration for master %1$s", masterId));
            cloneConfiguration.setModified(Date.from(Instant.now()));
            session.save(cloneConfiguration);
            latestConfigurationMappings = getJoinDepCfgDepCfgHistory(latestConfiguration.getId());
        }
        // Clone mapping for existing items
        // get all drafts from parent configuration if exists
        if (latestConfigurationMappings != null && !latestConfigurationMappings.isEmpty()) {
            for (DBItemJoinDepCfgDepCfgHistory mapping : latestConfigurationMappings) {
                DBItemJoinDepCfgDepCfgHistory newMapping = new DBItemJoinDepCfgDepCfgHistory();
                DBItemDeployedConfiguration deployedObject = session.get(DBItemDeployedConfiguration.class, mapping.getObjectId());
                DBItemInventoryConfiguration deletedDraft = null;
                if (deployedObject != null) {
                    if (deletedDrafts != null && !deletedDrafts.isEmpty()) {
                        deletedDraft = deletedDrafts.stream().filter(draft -> draft.getPath().equals(deployedObject.getPath())).findFirst().get();
                    }
                    if (cloneConfiguration != null) {
                        newMapping.setConfigurationId(cloneConfiguration.getId());
                    } else {
                        newMapping.setConfigurationId(newConfiguration.getId());
                    }
                    if (deletedDraft != null) {
                        DBItemDeployedConfiguration deployedToDelete = getDeployedConfigurationByPath(deletedDraft.getPath());
                        if (deployedToDelete != null) {
                            newMapping.setObjectId(deployedToDelete.getId());
                            newMapping.setOperation(DeployOperationStatus.DELETE.toString());
                            session.save(newMapping);
                        } else {
                            continue;
                        }
                    } else if (!deployedObjects.contains(deployedObject)) {
                        // do nothing if draft is marked for update, updates will be processed afterwards
                        newMapping.setObjectId(deployedObject.getId());
                        newMapping.setOperation(DeployOperationStatus.NONE.toString());
                        session.save(newMapping);
                    }
                }
            }
        }
        // updated items
        for (DBItemDeployedConfiguration updatedObject : deployedObjects) {
            DBItemJoinDepCfgDepCfgHistory newMapping = new DBItemJoinDepCfgDepCfgHistory();
            if (cloneConfiguration != null) {
                newMapping.setConfigurationId(cloneConfiguration.getId());
            } else {
                newMapping.setConfigurationId(newConfiguration.getId());
            }
            newMapping.setObjectId(updatedObject.getId());
            DBItemJoinDepCfgDepCfgHistory existingJoin = null;
            if (latestConfigurationMappings == null) {
                newMapping.setOperation(DeployOperationStatus.ADD.toString());
            } else {
                for (DBItemJoinDepCfgDepCfgHistory join : latestConfigurationMappings) {
                    if (join.getObjectId() != null && join.getObjectId() == updatedObject.getId()) {
                        existingJoin = join;
                    }
                }
//                existingJoin = latestConfigurationMappings.stream().filter(mapping -> updatedObject.getId() == mapping.getObjectId()).findFirst().get();
                if (existingJoin != null) {
                    newMapping.setOperation(DeployOperationStatus.UPDATE.toString());
                } else {
                    newMapping.setOperation(DeployOperationStatus.ADD.toString());
                }
            }
            session.save(newMapping);
        }
        // get scheduler to configuration mapping and save or update
        DBItemJoinJSDepCfgHistory cfgToJsMapping = getJoinJSDepCfgHistory(masterId);
        if (cfgToJsMapping == null) {
            cfgToJsMapping = new DBItemJoinJSDepCfgHistory();
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

    public void updateFailedJSMasterConfiguration(String masterId, String account, DBItemDeployedConfigurationHistory latestConfiguration,
            JSConfigurationState state) throws SOSHibernateException {
        DBItemDeployedConfigurationHistory cloneConfiguration = null;
        DBItemDeployedConfigurationHistory newConfiguration = null;
        if (latestConfiguration == null) {
            // create new configuration if not already exists
            newConfiguration = new DBItemDeployedConfigurationHistory();
            // Version of the configuration
            newConfiguration.setVersion(UUID.randomUUID().toString().substring(0, 19));
            newConfiguration.setParentVersion(null);
            newConfiguration.setState(state.toString());
            newConfiguration.setAccount(account);
            newConfiguration.setComment(String.format("new configuration for JobScheduler %1$s", masterId));
            newConfiguration.setModified(Date.from(Instant.now()));
            session.save(newConfiguration);
        } else {
            // clone new configuration from latest existing one
            cloneConfiguration = new DBItemDeployedConfigurationHistory();
            cloneConfiguration.setVersion(UUID.randomUUID().toString());
            cloneConfiguration.setParentVersion(latestConfiguration.getVersion());
            cloneConfiguration.setState(state.toString());
            cloneConfiguration.setAccount(account);
            cloneConfiguration.setComment(String.format("updated configuration for JobScheduler %1$s", masterId));
            cloneConfiguration.setModified(Date.from(Instant.now()));
            session.save(cloneConfiguration);
        }
    }

    public DBItemJoinJSDepCfgHistory getJoinJSDepCfgHistory(String jsMasterId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_JOIN_INV_JS_DEP_CFG_HISTORY);
        hql.append(" where jobschedulerId = :jsMasterId");
        Query<DBItemJoinJSDepCfgHistory> query = session.createQuery(hql.toString());
        query.setParameter("jsMasterId", jsMasterId);
        return session.getSingleResult(query);
    }

}