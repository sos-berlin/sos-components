package com.sos.joc.keys.db;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.pgp.DBItemDepKeys;
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
    
    public void saveOrUpdateKey (DBItemDepKeys key) throws SOSHibernateException {
        if (key.getId() != null && key.getId() != 0L) {
            session.update(key);
        } else {
            session.save(key);
        }
    }
    
    public void saveOrUpdateKey (Integer type, String key, String account) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        DBItemDepKeys existingKey =  session.getSingleResult(query);
        if (existingKey != null) {
            existingKey.setType(type);
            existingKey.setKey(key);
            session.update(existingKey);
        } else {
            DBItemDepKeys newKey = new DBItemDepKeys();
            newKey.setType(type);
            newKey.setKey(key);
            newKey.setAccount(account);
            session.save(newKey);
        }
    }

    public SOSPGPKeyPair getKeyPair(String account) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        DBItemDepKeys key = session.getSingleResult(query);
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
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where type = :type");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("type", JocPGPKeyType.DEFAULT.ordinal());
        DBItemDepKeys key = session.getSingleResult(query);
        if (key != null) {
            SOSPGPKeyPair keyPair = new SOSPGPKeyPair();
            keyPair.setPrivateKey(key.getKey());
            return keyPair;
        }
        return null;
    }

}
