package com.sos.joc.db.cluster;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class JocInstancesDBLayer {

    private SOSHibernateSession session;

    public JocInstancesDBLayer(SOSHibernateSession conn) {
        this.session = conn;
    }

    public List<DBItemJocInstance> getInstances() throws DBConnectionRefusedException, DBInvalidDataException {
        return getInstances(null, "ordering");
    }
    
    public List<DBItemJocInstance> getJocInstances() throws DBConnectionRefusedException, DBInvalidDataException {
        return getInstances(false, "ordering");
    }
    
    public List<DBItemJocInstance> getApiInstances() throws DBConnectionRefusedException, DBInvalidDataException {
        return getInstances(true, "ordering");
    }
    
    public List<DBItemJocInstance> getInstances(String orderBy) throws DBConnectionRefusedException, DBInvalidDataException {
        return getInstances(null, orderBy);
    }
    
    public List<DBItemJocInstance> getInstances(Boolean apiServer, String orderBy) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            String where = (apiServer == null) ? "" : " where apiServer = :apiServer";
            if (orderBy == null) {
                orderBy  = "";
            }
            if (!orderBy.isEmpty()) {
                orderBy = " order by " + orderBy;
            }
            Query<DBItemJocInstance> query = session.createQuery("from " + DBLayer.DBITEM_JOC_INSTANCES + where + orderBy);
            if (apiServer != null) {
                query.setParameter("apiServer", apiServer.booleanValue());
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemJocInstance getInstance(String memberId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<DBItemJocInstance> query = session.createQuery("from " + DBLayer.DBITEM_JOC_INSTANCES + " where memberId = :memberId");
            query.setParameter("memberId", memberId);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemJocCluster getCluster() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<DBItemJocCluster> query = session.createQuery("from " + DBLayer.DBITEM_JOC_CLUSTER);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemJocInstance getActiveInstance() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select i from ").append(DBLayer.DBITEM_JOC_INSTANCES).append(" i inner join ").append(
                    DBLayer.DBITEM_JOC_CLUSTER).append(" c on i.memberId = c.memberId");
            Query<DBItemJocInstance> query = session.createQuery(hql.toString());
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public void update(DBItemJocInstance item) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            session.update(item);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}