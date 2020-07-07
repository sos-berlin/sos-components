package com.sos.joc.db.inventory.os;

import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class InventoryOperatingSystemsDBLayer {
	
	private SOSHibernateSession session;

    public InventoryOperatingSystemsDBLayer(SOSHibernateSession connection) {
        this.session = connection;
    }

    public DBItemInventoryOperatingSystem getInventoryOperatingSystem(Long osId) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
        	if (osId == null || osId == 0L) {
        		return null;
        	}
        	return session.get(DBItemInventoryOperatingSystem.class, osId);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemInventoryOperatingSystem getOSItem(String hostname)
			throws DBConnectionRefusedException, DBInvalidDataException {
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("from ").append(DBLayer.DBITEM_INV_OPERATING_SYSTEMS);
			sql.append(" where hostname = :hostname");
			Query<DBItemInventoryOperatingSystem> query = session.createQuery(sql.toString());
			query.setParameter("hostname", hostname);
			return session.getSingleResult(query);
		} catch (SOSHibernateInvalidSessionException ex) {
			throw new DBConnectionRefusedException(ex);
		} catch (Exception ex) {
			throw new DBInvalidDataException(ex);
		}
	}
    
    public List<DBItemInventoryOperatingSystem> getOSItems(Collection<Long> ids)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (ids == null || ids.isEmpty()) {
                return null;
            }
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_OPERATING_SYSTEMS);
            sql.append(" where id in (:ids)");
            Query<DBItemInventoryOperatingSystem> query = session.createQuery(sql.toString());
            query.setParameter("ids", ids);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
	
	public Long saveOrUpdateOSItem(DBItemInventoryOperatingSystem osItem) throws DBConnectionRefusedException, DBInvalidDataException {
		try {
			if (osItem == null || osItem.getHostname() == null || osItem.getHostname().isEmpty()) {
				return 0L;
			}
			DBItemInventoryOperatingSystem oldOsItem = getOSItem(osItem.getHostname());
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
	
	public void deleteOSItem(DBItemInventoryOperatingSystem osItem) throws DBConnectionRefusedException, DBInvalidDataException {
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