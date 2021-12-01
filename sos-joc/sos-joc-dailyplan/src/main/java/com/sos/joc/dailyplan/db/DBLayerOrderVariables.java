package com.sos.joc.dailyplan.db;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;

public class DBLayerOrderVariables extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DBLayerOrderVariables(SOSHibernateSession session) {
        super(session);
    }

    public DBItemDailyPlanVariable getOrderVariable(Long plannedOrderId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("where plannedOrderId=:plannedOrderId ");

        Query<DBItemDailyPlanVariable> query = getSession().createQuery(hql);
        query.setParameter("plannedOrderId", plannedOrderId);
        List<DBItemDailyPlanVariable> result = getSession().getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public DBItemDailyPlanVariable getOrderVariable(String orderId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("where plannedOrderId in (");
        hql.append("select id from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" where orderId=:orderId");
        hql.append(")");

        Query<DBItemDailyPlanVariable> query = getSession().createQuery(hql);
        query.setParameter("orderId", orderId);
        List<DBItemDailyPlanVariable> result = getSession().getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public int update(Long oldId, Long newId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update  ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("set plannedOrderId=:newId ");
        hql.append("where plannedOrderId=:oldId");

        Query<DBItemDailyPlanSubmission> query = getSession().createQuery(hql);
        query.setParameter("newId", newId);
        query.setParameter("oldId", oldId);
        return getSession().executeUpdate(query);
    }
}