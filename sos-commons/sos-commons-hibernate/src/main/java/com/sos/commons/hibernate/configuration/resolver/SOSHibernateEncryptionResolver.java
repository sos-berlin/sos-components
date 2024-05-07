package com.sos.commons.hibernate.configuration.resolver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.commons.util.SOSString;

public class SOSHibernateEncryptionResolver implements ISOSHibernateConfigurationResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateEncryptionResolver.class);

    private static final String ENCRYPTION_IDENTIFIER = "enc://";

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

        EncryptedValue url = parse(configuration, SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL);
        EncryptedValue username = parse(configuration, SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME);
        EncryptedValue password = parse(configuration, SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD);
        if (password != null || url != null || username != null) {
            try {
                PrivateKey privKey = getPrivateKey(configuration);
                if (privKey == null) {
                    throw new SOSHibernateConfigurationException("encrypted values found, but no private key provided for decryption!");
                }

                if (url != null) {
                    configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL, decrypt(url, privKey));
                }
                if (username != null) {
                    configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME, decrypt(username, privKey));
                }
                if (password != null) {
                    configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD, decrypt(password, privKey));
                }
            } catch (SOSHibernateConfigurationException e) {
                throw e;
            } catch (Throwable e) {
                throw new SOSHibernateConfigurationException(e.toString(), e);
            }
        }
        return configuration;
    }

    private EncryptedValue parse(Configuration configuration, String configurationPropertyName) throws SOSHibernateConfigurationException {
        String configurationPropertyValue = configuration.getProperty(configurationPropertyName);
        if (!hasEncryptionIdentifier(configurationPropertyValue)) {
            return null;
        }
        String[] arr = configurationPropertyValue.replace(ENCRYPTION_IDENTIFIER, "").split(" ");
        if (arr.length < 3) {
            throw new SOSHibernateConfigurationException(String.format("[%s][%s]%s of 3 required values found", configurationPropertyName,
                    configurationPropertyValue, arr.length));
        }
        return new EncryptedValue(configurationPropertyName, configurationPropertyValue, arr);
    }

    private PrivateKey getPrivateKey(Configuration configuration) throws SOSHibernateConfigurationException {
        String privateKeyPath = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DECRYPTION_PRIVATE_KEY);
        if (privateKeyPath != null && !Files.exists(Paths.get(privateKeyPath))) {
            throw new SOSHibernateConfigurationException("File with path - " + privateKeyPath + " - does not exist.");
        }
        // preferred configured in file, fallback configured by setters
        String keystorePath = getValue(configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_KEYSTORE), getKeystorePath());
        if (privateKeyPath != null && keystorePath != null) {
            throw new SOSHibernateConfigurationException(
                    "key path and keystore path found. These configuration items are exclusive. Please do configure only one of them.");
        }

        // preferred configured in file, fallback configured by setters
        String keystoreType = getValue(configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_KEYSTORE_TYPE), getKeystoreType());
        String keystorePwd = getValue(configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_KEYSTORE_PWD), getKeystorePassword());
        String keystoreKeyPwd = getValue(configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_KEYSTORE_KEYPWD), getKeystoreKeyPassword());
        String keystoreAlias = getValue(configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_KEYSTORE_KEYALIAS), getKeystoreKeyAlias());

        PrivateKey privKey = null;
        try {
            if (privateKeyPath != null) {
                String privateKeyPwd = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_DECRYPTION_PRIVATE_KEYPWD);
                if (!SOSString.isEmpty(privateKeyPwd)) {
                    privKey = KeyUtil.getPrivateEncryptedKey(Files.readString(Paths.get(privateKeyPath)), privateKeyPwd);
                } else {
                    privKey = KeyUtil.getPrivateKeyFromString(Files.readString(Paths.get(privateKeyPath)));
                }
            } else if (keystorePath != null) {
                KeystoreType type = null;
                if (keystoreType == null) {
                    type = KeystoreType.PKCS12;
                } else {
                    type = KeystoreType.valueOf(keystoreType);
                }
                KeyStore keystore = KeyStoreUtil.readKeyStore(keystorePath, type, keystorePwd);
                if (keystoreAlias == null) {
                    Enumeration<String> aliases = keystore.aliases();
                    if (aliases.hasMoreElements()) {
                        String alias = aliases.nextElement();
                        if (keystore.isKeyEntry(alias)) {
                            keystoreAlias = alias;
                            LOGGER.debug(String.format("no key alias configured, use first found alias <%1$s> to retrieve key.", keystoreAlias));
                        }
                    }
                }
                if (keystoreKeyPwd != null) {
                    privKey = (PrivateKey) keystore.getKey(keystoreAlias, keystoreKeyPwd.toCharArray());
                } else {
                    privKey = (PrivateKey) keystore.getKey(keystoreAlias, "".toCharArray());
                }
            }
        } catch (Throwable e) {
            throw new SOSHibernateConfigurationException(e.toString(), e);
        }
        return privKey;
    }

    private String decrypt(EncryptedValue encryptedValue, PrivateKey privKey) throws SOSHibernateConfigurationException {
        try {
            return enOrDecrypt(encryptedValue.encryptedValue, getSecretKey(encryptedValue.symmetricKey, privKey), encryptedValue.base64encodedIv);
        } catch (Throwable e) {
            throw new SOSHibernateConfigurationException(String.format("[%s][%s]%s", encryptedValue.configurationPropertyName,
                    encryptedValue.configurationPropertyValue, e.toString()), e);
        }
    }

    private String enOrDecrypt(String encryptedValue, SecretKey key, String base64encodedIv) throws InvalidKeyException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        return EncryptionUtils.enOrDecrypt(EncryptionUtils.CIPHER_ALGORITHM, encryptedValue, key, new IvParameterSpec(Base64.getDecoder().decode(
                base64encodedIv)), Cipher.DECRYPT_MODE);
    }

    private SecretKey getSecretKey(String symmetricKey, PrivateKey privKey) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        return new SecretKeySpec(EncryptionUtils.decryptSymmetricKey(symmetricKey.getBytes(), privKey), EncryptionUtils.CIPHER_ALGORITHM);
    }

    private boolean hasEncryptionIdentifier(String val) {
        return val != null && val.startsWith(ENCRYPTION_IDENTIFIER);
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

    private class EncryptedValue {

        private final String configurationPropertyName;
        private final String configurationPropertyValue;

        private final String symmetricKey;
        private final String base64encodedIv;
        private final String encryptedValue;

        private EncryptedValue(String configurationPropertyName, String configurationPropertyValue, String[] values) {
            this.configurationPropertyName = configurationPropertyName;
            this.configurationPropertyValue = configurationPropertyValue;

            symmetricKey = values[0];
            base64encodedIv = values[1];
            encryptedValue = values[2];
        }
    }

}
