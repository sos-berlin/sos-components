package com.sos.joc.publish.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.model.agent.AgentRefEdit;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.WorkflowEdit;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDepVersions;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.InventoryMeta;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.model.publish.SetVersionFilter;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.joc.publish.mapper.UpDownloadMapper;

public class DBLayerDeploy {

    private SOSHibernateSession session;
    private ObjectMapper om = UpDownloadMapper.initiateObjectMapper();

    public DBLayerDeploy(SOSHibernateSession connection) {
        session = connection;
    }

    public SOSHibernateSession getSession() {
        return session;
    }

    public List<DBItemDeploymentHistory> getConfigurationHistory(String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(" where controllerId = :controllerId");
        Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        return session.getResultList(query);
    }
    
    public List<DBItemDepVersions> getVersions (Long invConfigurationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_DEP_VERSIONS);
        hql.append(" where invConfigurationId = :invConfigurationId");
        Query<DBItemDepVersions> query = session.createQuery(hql.toString());
        query.setParameter("invConfigurationId", invConfigurationId);
        return session.getResultList(query);
    }

    public List<DBItemDepSignatures> getSignatures (Long inventoryConfigurationId) throws DBConnectionRefusedException,
    DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_SIGNATURES);
            sql.append(" where invConfigurationId = :inventoryConfigurationId");
            Query<DBItemDepSignatures> query = session.createQuery(sql.toString());
            query.setParameter("inventoryConfigurationId", inventoryConfigurationId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getDeployedConfigurations (Long inventoryConfigurationId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where inventoryConfigurationId = :inventoryConfigurationId");
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("inventoryConfigurationId", inventoryConfigurationId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getDeployedConfigurationByPath(String path) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where path = :path");
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
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
            sql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
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
                sql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
                sql.append(" where path in (:paths)");
                Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
                query.setParameterList("paths", paths);
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

    public List<DBItemDeploymentHistory> getAllDeployedConfigurations() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeployedConfigurations(ExportFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getFilteredDeployedConfigurations(filter.getJsObjectPaths());
    }

    public List<DBItemDeploymentHistory> getFilteredDeployedConfigurations(SetVersionFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getFilteredDeployedConfigurations(filter.getJsObjects());
    }

    public List<DBItemDeploymentHistory> getFilteredDeployedConfigurations(List<String> paths) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            if (paths != null && !paths.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
                sql.append(" where path in (:paths)");
                Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
                query.setParameterList("paths", paths);
                return session.getResultList(query);
            } else {
                return new ArrayList<DBItemDeploymentHistory>();
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemDeploymentHistory getDeployedConfiguration(String path, Integer objectType) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where path = :path");
            sql.append(" and objectType = :objectType");
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
            query.setParameter("objectType", objectType);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void saveOrUpdateInventoryConfiguration(String path, JSObject jsObject, DeployType type, String account, Long auditLogId) throws SOSHibernateException,
            JsonProcessingException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where path = :path");
        Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
        query.setParameter("path", path);
        DBItemInventoryConfiguration existingJsObject = session.getSingleResult(query);
        Path folderPath = null;
        String name = null;
        if (existingJsObject != null) {
            existingJsObject.setModified(Date.from(Instant.now()));
            switch (type) {
            case WORKFLOW:
                existingJsObject.setContent(om.writeValueAsString(((WorkflowEdit) jsObject).getContent()));
                existingJsObject.setContentJoc(null);
                existingJsObject.setAuditLogId(auditLogId);
                existingJsObject.setDocumentationId(0L);
                existingJsObject.setDeployed(false);
                // save or update signature in different Table
                if (jsObject.getSignedContent() != null && !jsObject.getSignedContent().isEmpty()) {
                    saveOrUpdateSignature(existingJsObject.getId(), jsObject, account, type);
                }
                break;
            case AGENT_REF:
                existingJsObject.setContent(om.writeValueAsString(((AgentRefEdit) jsObject).getContent()));
                existingJsObject.setContentJoc(null);
                existingJsObject.setAuditLogId(auditLogId);
                existingJsObject.setDocumentationId(0L);
                existingJsObject.setDeployed(false);
                // save or update signature in different Table
                if (jsObject.getSignedContent() != null && !jsObject.getSignedContent().isEmpty()) {
                    saveOrUpdateSignature(existingJsObject.getId(), jsObject, account, type);
                }
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
            session.update(existingJsObject);
        } else {
            DBItemInventoryConfiguration newJsObject = new DBItemInventoryConfiguration();
            Date now = Date.from(Instant.now());
            newJsObject.setModified(now);
            newJsObject.setCreated(now);
            switch (type) {
            case WORKFLOW:
                newJsObject.setContent(om.writeValueAsString(((WorkflowEdit) jsObject).getContent()));
                newJsObject.setContentJoc(null);
                folderPath = Paths.get(((WorkflowEdit) jsObject).getContent().getPath() + JSObjectFileExtension.WORKFLOW_FILE_EXTENSION).getParent();
                newJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                newJsObject.setParentFolder(folderPath.getParent().toString().replace('\\', '/'));
                newJsObject.setPath(((WorkflowEdit) jsObject).getContent().getPath());
                name = Paths.get(((WorkflowEdit) jsObject).getContent().getPath()).getFileName().toString();
                newJsObject.setName(name);
                newJsObject.setType(InventoryMeta.ConfigurationType.WORKFLOW);
                newJsObject.setAuditLogId(auditLogId);
                newJsObject.setDocumentationId(0L);
                newJsObject.setDeployed(false);
                session.save(newJsObject);
                // save or update signature in different Table
                if (jsObject.getSignedContent() != null && !jsObject.getSignedContent().isEmpty()) {
                    saveOrUpdateSignature(newJsObject.getId(), jsObject, account, type);
                }
                break;
            case AGENT_REF:
                newJsObject.setContent(om.writeValueAsString(((AgentRefEdit) jsObject).getContent()));
                newJsObject.setContentJoc(null);
                folderPath = Paths.get(((AgentRefEdit) jsObject).getContent().getPath() + JSObjectFileExtension.AGENT_REF_FILE_EXTENSION).getParent();
                newJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                newJsObject.setParentFolder(folderPath.getParent().toString().replace('\\', '/'));
                newJsObject.setPath(((AgentRefEdit) jsObject).getContent().getPath());
                name = Paths.get(((AgentRefEdit) jsObject).getContent().getPath()).getFileName().toString();
                newJsObject.setName(name);
                newJsObject.setType(InventoryMeta.ConfigurationType.AGENTCLUSTER);
                newJsObject.setAuditLogId(auditLogId);
                newJsObject.setDocumentationId(0L);
                newJsObject.setDeployed(false);
                session.save(newJsObject);
                // save signature in different Table
                if (jsObject.getSignedContent() != null && !jsObject.getSignedContent().isEmpty()) {
                    saveOrUpdateSignature(newJsObject.getId(), jsObject, account, type);
                }
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
        }
    }
    
    public void saveOrUpdateSignature (Long invConfId, JSObject jsObject, String account, DeployType type) throws SOSHibernateException {
        DBItemDepSignatures dbItemSig = getSignature(invConfId);
        String signature = null;
        switch (type) {
            case WORKFLOW:
                signature = ((WorkflowEdit) jsObject).getSignedContent();
                break;
            case AGENT_REF:
                signature = ((AgentRefEdit) jsObject).getSignedContent();
                break;
            case JUNCTION:
                break;
            case LOCK:
                break;
            default:
                break;
        }
        if (dbItemSig != null) {
            dbItemSig.setAccount(account);
            dbItemSig.setSignature(signature);
            dbItemSig.setDepHistoryId(null);
            dbItemSig.setModified(Date.from(Instant.now()));
            session.update(dbItemSig);
        } else {
            dbItemSig = new DBItemDepSignatures();
            dbItemSig.setAccount(account);
            dbItemSig.setSignature(signature);
            dbItemSig.setDepHistoryId(null);
            dbItemSig.setInvConfigurationId(invConfId);
            dbItemSig.setModified(Date.from(Instant.now()));
            session.save(dbItemSig);
        }
    }
    
    public DBItemDepSignatures getSignature(long invConfId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_DEP_SIGNATURES);
        hql.append(" where invConfigurationId = :invConfId");
        Query<DBItemDepSignatures> query = session.createQuery(hql.toString());
        query.setParameter("invConfId", invConfId);
        return session.getSingleResult(query);
    }

    public List<DBItemInventoryJSInstance> getControllers(List<String> controllerIds) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_INV_JS_INSTANCES);
        hql.append(" where schedulerId in (:controllerIds)");
        Query<DBItemInventoryJSInstance> query = session.createQuery(hql.toString());
        query.setParameterList("controllerIds", controllerIds);
        return session.getResultList(query);
    }
    
    public DBItemDeploymentHistory getLatestDepHistoryItem (DBItemInventoryConfiguration invConfig) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append("where inventoryConfigurationId = invCfgId");
        Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
        query.setParameter("invCfgId", invConfig.getId());
        List<DBItemDeploymentHistory> depHistoryItems = session.getResultList(query);
        Comparator<DBItemDeploymentHistory> comp = Comparator.comparingLong(depHistory -> depHistory.getDeploymentDate().getTime());
        DBItemDeploymentHistory first = depHistoryItems.stream().sorted(comp).findFirst().get();
        DBItemDeploymentHistory last = depHistoryItems.stream().sorted(comp.reversed()).findFirst().get();
        if (first.getDeploymentDate().getTime() < last.getDeploymentDate().getTime()) {
            return last;
        } else {
            return first;
        }
    }

//    public void updateSuccessfulJSMasterConfiguration(String masterId, String account, DBItemDeployedConfigurationHistory latestConfiguration,
//            Set<DBItemDeployedConfiguration> deployedObjects, List<DBItemInventoryConfiguration> deletedDrafts, JSConfigurationState state)
//            throws SOSHibernateException, DBConnectionRefusedException, DBInvalidDataException {
//        List<DBItemJoinDepCfgDepCfgHistory> latestConfigurationMappings = null;
//        DBItemDeployedConfigurationHistory cloneConfiguration = null;
//        DBItemDeployedConfigurationHistory newConfiguration = null;
//        if (latestConfiguration == null) {
//            // create new configuration if not already exists
//            newConfiguration = new DBItemDeployedConfigurationHistory();
//            // Version of the configuration
//            newConfiguration.setVersion(UUID.randomUUID().toString());
//            newConfiguration.setParentVersion(null);
//            newConfiguration.setState(state.toString());
//            newConfiguration.setAccount(account);
//            newConfiguration.setComment(String.format("new configuration for master %1$s", masterId));
//            newConfiguration.setModified(Date.from(Instant.now()));
//            session.save(newConfiguration);
//        } else {
//            // clone new configuration from latest existing one
//            cloneConfiguration = new DBItemDeployedConfigurationHistory();
//            cloneConfiguration.setVersion(UUID.randomUUID().toString());
//            cloneConfiguration.setParentVersion(latestConfiguration.getVersion());
//            cloneConfiguration.setState(state.toString());
//            cloneConfiguration.setAccount(account);
//            cloneConfiguration.setComment(String.format("updated configuration for master %1$s", masterId));
//            cloneConfiguration.setModified(Date.from(Instant.now()));
//            session.save(cloneConfiguration);
//            latestConfigurationMappings = getJoinDepCfgDepCfgHistory(latestConfiguration.getId());
//        }
//        // Clone mapping for existing items
//        // get all drafts from parent configuration if exists
//        if (latestConfigurationMappings != null && !latestConfigurationMappings.isEmpty()) {
//            for (DBItemJoinDepCfgDepCfgHistory mapping : latestConfigurationMappings) {
//                DBItemJoinDepCfgDepCfgHistory newMapping = new DBItemJoinDepCfgDepCfgHistory();
//                DBItemDeployedConfiguration deployedObject = session.get(DBItemDeployedConfiguration.class, mapping.getObjectId());
//                DBItemInventoryConfiguration deletedDraft = null;
//                if (deployedObject != null) {
//                    if (deletedDrafts != null && !deletedDrafts.isEmpty()) {
//                        deletedDraft = deletedDrafts.stream().filter(draft -> draft.getPath().equals(deployedObject.getPath())).findFirst().get();
//                    }
//                    if (cloneConfiguration != null) {
//                        newMapping.setConfigurationId(cloneConfiguration.getId());
//                    } else {
//                        newMapping.setConfigurationId(newConfiguration.getId());
//                    }
//                    if (deletedDraft != null) {
//                        DBItemDeployedConfiguration deployedToDelete = getDeployedConfigurationByPath(deletedDraft.getPath());
//                        if (deployedToDelete != null) {
//                            newMapping.setObjectId(deployedToDelete.getId());
//                            newMapping.setOperation(DeployOperationStatus.DELETE.toString());
//                            session.save(newMapping);
//                        } else {
//                            continue;
//                        }
//                    } else if (!deployedObjects.contains(deployedObject)) {
//                        // do nothing if draft is marked for update, updates will be processed afterwards
//                        newMapping.setObjectId(deployedObject.getId());
//                        newMapping.setOperation(DeployOperationStatus.NONE.toString());
//                        session.save(newMapping);
//                    }
//                }
//            }
//        }
//        // updated items
//        for (DBItemDeployedConfiguration updatedObject : deployedObjects) {
//            DBItemJoinDepCfgDepCfgHistory newMapping = new DBItemJoinDepCfgDepCfgHistory();
//            if (cloneConfiguration != null) {
//                newMapping.setConfigurationId(cloneConfiguration.getId());
//            } else {
//                newMapping.setConfigurationId(newConfiguration.getId());
//            }
//            newMapping.setObjectId(updatedObject.getId());
//            DBItemJoinDepCfgDepCfgHistory existingJoin = null;
//            if (latestConfigurationMappings == null) {
//                newMapping.setOperation(DeployOperationStatus.ADD.toString());
//            } else {
//                for (DBItemJoinDepCfgDepCfgHistory join : latestConfigurationMappings) {
//                    if (join.getObjectId() != null && join.getObjectId() == updatedObject.getId()) {
//                        existingJoin = join;
//                    }
//                }
////                existingJoin = latestConfigurationMappings.stream().filter(mapping -> updatedObject.getId() == mapping.getObjectId()).findFirst().get();
//                if (existingJoin != null) {
//                    newMapping.setOperation(DeployOperationStatus.UPDATE.toString());
//                } else {
//                    newMapping.setOperation(DeployOperationStatus.ADD.toString());
//                }
//            }
//            session.save(newMapping);
//        }
//        // get scheduler to configuration mapping and save or update
//        DBItemJoinJSDepCfgHistory cfgToJsMapping = getJoinJSDepCfgHistory(masterId);
//        if (cfgToJsMapping == null) {
//            cfgToJsMapping = new DBItemJoinJSDepCfgHistory();
//            cfgToJsMapping.setJobschedulerId(masterId);
//            if (cloneConfiguration != null) {
//                cfgToJsMapping.setConfigurationId(cloneConfiguration.getId());
//            } else {
//                cfgToJsMapping.setConfigurationId(newConfiguration.getId());
//            }
//            session.save(cfgToJsMapping);
//        } else {
//            if (cloneConfiguration != null) {
//                cfgToJsMapping.setConfigurationId(cloneConfiguration.getId());
//            } else {
//                cfgToJsMapping.setConfigurationId(newConfiguration.getId());
//            }
//            session.update(cfgToJsMapping);
//            session.commit();
//        }
//    }

//    public void updateFailedJSMasterConfiguration(String masterId, String account, DBItemDeployedConfigurationHistory latestConfiguration,
//            JSConfigurationState state) throws SOSHibernateException {
//        DBItemDeployedConfigurationHistory cloneConfiguration = null;
//        DBItemDeployedConfigurationHistory newConfiguration = null;
//        if (latestConfiguration == null) {
//            // create new configuration if not already exists
//            newConfiguration = new DBItemDeployedConfigurationHistory();
//            // Version of the configuration
//            newConfiguration.setVersion(UUID.randomUUID().toString().substring(0, 19));
//            newConfiguration.setParentVersion(null);
//            newConfiguration.setState(state.toString());
//            newConfiguration.setAccount(account);
//            newConfiguration.setComment(String.format("new configuration for JobScheduler %1$s", masterId));
//            newConfiguration.setModified(Date.from(Instant.now()));
//            session.save(newConfiguration);
//        } else {
//            // clone new configuration from latest existing one
//            cloneConfiguration = new DBItemDeployedConfigurationHistory();
//            cloneConfiguration.setVersion(UUID.randomUUID().toString());
//            cloneConfiguration.setParentVersion(latestConfiguration.getVersion());
//            cloneConfiguration.setState(state.toString());
//            cloneConfiguration.setAccount(account);
//            cloneConfiguration.setComment(String.format("updated configuration for JobScheduler %1$s", masterId));
//            cloneConfiguration.setModified(Date.from(Instant.now()));
//            session.save(cloneConfiguration);
//        }
//    }

}