package com.sos.jobscheduler.history.db;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBItemSchedulerVariables;
import com.sos.jobscheduler.db.DBLayer;

public class DBLayerHistory {

    private final String schedulerVariablesName;

    public DBLayerHistory(String name) {
        schedulerVariablesName = name;
    }

    public DBItemSchedulerVariables getSchedulerVariables(SOSHibernateSession session) throws SOSHibernateException {
        String hql = String.format("from %s where name = :name", DBLayer.DBITEM_SCHEDULER_VARIABLES);
        Query<DBItemSchedulerVariables> query = session.createQuery(hql);
        query.setParameter("name", schedulerVariablesName);
        return session.getSingleResult(query);
    }

    public DBItemSchedulerVariables insertSchedulerVariables(SOSHibernateSession session, Long eventId) throws SOSHibernateException {
        DBItemSchedulerVariables item = new DBItemSchedulerVariables();
        item.setName(schedulerVariablesName);
        item.setNumericValue(eventId);
        session.save(item);
        return item;
    }
}
