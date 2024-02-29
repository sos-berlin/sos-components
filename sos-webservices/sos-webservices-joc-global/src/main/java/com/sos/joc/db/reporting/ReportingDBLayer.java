package com.sos.joc.db.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.inventory.model.report.TemplateId;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.reporting.items.ReportDbItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.reporting.ReportRunStateText;

public class ReportingDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public ReportingDBLayer(SOSHibernateSession session) {
        super(session);
    }
    
    public List<ReportDbItem> getAllReports(Set<Long> ids, boolean compact) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("select rh.id as id");
            hql.append(",rh.runId as runId");
            hql.append(",rr.path as path");
            hql.append(",rr.title as title");
            hql.append(",rr.templateId as templateName");
            hql.append(",rh.frequency as frequency");
            hql.append(",rr.hits as hits");
            hql.append(",rh.dateFrom as dateFrom");
            hql.append(",rh.dateTo as dateTo");
            hql.append(",rh.created as created");
            hql.append(",rh.modified as modified");
            if (!compact) {
                hql.append(",rh.content as content"); 
            }
            hql.append(" from ").append(DBLayer.DBITEM_REPORTS).append(" rh ");
            hql.append("left join ").append(DBLayer.DBITEM_REPORT_RUN).append(" rr ");
            hql.append("on rh.runId=rr.id");
            
            if (ids != null && !ids.isEmpty()) {
                hql.append(" where rh.id in (:ids)");
            }
            
            Query<ReportDbItem> query = getSession().createQuery(hql.toString(), ReportDbItem.class);
            
            if (ids != null && !ids.isEmpty()) {
                query.setParameterList("ids", ids);
            }
            
            List<ReportDbItem> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DBItemReportRun> getAllRuns() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_REPORT_RUN);
            Query<DBItemReportRun> query = getSession().createQuery(sql.toString());
            List<DBItemReportRun> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DBItemReportRun> getRuns(Collection<String> reportNames, Collection<TemplateId> templateNames, Collection<ReportRunStateText> states)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_REPORT_RUN);
            List<String> clause = new ArrayList<>(3);
//            if (reportNames != null && !reportNames.isEmpty()) {
//                clause.add("name in (:reportNames)");
//            } else {
               if (templateNames != null && !templateNames.isEmpty()) {
                   clause.add("templateId in (:templateIds)");
               }
               if (states != null && !states.isEmpty()) {
                   clause.add("state in (:states)");
               } 
//            }
            if (!clause.isEmpty()) {
                sql.append(clause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            Query<DBItemReportRun> query = getSession().createQuery(sql.toString());
//            if (reportNames != null && !reportNames.isEmpty()) {
//                query.setParameterList("reportNames", reportNames);
//            } else {
               if (templateNames != null && !templateNames.isEmpty()) {
                   query.setParameterList("templateIds", templateNames.stream().map(TemplateId::intValue).collect(Collectors.toList()));
               }
               if (states != null && !states.isEmpty()) {
                   query.setParameterList("states", states.stream().map(ReportRunStateText::intValue).collect(Collectors.toList()));
               } 
//            }
            List<DBItemReportRun> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DBItemReportTemplate> getTemplates() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_REPORT_TEMPLATE);
            Query<DBItemReportTemplate> query = getSession().createQuery(sql.toString());
            List<DBItemReportTemplate> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemReport getReport(String constraint) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_REPORTS).append(" where constraintHash = :constraintHash");
            Query<DBItemReport> query = getSession().createQuery(sql.toString());
            query.setParameter("constraintHash", constraint);
            return getSession().getSingleResult(query);
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
}
