package com.sos.joc.cleanup.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.db.DeploymentVersion;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.db.DBLayer;

public class CleanupTaskDeployment extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskDeployment.class);

    private int totalSubmissions = 0;
    private int totalCommitIds = 0;
    private int totalSignatures = 0;
    private int totalSearchWorkflowsDepHistory = 0;
    private int totalSearchWorkflows = 0;
    private int totalHistory = 0;

    public CleanupTaskDeployment(JocClusterHibernateFactory factory, int batchSize, String identifier) {
        super(factory, batchSize, identifier);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(int versions) throws Exception {
        try {
            LOGGER.info(String.format("[%s][versions=%s]start cleanup", getIdentifier(), versions));

            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            List<DeploymentVersion> depVersions = getDeploymentVersions(versions);
            getDbLayer().close();

            if (depVersions != null && depVersions.size() > 0) {
                int size = depVersions.size();
                if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                    ArrayList<DeploymentVersion> copy = (ArrayList<DeploymentVersion>) depVersions.stream().collect(Collectors.toList());

                    JocServiceTaskAnswerState state = null;
                    for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                        List<DeploymentVersion> subList;
                        if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                            subList = copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE));
                        } else {
                            subList = copy.subList(i, size);
                        }
                        state = execute(subList, versions);
                    }
                    return state;

                } else {
                    return execute(depVersions, versions);
                }
            } else {
                return JocServiceTaskAnswerState.COMPLETED;
            }
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            getDbLayer().close();
        }
    }

    private JocServiceTaskAnswerState execute(List<DeploymentVersion> depVersions, int versions) throws SOSHibernateException {
        JocServiceTaskAnswerState state = null;
        for (int i = 0; i < depVersions.size(); i++) {
            int counter = i + 1;
            if (isStopped()) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }

            DeploymentVersion depVersion = depVersions.get(i);
            boolean askService = i % 500 == 0;
            boolean run = true;
            while (run) {
                if (askService) {
                    if (askService()) {
                        askService = false;
                    } else {
                        waitFor(WAIT_INTERVAL_ON_BUSY);
                        continue;
                    }
                }
                if (isStopped()) {
                    return JocServiceTaskAnswerState.UNCOMPLETED;
                }

                getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
                getDbLayer().getSession().beginTransaction();

                List<Long> r = getLessThanMaxHistoryIds(depVersion);
                if (r == null || r.size() < versions) {
                    int size = r == null ? 0 : r.size();
                    LOGGER.info(String.format("[%s][%s) maxId=%s][skip]found versions(%s) <= configured versions(%s)", getIdentifier(), counter,
                            depVersion.getMaxId(), size, versions));
                    run = false;
                    getDbLayer().getSession().commit();
                    getDbLayer().close();
                    continue;
                }
                r.sort(Comparator.comparing(Long::valueOf));// sort by id ascending
                int toIndex = (r.size() + 1) - versions; // total versions = getLessThanMaxHistoryIds+1(maxVersion)
                if (toIndex > 0) {
                    List<Long> subList = r.subList(0, toIndex);
                    cleanupEntries(counter, depVersion, subList);
                } else {
                    LOGGER.warn(String.format("[%s][%s) maxId=%s][versions=%s][lessThanMax=%s][toIndex=%s]can't compute toIndex", getIdentifier(),
                            counter, depVersion.getMaxId(), versions, r.size(), toIndex));
                }
                getDbLayer().getSession().commit();
                getDbLayer().close();
                run = false;
                state = JocServiceTaskAnswerState.COMPLETED;
            }
        }
        return state == null ? JocServiceTaskAnswerState.UNCOMPLETED : state;
    }

    @Override
    protected boolean askService() {
        try {
            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            Date d = getLastDeploymentDate();
            if (d != null) {
                return (new Date().getTime() - d.getTime()) / 1_000 >= 60; // 1 minute
            }
        } catch (Throwable e) {
            getDbLayer().rollback();
            LOGGER.error(e.toString(), e);
        } finally {
            getDbLayer().close();
        }
        return true;
    }

    private void cleanupEntries(int counter, DeploymentVersion depVersion, List<Long> ids) throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][deleted][").append(counter).append(") maxId=").append(depVersion.getMaxId()).append("]");

        // getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DEP_SUBMISSIONS).append(" ");
        hql.append("where depHistoryId in (:ids)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        int r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().getSession().commit();
        totalSubmissions += r;
        log.append(getDeleted(DBLayer.TABLE_DEP_SUBMISSIONS, r, totalSubmissions));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return;
        }

        // getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DEP_COMMIT_IDS).append(" ");
        hql.append("where depHistoryId in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().getSession().commit();
        totalCommitIds += r;
        log.append(getDeleted(DBLayer.TABLE_DEP_COMMIT_IDS, r, totalCommitIds));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return;
        }

        // getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DEP_SIGNATURES).append(" ");
        hql.append("where depHistoryId in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().getSession().commit();
        totalSignatures += r;
        log.append(getDeleted(DBLayer.TABLE_DEP_SIGNATURES, r, totalSignatures));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return;
        }

        // getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY).append(" ");
        hql.append("where deploymentHistoryId in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().getSession().commit();
        totalSearchWorkflowsDepHistory += r;
        log.append(getDeleted(DBLayer.TABLE_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY, r, totalSearchWorkflowsDepHistory));

        if (Math.abs(r) > 0) {
            // getDbLayer().getSession().beginTransaction();
            hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_SEARCH_WORKFLOWS).append(" ");
            hql.append("where deployed=true ");
            hql.append("and id not in(");
            hql.append("select searchWorkflowId from ").append(DBLayer.DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY);
            hql.append(")");
            query = getDbLayer().getSession().createQuery(hql.toString());
            r = getDbLayer().getSession().executeUpdate(query);
            // getDbLayer().getSession().commit();
            totalSearchWorkflows += r;
            log.append(getDeleted(DBLayer.TABLE_SEARCH_WORKFLOWS, r, totalSearchWorkflows));
        }

        if (isStopped()) {
            LOGGER.info(log.toString());
            return;
        }

        // getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DEP_HISTORY).append(" ");
        hql.append("where id in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().getSession().commit();
        totalHistory += r;
        log.append(getDeleted(DBLayer.TABLE_DEP_HISTORY, r, totalHistory));

        LOGGER.info(log.toString());
    }

    private Date getLastDeploymentDate() throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select max(deploymentDate) from ");
        hql.append(DBLayer.DBITEM_DEP_HISTORY).append(" ");

        Query<Date> query = getDbLayer().getSession().createQuery(hql.toString());
        Date r = getDbLayer().getSession().getSingleResult(query);
        getDbLayer().getSession().commit();

        // LOGGER.info(String.format("[%s][%s][last deployment]found=%s", getIdentifier(), DBLayer.TABLE_DEP_HISTORY, r));
        return r;
    }

    private List<DeploymentVersion> getDeploymentVersions(int versions) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select max(dh.id) as maxId ");
        hql.append(",count(dh.id) as countVersions ");
        hql.append(",dh.inventoryConfigurationId as inventoryId ");
        hql.append(",dh.controllerId as controllerId ");
        hql.append("from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
        hql.append("group by dh.inventoryConfigurationId,dh.controllerId ");
        hql.append("having count(dh.id) > :versions ");

        Query<DeploymentVersion> query = getDbLayer().getSession().createQuery(hql.toString(), DeploymentVersion.class);
        query.setParameter("versions", new Long(versions));
        List<DeploymentVersion> r = getDbLayer().getSession().getResultList(query);
        getDbLayer().getSession().commit();

        LOGGER.info(String.format("[%s][%s][versions > %s]found=%s", getIdentifier(), DBLayer.TABLE_DEP_HISTORY, versions, r == null ? 0 : r.size()));
        return r;
    }

    private List<Long> getLessThanMaxHistoryIds(DeploymentVersion depVersion) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_DEP_HISTORY).append(" ");
        hql.append("where id < :maxId ");
        hql.append("and inventoryConfigurationId = :inventoryId ");
        hql.append("and controllerId = :controllerId ");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setMaxResults(SOSHibernate.LIMIT_IN_CLAUSE);
        query.setParameter("maxId", depVersion.getMaxId());
        query.setParameter("inventoryId", depVersion.getInventoryId());
        query.setParameter("controllerId", depVersion.getControllerId());
        // LOGGER.info(getDbLayer().getSession().getSQLString(query));
        return getDbLayer().getSession().getResultList(query);
    }

}
