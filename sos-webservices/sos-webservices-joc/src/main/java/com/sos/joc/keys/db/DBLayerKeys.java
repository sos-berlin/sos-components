package com.sos.joc.keys.db;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.history.DBItemAgent;
import com.sos.jobscheduler.db.pgp.DBItemJSKeys;
import com.sos.joc.model.pgp.JocPGPKeyType;
import com.sos.joc.model.pgp.SOSPGPKeyPair;

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
        hql.append(DBLayer.DBITEM_JS_KEYS);
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

    public SOSPGPKeyPair getKeyPair(String account) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_JS_KEYS);
        hql.append(" where account = :account");
        Query<DBItemJSKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        DBItemJSKeys key = session.getSingleResult(query);
        if (key != null) {
            SOSPGPKeyPair keyPair = new SOSPGPKeyPair();
            if(key.getType() == JocPGPKeyType.PRIVATE.ordinal()) {
                keyPair.setPrivateKey(key.getKey());
            } else if (key.getType() == JocPGPKeyType.PUBLIC.ordinal()) {
                keyPair.setPublicKey(key.getKey());
            }
            return keyPair;
        }
        return null;
    }

    public SOSPGPKeyPair getDefaultKeyPair() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_JS_KEYS);
        hql.append(" where type = :type");
        Query<DBItemJSKeys> query = session.createQuery(hql.toString());
        query.setParameter("type", JocPGPKeyType.DEFAULT.ordinal());
        DBItemJSKeys key = session.getSingleResult(query);
        if (key != null) {
            SOSPGPKeyPair keyPair = new SOSPGPKeyPair();
            keyPair.setPrivateKey(key.getKey());
            return keyPair;
        }
        return null;
    }

}
