package com.sos.joc.db.master;

import java.util.Date;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.history.DBItemMaster;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class HistoryMastersDBLayer {

	private SOSHibernateSession session;

	public HistoryMastersDBLayer(SOSHibernateSession connection) {
		this.session = connection;
	}

	public DBItemMaster getLastHistoryItem(String schedulerId, String hostname, Integer port, Date startedAt)
			throws DBConnectionRefusedException, DBInvalidDataException {
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("from ").append(DBLayer.HISTORY_DBITEM_MASTER);
			sql.append(" where masterId = :schedulerId");
			sql.append(" and hostname = :hostname");
			sql.append(" and port = :port");
			if (startedAt != null) {
				sql.append(" and startedAt >= :startedAt");
			}
			sql.append(" order by id desc");
			Query<DBItemMaster> query = session.createQuery(sql.toString());
			query.setMaxResults(1);
			query.setParameter("schedulerId", schedulerId);
			query.setParameter("hostname", hostname);
			query.setParameter("port", port);
			if (startedAt != null) {
				query.setParameter("startedAt", startedAt, TemporalType.TIMESTAMP);
			}
			return query.getSingleResult();
		} catch (SOSHibernateInvalidSessionException ex) {
			throw new DBConnectionRefusedException(ex);
		} catch (Exception ex) {
			throw new DBInvalidDataException(ex);
		}
	}

}
