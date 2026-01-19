package com.sos.commons.hibernate.configuration.resolver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;

import org.hibernate.cfg.Configuration;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.common.EncryptedValue;
import com.sos.commons.encryption.decrypt.Decrypt;
import com.sos.commons.encryption.exception.SOSEncryptionException;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.util.SOSString;

public class SOSHibernateEncryptionResolver implements ISOSHibernateConfigurationResolver {


    private String keystorePath;
    private String keystoreType;
    private String keystorePassword;
    private String keystoreKeyPassword;
    private String keystoreKeyAlias;

    /** The credentials can be configured in:
     * - the hibernate configuration file
     * - joc.properties
     * - private.conf of an agent
     * determined by the calling application
     */
    @Override
    public Configuration resolve(Configuration configuration) throws SOSHibernateConfigurationException {
        if (configuration == null) {
            return configuration;
        }

        EncryptedValue url = null;
        EncryptedValue username = null;
        EncryptedValue password = null; 
        try {
            if(hasEncryptedValue(configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL))) {
                url = EncryptedValue.getInstance(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL, 
                        configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL));
            }
            if(hasEncryptedValue(configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME))) {
                username = EncryptedValue.getInstance(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME, 
                        configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME));
            }
            if(hasEncryptedValue(configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD))) {
                password = EncryptedValue.getInstance(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD, 
                        configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD));
            }
        } catch (SOSEncryptionException e) {
            throw new SOSHibernateConfigurationException(e.toString(), e);
        }
        if (password != null || url != null || username != null) {
            try {
                PrivateKey privKey = getPrivateKey(configuration);
                if (privKey == null) {
                    throw new SOSHibernateConfigurationException("encrypted values found, but no private key provided for decryption!");
                }

                if (url != null) {
                    configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL, Decrypt.decrypt(url, privKey));
                }
                if (username != null) {
                    configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME, Decrypt.decrypt(username, privKey));
                }
                if (password != null) {
                    configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD, Decrypt.decrypt(password, privKey));
                }
            } catch (SOSHibernateConfigurationException e) {
                throw e;
            } catch (Exception e) {
                throw new SOSHibernateConfigurationException(e.toString(), e);
            }
        }
        return configuration;
    }

    private PrivateKey getPrivateKey(Configuration configuration) throws SOSHibernateConfigurationException {
        String privateKeyPath = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DECRYPTION_PRIVATE_KEY);
        // preferred configured in file, fallback configured by setters
        String keystorePath = getValue(configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_KEYSTORE), getKeystorePath());
        String privateKeyPwd = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DECRYPTION_PRIVATE_KEYPWD);

        if (privateKeyPath != null && keystorePath != null) {
            throw new SOSHibernateConfigurationException(
                    "key path and keystore path found. These configuration items are exclusive. Please do configure only one of them.");
        }
        try {
            if(privateKeyPath != null) {
                if (privateKeyPath != null && !Files.exists(Paths.get(privateKeyPath))) {
                    throw new SOSHibernateConfigurationException("File with path - " + privateKeyPath + " - does not exist.");
                }
                if (!SOSString.isEmpty(privateKeyPwd)) {
                    return KeyUtil.getPrivateKey(privateKeyPath, privateKeyPwd);
                } else {
                    return KeyUtil.getPrivateKey(privateKeyPath);
                }
            }
            if (keystorePath != null) {
                if (keystorePath != null && !Files.exists(Paths.get(keystorePath))) {
                    throw new SOSHibernateConfigurationException("File with path - " + keystorePath + " - does not exist.");
                }
                // preferred configured in file, fallback configured by setters
                String keystoreType = getValue(configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_KEYSTORE_TYPE), getKeystoreType());
                String keystorePwd = getValue(configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_KEYSTORE_PWD), getKeystorePassword());
                String keystoreKeyPwd = getValue(configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_KEYSTORE_KEYPWD), getKeystoreKeyPassword());
                String keystoreAlias = getValue(configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_KEYSTORE_KEYALIAS), getKeystoreKeyAlias());

                return KeyUtil.getPrivateKey(keystorePath, keystoreType, keystorePwd, keystoreKeyPwd, keystoreAlias);
            }
        } catch (Exception e) {
            throw new SOSHibernateConfigurationException(e.toString(), e);
        }
        return null;
    }

    private String getValue(String val, String defaultVal) {
        return val == null ? defaultVal : val;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String val) {
        keystorePath = val;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(String val) {
        keystoreType = val;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String val) {
        keystorePassword = val;
    }

    public String getKeystoreKeyPassword() {
        return keystoreKeyPassword;
    }

    public void setKeystoreKeyPassword(String val) {
        keystoreKeyPassword = val;
    }

    public String getKeystoreKeyAlias() {
        return keystoreKeyAlias;
    }

    public void setKeystoreKeyAlias(String val) {
        keystoreKeyAlias = val;
    }

    private boolean hasEncryptedValue(String propertyValue) {
        return (propertyValue != null && propertyValue.startsWith(EncryptionUtils.ENCRYPTION_IDENTIFIER));
    }
}
