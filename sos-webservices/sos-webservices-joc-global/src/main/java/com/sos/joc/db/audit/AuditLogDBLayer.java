package com.sos.joc.db.audit;

import java.util.List;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class AuditLogDBLayer {

	private SOSHibernateSession session;

	public AuditLogDBLayer(SOSHibernateSession connection) {
		this.session = connection;
	}

	private String getWhere(AuditLogDBFilter filter) {
		String where = "";
		String and = "";

		if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
			where += and + " schedulerId = :schedulerId";
			and = " and ";
		} else {
			where += " schedulerId != '-'";
			and = " and ";
		}

		if (filter.getCreatedFrom() != null) {
			where += and + " created >= :from";
			and = " and ";
		}
		if (filter.getCreatedTo() != null) {
			where += and + " created < :to";
			and = " and ";
		}
		if (filter.getTicketLink() != null && !filter.getTicketLink().isEmpty()) {
			where += and + String.format(" ticketLink %s :ticketLink",
					SearchStringHelper.getSearchOperator(filter.getTicketLink()));
			and = " and ";
		}
		if (filter.getAccount() != null && !filter.getAccount().isEmpty()) {
			where += and
					+ String.format(" account %s :account", SearchStringHelper.getSearchOperator(filter.getAccount()));
			and = " and ";
		}
		if (filter.getReason() != null && !filter.getReason().isEmpty()) {
			where += and
					+ String.format(" comment %s :comment", SearchStringHelper.getSearchOperator(filter.getReason()));
			and = " and ";
		}

		if (filter.getListOfJobs() != null && !filter.getListOfJobs().isEmpty()) {
			where += and + SearchStringHelper.getStringListPathSql(filter.getListOfJobs(), "job");
			and = " and ";
		}

		if (filter.getListOfFolders() != null && !filter.getListOfFolders().isEmpty()) {
			where += and + SearchStringHelper.getStringListPathSql(filter.getStringListOfFolders(), "folder");
			and = " and ";
		}

		if (filter.getListOfCalendars() != null && !filter.getListOfCalendars().isEmpty()) {
			where += and + SearchStringHelper.getStringListPathSql(filter.getListOfCalendars(), "calendar");
			and = " and ";
		}

		if (filter.getListOfOrders() != null && !filter.getListOfOrders().isEmpty()) {
			where += and;
			for (int i = 0; i < filter.getListOfOrders().size(); i++) {
				if (i == 0) {
					where += " (";
				} else if (i != 0 && i != filter.getListOfOrders().size()) {
					where += " or";
				}
				String workflow = filter.getListOfOrders().get(i).getWorkflow();
				String orderId = filter.getListOfOrders().get(i).getOrderId();
				where += String.format(" (workflow %s '%s'", SearchStringHelper.getSearchPathOperator(workflow),
						SearchStringHelper.getSearchPathValue(workflow));

				if (filter.getListOfOrders().get(i).getOrderId() != null
						&& !filter.getListOfOrders().get(i).getOrderId().isEmpty()) {
					where += String.format(" and orderId %s '%s'", SearchStringHelper.getSearchPathOperator(orderId),
							SearchStringHelper.getSearchPathValue(orderId));
				}
				where += ")";
				if (i == filter.getListOfOrders().size() - 1) {
					where += ")";
				}
			}
			and = " and ";
		}

		if (!"".equals(where.trim())) {
			where = " where " + where;
		} else {
			where = " ";
		}

		return where;
	}

	private void bindParameters(Query<DBItemJocAuditLog> query, AuditLogDBFilter filter) {
		if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
			query.setParameter("schedulerId", filter.getSchedulerId());
		}
		if (filter.getCreatedFrom() != null) {
			query.setParameter("from", filter.getCreatedFrom(), TemporalType.TIMESTAMP);
		}
		if (filter.getCreatedTo() != null) {
			query.setParameter("to", filter.getCreatedTo(), TemporalType.TIMESTAMP);
		}
		if (filter.getTicketLink() != null && !filter.getTicketLink().isEmpty()) {
			query.setParameter("ticketLink", filter.getTicketLink());
		}
		if (filter.getAccount() != null && !filter.getAccount().isEmpty()) {
			query.setParameter("account", filter.getAccount());
		}
		if (filter.getReason() != null && !filter.getReason().isEmpty()) {
			query.setParameter("comment", filter.getReason());
		}
	}

	public List<DBItemJocAuditLog> getAuditLogs(AuditLogDBFilter auditLogDBFilter, Integer limit)
			throws DBConnectionRefusedException, DBInvalidDataException {
		try {

			Query<DBItemJocAuditLog> query = session.createQuery(" from " + DBLayer.DBITEM_JOC_AUDIT_LOG
					+ getWhere(auditLogDBFilter) + " order by created desc");

			bindParameters(query, auditLogDBFilter);

			if (limit != null) {
				query.setMaxResults(limit);
			}
			return session.getResultList(query);
		} catch (SOSHibernateInvalidSessionException ex) {
			throw new DBConnectionRefusedException(ex);
		} catch (Exception ex) {
			throw new DBInvalidDataException(ex);
		}
	}

	public List<DBItemJocAuditLog> getAuditLogs(AuditLogDBFilter auditLogDBFilter)
			throws DBConnectionRefusedException, DBInvalidDataException {
		return getAuditLogs(auditLogDBFilter, null);
	}

}
