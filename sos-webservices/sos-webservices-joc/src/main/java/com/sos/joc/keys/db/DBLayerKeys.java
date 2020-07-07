package com.sos.joc.keys.db;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDepKeys;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.pgp.JocKeyType;
import com.sos.joc.publish.util.PublishUtils;

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
            existingKey.setKeyType(type);
            existingKey.setKey(key);
            existingKey.setKeyAlgorythm(PublishUtils.getKeyAlgorythm(key).ordinal());
            session.update(existingKey);
        } else {
            DBItemDepKeys newKey = new DBItemDepKeys();
            newKey.setKeyType(type);
            newKey.setKey(key);
            newKey.setKeyAlgorythm(PublishUtils.getKeyAlgorythm(key).ordinal());
            newKey.setAccount(account);
            session.save(newKey);
        }
    }

    public JocKeyPair getKeyPair(String account) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        DBItemDepKeys key = session.getSingleResult(query);
        if (key != null) {
            JocKeyPair keyPair = new JocKeyPair();
            if(key.getKeyType() == JocKeyType.PRIVATE.ordinal()) {
                keyPair.setPrivateKey(key.getKey());
            } else if (key.getKeyType() == JocKeyType.PUBLIC.ordinal()) {
                keyPair.setPublicKey(key.getKey());
            }
            return keyPair;
        }
        return null;
    }

    public JocKeyPair getDefaultKeyPair(String defaultAccount) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", defaultAccount);
        DBItemDepKeys key = session.getSingleResult(query);
        if (key != null) {
            JocKeyPair keyPair = new JocKeyPair();
            keyPair.setPrivateKey(key.getKey());
            return keyPair;
        }
        return null;
    }

}
