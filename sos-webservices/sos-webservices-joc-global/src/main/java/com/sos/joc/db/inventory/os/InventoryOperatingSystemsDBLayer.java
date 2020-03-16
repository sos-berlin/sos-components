package com.sos.joc.db.inventory.os;

import java.sql.Date;
import java.time.Instant;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class InventoryOperatingSystemsDBLayer {
	
	private SOSHibernateSession session;

    public InventoryOperatingSystemsDBLayer(SOSHibernateSession connection) {
        this.session = connection;
    }

    public DBItemOperatingSystem getInventoryOperatingSystem(Long osId) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
        	if (osId == null || osId == 0L) {
        		return null;
        	}
        	return session.get(DBItemOperatingSystem.class, osId);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemOperatingSystem getOSItem(String hostname)
			throws DBConnectionRefusedException, DBInvalidDataException {
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("from ").append(DBLayer.DBITEM_OPERATING_SYSTEMS);
			sql.append(" where hostname = :hostname");
			Query<DBItemOperatingSystem> query = session.createQuery(sql.toString());
			query.setParameter("hostname", hostname);
			return session.getSingleResult(query);
		} catch (SOSHibernateInvalidSessionException ex) {
			throw new DBConnectionRefusedException(ex);
		} catch (Exception ex) {
			throw new DBInvalidDataException(ex);
		}
	}
	
	public Long saveOrUpdateOSItem(DBItemOperatingSystem osItem) throws DBConnectionRefusedException, DBInvalidDataException {
		try {
			if (osItem == null || osItem.getHostname() == null || osItem.getHostname().isEmpty()) {
				return 0L;
			}
			DBItemOperatingSystem oldOsItem = getOSItem(osItem.getHostname());
			if (oldOsItem == null) {
				osItem.setModified(Date.from(Instant.now()));
				session.save(osItem);
				return osItem.getId();
			} else {
				EqualsBuilder eb = new EqualsBuilder();
				eb.append(oldOsItem.getArchitecture(), osItem.getArchitecture())
					.append(oldOsItem.getDistribution(), osItem.getDistribution())
					.append(oldOsItem.getName(), osItem.getName());
				if(!eb.isEquals()) {
					osItem.setId(oldOsItem.getId());
					osItem.setModified(Date.from(Instant.now()));
					session.update(osItem);
				}
				return oldOsItem.getId();
			}
		} catch (SOSHibernateInvalidSessionException ex) {
			throw new DBConnectionRefusedException(ex);
		} catch (Exception ex) {
			throw new DBInvalidDataException(ex);
		}
	}
	
	public void deleteOSItem(DBItemOperatingSystem osItem) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (osItem != null) {
                session.delete(osItem);
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}