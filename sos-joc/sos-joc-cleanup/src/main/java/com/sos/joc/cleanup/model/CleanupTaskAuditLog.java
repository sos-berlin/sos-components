package com.sos.joc.cleanup.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.db.DBLayer;

public class CleanupTaskAuditLog extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskAuditLog.class);

    public CleanupTaskAuditLog(JocClusterHibernateFactory factory, int batchSize, String identifier) {
        super(factory, batchSize, identifier);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            TaskDateTime datetime = datetimes.get(0);
            LOGGER.info(String.format("[%s][%s][%s]start cleanup", getIdentifier(), datetime.getAge().getConfigured(), datetime.getZonedDatetime()));

            boolean run = true;
            while (run) {
                getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
                List<Long> r = getIds(datetime);
                if (r == null || r.size() == 0) {
                    return JocServiceTaskAnswerState.COMPLETED;
                }
                if (isStopped()) {
                    return JocServiceTaskAnswerState.UNCOMPLETED;
                }

                int size = r.size();
                if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                    ArrayList<Long> copy = (ArrayList<Long>) r.stream().collect(Collectors.toList());

                    for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                        List<Long> subList;
                        if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                            subList = copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE));
                        } else {
                            subList = copy.subList(i, size);
                        }
                        cleanupEntries(datetime, subList);
                    }

                } else {
                    cleanupEntries(datetime, r);
                }

                cleanupEntries(datetime, r);
                getDbLayer().close();
            }
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            getDbLayer().close();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private List<Long> getIds(TaskDateTime datetime) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_JOC_AUDIT_LOG).append(" ");
        hql.append("where created < :created ");
        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("created", datetime.getDatetime());
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        LOGGER.info(String.format("[%s][%s][%s]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_JOC_AUDIT_LOG, r.size()));
        getDbLayer().getSession().commit();
        return r;
    }

    private void cleanupEntries(TaskDateTime datetime, List<Long> ids) throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][deleted][").append(datetime.getAge().getConfigured()).append("]");

        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_JOC_AUDIT_LOG).append(" ");
        hql.append("where id in (:ids)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        int r = getDbLayer().getSession().executeUpdate(query);
        log.append("[").append(DBLayer.TABLE_JOC_AUDIT_LOG).append("=").append(r).append("]");
        getDbLayer().getSession().commit();
        LOGGER.info(log.toString());
    }
}
