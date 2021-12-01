package com.sos.joc.dailyplan.db;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DBLayerReleasedConfigurations extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DBLayerReleasedConfigurations(SOSHibernateSession session) {
        super(session);
    }

    public DBItemInventoryReleasedConfiguration getReleasedConfiguration(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" ");
        hql.append("where id=:id ");

        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql);
        query.setParameter("id", id);
        List<DBItemInventoryReleasedConfiguration> result = getSession().getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public DBItemInventoryReleasedConfiguration getReleasedConfiguration(ConfigurationType type, String name) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and name=:name ");

        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql);
        query.setParameter("type", type.intValue());
        query.setParameter("name", name);
        List<DBItemInventoryReleasedConfiguration> result = getSession().getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }
}