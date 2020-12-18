package com.sos.joc.db.master;

import java.util.Date;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class HistoryMastersDBLayer {

	private SOSHibernateSession session;

	public HistoryMastersDBLayer(SOSHibernateSession connection) {
		this.session = connection;
	}

	public DBItemHistoryController getLastHistoryItem(String controllerId, String hostname, Integer port, Date startedAt)
			throws DBConnectionRefusedException, DBInvalidDataException {
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("from ").append(DBLayer.DBITEM_HISTORY_CONTROLLER);
			sql.append(" where controllerId = :controllerId");
			sql.append(" and hostname = :hostname");
			sql.append(" and port = :port");
			if (startedAt != null) {
				sql.append(" and startedAt >= :startedAt");
			}
			sql.append(" order by id desc");
			Query<DBItemHistoryController> query = session.createQuery(sql.toString());
			query.setMaxResults(1);
			query.setParameter("controllerId", controllerId);
			query.setParameter("hostname", hostname);
			query.setParameter("port", port);
			if (startedAt != null) {
				query.setParameter("startedAt", startedAt, TemporalType.TIMESTAMP);
			}
			return session.getSingleResult(query);
		} catch (SOSHibernateInvalidSessionException ex) {
			throw new DBConnectionRefusedException(ex);
		} catch (Exception ex) {
			throw new DBInvalidDataException(ex);
		}
	}

}
