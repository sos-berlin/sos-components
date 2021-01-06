package com.sos.joc.keys.db;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDepKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.sign.JocKeyAlgorithm;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;

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
    
    public void saveOrUpdateGeneratedKey (JocKeyPair keyPair, String account, JocSecurityLevel secLvl)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("secLvl", secLvl.intValue());
        DBItemDepKeys existingKey =  session.getSingleResult(query);
        if (existingKey != null) {
            existingKey.setKeyType(JocKeyType.PRIVATE.value());
            existingKey.setKey(keyPair.getPrivateKey());
            existingKey.setCertificate(null);
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.PGP.value());
            } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.RSA.value());
            } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.ECDSA.value());
            }
            session.update(existingKey);
        } else {
            DBItemDepKeys newKey = new DBItemDepKeys();
            newKey.setKeyType(JocKeyType.PRIVATE.value());
            newKey.setKey(keyPair.getPrivateKey());
            newKey.setCertificate(null);
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.PGP.value());
            } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.RSA.value());
            } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.ECDSA.value());
            }
            newKey.setAccount(account);
            newKey.setSecLvl(secLvl.intValue());
            session.save(newKey);
        }
    }

    public void saveOrUpdateKey (Integer type, String key, String account, JocSecurityLevel secLvl, String keyAlgorythm)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        hql.append(" and keyType = :keyType");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("secLvl", secLvl.intValue());
        query.setParameter("keyType", type);
        DBItemDepKeys existingKey =  session.getSingleResult(query);
        if (existingKey != null) {
            existingKey.setKeyType(type);
            if (key.startsWith(SOSKeyConstants.CERTIFICATE_HEADER)) {
                existingKey.setCertificate(key);
                if (secLvl.equals(JocSecurityLevel.HIGH) && existingKey.getKey() != null) {
                    if ((SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyAlgorythm) 
                            && (existingKey.getKey().startsWith(SOSKeyConstants.PUBLIC_PGP_KEY_HEADER) 
                                    || existingKey.getKey().startsWith(SOSKeyConstants.PUBLIC_ECDSA_KEY_HEADER))) 
                        || (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyAlgorythm) 
                                && (existingKey.getKey().startsWith(SOSKeyConstants.PUBLIC_PGP_KEY_HEADER) 
                                        || existingKey.getKey().startsWith(SOSKeyConstants.PUBLIC_RSA_KEY_HEADER)))
                        || (JocKeyType.fromValue(existingKey.getKeyType()).equals(JocKeyType.PRIVATE))) {
                        existingKey.setKey(null);
                   } 
                }
            } else {
                existingKey.setKey(key);
            }
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyAlgorythm)) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.PGP.value());
            } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyAlgorythm)) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.RSA.value());
            } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyAlgorythm)) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.ECDSA.value());
            }
            session.update(existingKey);
        } else {
            DBItemDepKeys newKey = new DBItemDepKeys();
            newKey.setKeyType(type);
            if (key.startsWith(SOSKeyConstants.CERTIFICATE_HEADER)) {
                newKey.setCertificate(key);
            } else {
                newKey.setKey(key);
            }
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyAlgorythm)) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.PGP.value());
            } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyAlgorythm)) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.RSA.value());
            } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyAlgorythm)) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.ECDSA.value());
            }
            newKey.setAccount(account);
            newKey.setSecLvl(secLvl.intValue());
            session.save(newKey);
        }
    }

    public void saveOrUpdateKey (Integer type, String key, String certificate, String account, JocSecurityLevel secLvl,
            String keyAlgorythm) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        hql.append(" and keyType = :keyType");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("secLvl", secLvl.intValue());
        query.setParameter("keyType", type);
        DBItemDepKeys existingKey =  session.getSingleResult(query);
        if (existingKey != null) {
            existingKey.setKeyType(type);
            existingKey.setCertificate(certificate);
            existingKey.setKey(key);
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyAlgorythm)) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.PGP.value());
            } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyAlgorythm)) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.RSA.value());
            } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyAlgorythm)) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.ECDSA.value());
            }
            session.update(existingKey);
        } else {
            DBItemDepKeys newKey = new DBItemDepKeys();
            newKey.setKeyType(type);
            newKey.setCertificate(certificate);
            newKey.setKey(key);
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyAlgorythm)) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.PGP.value());
            } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyAlgorythm)) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.RSA.value());
            } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyAlgorythm)) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.ECDSA.value());
            }
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
            keyPair.setKeyAlgorithm(JocKeyAlgorithm.fromValue(key.getKeyAlgorithm()).name());
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
