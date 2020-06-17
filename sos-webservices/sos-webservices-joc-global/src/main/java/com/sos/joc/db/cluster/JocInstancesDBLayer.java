package com.sos.joc.db.cluster;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.joc.DBItemJocCluster;
import com.sos.jobscheduler.db.joc.DBItemJocInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class JocInstancesDBLayer {

    private SOSHibernateSession session;

    public JocInstancesDBLayer(SOSHibernateSession conn) {
        this.session = conn;
    }

    public List<DBItemJocInstance> getInstances() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<DBItemJocInstance> query = session.createQuery("from " + DBLayer.DBITEM_JOC_INSTANCES);
            return session.getResultList(query);
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

}