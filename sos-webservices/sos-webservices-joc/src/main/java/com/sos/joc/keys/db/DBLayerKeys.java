package com.sos.joc.keys.db;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.pgp.SOSPGPConstants;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDepKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyAlgorythm;
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
    
    public void saveOrUpdateKey (Integer type, String key, String account, JocSecurityLevel secLvl) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("secLvl", secLvl.intValue());
        DBItemDepKeys existingKey =  session.getSingleResult(query);
        if (existingKey != null) {
            existingKey.setKeyType(type);
            if (key.startsWith(SOSPGPConstants.CERTIFICATE_HEADER)) {
                existingKey.setCertificate(key);
            } else {
                existingKey.setKey(key);
            }
            existingKey.setKeyAlgorythm(PublishUtils.getKeyAlgorythm(key).value());
            session.update(existingKey);
        } else {
            DBItemDepKeys newKey = new DBItemDepKeys();
            newKey.setKeyType(type);
            if (key.startsWith(SOSPGPConstants.CERTIFICATE_HEADER)) {
                newKey.setCertificate(key);
            } else {
                newKey.setKey(key);
            }
            newKey.setKeyAlgorythm(PublishUtils.getKeyAlgorythm(key).value());
            newKey.setAccount(account);
            newKey.setSecLvl(secLvl.intValue());
            session.save(newKey);
        }
    }

    public void saveOrUpdateKey (Integer type, String key, String certificate, String account, JocSecurityLevel secLvl) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("secLvl", secLvl.intValue());
        DBItemDepKeys existingKey =  session.getSingleResult(query);
        if (existingKey != null) {
            existingKey.setKeyType(type);
            existingKey.setCertificate(certificate);
            existingKey.setKey(key);
            existingKey.setKeyAlgorythm(PublishUtils.getKeyAlgorythm(key).value());
            session.update(existingKey);
        } else {
            DBItemDepKeys newKey = new DBItemDepKeys();
            newKey.setKeyType(type);
            newKey.setCertificate(certificate);
            newKey.setKey(key);
            newKey.setKeyAlgorythm(PublishUtils.getKeyAlgorythm(key).value());
            newKey.setAccount(account);
            newKey.setSecLvl(secLvl.intValue());
            session.save(newKey);
        }
    }

    public JocKeyPair getKeyPair(String account, JocSecurityLevel secLvl) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("secLvl", secLvl.intValue());
        DBItemDepKeys key = session.getSingleResult(query);
        if (key != null) {
            JocKeyPair keyPair = new JocKeyPair();
            if(key.getKeyType() == JocKeyType.PRIVATE.value()) {
                keyPair.setPrivateKey(key.getKey());
                keyPair.setCertificate(key.getCertificate());
            } else if (key.getKeyType() == JocKeyType.PUBLIC.value()) {
                keyPair.setPublicKey(key.getKey());
                keyPair.setCertificate(key.getCertificate());
            }
            keyPair.setKeyType(JocKeyAlgorythm.fromValue(key.getKeyAlgorythm()).name());
            return keyPair;
        }
        return null;
    }

    public JocKeyPair getDefaultKeyPair(String defaultAccount) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", defaultAccount);
        query.setParameter("secLvl", JocSecurityLevel.LOW.intValue());
        DBItemDepKeys key = session.getSingleResult(query);
        if (key != null) {
            JocKeyPair keyPair = new JocKeyPair();
            keyPair.setPrivateKey(key.getKey());
            return keyPair;
        }
        return null;
    }

}
