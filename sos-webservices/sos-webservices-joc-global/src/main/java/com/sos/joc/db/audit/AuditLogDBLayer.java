package com.sos.joc.db.audit;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.audit.CategoryType;

public class AuditLogDBLayer {

	private SOSHibernateSession session;

	public AuditLogDBLayer(SOSHibernateSession connection) {
		this.session = connection;
	}
	
    public DBItemJocAuditLog getDBItemJocAuditLog(Long id) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            return session.get(DBItemJocAuditLog.class, id);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public ScrollableResults getAuditLogs(AuditLogDBFilter auditLogDBFilter, Integer limit) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {

            Query<DBItemJocAuditLog> query = session.createQuery(" from " + DBLayer.DBITEM_JOC_AUDIT_LOG + getWhere(auditLogDBFilter)
                    + " order by created desc");

            bindParameters(query, auditLogDBFilter);

            if (limit != null) {
                query.setMaxResults(limit);
            }
            return session.scroll(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public ScrollableResults getAuditLogs(AuditLogDBFilter auditLogDBFilter) throws DBConnectionRefusedException, DBInvalidDataException {
        return getAuditLogs(auditLogDBFilter, null);
    }

	private String getWhere(AuditLogDBFilter filter) {
		Set<String> clause = new LinkedHashSet<>();

		if (!filter.getControllerIds().isEmpty()) {
		    if (filter.getControllerIds().size() == 1) {
		        clause.add("controllerId = :controllerId");
		    } else {
		        clause.add("controllerId in (:controllerIds)");
		    }
		}
		if (!filter.getCategories().isEmpty()) {
            if (filter.getCategories().size() == 1) {
                clause.add("category = :category");
            } else {
                clause.add("category in (:categories)");
            }
        }
		if (filter.getCreatedFrom() != null) {
		    clause.add("created >= :from");
		}
		if (filter.getCreatedTo() != null) {
		    clause.add("created < :to");
		}
		if (filter.getTicketLink() != null && !filter.getTicketLink().isEmpty()) {
		    clause.add(String.format("ticketLink %s :ticketLink", SearchStringHelper.getSearchOperator(filter.getTicketLink())));
		}
		if (filter.getAccount() != null && !filter.getAccount().isEmpty()) {
		    clause.add(String.format("account %s :account", SearchStringHelper.getSearchOperator(filter.getAccount())));
		}
		if (filter.getReason() != null && !filter.getReason().isEmpty()) {
		    clause.add(String.format("comment %s :comment", SearchStringHelper.getSearchOperator(filter.getReason())));
		}
//        if (!filter.getFolders().isEmpty()) {
//            String folderClause = filter.getFolders().stream().map(folder -> {
//                if (folder.getRecursive()) {
//                    return "(folder = '" + folder.getFolder() + "' or folder like '" + (folder.getFolder() + "/%").replaceAll("//+", "/") + "')";
//                } else {
//                    return "folder = '" + folder.getFolder() + "'";
//                }
//            }).collect(Collectors.joining(" or "));
//            if (filter.getFolders().size() > 1) {
//                folderClause = "(" + folderClause + ")";
//            }
//            clause.add(folderClause);
//        }
		
		return clause.stream().collect(Collectors.joining(" and ", " where ", ""));
	}

	private void bindParameters(Query<DBItemJocAuditLog> query, AuditLogDBFilter filter) {
		if (!filter.getControllerIds().isEmpty()) {
		    if (filter.getControllerIds().size() == 1) {
		        query.setParameter("controllerId", filter.getControllerIds().iterator().next());
		    } else {
		        query.setParameterList("controllerIds", filter.getControllerIds());
		    }
		}
		if (!filter.getCategories().isEmpty()) {
            if (filter.getCategories().size() == 1) {
                query.setParameter("category", filter.getCategories().iterator().next().intValue());
            } else {
                query.setParameterList("categories", filter.getCategories().stream().map(CategoryType::intValue).collect(Collectors.toList()));
            }
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

}
