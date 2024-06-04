package com.sos.joc.db.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.inventory.model.report.TemplateId;
import com.sos.joc.classes.inventory.JocInventory;
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
    
    public List<ReportDbItem> getGeneratedReports(Collection<Long> runIds, boolean compact, Collection<String> reportPaths,
            Collection<TemplateId> templateNames, Date dateFrom, Date dateTo) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("select rh.id as id");
            hql.append(",rh.runId as runId");
            hql.append(",rr.path as path");
            hql.append(",rr.title as title");
            hql.append(",rr.templateId as templateName");
            hql.append(",rh.frequency as frequency");
            hql.append(",rr.sort as sort");
            hql.append(",rr.periodLength as periodLength");
            hql.append(",rr.periodStep as periodStep");
            hql.append(",rr.hits as hits");
            hql.append(",rr.controllerId as controllerId");
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
            
            List<String> clause = new ArrayList<>(4);
            
            if (runIds != null && !runIds.isEmpty()) {
                if (runIds.size() == 1) {
                    clause.add("rr.id =:runId");
                } else {
                    clause.add("rr.id in (:runIds)");
                }
            }
            if (reportPaths != null && !reportPaths.isEmpty()) {
                if (reportPaths.size() == 1) {
                    clause.add("rr.name =:reportPath");
                } else {
                    clause.add("rr.name in (:reportPaths)");
                }
            }
            if (templateNames != null && !templateNames.isEmpty()) {
                if (templateNames.size() == 1) {
                    clause.add("rr.templateId =:templateId");
                } else {
                    clause.add("rr.templateId in (:templateIds)");
                }
            }
            if (dateFrom != null) {
                clause.add("rh.dateFrom >= :dateFrom");
            }
            if (dateTo != null) {
                clause.add("rh.dateTo < :dateTo");
            }
            
            if (!clause.isEmpty()) {
                hql.append(clause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            
            Query<ReportDbItem> query = getSession().createQuery(hql.toString(), ReportDbItem.class);
            
            if (runIds != null && !runIds.isEmpty()) {
                if (runIds.size() == 1) {
                    query.setParameter("runId", runIds.iterator().next());
                } else {
                    query.setParameterList("runIds", runIds);
                }
            }
            if (reportPaths != null && !reportPaths.isEmpty()) {
                if (reportPaths.size() == 1) {
                    query.setParameter("reportPath", JocInventory.pathToName(reportPaths.iterator().next()));
                } else {
                    query.setParameterList("reportPaths", reportPaths.stream().map(JocInventory::pathToName).collect(Collectors.toSet()));
                }
            }
            if (templateNames != null && !templateNames.isEmpty()) {
                if (templateNames.size() == 1) {
                    query.setParameter("templateId", templateNames.iterator().next().intValue());
                } else {
                    query.setParameterList("templateIds", templateNames.stream().map(TemplateId::intValue).collect(Collectors.toList()));
                }
            }
            if (dateFrom != null) {
                query.setParameter("dateFrom", dateFrom);
            }
            if (dateTo != null) {
                query.setParameter("dateTo", dateTo);
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
    
    public void delete(Long id) {
        try {
            DBItemReport item = getSession().get(DBItemReport.class, id);
            if (item != null) {
                getSession().delete(item);
            }
        } catch (SOSHibernateException e) {
            throw new DBInvalidDataException(e);
        }
    }
    
    public Map<Long, Long> getNumReports(Collection<Long> runIds) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("select runId, count(id) as num from ").append(DBLayer.DBITEM_REPORTS);
            
            if (runIds != null && !runIds.isEmpty()) {
                if (runIds.size() == 1) {
                    hql.append(" where runId =:runId");
                } else {
                    hql.append(" where runId in (:runIds)");
                }
            }
            
            hql.append(" group by runId");
            
            Query<Object[]> query = getSession().createQuery(hql.toString());
            
            if (runIds != null && !runIds.isEmpty()) {
                if (runIds.size() == 1) {
                    query.setParameter("runId", runIds.iterator().next());
                } else {
                    query.setParameterList("runIds", runIds);
                }
            }
            
            List<Object[]> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyMap();
            }
            return result.stream().collect(Collectors.toMap(item -> (Long) item[0], item -> (Long) item[1]));
            
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
            if (reportNames != null && !reportNames.isEmpty()) {
                clause.add("name in (:reportNames)");
            } else {
               if (templateNames != null && !templateNames.isEmpty()) {
                   clause.add("templateId in (:templateIds)");
               }
               if (states != null && !states.isEmpty()) {
                   clause.add("state in (:states)");
               } 
            }
            if (!clause.isEmpty()) {
                sql.append(clause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            Query<DBItemReportRun> query = getSession().createQuery(sql.toString());
            if (reportNames != null && !reportNames.isEmpty()) {
                query.setParameterList("reportNames", reportNames.stream().map(JocInventory::pathToName).collect(Collectors.toSet()));
            } else {
               if (templateNames != null && !templateNames.isEmpty()) {
                   query.setParameterList("templateIds", templateNames.stream().map(TemplateId::intValue).collect(Collectors.toList()));
               }
               if (states != null && !states.isEmpty()) {
                   query.setParameterList("states", states.stream().map(ReportRunStateText::intValue).collect(Collectors.toList()));
               } 
            }
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
