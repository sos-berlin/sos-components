package com.sos.joc.cleanup.model;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.profiles.Profiles;
import com.sos.joc.classes.profiles.ProfilesDeleteResult;
import com.sos.joc.cleanup.CleanupServiceConfiguration.ForceCleanup;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.cluster.common.state.JocClusterServiceTaskState;
import com.sos.joc.model.profile.ProfilesFilter;

public class CleanupTaskUserProfiles extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskUserProfiles.class);

    public CleanupTaskUserProfiles(JocClusterHibernateFactory factory, int batchSize, String identifier, ForceCleanup forceCleanup) {
        super(factory, batchSize, identifier, forceCleanup);
    }

    @Override
    public JocClusterServiceTaskState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            TaskDateTime profileAge = datetimes.get(0);
            TaskDateTime failedLoginHistoryAge = datetimes.get(1);

            tryOpenSession();

            // PROFILES
            if (profileAge.getDatetime() == null) {
                LOGGER.info(String.format("[%s][profile][%s]skip", getIdentifier(), profileAge.getAge().getConfigured()));
            } else {
                LOGGER.info(String.format("[%s][profile][%s][%s]start cleanup", getIdentifier(), profileAge.getAge().getConfigured(), profileAge
                        .getZonedDatetime()));

                StringBuilder log = new StringBuilder();
                log.append(String.format("[%s][profile][%s][deleted]", getIdentifier(), profileAge.getAge().getConfigured()));
                log.append("ProfilesDeleteResult=[");
                getDbLayer().beginTransaction();
                ProfilesDeleteResult r = cleanupUserProfiles(profileAge);
                if (r == null) {
                    log.append("0");
                } else {
                    log.append(r.toString());
                }
                log.append("]");
                log.append(cleanupIamHistoryBasedOnProfileAge(profileAge));

                if (failedLoginHistoryAge.getDatetime() == null) {
                    log.append(cleanupIamHistoryDetails());
                }
                getDbLayer().commit();
                LOGGER.info(log.toString());
            }
            if (isStopped()) {
                return JocClusterServiceTaskState.UNCOMPLETED;
            }

            // FAILED LOGINS
            if (failedLoginHistoryAge.getDatetime() == null) {
                LOGGER.info(String.format("[%s][failedLoginHistory][%s]skip", getIdentifier(), failedLoginHistoryAge.getAge().getConfigured()));
            } else {
                LOGGER.info(String.format("[%s][failedLoginHistory][%s][%s]start cleanup", getIdentifier(), failedLoginHistoryAge.getAge()
                        .getConfigured(), failedLoginHistoryAge.getZonedDatetime()));

                StringBuilder log = new StringBuilder();
                log.append(String.format("[%s][failedLoginHistory][%s][deleted]", getIdentifier(), failedLoginHistoryAge.getAge().getConfigured()));

                getDbLayer().beginTransaction();
                log.append(String.format("[%s][failedLoginHistory][%s][deleted]", getIdentifier(), failedLoginHistoryAge.getAge().getConfigured()));
                log.append(cleanupIamHistoryBasedOnFailedLoginAge(failedLoginHistoryAge));
                log.append(cleanupIamHistoryDetails());
                getDbLayer().commit();

                LOGGER.info(log.toString());
            }
            return JocClusterServiceTaskState.COMPLETED;
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            close();
        }
    }

    private ProfilesDeleteResult cleanupUserProfiles(TaskDateTime datetime) throws Exception {
        StringBuilder hql = new StringBuilder("select h.accountName from ");
        hql.append(DBLayer.DBITEM_IAM_HISTORY).append(" h ");
        hql.append("where h.loginSuccess=true ");
        hql.append("group by h.accountName ");
        hql.append("having max(h.loginDate) < :loginDate ");

        Query<String> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("loginDate", datetime.getDatetime());
        List<String> result = getDbLayer().getSession().getResultList(query);
        if (result != null && result.size() > 0) {
            ProfilesFilter f = new ProfilesFilter();
            f.setAccounts(result);
            f.setComplete(true);

            return Profiles.delete(getDbLayer().getSession(), f);
        }
        return null;
    }

    private StringBuilder cleanupIamHistoryBasedOnProfileAge(TaskDateTime datetime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_IAM_HISTORY).append(" ");
        hql.append("where loginDate < :loginDate ");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("loginDate", datetime.getDatetime());

        int r = getDbLayer().getSession().executeUpdate(query);
        return getDeleted(DBLayer.TABLE_IAM_HISTORY, r, r);
    }

    private StringBuilder cleanupIamHistoryBasedOnFailedLoginAge(TaskDateTime datetime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_IAM_HISTORY).append(" ");
        hql.append("where loginDate < :loginDate ");
        hql.append("and loginSuccess=false ");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("loginDate", datetime.getDatetime());

        int r = getDbLayer().getSession().executeUpdate(query);
        return getDeleted(DBLayer.TABLE_IAM_HISTORY, r, r);
    }

    private StringBuilder cleanupIamHistoryDetails() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_IAM_HISTORY_DETAILS).append(" ");
        hql.append("where iamHistoryId not in (");
        hql.append("select id from ").append(DBLayer.DBITEM_IAM_HISTORY);
        hql.append(")");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        int r = getDbLayer().getSession().executeUpdate(query);
        return getDeleted(DBLayer.TABLE_IAM_HISTORY_DETAILS, r, r);
    }

}
