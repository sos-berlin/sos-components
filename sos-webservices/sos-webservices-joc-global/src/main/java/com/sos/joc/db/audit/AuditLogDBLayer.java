package com.sos.joc.db.audit;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.audit.AuditLogDetailItem;
import com.sos.joc.model.audit.AuditLogFilter;
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

    public ScrollableResults getAuditLogs(AuditLogFilter filter, Collection<String> controllerIds, Collection<CategoryType> categories,
            boolean withDeployment) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            
            // advanced search with objects or folders
            boolean withFolders = filter.getFolders() != null && !filter.getFolders().isEmpty();
            boolean withObjectName = filter.getObjectName() != null && !filter.getObjectName().isEmpty();
            boolean withObjectTypes = filter.getObjectTypes() != null && !filter.getObjectTypes().isEmpty();
            boolean withAdvancedSearch = withFolders || withObjectName || withObjectTypes;
            
            boolean searchInDepHistory = withDeployment;
            if (withDeployment && withObjectTypes && !filter.getObjectTypes().stream().anyMatch(t -> !ObjectType.ORDER.equals(t))) {
                searchInDepHistory = false;
            }
            
            Date createdFrom = JobSchedulerDate.getDateFrom(filter.getDateFrom(), filter.getTimeZone());
            Date createdTo = JobSchedulerDate.getDateTo(filter.getDateTo(), filter.getTimeZone());

            StringBuilder hql = new StringBuilder("select ");
            hql.append("al.id as id ");
            hql.append(",al.account as account ");
            hql.append(",al.request as request ");
            hql.append(",al.created as created ");
            hql.append(",al.controllerId as controllerId ");
            hql.append(",al.category as category ");
            hql.append(",al.comment as comment ");
            hql.append(",al.parameters as parameters ");
            hql.append(",al.timeSpent as timeSpent ");
            hql.append(",al.ticketLink as ticketLink ");
            hql.append("from ").append(DBLayer.DBITEM_JOC_AUDIT_LOG).append(" al ");
            
            String tableAlias = "al.";
            Set<String> clause = new LinkedHashSet<>();

            if (!controllerIds.isEmpty()) {
                if (controllerIds.size() == 1) {
                    clause.add(tableAlias + "controllerId = :controllerId");
                } else {
                    clause.add(tableAlias + "controllerId in (:controllerIds)");
                }
            }
            if (!categories.isEmpty()) {
                if (categories.size() == 1) {
                    clause.add(tableAlias + "category = :category");
                } else {
                    clause.add(tableAlias + "category in (:categories)");
                }
            }
            if (withAdvancedSearch) {
                String idsClause = tableAlias + "id in (" + getAuditlogIdsSelectInAuditLogDetails(filter.getFolders(), filter.getObjectTypes(), filter
                        .getObjectName()) + ")";
                if (searchInDepHistory) {
                    idsClause = "(" + idsClause + " or " + tableAlias + "id in (" + getAuditlogIdsSelectInDepHistory(filter.getFolders(), filter
                            .getObjectTypes(), filter.getObjectName()) + "))";
                }
                clause.add(idsClause);
            }
            if (createdFrom != null) {
                clause.add(tableAlias + "created >= :from");
            }
            if (createdTo != null) {
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
            if (filter.getComment() != null && !filter.getComment().isEmpty()) {
                if (SearchStringHelper.isGlobPattern(filter.getComment())) {
                    clause.add(tableAlias + "comment like :comment");
                } else {
                    clause.add(tableAlias + "comment = :comment");
                }
            }
            
            if (!clause.isEmpty()) {
                hql.append(clause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }

            hql.append(" order by al.id desc");
            Query<AuditLogDBItem> query = session.createQuery(hql.toString(), AuditLogDBItem.class);

            //bindParameters
            if (!controllerIds.isEmpty()) {
                if (controllerIds.size() == 1) {
                    query.setParameter("controllerId", controllerIds.iterator().next());
                } else {
                    query.setParameterList("controllerIds", controllerIds);
                }
            }
            if (!categories.isEmpty()) {
                if (categories.size() == 1) {
                    query.setParameter("category", categories.iterator().next().intValue());
                } else {
                    query.setParameterList("categories", categories.stream().map(CategoryType::intValue).collect(Collectors.toList()));
                }
            }
            if (withAdvancedSearch) {
                if (filter.getObjectTypes() != null && !filter.getObjectTypes().isEmpty()) {
                    if (filter.getObjectTypes().size() == 1) {
                        query.setParameter("type", filter.getObjectTypes().iterator().next().intValue());
                    } else {
                        query.setParameterList("types", filter.getObjectTypes().stream().map(ObjectType::intValue).collect(Collectors.toSet()));
                    }
                }
                if (filter.getObjectName() != null && !filter.getObjectName().isEmpty()) {
                    if (SearchStringHelper.isGlobPattern(filter.getObjectName())) {
                        query.setParameter("name", SearchStringHelper.globToSqlPattern(filter.getObjectName()));
                    } else {
                        query.setParameter("name", filter.getObjectName());
                    }
                }
            }
            if (createdFrom != null) {
                query.setParameter("from", createdFrom, TemporalType.TIMESTAMP);
            }
            if (createdTo != null) {
                query.setParameter("to", createdTo, TemporalType.TIMESTAMP);
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
            if (filter.getComment() != null && !filter.getComment().isEmpty()) {
                if (SearchStringHelper.isGlobPattern(filter.getComment())) {
                    query.setParameter("comment", SearchStringHelper.globToSqlPattern(filter.getComment()));
                } else {
                    query.setParameter("comment", filter.getComment());
                }
            }
            if (filter.getLimit() != null) {
                query.setMaxResults(filter.getLimit());
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
            // TODO select new ok when without join otherwise use entity class as second parameter in getResultSet
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
    
    private String getAuditlogIdsSelectInAuditLogDetails(List<Folder> folders, Set<ObjectType> objectTypes, String objectName) {
        boolean hasOrderType = objectTypes == null || objectTypes.isEmpty() || objectTypes.contains(ObjectType.ORDER);
        StringBuilder hql = new StringBuilder("select auditLogId from ");
        hql.append(DBLayer.DBITEM_JOC_AUDIT_LOG_DETAILS);
        hql.append(getWhere(folders, objectTypes, objectName, hasOrderType));
        hql.append(" group by auditLogId");
        return hql.toString();
    }
    
    private String getAuditlogIdsSelectInDepHistory(List<Folder> folders, Set<ObjectType> objectTypes, String objectName) {
        StringBuilder hql = new StringBuilder("select auditlogId from ");
        hql.append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(getWhere(folders, objectTypes, objectName, false));
        hql.append(" group by auditlogId");
        return hql.toString();
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

}
