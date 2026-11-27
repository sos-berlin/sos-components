package com.sos.joc.classes.dependencies.resolvers;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.filetransfer.FileTransferJobResource;
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
import com.sos.joc.classes.filetransfer.FileTransferHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.inventory.common.ConfigurationType;

/** Resolves references that the object itself holds (references pointing to other objects). */
public class FileTransferUsedRefsDependencyResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferUsedRefsDependencyResolver.class);

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
        AzureBlobStorageFragment f = (AzureBlobStorageFragment) JocInventory
            .content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE);

        String name = "azureBlobStorage";
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE, f, name);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getJumpFragmentName(), name + ".jumpFragmentName");
        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_HTTP_PROXY,
            f.getHttpProxyFragmentName(),
            name + ".httpProxyFragmentName");
    }

    public static void resolveFtpFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        FtpFragment f = (FtpFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_FTP);

        String name = "ftp";
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_FTP, f, name);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getJumpFragmentName(), name + ".jumpFragmentName");
        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_HTTP_PROXY,
            f.getHttpProxyFragmentName(),
            name + ".httpProxyFragmentName");
        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_SOCKS_PROXY,
            f.getSocksProxyFragmentName(),
            name + ".socksProxyFragmentName");
    }

    public static void resolveHttpFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        HttpFragment f = (HttpFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_HTTP);

        String name = "http";
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_HTTP, f, name);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getJumpFragmentName(), name + ".jumpFragmentName");
        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_HTTP_PROXY,
            f.getHttpProxyFragmentName(),
            name + ".httpProxyFragmentName");
    }

    public static void resolveJumpFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        JumpFragment f = (JumpFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_JUMP);

        String name = "jump";
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f, name);
        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_HTTP_PROXY,
            f.getHttpProxyFragmentName(),
            name + ".httpProxyFragmentName");
        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_SOCKS_PROXY,
            f.getSocksProxyFragmentName(),
            name + ".socksProxyFragmentName");
    }

    public static void resolveSftpFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        SftpFragment f = (SftpFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_SFTP);

        String name = "sftp";
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_SFTP, f, name);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getJumpFragmentName(), name + ".jumpFragmentName");
        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_HTTP_PROXY,
            f.getHttpProxyFragmentName(),
            name + ".httpProxyFragmentName");
        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_SOCKS_PROXY,
            f.getSocksProxyFragmentName(),
            name + ".socksProxyFragmentName");
    }

    public static void resolveSmbFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        SmbFragment f = (SmbFragment) JocInventory.content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_SMB);

        String name = "smb";
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_SMB, f, name);
    }

    public static void resolveWebdavFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
        WebdavFragment f = (WebdavFragment) JocInventory
            .content2IJSObject(item.getReferencedItem().getContent(), ConfigurationType.FT_FRAGMENT_WEBDAV);

        String name = "webdav";
        resolveCommonProtocolFragmentNames(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_WEBDAV, f, name);
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_JUMP, f.getJumpFragmentName(), name + ".jumpFragmentName");
        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_HTTP_PROXY,
            f.getHttpProxyFragmentName(),
            name + ".httpProxyFragmentName");
    }

    public static void resolveProfileJobResource(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {

        FileTransferJobResource ftjr = FileTransferHelper.toFileTransferJobResource(item.getReferencedItem().getContent());

        Profile p = ftjr.getProfile();

        String name = "profile";
        // 1) Alternative profile name
        resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_PROFILE_JOBRESOURCE, p.getAlternativeProfileName(), name + ".alternative");

        // 2) Source
        if (p.getSource() != null) {
            resolveProfileFragment(dbLayer, item, allItemsGrouped, p.getSource().getFragment(), name + ".source");
        }

        // 3) Target
        if (p.getTarget() != null) {
            resolveProfileFragment(dbLayer, item, allItemsGrouped, p.getTarget().getFragment(), name + ".target");
        }

        // 4) Notification triggers
        if (p.getNotificationTriggers() != null) {
            if (p.getNotificationTriggers().getOnError() != null) {
                resolveName(
                    dbLayer,
                    item,
                    allItemsGrouped,
                    ConfigurationType.FT_FRAGMENT_MAIL,
                    p.getNotificationTriggers().getOnError().getMailFragmentName(),
                    name + ".notificationTriggers.onError.mailFragmentName");
            }
            if (p.getNotificationTriggers().getOnSuccess() != null) {
                resolveName(
                    dbLayer,
                    item,
                    allItemsGrouped,
                    ConfigurationType.FT_FRAGMENT_MAIL,
                    p.getNotificationTriggers().getOnSuccess().getMailFragmentName(),
                    name + ".notificationTriggers.onSuccess.mailFragmentName");
            }
            if (p.getNotificationTriggers().getOnEmptyFiles() != null) {
                resolveName(
                    dbLayer,
                    item,
                    allItemsGrouped,
                    ConfigurationType.FT_FRAGMENT_MAIL,
                    p.getNotificationTriggers().getOnEmptyFiles().getMailFragmentName(),
                    name + ".notificationTriggers.onEmtyFiles.mailFragmentName");
            }
        }
    }

    private static void resolveCommonProtocolFragmentNames(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped, ConfigurationType type, AProtocolFragment f,
            String fragmentTypeForLog) {

        // 1) 
        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_CREDENTIAL_STORE,
            f.getCredentialStoreFragmentName(),
            fragmentTypeForLog + ".credentialStoreFragmentName");

        resolveName(
            dbLayer,
            item,
            allItemsGrouped,
            ConfigurationType.FT_FRAGMENT_DECRYPTION,
            f.getDecryptionFragmentName(),
            fragmentTypeForLog + ".decryptionFragmentName");

        // 2) Alternative name
        resolveName(dbLayer, item, allItemsGrouped, type, f.getAlternativeFragmentName(), fragmentTypeForLog + ".alternative");
    }

    private static void resolveProfileFragment(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped, Fragment f, String sourceOrTargetForLog)
            throws JocConfigurationException {
        // Referenced protocol fragments
        if (FileTransferHelper.isSftp(f)) {
            resolveName(
                dbLayer,
                item,
                allItemsGrouped,
                ConfigurationType.FT_FRAGMENT_SFTP,
                f.getSftp().getName(),
                sourceOrTargetForLog + ".sftp.name");
        } else if (FileTransferHelper.isSmb(f)) {
            resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_SMB, f.getSmb().getName(), sourceOrTargetForLog + ".smb.name");
        } else if (FileTransferHelper.isFtp(f)) {
            resolveName(dbLayer, item, allItemsGrouped, ConfigurationType.FT_FRAGMENT_FTP, f.getFtp().getName(), sourceOrTargetForLog + ".ftp.name");
        } else if (FileTransferHelper.isWebdav(f)) {
            resolveName(
                dbLayer,
                item,
                allItemsGrouped,
                ConfigurationType.FT_FRAGMENT_WEBDAV,
                f.getWebdav().getName(),
                sourceOrTargetForLog + ".webdav.name");
        } else if (FileTransferHelper.isHttp(f)) {
            resolveName(
                dbLayer,
                item,
                allItemsGrouped,
                ConfigurationType.FT_FRAGMENT_HTTP,
                f.getHttp().getName(),
                sourceOrTargetForLog + ".http.name");
        } else if (FileTransferHelper.isAzureBlobStorage(f)) {
            resolveName(
                dbLayer,
                item,
                allItemsGrouped,
                ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE,
                f.getAzureBlobStorage().getName(),
                sourceOrTargetForLog + ".azureBlobStorage.name");
        }
    }

    private static void resolveName(InventoryDBLayer dbLayer, ReferencedDbItem item,
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped, ConfigurationType type, String name,
            String propertyNameForLog) {
        if (SOSString.isEmpty(name) || name.startsWith("${")) {
            // Empty or variable reference: skip lookup.
            if (LOGGER.isDebugEnabled()) {
                if (SOSString.isEmpty(name)) {
                    LOGGER.trace(String.format("[resolveName][%s][%s][skip]name=%s", propertyNameForLog, type, name));
                } else {
                    LOGGER.debug(String.format("[resolveName][%s][%s][skip]name=%s", propertyNameForLog, type, name));
                }
            }
            return;
        }

        if (allItemsGrouped.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[resolveName][%s=%s][%s]select from db ...", propertyNameForLog, name, type));
            }
            item.getReferences().addAll(dbLayer.getConfigurationByName(name, type.intValue()));
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[resolveName][%s=%s][%s]use allItemsGrouped ...", propertyNameForLog, name, type));
            }
            allItemsGrouped
                .getOrDefault(type, Collections.emptySet())
                .stream()
                .filter(cfg -> name.equals(cfg.getName()))
                .forEach(item.getReferences()::add);
        }

    }

}
