package com.sos.joc.publish.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.filetransfer.FileTransferJobResource;
import com.sos.inventory.model.filetransfer.fragments.AFragment;
import com.sos.inventory.model.filetransfer.fragments.cs.CredentialStoreFragment;
import com.sos.inventory.model.filetransfer.fragments.decryption.DecryptionFragment;
import com.sos.inventory.model.filetransfer.fragments.mail.MailFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.AProtocolFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.AzureBlobStorageFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.FtpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.HttpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.JumpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.SmbFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.WebdavFragment;
import com.sos.inventory.model.filetransfer.fragments.proxy.HttpProxyFragment;
import com.sos.inventory.model.filetransfer.fragments.proxy.SocksProxyFragment;
import com.sos.inventory.model.filetransfer.profile.Profile;
import com.sos.inventory.model.filetransfer.profile.fragments.Fragment;
import com.sos.inventory.model.filetransfer.settings.AlternativeProfiles;
import com.sos.inventory.model.filetransfer.settings.AzureBlobStorageFragments;
import com.sos.inventory.model.filetransfer.settings.CredentialStoreFragments;
import com.sos.inventory.model.filetransfer.settings.DecryptionFragments;
import com.sos.inventory.model.filetransfer.settings.FtpFragments;
import com.sos.inventory.model.filetransfer.settings.HttpFragments;
import com.sos.inventory.model.filetransfer.settings.HttpProxyFragments;
import com.sos.inventory.model.filetransfer.settings.JumpFragments;
import com.sos.inventory.model.filetransfer.settings.MailFragments;
import com.sos.inventory.model.filetransfer.settings.Settings;
import com.sos.inventory.model.filetransfer.settings.SftpFragments;
import com.sos.inventory.model.filetransfer.settings.SmbFragments;
import com.sos.inventory.model.filetransfer.settings.SocksProxyFragments;
import com.sos.inventory.model.filetransfer.settings.WebdavFragments;
import com.sos.joc.Globals;
import com.sos.joc.classes.filetransfer.FileTransferHelper;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.publish.db.DBLayerDeploy;

// TODO alternative profile - source table?
public class FileTransferJobResourceUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferJobResourceUtils.class);

    // TODO - remove duplicate definition - see FileTransferValidator
    private static final String PROPERTY_NAME_FRAGMENT_CREDENTIAL_STORE = "Credential Store Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_DECRYPTION = "Decryption Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_MAIL = "Mail Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_HTTP_PROXY = "HTTP Proxy Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_SOCKS_PROXY = "SOCKS Proxy Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_AZURE_BLOB_STORAGE = "Azure Blob Storage Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_FTP = "FTP Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_HTTP = "HTTP Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_JUMP = "Jump Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_SFTP = "SFTP Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_SMB = "SMB Fragment";
    private static final String PROPERTY_NAME_FRAGMENT_WEBDAV = "WebDAV Fragment";
    // private static final String PROPERTY_NAME_ALTERNATIVE = "alternative";

    // TODO remove it
    private static final boolean testMode = false;

    public static String completeForDeploy(DBItemInventoryConfiguration c, DBLayerDeploy dbLayer) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[completeForDeploy]" + c.getPath());
        }

        FileTransferJobResource ft = FileTransferHelper.toFileTransferJobResource(c.getContent());
        Profile p = ft.getProfile();
        if (p == null) {
            throw new Exception("Missing profile");
        }
        if (p.getSource() == null) {
            throw new Exception("Missing profile.source");
        }

        Settings s = new Settings();
        s.setProfileName(c.getName());

        resolveProfile(dbLayer, c.getPath(), s, p);

        ft.setSettings(s);
        return Globals.objectMapper.writeValueAsString(ft);
    }

    // All referenced fragments should already have been released
    private static void resolveProfile(DBLayerDeploy dbLayer, String path, Settings s, Profile p) throws Exception {
        boolean hasAlternativeName = !SOSString.isEmpty(p.getAlternativeProfileName()) && !p.getAlternativeProfileName().equalsIgnoreCase(s
                .getProfileName());
        boolean hasNotificationTriggers = p.getNotificationTriggers() != null;
        boolean isSourceFragmentLocal = FileTransferHelper.isSourceFragmentLocal(p.getSource());
        boolean isTargetFragmentNullOrLocal = FileTransferHelper.isTargetFragmentNullOrLocal(p.getTarget());
        if (!hasAlternativeName && !hasNotificationTriggers && isSourceFragmentLocal && isTargetFragmentNullOrLocal) {
            // no references for settings
            return;
        }

        // 1) Source fragment
        setSettingsFragment(dbLayer, path, s, p.getSource().getFragment());

        // 2) Target fragment
        if (!isTargetFragmentNullOrLocal) {
            setSettingsFragment(dbLayer, path, s, p.getTarget().getFragment());
        }

        // 3) Notification triggers
        if (hasNotificationTriggers) {
            if (p.getNotificationTriggers().getOnError() != null) {
                resolveMailFragment(dbLayer, path, s, p.getNotificationTriggers().getOnError().getMailFragmentName());
            }
            if (p.getNotificationTriggers().getOnSuccess() != null) {
                resolveMailFragment(dbLayer, path, s, p.getNotificationTriggers().getOnSuccess().getMailFragmentName());
            }
            if (p.getNotificationTriggers().getOnEmptyFiles() != null) {
                resolveMailFragment(dbLayer, path, s, p.getNotificationTriggers().getOnSuccess().getMailFragmentName());
            }
        }

        // 4) Alternative profile
        if (hasAlternativeName) {
            // alternative profiles should already have been deployed... how ? - they reference each other...
            if (s.getAlternativeProfiles() == null) {
                s.setAlternativeProfiles(new AlternativeProfiles());
            } else {
                // already processed
                if (s.getAlternativeProfiles().getAdditionalProperties().containsKey(p.getAlternativeProfileName())) {
                    return;
                }
            }

            // Profile pa = null;
            // s.getAlternativeProfiles().getAdditionalProperties().put(p.getAlternativeProfileName(), pa);
            // resolveProfile(dbLayer, s, pa);
        }
    }

    private static void setSettingsFragment(DBLayerDeploy dbLayer, String path, Settings s, Fragment f) throws Exception {
        if (FileTransferHelper.isSftp(f)) {
            resolveSftpFragment(dbLayer, path, s, f.getSftp().getName());
        } else if (FileTransferHelper.isSmb(f)) {
            resolveSmbFragment(dbLayer, path, s, f.getSmb().getName());
        } else if (FileTransferHelper.isFtp(f)) {
            resolveFtpFragment(dbLayer, path, s, f.getFtp().getName());
        } else if (FileTransferHelper.isWebdav(f)) {
            resolveWebdavFragment(dbLayer, path, s, f.getWebdav().getName());
        } else if (FileTransferHelper.isHttp(f)) {
            resolveHttpFragment(dbLayer, path, s, f.getHttp().getName());
        } else if (FileTransferHelper.isAzureBlobStorage(f)) {
            resolveAzureBlobStorageFragment(dbLayer, path, s, f.getAzureBlobStorage().getName());
        }
    }

    private static void resolveCommonFragmentReferences(DBLayerDeploy dbLayer, String path, Settings s, AProtocolFragment f) throws Exception {
        resolveCredentialStoreFragment(dbLayer, path, s, f.getCredentialStoreFragmentName());
        resolveDecryptionFragment(dbLayer, path, s, f.getDecryptionFragmentName());
    }

    private static void resolveCredentialStoreFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getCredentialStoreFragments() == null) {
            s.setCredentialStoreFragments(new CredentialStoreFragments());
        } else {
            // already processed
            if (s.getCredentialStoreFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        CredentialStoreFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_CREDENTIAL_STORE, name,
                ConfigurationType.FT_FRAGMENT_CREDENTIAL_STORE, CredentialStoreFragment.class);
        f.setVersion(null);
        s.getCredentialStoreFragments().getAdditionalProperties().put(name, f);
    }

    private static void resolveDecryptionFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getDecryptionFragments() == null) {
            s.setDecryptionFragments(new DecryptionFragments());
        } else {
            // already processed
            if (s.getDecryptionFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        DecryptionFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_DECRYPTION, name, ConfigurationType.FT_FRAGMENT_DECRYPTION,
                DecryptionFragment.class);
        f.setVersion(null);
        s.getDecryptionFragments().getAdditionalProperties().put(name, f);
    }

    private static void resolveMailFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getMailFragments() == null) {
            s.setMailFragments(new MailFragments());
        } else {
            // already processed
            if (s.getMailFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        MailFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_MAIL, name, ConfigurationType.FT_FRAGMENT_MAIL,
                MailFragment.class);
        f.setVersion(null);
        s.getMailFragments().getAdditionalProperties().put(name, f);
    }

    private static void resolveHttpProxyFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getHttpProxyFragments() == null) {
            s.setHttpProxyFragments(new HttpProxyFragments());
        } else {
            // already processed
            if (s.getHttpProxyFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        HttpProxyFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_HTTP_PROXY, name, ConfigurationType.FT_FRAGMENT_HTTP_PROXY,
                HttpProxyFragment.class);
        f.setVersion(null);
        s.getHttpProxyFragments().getAdditionalProperties().put(name, f);
    }

    private static void resolveSocksProxyFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getSocksProxyFragments() == null) {
            s.setSocksProxyFragments(new SocksProxyFragments());
        } else {
            // already processed
            if (s.getSocksProxyFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        SocksProxyFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_SOCKS_PROXY, name, ConfigurationType.FT_FRAGMENT_SOCKS_PROXY,
                SocksProxyFragment.class);
        f.setVersion(null);
        s.getSocksProxyFragments().getAdditionalProperties().put(name, f);
    }

    private static void resolveJumpFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getJumpFragments() == null) {
            s.setJumpFragments(new JumpFragments());
        } else {
            // already processed
            if (s.getJumpFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        JumpFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_JUMP, name, ConfigurationType.FT_FRAGMENT_JUMP,
                JumpFragment.class);
        f.setVersion(null);
        s.getJumpFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, path, s, f);
        resolveHttpProxyFragment(dbLayer, path, s, f.getHttpProxyFragmentName());
        resolveSocksProxyFragment(dbLayer, path, s, f.getSocksProxyFragmentName());
        resolveJumpFragment(dbLayer, path, s, f.getAlternativeJumpFragmentName());
    }

    private static void resolveAzureBlobStorageFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getAzureBlobStorageFragments() == null) {
            s.setAzureBlobStorageFragments(new AzureBlobStorageFragments());
        } else {
            // already processed
            if (s.getAzureBlobStorageFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        AzureBlobStorageFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_AZURE_BLOB_STORAGE, name,
                ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE, AzureBlobStorageFragment.class);
        f.setVersion(null);
        s.getAzureBlobStorageFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, path, s, f);
        resolveHttpProxyFragment(dbLayer, path, s, f.getHttpProxyFragmentName());
        resolveJumpFragment(dbLayer, path, s, f.getJumpFragmentName());

        resolveAzureBlobStorageFragment(dbLayer, path, s, f.getAlternativeAzureBlobStorageFragmentName());
    }

    private static void resolveFtpFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getFtpFragments() == null) {
            s.setFtpFragments(new FtpFragments());
        } else {
            // already processed
            if (s.getFtpFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        FtpFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_FTP, name, ConfigurationType.FT_FRAGMENT_FTP, FtpFragment.class);
        f.setVersion(null);
        s.getFtpFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, path, s, f);
        resolveHttpProxyFragment(dbLayer, path, s, f.getHttpProxyFragmentName());
        resolveSocksProxyFragment(dbLayer, path, s, f.getSocksProxyFragmentName());

        resolveFtpFragment(dbLayer, path, s, f.getAlternativeFtpFragmentName());
    }

    private static void resolveHttpFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getHttpFragments() == null) {
            s.setHttpFragments(new HttpFragments());
        } else {
            // already processed
            if (s.getHttpFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        HttpFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_HTTP, name, ConfigurationType.FT_FRAGMENT_HTTP,
                HttpFragment.class);
        f.setVersion(null);
        s.getHttpFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, path, s, f);
        resolveHttpProxyFragment(dbLayer, path, s, f.getHttpProxyFragmentName());
        resolveJumpFragment(dbLayer, path, s, f.getJumpFragmentName());

        resolveHttpFragment(dbLayer, path, s, f.getAlternativeHttpFragmentName());
    }

    private static void resolveSftpFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getSftpFragments() == null) {
            s.setSftpFragments(new SftpFragments());
        } else {
            // already processed
            if (s.getSftpFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        SftpFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_SFTP, name, ConfigurationType.FT_FRAGMENT_SFTP,
                SftpFragment.class);
        f.setVersion(null);
        s.getSftpFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, path, s, f);
        resolveHttpProxyFragment(dbLayer, path, s, f.getHttpProxyFragmentName());
        resolveSocksProxyFragment(dbLayer, path, s, f.getSocksProxyFragmentName());
        resolveJumpFragment(dbLayer, path, s, f.getJumpFragmentName());

        resolveSftpFragment(dbLayer, path, s, f.getAlternativeSftpFragmentName());
    }

    private static void resolveSmbFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getSmbFragments() == null) {
            s.setSmbFragments(new SmbFragments());
        } else {
            // already processed
            if (s.getSmbFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        SmbFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_SMB, name, ConfigurationType.FT_FRAGMENT_SMB, SmbFragment.class);
        f.setVersion(null);
        s.getSmbFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, path, s, f);

        resolveSmbFragment(dbLayer, path, s, f.getAlternativeSmbFragmentName());
    }

    private static void resolveWebdavFragment(DBLayerDeploy dbLayer, String path, Settings s, String name) throws Exception {
        if (SOSString.isEmpty(name)) {
            return;
        }
        if (s.getWebdavFragments() == null) {
            s.setWebdavFragments(new WebdavFragments());
        } else {
            // already processed
            if (s.getWebdavFragments().getAdditionalProperties().containsKey(name)) {
                return;
            }
        }
        WebdavFragment f = getReleasedFragment(dbLayer, path, PROPERTY_NAME_FRAGMENT_WEBDAV, name, ConfigurationType.FT_FRAGMENT_WEBDAV,
                WebdavFragment.class);
        f.setVersion(null);
        s.getWebdavFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, path, s, f);
        resolveHttpProxyFragment(dbLayer, path, s, f.getHttpProxyFragmentName());
        resolveJumpFragment(dbLayer, path, s, f.getJumpFragmentName());

        resolveWebdavFragment(dbLayer, path, s, f.getAlternativeWebdavFragmentName());
    }

    // TODO remove test
    private static <T extends AFragment> T getReleasedFragment(DBLayerDeploy dbLayer, String path, String propertyName, String name,
            ConfigurationType type, Class<T> clazz) throws Exception {

        if (testMode) {
            DBItemInventoryConfiguration item = dbLayer.getConfigurationByName(name, type);
            if (item == null) {
                throw new Exception("[" + path + "] The assigned " + propertyName + " name  does not exist: " + name);
            }
            return Globals.objectMapper.readValue(item.getJsonContent(), clazz);
        } else {
            DBItemInventoryReleasedConfiguration item = dbLayer.getReleasedConfiguration(name, type);
            if (item == null) {
                throw new Exception("[" + path + "] No released " + propertyName + " found with name: " + name);
            }
            return Globals.objectMapper.readValue(item.getJsonContent(), clazz);
        }
    }

}
