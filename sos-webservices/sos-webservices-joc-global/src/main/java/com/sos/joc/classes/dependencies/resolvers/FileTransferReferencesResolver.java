package com.sos.joc.classes.dependencies.resolvers;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
import com.sos.joc.classes.dependencies.items.ReferencedDbItem;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class FileTransferReferencesResolver {

    /** Currently nothing to do, contains no references. May be extended later.
     * 
     * @param dbLayer
     * @param item
     * @param allItemsGrouped
     * @throws IOException */
    public static void resolveCredentialStoreFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
    }

    /** Currently nothing to do, contains no references. May be extended later.
     * 
     * @param dbLayer
     * @param item
     * @param allItemsGrouped
     * @throws IOException */
    public static void resolveDecryptionFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
    }

    /** Currently nothing to do, contains no references. May be extended later.
     * 
     * @param dbLayer
     * @param item
     * @param allItemsGrouped
     * @throws IOException */
    public static void resolveMailFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {

    }

    /** Currently nothing to do, contains no references. May be extended later.
     * 
     * @param dbLayer
     * @param item
     * @param allItemsGrouped
     * @throws IOException */
    public static void resolveHttpProxyFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {

    }

    /** Currently nothing to do, contains no references. May be extended later.
     * 
     * @param dbLayer
     * @param item
     * @param allItemsGrouped
     * @throws IOException */
    public static void resolveSocksProxyFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {

    }

    public static void resolveAzureBlobStorageFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        AzureBlobStorageFragment f = (AzureBlobStorageFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(),
                ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE);

        // 1) Fragment references
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, f);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getJumpFragmentName());
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());

        // 2) Alternative name
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE, f.getAlternativeAzureBlobStorageFragmentName());
    }

    public static void resolveFtpFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        FtpFragment f = (FtpFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_FTP);

        // 1) Fragment references
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, f);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getJumpFragmentName());
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_SOCKS_PROXY, f.getSocksProxyFragmentName());

        // 2) Alternative name
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_FTP, f.getAlternativeFtpFragmentName());
    }

    public static void resolveHttpFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        HttpFragment f = (HttpFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_HTTP);

        // 1) Fragment references
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, f);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getJumpFragmentName());
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());

        // 2) Alternative name
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_HTTP, f.getAlternativeHttpFragmentName());
    }

    public static void resolveJumpFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        JumpFragment f = (JumpFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_JUMP);

        // 1) Fragment references
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, f);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_SOCKS_PROXY, f.getSocksProxyFragmentName());

        // 2) Alternative name
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getAlternativeJumpFragmentName());
    }

    public static void resolveSftpFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        SftpFragment f = (SftpFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_SFTP);

        // 1) Fragment references
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, f);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getJumpFragmentName());
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_SOCKS_PROXY, f.getSocksProxyFragmentName());

        // 2) Alternative name
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_SFTP, f.getAlternativeSftpFragmentName());
    }

    public static void resolveSmbFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        SmbFragment f = (SmbFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_SMB);

        // 1) Fragment references
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, f);

        // 2) Alternative name
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_SMB, f.getAlternativeSmbFragmentName());
    }

    public static void resolveWebdavFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        WebdavFragment f = (WebdavFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(),
                ConfigurationType.FT_FRAGMENT_WEBDAV);

        // 1) Fragment references
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, f);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getJumpFragmentName());
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_HTTP_PROXY, f.getHttpProxyFragmentName());

        // 2) Alternative name
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_WEBDAV, f.getAlternativeWebdavFragmentName());
    }

    public static void resolveProfile(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        Profile p = (Profile) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_PROFILE);

        // 1) Alternative profile name
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_PROFILE, p.getAlternativeProfileName());

        // 2) Source
        if (p.getSource() != null) {
            resolveProfileFragment(dbLayer, item, allItemsGrouped, p.getSource().getFragment());
        }

        // 3) Target
        if (p.getTarget() != null) {
            resolveProfileFragment(dbLayer, item, allItemsGrouped, p.getTarget().getFragment());
        }

        // 4) Notification triggers
        if (p.getNotificationTriggers() != null) {
            if (p.getNotificationTriggers().getOnError() != null) {
                resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_MAIL, p.getNotificationTriggers().getOnError()
                        .getMailFragmentName());
            }
            if (p.getNotificationTriggers().getOnSuccess() != null) {
                resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_MAIL, p.getNotificationTriggers().getOnSuccess()
                        .getMailFragmentName());
            }
            if (p.getNotificationTriggers().getOnEmptyFiles() != null) {
                resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_MAIL, p.getNotificationTriggers().getOnEmptyFiles()
                        .getMailFragmentName());
            }
        }
    }

    private static void resolveCommonProtocolFragmentNames(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped, AProtocolFragment f) {
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_CREDENTIAL_STORE, f.getCredentialStoreFragmentName());
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_DECRYPTION, f.getDecryptionFragmentName());
    }

    private static void resolveProfileFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped, Fragment f) throws JocConfigurationException {
        if (f == null) {
            return;
        }

        if (f.getLocal() == null) {
            // Referenced protocol fragments
            if (f.getSftp() != null) {
                resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_SFTP, f.getSftp().getName());
            } else if (f.getSmb() != null) {
                resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_SMB, f.getSmb().getName());
            } else if (f.getFtp() != null) {
                resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_FTP, f.getFtp().getName());
            } else if (f.getWebdav() != null) {
                resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_WEBDAV, f.getWebdav().getName());
            } else if (f.getHttp() != null) {
                resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_HTTP, f.getHttp().getName());
            } else if (f.getAzureBlobStorage() != null) {
                resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE, f.getAzureBlobStorage().getName());
            }
        } else {
            // Local
            resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_CREDENTIAL_STORE, f.getLocal()
                    .getCredentialStoreFragmentName());
            resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_DECRYPTION, f.getLocal().getDecryptionFragmentName());
        }
    }

    private static void resolveName(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped, ConfigurationType type, String name) {
        if (SOSString.isEmpty(name) || name.startsWith("${")) {
            // Empty or variable reference: skip lookup.
            return;
        }

        // when not found?
        // if (SOSCollection.isEmpty(dbLayer.getConfigurationByName(name, type.intValue()))) {
        //
        // }

        if (allItemsGrouped.isEmpty()) {
            item.getReferences().addAll(dbLayer.getConfigurationByName(name, type.intValue()));
        } else {
            allItemsGrouped.getOrDefault(type, Collections.emptySet()).stream().filter(cfg -> name.equals(cfg.getName())).forEach(item
                    .getReferences()::add);
        }

    }

}
