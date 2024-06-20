package com.sos.joc.db.keys;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
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
import com.sos.joc.db.encipherment.DBItemEncAgentCertificate;
import com.sos.joc.db.encipherment.DBItemEncCertificate;
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

    public void saveOrUpdateGeneratedKey(JocKeyPair keyPair, String account, JocSecurityLevel secLvl, String dn) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("secLvl", secLvl.intValue());
        DBItemDepKeys existingKey = session.getSingleResult(query);
        String certificate = null;
        if (keyPair.getCertificate() != null) {
            certificate = keyPair.getCertificate();
        } else {
            try {
                if(keyPair.getKeyAlgorithm().equals(JocKeyAlgorithm.ECDSA.name())) {
                    certificate = CertificateUtils.asPEMString(KeyUtil.generateCertificateFromKeyPair(KeyUtil.getKeyPairFromECDSAPrivatKeyString(
                            keyPair.getPrivateKey()), account, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, dn));
                } else {
                    certificate = CertificateUtils.asPEMString(KeyUtil.generateCertificateFromKeyPair(KeyUtil.getKeyPairFromRSAPrivatKeyString(
                            keyPair.getPrivateKey()), account, SOSKeyConstants.RSA_SIGNER_ALGORITHM, dn));
                }
            } catch (CertificateEncodingException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                LOGGER.warn("could not extract certificate from key pair. cause: ", e);
            }
        }
        
        if (existingKey != null) {
            existingKey.setKeyType(JocKeyType.PRIVATE.value());
            existingKey.setKey(keyPair.getPrivateKey());
            existingKey.setCertificate(certificate);
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
            newKey.setCertificate(certificate);
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

    public DBItemDepKeys getDbItemDepKeys(String account, JocSecurityLevel secLvl) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_DEP_KEYS);
        hql.append(" where account = :account");
        hql.append(" and secLvl = :secLvl");
        Query<DBItemDepKeys> query = session.createQuery(hql.toString());
        query.setParameter("account", account);
        query.setParameter("secLvl", secLvl.intValue());
        return session.getSingleResult(query);
    }

    public JocKeyPair getKeyPair(String account, JocSecurityLevel secLvl) throws SOSHibernateException {
        DBItemDepKeys key = getDbItemDepKeys(account, secLvl);
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
            if(!certificate.getPem().equals(keyPair.getCertificate())) {
                certificate.setCa(true);
                certificate.setKeyAlgorithm(JocKeyAlgorithm.valueOf(keyPair.getKeyAlgorithm()).value());
                certificate.setKeyType(JocKeyType.CA.value());
                certificate.setPem(keyPair.getCertificate());
                certificate.setAccount(account);
                certificate.setSecLvl(secLvl);
                session.update(certificate);
            }
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
    
    public void storeEnciphermentCertificate (String alias, String certificate, Path privateKeyPath) throws SOSHibernateException {
        if (privateKeyPath != null) {
            storeEnciphermentCertificate(alias, certificate, privateKeyPath.toString().replace('\\', '/'));
        } else {
            storeEnciphermentCertificate(alias, certificate, (String)null);
        }
    }

    public void storeEnciphermentCertificate (String alias, String certificate, String privateKeyPath) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_ENC_CERTIFICATE).append(" where ");
        hql.append(" alias = :alias");
        Query<DBItemEncCertificate> query = session.createQuery(hql.toString());
        query.setParameter("alias", alias);
        DBItemEncCertificate existingCertificate = session.getSingleResult(query);
        if(existingCertificate == null) {
            DBItemEncCertificate newCert = new DBItemEncCertificate();
            newCert.setAlias(alias);
            newCert.setCertificate(certificate);
            newCert.setPrivateKeyPath(privateKeyPath);
            session.save(newCert);
        } else {
            existingCertificate.setCertificate(certificate);
            existingCertificate.setPrivateKeyPath(privateKeyPath);
            session.update(existingCertificate);
        }
    }

    public List<DBItemEncCertificate> getAllEnciphermentCertificates() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_ENC_CERTIFICATE);
        Query<DBItemEncCertificate> query = session.createQuery(hql.toString());
        List<DBItemEncCertificate> results = session.getResultList(query);
        if(results == null || results.isEmpty()) {
            return Collections.emptyList();
        } else {
            return results;
        }
    }
    
    public List<DBItemEncCertificate> getEnciphermentCertificates(Collection<String> certAliases) throws SOSHibernateException {
        boolean withCertAliases = certAliases != null && !certAliases.isEmpty();
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_ENC_CERTIFICATE);
        if (withCertAliases) {
            hql.append(" where alias in (:aliases)");
        }
        Query<DBItemEncCertificate> query = session.createQuery(hql.toString());
        if (withCertAliases) {
            query.setParameterList("aliases", certAliases);
        }
        List<DBItemEncCertificate> results = session.getResultList(query);
        if (results == null) {
            return Collections.emptyList();
        } else {
            return results;
        }
    }
    
    public DBItemEncCertificate getEnciphermentCertificate(String certAlias) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_ENC_CERTIFICATE).append(" where ");
        hql.append(" alias = :alias");
        Query<DBItemEncCertificate> query = session.createQuery(hql.toString());
        query.setParameter("alias", certAlias);
        return session.getSingleResult(query);
    }
    
    public void removeAllEnciphermentCertificateMappingsByAgent(String certAlias) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_ENC_AGENT_CERTIFICATES).append(" where ");
        hql.append(" certAlias = :certAlias");
        Query<DBItemEncAgentCertificate> query = session.createQuery(hql.toString());
        query.setParameter("certAlias", certAlias);
        session.executeUpdate(query);
    }

    public void removeEnciphermentCertificateMapping(String certAlias, String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_ENC_AGENT_CERTIFICATES);
        hql.append(" where certAlias = :certAlias");
        hql.append(" and agentId = :agentId");
        Query<DBItemEncAgentCertificate> query = session.createQuery(hql.toString());
        query.setParameter("certAlias", certAlias);
        query.setParameter("agentId", agentId);
        DBItemEncAgentCertificate result = session.getSingleResult(query);
        if (result != null) {
            session.delete(result);
        }
    }

    public List<DBItemEncAgentCertificate> getEnciphermentCertificateMappings(String certAlias) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_ENC_AGENT_CERTIFICATES).append(" where ");
        hql.append(" certAlias = :certAlias");
        Query<DBItemEncAgentCertificate> query = session.createQuery(hql.toString());
        query.setParameter("certAlias", certAlias);
        List<DBItemEncAgentCertificate> results = session.getResultList(query);
        if(results == null) {
            results = Collections.emptyList();
        }
        return results;
    }

    public List<DBItemEncAgentCertificate> getEnciphermentCertificateMappingsByAgents(Collection<String> agentIds) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_ENC_AGENT_CERTIFICATES);
        if (agentIds != null && !agentIds.isEmpty()) {
            hql.append(" where agentId in (:agentIds)");
        }
        Query<DBItemEncAgentCertificate> query = session.createQuery(hql.toString());
        if (agentIds != null && !agentIds.isEmpty()) {
            query.setParameterList("agentIds", agentIds);
        }
        List<DBItemEncAgentCertificate> results = session.getResultList(query);
        if (results == null) {
            results = Collections.emptyList();
        }
        return results;
    }

    public List<DBItemEncAgentCertificate> getEnciphermentCertificateMappingsByCertAliases(Collection<String> certAliases) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_ENC_AGENT_CERTIFICATES);
        if (certAliases != null && !certAliases.isEmpty()) {
            hql.append(" where certAlias in (:certAliases)");
        }
        Query<DBItemEncAgentCertificate> query = session.createQuery(hql.toString());
        if (certAliases != null && !certAliases.isEmpty()) {
            query.setParameterList("certAlias", certAliases);
        }
        List<DBItemEncAgentCertificate> results = session.getResultList(query);
        if(results == null) {
            results = Collections.emptyList();
        }
        return results;
    }

    public List<DBItemEncAgentCertificate> getAllEnciphermentCertificateMappings() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_ENC_AGENT_CERTIFICATES);
        Query<DBItemEncAgentCertificate> query = session.createQuery(hql.toString());
        List<DBItemEncAgentCertificate> results = session.getResultList(query);
        if(results == null) {
            results = Collections.emptyList();
        }
        return results;
    }
    
    public void addEnciphermentCertificateMapping(String certAlias, String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ").append(DBLayer.DBITEM_ENC_AGENT_CERTIFICATES).append(" where ");
        hql.append(" certAlias = :certAlias and ");
        hql.append(" agentId = :agentId");
        Query<DBItemEncAgentCertificate> query = session.createQuery(hql.toString());
        query.setParameter("certAlias", certAlias);
        query.setParameter("agentId", agentId);
        DBItemEncAgentCertificate result = session.getSingleResult(query);
        if(result == null) {
            DBItemEncAgentCertificate newItem = new DBItemEncAgentCertificate();
            newItem.setAgentId(agentId);
            newItem.setCertAlias(certAlias);
            session.save(newItem);
        } else {
            LOGGER.warn("a certificate mapping for this certificate and agent already exists.");
        }
    }
}
