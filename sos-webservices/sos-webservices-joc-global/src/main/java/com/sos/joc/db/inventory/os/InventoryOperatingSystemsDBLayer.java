package com.sos.joc.db.inventory.os;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class InventoryOperatingSystemsDBLayer {
	
	private SOSHibernateSession session;

    public InventoryOperatingSystemsDBLayer(SOSHibernateSession connection) {
        this.session = connection;
    }

    public DBItemOperatingSystem getInventoryOperatingSystem(Long osId)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
        	return session.get(DBItemOperatingSystem.class, osId);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}