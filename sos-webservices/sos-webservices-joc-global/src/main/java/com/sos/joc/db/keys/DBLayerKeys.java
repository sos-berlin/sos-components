package com.sos.joc.db.keys;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDepKeys;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.sign.JocKeyAlgorithm;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;

public class DBLayerKeys {

    private final SOSHibernateSession session;
    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerKeys.class);

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

    public void saveOrUpdateKey(DBItemDepKeys key) throws SOSHibernateException {
        if (key.getId() != null && key.getId() != 0L) {
            session.update(key);
        } else {
            session.save(key);
        }
    }

    public void saveOrUpdateGeneratedKey(JocKeyPair keyPair, String account, JocSecurityLevel secLvl) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("secLvl", secLvl.intValue());
        DBItemDepKeys existingKey = session.getSingleResult(query);
        if (existingKey != null) {
            existingKey.setKeyType(JocKeyType.PRIVATE.value());
            existingKey.setKey(keyPair.getPrivateKey());
            if (keyPair.getCertificate() != null) {
                existingKey.setCertificate(keyPair.getCertificate());
            } else {
                try {
                    if(keyPair.getKeyAlgorithm().equals(JocKeyAlgorithm.ECDSA.name())) {
                        existingKey.setCertificate(CertificateUtils.asPEMString(KeyUtil.generateCertificateFromKeyPair(KeyUtil
                                .getKeyPairFromECDSAPrivatKeyString(keyPair.getPrivateKey()), account, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM)));
                    } else {
                        existingKey.setCertificate(CertificateUtils.asPEMString(KeyUtil.generateCertificateFromKeyPair(KeyUtil
                                .getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey()), account)));
                    }
                } catch (CertificateEncodingException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                    LOGGER.warn("could not extract certificate from key pair. cause: ", e);
                    existingKey.setCertificate(null);
                }
            }
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
            if(keyPair.getCertificate() != null) {
                newKey.setCertificate(keyPair.getCertificate());
            } else {
                try {
                    if(keyPair.getKeyAlgorithm().equals(JocKeyAlgorithm.ECDSA.name())) {
                        newKey.setCertificate(CertificateUtils.asPEMString(KeyUtil.generateCertificateFromKeyPair(KeyUtil
                                .getKeyPairFromECDSAPrivatKeyString(keyPair.getPrivateKey()), account, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM)));
                    } else {
                        newKey.setCertificate(CertificateUtils.asPEMString(KeyUtil.generateCertificateFromKeyPair(KeyUtil
                                .getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey()), account)));
                    }
                } catch (CertificateEncodingException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                    LOGGER.warn("could not extract certificate from key pair. cause: ", e);
                    newKey.setCertificate(null);
                }
            }
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

    public void saveOrUpdateKey(Integer type, String key, String account, JocSecurityLevel secLvl, String keyAlgorythm) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        hql.append(" and keyType = :keyType");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("secLvl", secLvl.intValue());
        query.setParameter("keyType", type);
        DBItemDepKeys existingKey = session.getSingleResult(query);
        if (existingKey != null) {
            existingKey.setKeyType(type);
            if (key.startsWith(SOSKeyConstants.CERTIFICATE_HEADER)) {
                existingKey.setCertificate(key);
                if (secLvl.equals(JocSecurityLevel.HIGH) && existingKey.getKey() != null) {
                    if ((SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyAlgorythm) && (existingKey.getKey().startsWith(
                            SOSKeyConstants.PUBLIC_PGP_KEY_HEADER) || existingKey.getKey().startsWith(SOSKeyConstants.PUBLIC_ECDSA_KEY_HEADER)))
                            || (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyAlgorythm) && (existingKey.getKey().startsWith(
                                    SOSKeyConstants.PUBLIC_PGP_KEY_HEADER) || existingKey.getKey().startsWith(SOSKeyConstants.PUBLIC_RSA_KEY_HEADER)))
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

    public void saveOrUpdateKey(Integer type, String key, String certificate, String account, JocSecurityLevel secLvl, String keyAlgorithm)
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
        DBItemDepKeys existingKey = session.getSingleResult(query);
        if (existingKey != null) {
            existingKey.setKeyType(type);
            existingKey.setCertificate(certificate);
            existingKey.setKey(key);
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyAlgorithm)) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.PGP.value());
            } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyAlgorithm)) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.RSA.value());
            } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyAlgorithm)) {
                existingKey.setKeyAlgorithm(JocKeyAlgorithm.ECDSA.value());
            }
            session.update(existingKey);
        } else {
            DBItemDepKeys newKey = new DBItemDepKeys();
            newKey.setKeyType(type);
            newKey.setCertificate(certificate);
            newKey.setKey(key);
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyAlgorithm)) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.PGP.value());
            } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyAlgorithm)) {
                newKey.setKeyAlgorithm(JocKeyAlgorithm.RSA.value());
            } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyAlgorithm)) {
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
            if (key.getKeyType() == JocKeyType.PRIVATE.value()) {
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

    public List<DBItemDepKeys> getDBItemDepKeys(JocSecurityLevel secLvl) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where secLvl = :secLvl");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("secLvl", secLvl.intValue());
        List<DBItemDepKeys> listOfDBItemDepKeys = session.getResultList(query);
        return listOfDBItemDepKeys == null ? Collections.emptyList() : listOfDBItemDepKeys;
    }

    public JocKeyPair getAuthRootCaKeyPair() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where keyType = 3");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setMaxResults(1);
        DBItemDepKeys key = session.getSingleResult(query);
        if (key != null) {
            JocKeyPair keyPair = new JocKeyPair();
            keyPair.setPrivateKey(key.getKey());
            keyPair.setCertificate(key.getCertificate());
            keyPair.setKeyAlgorithm(JocKeyAlgorithm.fromValue(key.getKeyAlgorithm()).name());
            keyPair.setKeyType(JocKeyType.fromValue(key.getKeyType()).name());
            return keyPair;
        }
        return null;
    }

    public List<DBItemInventoryCertificate> getSigningRootCaCertificates() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_INV_CERTS);
        hql.append(" where ca = 1");
        Query<DBItemInventoryCertificate> query = session.createQuery(hql.toString());
        List<DBItemInventoryCertificate> listOfDBItemDepKeys = session.getResultList(query);
        return listOfDBItemDepKeys == null ? Collections.emptyList() : listOfDBItemDepKeys;
    }

    public DBItemInventoryCertificate getSigningRootCaCertificate(String account) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_INV_CERTS);
        hql.append(" where ca = 1");
        hql.append(" and account = :account");
        Query<DBItemInventoryCertificate> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setMaxResults(1);
        DBItemInventoryCertificate key = session.getSingleResult(query);
        return key;
    }

    public void saveOrUpdateSigningRootCaCertificate(JocKeyPair keyPair, String account, Integer secLvl) throws SOSHibernateException {
        DBItemInventoryCertificate certificate = getSigningRootCaCertificate(account);
        if (certificate != null) {
            certificate.setCa(true);
            certificate.setKeyAlgorithm(JocKeyAlgorithm.valueOf(keyPair.getKeyAlgorithm()).value());
            certificate.setKeyType(JocKeyType.CA.value());
            certificate.setPem(keyPair.getCertificate());
            certificate.setAccount(account);
            certificate.setSecLvl(secLvl);
            session.update(certificate);
        } else {
            DBItemInventoryCertificate newCertificate = new DBItemInventoryCertificate();
            newCertificate.setCa(true);
            newCertificate.setKeyAlgorithm(JocKeyAlgorithm.valueOf(keyPair.getKeyAlgorithm()).value());
            newCertificate.setKeyType(JocKeyType.CA.value());
            newCertificate.setPem(keyPair.getCertificate());
            newCertificate.setAccount(account);
            newCertificate.setSecLvl(secLvl);
            session.save(newCertificate);
        }
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

    public int deleteKeyByAccount(String accountName) throws SOSHibernateException {
        String hql = "delete " + DBLayer.DBITEM_DEP_KEYS + " where account=:accountName";
        Query<DBItemDepKeys> query = session.createQuery(hql);
        query.setParameter("accountName", accountName);
        int row = session.executeUpdate(query);
        return row;
    }

    public int deleteCertByAccount(String accountName) throws SOSHibernateException {
        String hql = "delete " + DBLayer.DBITEM_INV_CERTS + " where account=:accountName";
        Query<DBItemInventoryCertificate> query = session.createQuery(hql);
        query.setParameter("accountName", accountName);
        int row = session.executeUpdate(query);
        return row;
    }

}
