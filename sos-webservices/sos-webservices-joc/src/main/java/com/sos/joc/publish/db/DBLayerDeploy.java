package com.sos.joc.publish.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.model.agent.AgentRefEdit;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.WorkflowEdit;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDepVersions;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.InventoryMeta;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.JSDeploymentState;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.SetVersionFilter;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.joc.publish.mapper.UpDownloadMapper;
import com.sos.joc.publish.util.PublishUtils;

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
        return getFilteredInventoryConfigurationsByPaths(filter.getJsObjectPaths());
    }

    public List<DBItemInventoryConfiguration> getFilteredInventoryConfigurationsForSetVersion(SetVersionFilter filter)
            throws DBConnectionRefusedException, DBInvalidDataException {
        return getFilteredInventoryConfigurationsByPaths(filter.getJsObjects());
    }

    public List<DBItemInventoryConfiguration> getFilteredInventoryConfigurationsByPaths(List<String> paths) throws DBConnectionRefusedException,
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

    public List<DBItemInventoryConfiguration> getFilteredInventoryConfigurationsByIds(Collection<Long> configurationIds)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (!configurationIds.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
                sql.append(" where id in (:ids)");
                Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
                query.setParameterList("ids", configurationIds);
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

    public List<DBItemDeploymentHistory> getFilteredDeploymentHistory(Collection<Long> deployIds)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (deployIds != null && !deployIds.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
                sql.append(" where id in (:ids)");
                Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
                query.setParameterList("ids", deployIds);
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
            case LOCK:
            default:
                throw new JocNotImplementedException();
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

    public List<DBItemInventoryJSInstance> getAllControllers() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_INV_JS_INSTANCES);
        Query<DBItemInventoryJSInstance> query = session.createQuery(hql.toString());
        return session.getResultList(query);
    }
    
    public List<DBItemInventoryJSInstance> getControllers(Collection<String> controllerIds) throws SOSHibernateException {
        if (controllerIds != null) {
            StringBuilder hql = new StringBuilder(" from ");
            hql.append(DBLayer.DBITEM_INV_JS_INSTANCES);
            hql.append(" where schedulerId in (:controllerIds)");
            Query<DBItemInventoryJSInstance> query = session.createQuery(hql.toString());
            query.setParameterList("controllerIds", controllerIds);
            return session.getResultList(query);
        } else {
            return new ArrayList<DBItemInventoryJSInstance>();
        }
    }
    
    public DBItemInventoryJSInstance getController(String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_INV_JS_INSTANCES);
        hql.append(" where schedulerId = :controllerId");
        Query<DBItemInventoryJSInstance> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        return session.getResultList(query).get(0);
    }
    
    public Long getActiveClusterControllerDBItemId(String clusterUri) throws SOSHibernateException {
            StringBuilder hql = new StringBuilder("select id from ");
            hql.append(DBLayer.DBITEM_INV_JS_INSTANCES);
            hql.append(" where clusterUri = :clusterUri");
            Query<Long> query = session.createQuery(hql.toString());
            query.setParameter("clusterUri", clusterUri);
            return session.getSingleResult(query);
    }
    
    public DBItemDeploymentHistory getLatestDepHistoryItem (DBItemInventoryConfiguration invConfig, DBItemInventoryJSInstance controller)
            throws SOSHibernateException {
        return getLatestDepHistoryItem(invConfig.getId(), controller.getSchedulerId());
    }
    
    public DBItemDeploymentHistory getLatestDepHistoryItem (DBItemInventoryConfiguration invConfig, String controllerId)
            throws SOSHibernateException {
        return getLatestDepHistoryItem(invConfig.getId(), controllerId);
    }
    
    public DBItemDeploymentHistory getLatestActiveDepHistoryItem (DBItemInventoryConfiguration invConfig, DBItemInventoryJSInstance controller)
            throws SOSHibernateException {
        return getLatestActiveDepHistoryItem(invConfig.getId(), controller.getSchedulerId());
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem (DBItemInventoryConfiguration invConfig, String controllerId)
            throws SOSHibernateException {
        return getLatestActiveDepHistoryItem(invConfig.getId(), controllerId);
    }

    public DBItemDeploymentHistory getLatestDepHistoryItem (Long configurationId, String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
        hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
        hql.append(" where dep.inventoryConfigurationId = :cid");
        hql.append(" and dep.controllerId = :controllerId").append(")");
        Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
        query.setParameter("cid", configurationId);
        query.setParameter("controllerId", controllerId);
        return session.getSingleResult(query);
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem (Long configurationId, String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
        hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
        hql.append(" where dep.inventoryConfigurationId = :cid");
        hql.append(" and dep.controllerId = :controllerId");
        hql.append(" and dep.operation = 0").append(")");
        Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
        query.setParameter("cid", configurationId);
        query.setParameter("controllerId", controllerId);
        return session.getSingleResult(query);
    }

    public Long getLatestDeploymentFromConfigurationId(Long configurationId, Long controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select max(id) from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(" where inventoryConfigurationId = :configurationId");
        hql.append(" and controllerId = :controllerId group by id");
        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("configurationId", configurationId);
        query.setParameter("controllerId", controllerId);
        return session.getSingleResult(query);
    }

    public List<Long> getLatestDeploymentFromConfigurationId(Set<Long> configurationIds, String controllerId) throws SOSHibernateException {
        if (configurationIds != null && !configurationIds.isEmpty()) {
            StringBuilder hql = new StringBuilder("select max(id) from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where inventoryConfigurationId in (:configurationIds)");
            hql.append(" and controllerId = :controllerId group by id");
            Query<Long> query = session.createQuery(hql.toString());
            query.setParameter("configurationIds", configurationIds);
            query.setParameter("controllerId", controllerId);
            return session.getResultList(query);
        } else {
            return new ArrayList<Long>();
        }
    }
        
    public void updateFailedDeploymentForUpdate(
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables, 
            String controllerId, String account, String versionId) throws SOSHibernateException {
        for (DBItemInventoryConfiguration inventoryConfig : verifiedConfigurations.keySet()) {
            DBItemDeploymentHistory newDepHistoryItem = new DBItemDeploymentHistory();
            newDepHistoryItem.setAccount(account);
            newDepHistoryItem.setCommitId(versionId);
            newDepHistoryItem.setContent(inventoryConfig.getContent());
            Long controllerInstanceId = 0L;
            try {
                controllerInstanceId = getController(controllerId).getId();
            } catch (SOSHibernateException e) {
                continue;
            }
            newDepHistoryItem.setControllerInstanceId(controllerInstanceId);
            newDepHistoryItem.setControllerId(controllerId);
            newDepHistoryItem.setDeletedDate(null);
            newDepHistoryItem.setDeploymentDate(Date.from(Instant.now()));
            newDepHistoryItem.setInventoryConfigurationId(inventoryConfig.getId());
            DeployType deployType = PublishUtils.mapInventoryMetaConfigurationType(
                    InventoryMeta.ConfigurationType.fromValue(inventoryConfig.getType()));
            newDepHistoryItem.setObjectType(deployType.ordinal());
            newDepHistoryItem.setOperation(OperationType.UPDATE.value());
            newDepHistoryItem.setState(JSDeploymentState.NOT_DEPLOYED.value());
            newDepHistoryItem.setPath(inventoryConfig.getPath());
            newDepHistoryItem.setFolder(inventoryConfig.getFolder());
            newDepHistoryItem.setSignedContent(verifiedConfigurations.get(inventoryConfig).getSignature());
            // TODO: get Version to set here
            newDepHistoryItem.setVersion(null);
            session.save(newDepHistoryItem);
        }
        for (DBItemDeploymentHistory deploy : verifiedReDeployables.keySet()) {
            deploy.setSignedContent(verifiedReDeployables.get(deploy).getSignature());
            deploy.setId(null);
            deploy.setCommitId(versionId);
            deploy.setAccount(account);
            Long controllerInstanceId = 0L;
            try {
                controllerInstanceId = getController(controllerId).getId();
            } catch (SOSHibernateException e) {
                continue;
            }
            deploy.setControllerInstanceId(controllerInstanceId);
            deploy.setControllerId(controllerId);
            deploy.setState(JSDeploymentState.NOT_DEPLOYED.value());
            deploy.setDeploymentDate(Date.from(Instant.now()));
            // TODO: get Version to set here
            deploy.setVersion(null);
            session.save(deploy);
        }
    }
    
    public void updateFailedDeploymentForDelete(
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete, String controllerId, String account, String versionId)
                    throws SOSHibernateException {
        for (DBItemDeploymentHistory deploy : depHistoryDBItemsToDeployDelete) {
            deploy.setId(null);
            deploy.setAccount(account);
            deploy.setCommitId(versionId);
            deploy.setControllerId(controllerId);
            Long controllerInstanceId = 0L;
            try {
                controllerInstanceId = getController(controllerId).getId();
            } catch (SOSHibernateException e) {
                continue;
            }
            deploy.setControllerInstanceId(controllerInstanceId);
            deploy.setDeletedDate(Date.from(Instant.now()));
            deploy.setDeploymentDate(Date.from(Instant.now()));
            deploy.setOperation(OperationType.DELETE.value());
            deploy.setState(JSDeploymentState.NOT_DEPLOYED.value());
            // TODO: get Version to set here
            session.save(deploy);
        }
    }
    
    public DBItemDeploymentHistory getDeployedInventory(String controllerId, Integer objectType, String path) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select a.* from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as a inner join (");
            sql.append(" select controllerId, objectType, path, max(id) as id from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" group by controllerId, objectType, path ) as b on a.id = b.id");
            sql.append(" where a.operation = 0");
            sql.append(" and a.controllerId = :controllerId");
            sql.append(" and a.objectType = :objectType");
            sql.append(" and a.path = :path");
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("objectType", objectType);
            query.setParameter("path", path);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    

}