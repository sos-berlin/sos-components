package com.sos.joc.keys.db;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.history.DBItemAgent;
import com.sos.jobscheduler.db.pgp.DBItemJSKeys;

public class DBLayerKeys {

    private final SOSHibernateSession session;

    public DBLayerKeys(SOSHibernateSession hibernateSession) {
        session = hibernateSession;
    }

    public SOSHibernateSession getSession() {
        return session;
    }

    public void close() {
        if (session != null) {
            session.close();
        }
    }
    
    public void saveOrUpdateKey (DBItemJSKeys key) throws SOSHibernateException {
        if (key.getId() != null && key.getId() != 0L) {
            session.update(key);
        } else {
            session.save(key);
        }
    }
    
    public void saveOrUpdateKey (Integer type, String key, String account) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.TABLE_JS_KEYS);
        hql.append(" where account = :account");
        Query<DBItemJSKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        DBItemJSKeys existingKey =  session.getSingleResult(query);
        if (existingKey != null) {
            existingKey.setType(type);
            existingKey.setKey(key);
            session.update(existingKey);
        } else {
            DBItemJSKeys newKey = new DBItemJSKeys();
            newKey.setType(type);
            newKey.setKey(key);
            newKey.setAccount(account);
            session.save(newKey);
        }
    }

}
