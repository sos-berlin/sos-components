package com.sos.joc.db.configuration;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.type.BooleanType;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.JocDBItemConstants;
import com.sos.jobscheduler.db.configuration.DBItemJocConfiguration;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.configuration.Profile;

public class JocConfigurationDbLayer {

    private JocConfigurationFilter filter = null;
    private SOSHibernateSession session;
    private static final String CONFIGURATION_PROFILE = ConfigurationProfile.class.getName();

    public JocConfigurationDbLayer(SOSHibernateSession sosHibernateSession) {
        this.session = sosHibernateSession;
        resetFilter();
    }

    public DBItemJocConfiguration getDBItemJocConfiguration(final Long id) throws SOSHibernateException {
        return (DBItemJocConfiguration) (this.session.get(DBItemJocConfiguration.class, id));
    }

    public void resetFilter() {
        filter = new JocConfigurationFilter();
        filter.setSchedulerId(null);
        filter.setName("");
        filter.setConfigurationType("");
        filter.setObjectType("");
        filter.setAccount("");
        filter.setShared(null);
        filter.setId(null);
    }

    public int delete() throws SOSHibernateException {
        String hql = "delete from " + JocDBItemConstants.DBITEM_JOC_CONFIGURATIONS + " " + getWhere();
        Query<Integer> query = null;
        query = this.session.createQuery(hql);
        if (filter.getName() != null && !"".equals(filter.getName())) {
            query.setParameter("name", filter.getName());
        }
        if (filter.getConfigurationType() != null && !"".equals(filter.getConfigurationType())) {
            query.setParameter("configurationType", filter.getConfigurationType());
        }
        if (filter.getObjectType() != null && !"".equals(filter.getObjectType())) {
            query.setParameter("objectType", filter.getObjectType());
        }
        if (filter.getAccount() != null && !"".equals(filter.getAccount())) {
            query.setParameter("account", filter.getAccount());
        }
        if (filter.isShared() != null) {
            query.setParameter("shared", filter.isShared());
        }
        return this.session.executeUpdate(query);
    }

    private String getWhere() {
        String where = "";
        String and = "";
        if (filter.getName() != null && !"".equals(filter.getName())) {
            where += and + " name = :name";
            and = " and ";
        }
        if (filter.getSchedulerId() != null) {
            where += and + " schedulerId = :schedulerId ";
            and = " and ";
        }
        if (filter.getId() != null) {
            where += and + " id = :id ";
            and = " and ";
        }
        if (filter.getConfigurationType() != null && !"".equals(filter.getConfigurationType())) {
            where += and + " configurationType = :configurationType ";
            and = " and ";
        }
        if (filter.getObjectType() != null && !"".equals(filter.getObjectType())) {
            where += and + " objectType = :objectType";
            and = " and ";
        }
        if (filter.getAccount() != null && !"".equals(filter.getAccount())) {
            where += and + " account = :account";
            and = " and ";
        }
        if (filter.isShared() != null) {
            where += and + " shared = :shared";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    private void bindParameters(Query<DBItemJocConfiguration> query) {
        if (filter.getName() != null && !"".equals(filter.getName())) {
            query.setParameter("name", filter.getName());
        }
        if (filter.getSchedulerId() != null) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getId() != null) {
            query.setParameter("id", filter.getId());
        }
        if (filter.getConfigurationType() != null && !"".equals(filter.getConfigurationType())) {
            query.setParameter("configurationType", filter.getConfigurationType());
        }
        if (filter.getObjectType() != null && !"".equals(filter.getObjectType())) {
            query.setParameter("objectType", filter.getObjectType());
        }
        if (filter.getAccount() != null && !"".equals(filter.getAccount())) {
            query.setParameter("account", filter.getAccount());
        }
        if (filter.isShared() != null) {
            query.setParameter("shared", filter.isShared(), BooleanType.INSTANCE);
        }
    }

    public List<DBItemJocConfiguration> getJocConfigurationList(final int limit) throws SOSHibernateException {

        String sql = "from " + JocDBItemConstants.DBITEM_JOC_CONFIGURATIONS + " " + getWhere() + filter.getOrderCriteria()
        	+ filter.getSortMode();
        Query<DBItemJocConfiguration> query = this.session.createQuery(sql);
        bindParameters(query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return this.session.getResultList(query);

    }

	public List<Profile> getJocConfigurationProfiles() throws SOSHibernateException {
		StringBuilder sql = new StringBuilder();
		sql.append("select new ").append(CONFIGURATION_PROFILE);
		sql.append("(jc.account, max(al.created)) from ").append(JocDBItemConstants.DBITEM_JOC_CONFIGURATIONS)
				.append(" jc, ").append(JocDBItemConstants.DBITEM_AUDIT_LOG).append(" al ");
		sql.append(
				"where jc.account=al.account and jc.configurationType='PROFILE' and al.request='./login' group by jc.account");
		Query<Profile> query = session.createQuery(sql.toString());

		return session.getResultList(query);
	}

    public DBItemJocConfiguration getJocConfiguration(Long id) throws SOSHibernateException {
        return session.get(DBItemJocConfiguration.class, id);
    }

    public List<DBItemJocConfiguration> getJocConfigurations(final int limit) throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(JocDBItemConstants.DBITEM_JOC_CONFIGURATIONS).append(" ").append(getWhere())
        	.append(filter.getOrderCriteria()).append(filter.getSortMode());
        Query<DBItemJocConfiguration> query = this.session.createQuery(sql.toString());
        bindParameters(query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return this.session.getResultList(query);
    }

    public Long saveOrUpdateConfiguration(DBItemJocConfiguration DBItemJocConfiguration) throws SOSHibernateException {
        DBItemJocConfiguration.setModified(new Date());
        if (DBItemJocConfiguration.getId() == null) {
            this.session.save(DBItemJocConfiguration);
        } else {
            this.session.update(DBItemJocConfiguration);
        }
        return DBItemJocConfiguration.getId();
    }

    public int deleteConfiguration() throws SOSHibernateInvalidSessionException, SOSHibernateException {
        List<DBItemJocConfiguration> l = getJocConfigurationList(1);
        int size = (l != null) ? l.size() : 0;
        if (size > 0) {
            this.session.delete(l.get(0));
        }
        return size;
    }
    
    public int deleteConfigurations(List<String> accounts) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            String hql = "delete from " + JocDBItemConstants.DBITEM_JOC_CONFIGURATIONS + " where account in (:accounts)";
            Query<Integer> query = session.createQuery(hql);
            query.setParameterList("accounts", accounts);
            return session.executeUpdate(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void deleteConfiguration(DBItemJocConfiguration dbItem) throws SOSHibernateException {
        this.session.delete(dbItem);
    }

    public JocConfigurationFilter getFilter() {
        return filter;
    }

    public void setFilter(final JocConfigurationFilter filter) {
        this.filter = filter;
    }

}