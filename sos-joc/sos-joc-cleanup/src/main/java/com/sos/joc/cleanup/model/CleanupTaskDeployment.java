package com.sos.joc.cleanup.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.db.DBLayer;

public class CleanupTaskDeployment extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskDeployment.class);

    public CleanupTaskDeployment(JocClusterHibernateFactory factory, int batchSize, String identifier) {
        super(factory, batchSize, identifier);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(int counter) throws SOSHibernateException {
        LOGGER.info(String.format("[%s]currently under refactoring/development", getIdentifier()));
        return JocServiceTaskAnswerState.COMPLETED;
    }

    public JocServiceTaskAnswerState oldCleanup(Date date) throws SOSHibernateException {
        try {
            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            List<Long> deployed = getDeployedIds();
            getDbLayer().close();

            if (deployed != null && deployed.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
                int size = deployed.size();
                ArrayList<Long> copy = (ArrayList<Long>) deployed.stream().collect(Collectors.toList());

                JocServiceTaskAnswerState state = null;
                for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    List<Long> subList;
                    if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                        subList = copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE));
                    } else {
                        subList = copy.subList(i, size);
                    }
                    state = execute(date, subList);
                }
                return state;

            } else {
                return execute(date, deployed);
            }
        } catch (SOSHibernateException e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            getDbLayer().close();
        }
    }

    private JocServiceTaskAnswerState execute(Date date, List<Long> deployed) throws SOSHibernateException {
        boolean run = true;
        while (run) {
            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            List<Long> r = getHistoryIds(date, deployed);
            getDbLayer().close();

            if (r == null || r.size() == 0) {
                return JocServiceTaskAnswerState.COMPLETED;
            }
            if (isStopped()) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }

            if (askService()) {
                getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
                cleanup(r);
                getDbLayer().close();
            } else {
                waitFor(WAIT_INTERVAL_ON_BUSY);
            }
        }
        return JocServiceTaskAnswerState.UNCOMPLETED;
    }

    private void cleanup(List<Long> ids) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DEP_SUBMISSIONS).append(" ");
        hql.append("where depHistoryId in (:ids)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        int r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.TABLE_DEP_SUBMISSIONS, r));
        getDbLayer().getSession().commit();

        if (isStopped()) {
            return;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DEP_COMMIT_IDS).append(" ");
        hql.append("where depHistoryId in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.TABLE_DEP_COMMIT_IDS, r));
        getDbLayer().getSession().commit();

        if (isStopped()) {
            return;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DEP_SIGNATURES).append(" ");
        hql.append("where depHistoryId in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.TABLE_DEP_SIGNATURES, r));
        getDbLayer().getSession().commit();

        if (isStopped()) {
            return;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY).append(" ");
        hql.append("where deploymentHistoryId in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.TABLE_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY, r));
        getDbLayer().getSession().commit();

        if (Math.abs(r) > 0) {
            getDbLayer().getSession().beginTransaction();
            hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" ");
            hql.append("where deployed=true ");
            hql.append("and id not in(");
            hql.append("select searchWorkflowId from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY);
            hql.append(")");
            query = getDbLayer().getSession().createQuery(hql.toString());
            r = getDbLayer().getSession().executeUpdate(query);
            LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.TABLE_SEARCH_WORKFLOWS, r));
            getDbLayer().getSession().commit();
        }

        if (isStopped()) {
            return;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DEP_HISTORY).append(" ");
        hql.append("where id in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.TABLE_DEP_HISTORY, r));
        getDbLayer().getSession().commit();
    }

    private List<Long> getDeployedIds() throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" ");
        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        LOGGER.info(String.format("[%s][%s][currently deployed]found=%s", getIdentifier(), DBLayer.TABLE_DEP_CONFIGURATIONS, r.size()));
        getDbLayer().getSession().commit();
        return r;
    }

    private List<Long> getHistoryIds(Date date, List<Long> deployedIds) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_DEP_HISTORY).append(" ");
        hql.append("where deploymentDate < :date ");
        if (deployedIds != null && deployedIds.size() > 0) {
            hql.append("and id not in(:deployedIds)");
        }
        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("date", date);
        if (deployedIds != null && deployedIds.size() > 0) {
            query.setParameterList("deployedIds", deployedIds);
        }
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        LOGGER.info(String.format("[%s][%s][old entries]found=%s", getIdentifier(), DBLayer.TABLE_DEP_HISTORY, r.size()));
        getDbLayer().getSession().commit();
        return r;
    }

}
