package com.sos.joc.classes.inventory.validators;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.filetransfer.fragments.protocol.AProtocolFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.AzureBlobStorageFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.FtpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.HttpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.JumpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.SmbFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.WebdavFragment;
import com.sos.inventory.model.filetransfer.profile.Profile;
import com.sos.inventory.model.filetransfer.profile.fragments.Fragment;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class FileTransferValidator {

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
    private static final String PROPERTY_NAME_ALTERNATIVE = "alternative";

    public static void validateCredentialStoreFragment(IConfigurationObject config) throws Exception {
        // Currently nothing to do, validated by the schema. May be extended later.
    }

    public static void validateDecryptionFragment(IConfigurationObject config) throws Exception {
        // Currently nothing to do, validated by the schema. May be extended later.
    }

    public static void validateMailFragment(IConfigurationObject config) throws Exception {
        // Currently nothing to do, validated by the schema. May be extended later.
    }

    public static void validateHttpProxyFragment(IConfigurationObject config) throws Exception {
        // Currently nothing to do, validated by the schema. May be extended later.
    }

    public static void validateSocksProxyFragment(IConfigurationObject config) throws Exception {
        // Currently nothing to do, validated by the schema. May be extended later.
    }

    public static void validateAzureBlobStorageFragment(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        AzureBlobStorageFragment f = (AzureBlobStorageFragment) config;
        // 1) Fragment references
        checkCommonProtocolFragmentNames(dbLayer, f);
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_JUMP, PROPERTY_NAME_FRAGMENT_JUMP, f.getJumpFragmentName());
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, PROPERTY_NAME_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());
        // 2) Alternative name
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE, PROPERTY_NAME_ALTERNATIVE, f
                .getAlternativeAzureBlobStorageFragmentName());
    }

    public static void validateFtpFragment(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        FtpFragment f = (FtpFragment) config;
        // 1) Fragment references
        checkCommonProtocolFragmentNames(dbLayer, f);
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_JUMP, PROPERTY_NAME_FRAGMENT_JUMP, f.getJumpFragmentName());
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, PROPERTY_NAME_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, PROPERTY_NAME_FRAGMENT_SOCKS_PROXY, f.getSocksProxyFragmentName());
        // 2) Alternative name
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_FTP, PROPERTY_NAME_ALTERNATIVE, f.getAlternativeFtpFragmentName());
    }

    public static void validateHttpFragment(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        HttpFragment f = (HttpFragment) config;
        // 1) Fragment references
        checkCommonProtocolFragmentNames(dbLayer, f);
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_JUMP, PROPERTY_NAME_FRAGMENT_JUMP, f.getJumpFragmentName());
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, PROPERTY_NAME_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());
        // 2) Alternative name
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP, PROPERTY_NAME_ALTERNATIVE, f.getAlternativeHttpFragmentName());
    }

    public static void validateJumpFragment(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        JumpFragment f = (JumpFragment) config;
        // 1) Fragment references
        checkCommonProtocolFragmentNames(dbLayer, f);
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, PROPERTY_NAME_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, PROPERTY_NAME_FRAGMENT_SOCKS_PROXY, f.getSocksProxyFragmentName());
        // 2) Alternative name
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_JUMP, PROPERTY_NAME_ALTERNATIVE, f.getAlternativeJumpFragmentName());
    }

    public static void validateSftpFragment(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        SftpFragment f = (SftpFragment) config;
        // 1) Fragment references
        checkCommonProtocolFragmentNames(dbLayer, f);
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_JUMP, PROPERTY_NAME_FRAGMENT_JUMP, f.getJumpFragmentName());
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, PROPERTY_NAME_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, PROPERTY_NAME_FRAGMENT_SOCKS_PROXY, f.getSocksProxyFragmentName());
        // 2) Alternative name
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_SFTP, PROPERTY_NAME_ALTERNATIVE, f.getAlternativeSftpFragmentName());
    }

    public static void validateSmbFragment(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        SmbFragment f = (SmbFragment) config;
        // 1) Fragment references
        checkCommonProtocolFragmentNames(dbLayer, f);
        // 2) Alternative name
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_SMB, PROPERTY_NAME_ALTERNATIVE, f.getAlternativeSmbFragmentName());
    }

    public static void validateWebdavFragment(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        WebdavFragment f = (WebdavFragment) config;
        // 1) Fragment references
        checkCommonProtocolFragmentNames(dbLayer, f);
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_JUMP, PROPERTY_NAME_FRAGMENT_JUMP, f.getJumpFragmentName());
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, PROPERTY_NAME_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());
        // 2) Alternative name
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_WEBDAV, PROPERTY_NAME_ALTERNATIVE, f.getAlternativeWebdavFragmentName());
    }

    public static void validateProfile(IConfigurationObject config, InventoryDBLayer dbLayer) throws JocConfigurationException {
        Profile p = (Profile) config;
        // 1) alternative profile name
        checkName(dbLayer, ConfigurationType.FT_PROFILE, PROPERTY_NAME_ALTERNATIVE, p.getAlternativeProfileName());

        // Redundant to schema validation, but kept as a safety net.
        // 2) Source
        if (p.getSource() == null) {
            throw new JocConfigurationException("Missing source");
        }
        checkProfileFragment(dbLayer, "source", p.getSource().getFragment());

        // 3) Target
        if (p.getTarget() != null) {
            checkProfileFragment(dbLayer, "target", p.getTarget().getFragment());
        }

        // 4) Notification triggers
        if (p.getNotificationTriggers() != null) {
            if (p.getNotificationTriggers().getOnError() != null) {
                checkName(dbLayer, ConfigurationType.FT_FRAGMENT_MAIL, PROPERTY_NAME_FRAGMENT_MAIL, p.getNotificationTriggers().getOnError()
                        .getMailFragmentName());
            } else if (p.getNotificationTriggers().getOnSuccess() != null) {
                checkName(dbLayer, ConfigurationType.FT_FRAGMENT_MAIL, PROPERTY_NAME_FRAGMENT_MAIL, p.getNotificationTriggers().getOnSuccess()
                        .getMailFragmentName());
            } else if (p.getNotificationTriggers().getOnEmptyFiles() != null) {
                checkName(dbLayer, ConfigurationType.FT_FRAGMENT_MAIL, PROPERTY_NAME_FRAGMENT_MAIL, p.getNotificationTriggers().getOnEmptyFiles()
                        .getMailFragmentName());
            }
        }
    }

    private static void checkProfileFragment(InventoryDBLayer dbLayer, String sourceOrTarget, Fragment f) throws JocConfigurationException {
        if (f == null) {
            throw new JocConfigurationException("Missing " + sourceOrTarget + ".fragment");
        }

        if (f.getLocal() == null) {
            if (f.getAzureBlobStorage() != null) {
                checkName(dbLayer, ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE, sourceOrTarget + " " + PROPERTY_NAME_FRAGMENT_AZURE_BLOB_STORAGE,
                        f.getAzureBlobStorage().getName());
            } else if (f.getFtp() != null) {
                checkName(dbLayer, ConfigurationType.FT_FRAGMENT_FTP, sourceOrTarget + " " + PROPERTY_NAME_FRAGMENT_FTP, f.getFtp().getName());
            } else if (f.getHttp() != null) {
                checkName(dbLayer, ConfigurationType.FT_FRAGMENT_HTTP, sourceOrTarget + " " + PROPERTY_NAME_FRAGMENT_HTTP, f.getHttp().getName());
            } else if (f.getSftp() != null) {
                checkName(dbLayer, ConfigurationType.FT_FRAGMENT_SFTP, sourceOrTarget + " " + PROPERTY_NAME_FRAGMENT_SFTP, f.getSftp().getName());
            } else if (f.getSmb() != null) {
                checkName(dbLayer, ConfigurationType.FT_FRAGMENT_SMB, sourceOrTarget + " " + PROPERTY_NAME_FRAGMENT_SMB, f.getSmb().getName());
            } else if (f.getWebdav() != null) {
                checkName(dbLayer, ConfigurationType.FT_FRAGMENT_WEBDAV, sourceOrTarget + " " + PROPERTY_NAME_FRAGMENT_WEBDAV, f.getWebdav()
                        .getName());
            } else {
                throw new JocConfigurationException("Not supported " + sourceOrTarget + ".fragment: " + SOSString.toString(f));
            }
        } else {
            checkName(dbLayer, ConfigurationType.FT_FRAGMENT_CREDENTIAL_STORE, sourceOrTarget + " " + PROPERTY_NAME_FRAGMENT_CREDENTIAL_STORE, f
                    .getLocal().getCredentialStoreFragmentName());
            checkName(dbLayer, ConfigurationType.FT_FRAGMENT_DECRYPTION, sourceOrTarget + " " + PROPERTY_NAME_FRAGMENT_DECRYPTION, f.getLocal()
                    .getDecryptionFragmentName());
        }
    }

    private static void checkCommonProtocolFragmentNames(InventoryDBLayer dbLayer, AProtocolFragment f) throws JocConfigurationException {
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_CREDENTIAL_STORE, PROPERTY_NAME_FRAGMENT_CREDENTIAL_STORE, f
                .getCredentialStoreFragmentName());
        checkName(dbLayer, ConfigurationType.FT_FRAGMENT_DECRYPTION, PROPERTY_NAME_FRAGMENT_DECRYPTION, f.getDecryptionFragmentName());
    }

    private static void checkName(InventoryDBLayer dbLayer, ConfigurationType type, String propertyName, String name)
            throws JocConfigurationException {
        if (SOSString.isEmpty(name) || name.startsWith("${")) {
            // Empty or variable reference: skip lookup.
            return;
        }

        // YADE allows/ignores duplicate names, so no uniqueness check is performed here.
        // if (SOSString.isEmpty(currentPath)) {
        // throw new JocConfigurationException("Missing " + type + " item path ");
        // }

        if (SOSCollection.isEmpty(dbLayer.getConfigurationByName(name, type.intValue()))) {
            throw new JocConfigurationException("The assigned " + propertyName + " name  does not exist: " + name);
        }
    }
}
