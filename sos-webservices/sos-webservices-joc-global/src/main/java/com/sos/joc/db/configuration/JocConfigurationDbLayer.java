package com.sos.joc.db.configuration;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.type.BooleanType;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.webservices.db.JocDBItemConstants;
import com.sos.webservices.db.configuration.DBItemJocConfiguration;

/** @author Uwe Risse */
public class JocConfigurationDbLayer {

    private JocConfigurationFilter filter = null;
    private SOSHibernateSession session;

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

    public DBItemJocConfiguration getJocConfiguration(Long id) throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(JocDBItemConstants.DBITEM_JOC_CONFIGURATIONS).append(" where id = :id");
        Query<DBItemJocConfiguration> query = this.session.createQuery(sql.toString());
        query.setParameter("id", id);
        return this.session.getSingleResult(query);
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