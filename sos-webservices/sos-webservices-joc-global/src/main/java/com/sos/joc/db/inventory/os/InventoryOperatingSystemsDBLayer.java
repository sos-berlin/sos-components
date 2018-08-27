package com.sos.joc.db.inventory.os;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.inventory.InventoryDBItemConstants;
import com.sos.jobscheduler.db.os.DBItemInventoryOperatingSystem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class InventoryOperatingSystemsDBLayer {
	
	private SOSHibernateSession session;

    public InventoryOperatingSystemsDBLayer(SOSHibernateSession connection) {
        this.session = connection;
    }

    public DBItemInventoryOperatingSystem getInventoryOperatingSystem(Long osId)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(InventoryDBItemConstants.DBITEM_INVENTORY_OPERATING_SYSTEMS);
            sql.append(" where id = :id");
            Query<DBItemInventoryOperatingSystem> query = session.createQuery(sql.toString());
            query.setParameter("id", osId);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}