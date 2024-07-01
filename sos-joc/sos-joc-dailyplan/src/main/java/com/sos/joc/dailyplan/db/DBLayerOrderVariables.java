package com.sos.joc.dailyplan.db;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.DBLayer;
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
            hql.append("=:orderId ");
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
    
    public int update(String controllerId, String oldOrderId, String newOrderId, boolean isCyclic) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("set orderId=:newOrderId ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and orderId ");
        if (isCyclic) {
            hql.append("like :mainPart ");
        } else {
            hql.append("=:orderId ");
        }

        Query<Integer> query = getSession().createQuery(hql);
        query.setParameter("newOrderId", newOrderId);
        if (isCyclic) {
            query.setParameter("mainPart", OrdersHelper.getCyclicOrderIdMainPart(oldOrderId) + "%");
        } else {
            query.setParameter("orderId", oldOrderId);
        }
        query.setParameter("controllerId", controllerId);
        int i = getSession().executeUpdate(query);
        
        OrderTags.updateOrderIdOfOrder(controllerId, oldOrderId, newOrderId, getSession());
        
        return i;
    }

    public int update(String controllerId, String oldOrderId, String newOrderId) throws SOSHibernateException {
        return update(controllerId, oldOrderId, newOrderId, false);
    }
    
    public DBItemDailyPlanVariable copy(String controllerId, String oldOrderId, String newOrderId, boolean isCyclic)
            throws SOSHibernateException {

        DBItemDailyPlanVariable vars = getOrderVariable(controllerId, oldOrderId, isCyclic);
        if (vars != null) {
            DBItemDailyPlanVariable copiedVars = new DBItemDailyPlanVariable();
            copiedVars.setControllerId(vars.getControllerId());
            copiedVars.setCreated(Date.from(Instant.now()));
            copiedVars.setId(null);
            copiedVars.setModified(copiedVars.getCreated());
            copiedVars.setOrderId(newOrderId);
            copiedVars.setVariableValue(vars.getVariableValue());
            getSession().save(copiedVars);
            return copiedVars;
        }
        return null;
    }
}