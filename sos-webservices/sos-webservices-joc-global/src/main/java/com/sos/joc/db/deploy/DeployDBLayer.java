package com.sos.joc.db.deploy;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.documentation.DBItemDocumentation;
import com.sos.jobscheduler.db.inventory.DBItemJSConfiguration;
import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.deploy.LoadableObject;

public class DeployDBLayer {

	private SOSHibernateSession session;

    public DeployDBLayer(SOSHibernateSession connection) {
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

    public List<DBItemJSObject> getJSObjects(String schedulerId, List<LoadableObject> toLoad) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_JS_OBJECTS);
            sql.append("where ");
            sql.append(" schedulerId = :schedulerId");
            sql.append(" and (");
            boolean init = true;
            for (LoadableObject objectToLoad : toLoad) {
            	if (!init) {
            		sql.append(" or ");
            	} else {
            		init = false;
            	}
            	sql.append(" (path is '").append(objectToLoad.getPath())
            		.append("' and ").append("objectType is '").append(objectToLoad.getObjectType()).append("')");
            	
            }
            sql.append(")");
            Query<DBItemJSObject> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}
