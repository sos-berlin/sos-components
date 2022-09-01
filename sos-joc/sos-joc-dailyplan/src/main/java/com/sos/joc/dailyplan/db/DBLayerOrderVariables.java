package com.sos.joc.dailyplan.db;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;

public class DBLayerOrderVariables extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DBLayerOrderVariables(SOSHibernateSession session) {
        super(session);
    }

    public DBItemDailyPlanVariable getOrderVariable(String controllerId, String orderId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select controllerId,startMode from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where orderId=:orderId ");
        if (!SOSString.isEmpty(controllerId)) {
            hql.append("and controllerId=:controllerId ");
        }

        Query<Object[]> query = getSession().createQuery(hql);
        query.setParameter("orderId", orderId);
        if (!SOSString.isEmpty(controllerId)) {
            query.setParameter("controllerId", controllerId);
        }
        List<Object[]> result = getSession().getResultList(query);
        if (result != null && result.size() > 0) {
            Object[] o = result.get(0);
            return getOrderVariable(o[0].toString(), orderId, ((Integer) o[1]).equals(Integer.valueOf(1)));
        }
        return null;
    }

    public DBItemDailyPlanVariable getOrderVariable(String controllerId, String orderId, boolean isCyclic) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and orderId ");
        if (isCyclic) {
            hql.append("like :mainPart ");
        } else {
            hql.append("=:orderId");
        }

        Query<DBItemDailyPlanVariable> query = getSession().createQuery(hql);
        query.setParameter("controllerId", controllerId);
        if (isCyclic) {
            query.setParameter("mainPart", OrdersHelper.getCyclicOrderIdMainPart(orderId) + "%");
        } else {
            query.setParameter("orderId", orderId);
        }
        List<DBItemDailyPlanVariable> result = getSession().getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public int update(String controllerId, String oldOrderId, String newOrderId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update  ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("set orderId=:newOrderId ");
        hql.append("where orderId=:oldOrderId ");
        hql.append("and controllerId=:controllerId");

        Query<DBItemDailyPlanSubmission> query = getSession().createQuery(hql);
        query.setParameter("newOrderId", newOrderId);
        query.setParameter("oldOrderId", oldOrderId);
        query.setParameter("controllerId", controllerId);
        return getSession().executeUpdate(query);
    }
}