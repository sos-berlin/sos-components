package com.sos.joc.db.reporting;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.reporting.items.ReportDbItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

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
