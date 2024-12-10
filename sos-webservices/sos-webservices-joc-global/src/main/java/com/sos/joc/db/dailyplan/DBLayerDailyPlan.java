package com.sos.joc.db.dailyplan;

import java.util.Collections;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;

public class DBLayerDailyPlan {

    private SOSHibernateSession session;
    
    public DBLayerDailyPlan(SOSHibernateSession session) {
        this.session = session;
    }

    public SOSHibernateSession getSession() {
        return session;
    }

    public List<DBItemDailyPlanOrder> getOrdersByWorkflowName(String workflowName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDERS);
        hql.append(" where workflowName = :workflowName");
        Query<DBItemDailyPlanOrder> query = session.createQuery(hql.toString());
        query.setParameter("workflowName", workflowName);
        List<DBItemDailyPlanOrder> result = session.getResultList(query);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
    
    public List<DBItemDailyPlanOrder> getOrdersByScheduleName(String scheduleName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDERS);
        hql.append(" where scheduleName = :scheduleName");
        Query<DBItemDailyPlanOrder> query = session.createQuery(hql.toString());
        query.setParameter("scheduleName", scheduleName);
        List<DBItemDailyPlanOrder> result = session.getResultList(query);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

}
