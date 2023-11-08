package com.sos.joc.classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.joc.Globals;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.cluster.NewJocAddedEvent;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class JocCertificate {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocCertificate.class);
    private static JocCertificate jocCert;

    private JocCertificate() {
        EventBus.getInstance().register(this);
    }

    public static synchronized JocCertificate getInstance() {
        if (jocCert == null) {
            jocCert = new JocCertificate();
        }
        return jocCert;
    }

    public static synchronized void updateCertificate() {
        try {
            JocCertificate jocCertificate = JocCertificate.getInstance();
            jocCertificate.updateCertificate(null, Globals.getClusterId(), Globals.getMemberId(), Globals.getOrdering());
        } catch (Exception e) {
            LOGGER.warn(e.toString(), e);
        }
    }

    @Subscribe({ NewJocAddedEvent.class })
    public synchronized void updateCertificate(NewJocAddedEvent event) {
        updateCertificate(event.getJocId(), event.getClusterId(), event.getMemberId(), event.getOrdering());
    }

    private synchronized void updateCertificate(Long jocId, String clusterId, String memberId, Integer ordering) {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(JocCertificate.class.getSimpleName());
            JocInstancesDBLayer dbLayer = new JocInstancesDBLayer(session);
            DBItemJocInstance dbItem = null;
            if (jocId != null) {
                dbItem = session.get(DBItemJocInstance.class, jocId);
            } else {
                dbItem = dbLayer.getInstance(clusterId, ordering);
            }
            update(dbItem, session);
        } catch (SOSHibernateException | DBConnectionRefusedException | DBInvalidDataException e) {
            LOGGER.warn(String.format("JOC instance with clusterId=%1$s and memberId=%2$s not found in DB.", clusterId, memberId));
        } finally {
            Globals.disconnect(session);
        }
    }

    private synchronized void update(DBItemJocInstance jocInstance, SOSHibernateSession session) {
        Path keyStorePath = Globals.sosCockpitProperties.resolvePath(Globals.sosCockpitProperties.getProperty("keystore_path"));
        LOGGER.debug("KeyStore path: " + keyStorePath);
        String keyStorePw = Globals.sosCockpitProperties.getProperty("keystore_password", "jobscheduler");
        String keyStoreType = Globals.sosCockpitProperties.getProperty("keystore_type", "PKCS12");
        LOGGER.debug("KeyStore type: " + keyStoreType);
        String keyStoreAlias = Globals.sosCockpitProperties.getProperty("keystore_alias", "");
        LOGGER.debug("KeyStore alias: " + keyStoreAlias);
        if (keyStorePath != null && Files.exists(keyStorePath)) {
            LOGGER.debug("reading KeyStore from " + keyStorePath);
            try {
                KeyStore keystore = KeyStoreUtil.readKeyStore(keyStorePath, KeystoreType.fromValue(keyStoreType), keyStorePw);
                if (keystore != null) {
                    X509Certificate certificate = KeyStoreUtil.getX509CertificateFromKeyStore(keystore, keyStoreAlias);
                    if (certificate != null) {
                        String dn = CertificateUtils.getDistinguishedName(certificate);
                        LOGGER.debug(String.format("Certificate: %1$s read from Keystore", dn));
                        if (jocInstance != null) {
                            if (certificate != null) {
                                X509Certificate dbCert = null;
                                if (jocInstance.getCertificate() != null) {
                                    dbCert = KeyUtil.getX509Certificate(jocInstance.getCertificate());
                                }
                                if ((dbCert != null && !dbCert.equals(certificate)) || dbCert == null) {
                                    jocInstance.setCertificate(CertificateUtils.asPEMString(certificate));
                                    session.update(jocInstance);
                                    LOGGER.debug("new certificate stored in DB.");
                                }
                            }
                        }
                    }

                }
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | SOSHibernateException e) {
                LOGGER.warn("Couldn´t read KeyStore from " + keyStorePath.toString());
            }
        }
    }

}
