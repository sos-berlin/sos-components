package com.sos.joc.db.inventory.os;

import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class InventoryOperatingSystemsDBLayer {

    private SOSHibernateSession session;
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryOperatingSystemsDBLayer.class);

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

    public DBItemInventoryOperatingSystem getOSItem(String hostname) throws DBConnectionRefusedException, DBInvalidDataException {
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

    public List<DBItemInventoryOperatingSystem> getOSItems(Collection<Long> ids) throws DBConnectionRefusedException, DBInvalidDataException {
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
        boolean isAutoCommit = session.isAutoCommit();
        // if isAutoCommit==false then the caller takes care of the transaction
        if (osItem == null || osItem.getHostname() == null || osItem.getHostname().isEmpty()) {
            return 0L;
        }
        try {
            if (isAutoCommit) {
                session.setAutoCommit(false);
                LOGGER.info("change autocommit to false");
                session.beginTransaction();
            }
            DBItemInventoryOperatingSystem oldOsItem = getOSItem(osItem.getHostname());
            if (oldOsItem == null) {
                DBItemInventoryOperatingSystem newItem = new DBItemInventoryOperatingSystem();
                newItem.setId(null);
                newItem.setArchitecture(osItem.getArchitecture());
                newItem.setDistribution(osItem.getDistribution());
                newItem.setHostname(osItem.getHostname());
                newItem.setName(osItem.getName());
                newItem.setModified(Date.from(Instant.now()));
                session.save(newItem);
                if (isAutoCommit) {
                    Globals.commit(session);
                }
                return newItem.getId();
            } else {
                EqualsBuilder eb = new EqualsBuilder();
                eb.append(oldOsItem.getArchitecture(), osItem.getArchitecture()).append(oldOsItem.getDistribution(), osItem.getDistribution()).append(
                        oldOsItem.getName(), osItem.getName());
                if (!eb.isEquals()) {
                    oldOsItem.setArchitecture(osItem.getArchitecture());
                    oldOsItem.setDistribution(osItem.getDistribution());
                    oldOsItem.setName(osItem.getName());
                    oldOsItem.setModified(Date.from(Instant.now()));
                    session.update(oldOsItem);
                }
                if (isAutoCommit) {
                    Globals.commit(session);
                }
                return oldOsItem.getId();
            }
        } catch (Exception ex) {
            if (isAutoCommit) {
                Globals.rollback(session);
            }
            LOGGER.warn(ex.toString());
            return 0L;
        } finally {
            if (isAutoCommit) {
                try {
                    session.setAutoCommit(true);
                } catch (SOSHibernateException e) {
                    //
                }
            }
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