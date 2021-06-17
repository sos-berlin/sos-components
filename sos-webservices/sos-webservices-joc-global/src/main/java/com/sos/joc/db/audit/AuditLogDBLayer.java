package com.sos.joc.db.audit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.sos.joc.model.audit.AuditLogDetailItem;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.audit.ObjectType;
import com.sos.joc.model.common.Folder;

public class AuditLogDBLayer {

	private SOSHibernateSession session;

	public AuditLogDBLayer(SOSHibernateSession connection) {
		this.session = connection;
	}
	
    public DBItemJocAuditLog getAuditLog(Long id) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (id != null && id > 0L) {
                return session.get(DBItemJocAuditLog.class, id);
            }
            return null;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public ScrollableResults getAuditLogs(AuditLogDBFilter auditLogDBFilter, boolean withDeploymentJoin, Integer limit) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(AuditLogDBItem.class.getName());
            if (withDeploymentJoin) {
                hql.append("(al, dh.commitId) from ");
                hql.append(DBLayer.DBITEM_JOC_AUDIT_LOG).append(" al left join ");
                hql.append(DBLayer.DBITEM_DEP_HISTORY).append(" dh on al.id=dh.auditlogId ");
            } else {
                hql.append("(al) from ");
                hql.append(DBLayer.DBITEM_JOC_AUDIT_LOG).append(" al ");
            }
            hql.append(getWhere(auditLogDBFilter, "al."));
            if (withDeploymentJoin) {
                hql.append(" group by al, dh.commitId");
            }
            hql.append(" order by al.id desc");
            Query<AuditLogDBItem> query = session.createQuery(hql.toString());

            bindParameters(query, auditLogDBFilter);

            if (limit != null) {
                query.setMaxResults(limit);
            }
            return query.scroll();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<AuditLogDetailItem> getDetails(Long auditLogId) {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(AuditLogDBDetailItem.class.getName());
            hql.append("(path, type, orderId) from ");
            hql.append(DBLayer.DBITEM_JOC_AUDIT_LOG_DETAILS);
            hql.append(" where auditLogId = :auditLogId");
            Query<AuditLogDBDetailItem> query = session.createQuery(hql.toString());
            query.setParameter("auditLogId", auditLogId);
            List<AuditLogDBDetailItem> result = session.getResultList(query);
            if (result != null) {
                return result.stream().map(AuditLogDetailItem.class::cast).collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Stream<Long> getAuditlogIds(List<Folder> folders, Set<ObjectType> objectTypes, String objectName) {
        boolean hasOrderType = objectTypes == null || objectTypes.isEmpty() || objectTypes.contains(ObjectType.ORDER);
        try {
            StringBuilder hql = new StringBuilder("select auditLogId from ");
            hql.append(DBLayer.DBITEM_JOC_AUDIT_LOG_DETAILS);
            hql.append(getWhere(folders, objectTypes, objectName, hasOrderType));
            hql.append(" group by auditLogId");
            Query<Long> query = session.createQuery(hql.toString());
            if (objectTypes != null && !objectTypes.isEmpty()) {
                if (objectTypes.size() == 1) {
                    query.setParameter("type", objectTypes.iterator().next().intValue());
                } else {
                    query.setParameterList("types", objectTypes.stream().map(ObjectType::intValue).collect(Collectors.toSet()));
                }
            }
            if (objectName != null && !objectName.isEmpty()) {
                if (SearchStringHelper.isGlobPattern(objectName)) {
                    query.setParameter("name", SearchStringHelper.globToSqlPattern(objectName));
                } else {
                    query.setParameter("name", objectName);
                }
            }
            List<Long> result = session.getResultList(query);
            if (result == null) {
                return Stream.empty();
            }
            return result.stream().distinct();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<AuditLogDetailItem> getDeploymentDetails(Long auditLogId, Collection<String> controllerIds) {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(AuditLogDBDetailItem.class.getName());
            hql.append("(path, type) from ");
            hql.append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where auditlogId = :auditLogId");
            if (!controllerIds.isEmpty()) {
                hql.append(" and controller in (:controllerIds)"); 
            }
            Query<AuditLogDBDetailItem> query = session.createQuery(hql.toString());
            query.setParameter("auditLogId", auditLogId);
            if (!controllerIds.isEmpty()) {
                query.setParameterList("controllerIds", controllerIds);
            }
            List<AuditLogDBDetailItem> result = session.getResultList(query);
            if (result != null) {
                return result.stream().map(AuditLogDetailItem.class::cast).collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Stream<Long> getAuditlogIdsFromDepHistory(List<Folder> folders, Set<ObjectType> objectTypes, String objectName) {
        try {
            StringBuilder hql = new StringBuilder("select auditlogId from ");
            hql.append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(getWhere(folders, objectTypes, objectName, false));
            hql.append(" group by auditlogId");
            Query<Long> query = session.createQuery(hql.toString());
            if (objectTypes != null && !objectTypes.isEmpty()) {
                if (objectTypes.size() == 1) {
                    query.setParameter("type", objectTypes.iterator().next().intValue());
                } else {
                    query.setParameterList("types", objectTypes.stream().map(ObjectType::intValue).collect(Collectors.toSet()));
                }
            }
            if (objectName != null && !objectName.isEmpty()) {
                if (SearchStringHelper.isGlobPattern(objectName)) {
                    query.setParameter("name", SearchStringHelper.globToSqlPattern(objectName));
                } else {
                    query.setParameter("name", objectName);
                }
            }
            List<Long> result = session.getResultList(query);
            if (result == null) {
                return Stream.empty();
            }
            return result.stream().distinct();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

	private String getWhere(AuditLogDBFilter filter, String tableAlias) {
	    if (tableAlias == null) {
	        tableAlias = "";
	    }
		Set<String> clause = new LinkedHashSet<>();

		if (!filter.getAuditLogIds().isEmpty()) {
            if (filter.getAuditLogIds().size() == 1) {
                clause.add(tableAlias + "id = :auditLogId");
            } else {
                clause.add(tableAlias + "id in (:auditLogIds)");
            }
        }
		if (!filter.getControllerIds().isEmpty()) {
		    if (filter.getControllerIds().size() == 1) {
		        clause.add(tableAlias + "controllerId = :controllerId");
		    } else {
		        clause.add(tableAlias + "controllerId in (:controllerIds)");
		    }
		}
		if (!filter.getCategories().isEmpty()) {
            if (filter.getCategories().size() == 1) {
                clause.add(tableAlias + "category = :category");
            } else {
                clause.add(tableAlias + "category in (:categories)");
            }
        }
		if (filter.getCreatedFrom() != null) {
		    clause.add(tableAlias + "created >= :from");
		}
		if (filter.getCreatedTo() != null) {
		    clause.add(tableAlias + "created < :to");
		}
		if (filter.getTicketLink() != null && !filter.getTicketLink().isEmpty()) {
		    if (SearchStringHelper.isGlobPattern(filter.getTicketLink())) {
		        clause.add(tableAlias + "ticketLink like :ticketLink");
		    } else {
		        clause.add(tableAlias + "ticketLink = :ticketLink");
		    }
		}
		if (filter.getAccount() != null && !filter.getAccount().isEmpty()) {
		    if (SearchStringHelper.isGlobPattern(filter.getAccount())) {
                clause.add(tableAlias + "account like :account");
            } else {
                clause.add(tableAlias + "account = :account");
            }
		}
		if (filter.getReason() != null && !filter.getReason().isEmpty()) {
		    if (SearchStringHelper.isGlobPattern(filter.getReason())) {
                clause.add(tableAlias + "comment like :comment");
            } else {
                clause.add(tableAlias + "comment = :comment");
            }
		}
		
		if (!clause.isEmpty()) {
		    return clause.stream().collect(Collectors.joining(" and ", " where ", ""));
		}
		return "";
	}
	
	private String getWhere(List<Folder> folders, Set<ObjectType> objectTypes, String objectName, boolean hasOrderType) {
        Set<String> clause = new LinkedHashSet<>();

        if (folders != null && !folders.isEmpty()) {
            String folderClause = folders.stream().map(folder -> {
                if (folder.getRecursive()) {
                    return "(folder = '" + folder.getFolder() + "' or folder like '" + (folder.getFolder() + "/%").replaceAll("//+", "/") + "')";
                } else {
                    return "folder = '" + folder.getFolder() + "'";
                }
            }).collect(Collectors.joining(" or "));
            if (folders.size() > 1) {
                folderClause = "(" + folderClause + ")";
            }
            clause.add(folderClause);
        }
        
        if (objectTypes != null && !objectTypes.isEmpty()) {
            if (objectTypes.size() == 1) {
                clause.add("type = :type"); 
            } else {
                clause.add("type in (:types)");
            }
        }
        
        if (objectName != null && !objectName.isEmpty()) {
            if (hasOrderType) {
                if (SearchStringHelper.isGlobPattern(objectName)) {
                    clause.add("(name like :name or orderId like :name)");
                } else {
                    clause.add("(name = :name or orderId = :name)");
                }
            } else {
                if (SearchStringHelper.isGlobPattern(objectName)) {
                    clause.add("name like :name");
                } else {
                    clause.add("name = :name");
                }
            }
        }
        
        if (!clause.isEmpty()) {
            return clause.stream().collect(Collectors.joining(" and ", " where ", ""));
        }
        return "";
    }

	private void bindParameters(Query<AuditLogDBItem> query, AuditLogDBFilter filter) {
	    if (!filter.getAuditLogIds().isEmpty()) {
            if (filter.getAuditLogIds().size() == 1) {
                query.setParameter("auditLogId", filter.getAuditLogIds().iterator().next());
            } else {
                query.setParameterList("auditLogIds", filter.getAuditLogIds());
            }
        }
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
			if (SearchStringHelper.isGlobPattern(filter.getTicketLink())) {
                query.setParameter("ticketLink", SearchStringHelper.globToSqlPattern(filter.getTicketLink()));
            } else {
                query.setParameter("ticketLink", filter.getTicketLink());
            }
		}
		if (filter.getAccount() != null && !filter.getAccount().isEmpty()) {
			if (SearchStringHelper.isGlobPattern(filter.getAccount())) {
                query.setParameter("account", SearchStringHelper.globToSqlPattern(filter.getAccount()));
            } else {
                query.setParameter("account", filter.getAccount());
            }
		}
		if (filter.getReason() != null && !filter.getReason().isEmpty()) {
			if (SearchStringHelper.isGlobPattern(filter.getReason())) {
			    query.setParameter("comment", SearchStringHelper.globToSqlPattern(filter.getReason()));
            } else {
                query.setParameter("comment", filter.getReason());
            }
		}
	}

}
