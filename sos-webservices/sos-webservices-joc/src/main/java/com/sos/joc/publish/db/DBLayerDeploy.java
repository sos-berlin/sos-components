package com.sos.joc.publish.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.inventory.DBItemJSConfiguration;
import com.sos.jobscheduler.db.inventory.DBItemJSDraftObject;
import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.jobscheduler.db.pgp.DBItemJSKeys;
import com.sos.jobscheduler.model.agent.AgentRefEdit;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.jobscheduler.model.workflow.WorkflowEdit;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.publish.common.JSObjectFileExtension;

public class DBLayerDeploy {

	private SOSHibernateSession session;

    public DBLayerDeploy(SOSHibernateSession connection) {
    	session = connection;
    }
    
    public SOSHibernateSession getSession() {
    	return session;
    }

    public DBItemJSConfiguration getConfiguration(String schedulerId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_JS_CONFIGURATION);
            sql.append(" where schedulerId = :schedulerId");
            Query<DBItemJSConfiguration> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemJSObject> getJobSchedulerObjects(String schedulerId, Long configurationId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_JS_CONFIGURATION_MAPPING);
            sql.append(" where schedulerId = :schedulerId");
            sql.append(" and configurationId = :configurationId");
            Query<DBItemJSObject> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            query.setParameter("configurationId", configurationId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemJSObject> getJobSchedulerObjectsByConfiguration(String schedulerId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select mapping from ").append(DBLayer.DBITEM_JS_CONFIGURATION).append("as conf, ");
            sql.append(DBLayer.DBITEM_JS_CONFIGURATION_MAPPING).append("as mapping");
            sql.append(" where conf.schedulerId = :schedulerId");
            sql.append(" and mapping.configurationId = conf.id");
            Query<DBItemJSObject> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemJSDraftObject> getAllJobSchedulerDraftObjects() throws DBConnectionRefusedException, DBInvalidDataException {
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

    public List<DBItemJSDraftObject> getFilteredJobSchedulerDraftObjects(ExportFilter filter)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_JS_DRAFT_OBJECTS);
            sql.append(" where path in :paths");
            Query<DBItemJSDraftObject> query = session.createQuery(sql.toString());
            query.setParameter("paths", filter.getJsObjectPaths());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemJSObject> getAllJobSchedulerDeployedObjects() throws DBConnectionRefusedException, DBInvalidDataException {
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

    public List<DBItemJSObject> getFilteredJobSchedulerDeployedObjects(ExportFilter filter) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_JS_OBJECTS);
            sql.append(" where path in :paths");
            Query<DBItemJSObject> query = session.createQuery(sql.toString());
            query.setParameter("paths", filter.getJsObjectPaths());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemJSObject getJSObject(String schedulerId, String path, String objectType) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_JS_OBJECTS);
            sql.append(" where ");
            sql.append(" schedulerId = :schedulerId");
            sql.append(" and path = :path");
            sql.append(" and objectType = :objectType");
            Query<DBItemJSObject> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            query.setParameter("path", path);
            query.setParameter("objectType", objectType);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void saveOrUpdateJSDraftObject (String path, JSObject jsObject, String type, String account)
            throws SOSHibernateException, JsonProcessingException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_JS_DRAFT_OBJECTS);
        hql.append(" where path = :path");
        Query<DBItemJSDraftObject> query = session.createQuery(hql.toString());
        query.setParameter("path", path);
        DBItemJSDraftObject existingJsObject =  session.getSingleResult(query);
        Path folderPath = null;
        if (existingJsObject != null) {
            existingJsObject.setSchedulerId("");
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
            newJsObject.setSchedulerId("");
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

}
