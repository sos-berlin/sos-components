package com.sos.joc.classes.filetransfer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.util.SOSCollection;
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
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.inventory.common.ConfigurationType;

// TODO alternative profile - source table?
public class FileTransferJobResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferJobResourceHandler.class);

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

    private static final String JOBRESOURCE_ARG_NAME = "settings";

    // TODO remove it
    private static final boolean testMode = true;

    /** Converts a FileTransferJobResource to a JobResource.<br />
     * The dbLayer argument (null or not null) acts as a flag to include additional information.
     *
     * @param ft the file transfer job resource
     * @param ftJobResourceName Inventory name of the file transfer job resource
     * @param dbLayer inventory DB layer; if not null, additional information is included
     * @return the serialized JobResource as JSON string
     * @throws Exception if serialization fails */
    public static JobResource toJobResource(FileTransferJobResource ft, String ftJobResourceName, InventoryDBLayer dbLayer) throws Exception {
        JobResource jr = new JobResource();
        jr.setTitle(ft.getTitle());

        if (dbLayer != null) { // include additional information, resolve dependencies ...
            complete(ft, ftJobResourceName, dbLayer);
        }

        Environment args = new Environment();
        args.setAdditionalProperty(JOBRESOURCE_ARG_NAME, "toFile( '" + Globals.objectMapper.writeValueAsString(ft) + "', '*.json' )");
        jr.setArguments(args);

        Environment env = new Environment();
        env.setAdditionalProperty(JOBRESOURCE_ARG_NAME.toUpperCase(), "$" + JOBRESOURCE_ARG_NAME);
        jr.setEnv(env);

        return jr;
    }

    public static FileTransferJobResource toFileTransferJobResource(JobResource jr) throws JsonProcessingException {
        String val = jr.getArguments().getAdditionalProperties().get(JOBRESOURCE_ARG_NAME);
        if (val == null) {
            return null;
        }
        return Globals.objectMapper.readValue(extractJson(val), FileTransferJobResource.class);
    }

    public static FileTransferJobResource toFileTransferJobResource(String val) throws JsonProcessingException {
        return Globals.objectMapper.readValue(extractJson(val), FileTransferJobResource.class);
    }

    // throw specific exception
    private static void complete(FileTransferJobResource ft, String ftJobResourceName, InventoryDBLayer dbLayer) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[complete]" + ftJobResourceName);
        }

        if (ft.getProfile() == null) {
            return;
        }

        Profile p = ft.getProfile();
        if (p.getSource() == null) {
            return;
        }

        Settings s = new Settings();
        s.setProfileName(ftJobResourceName);

        resolveProfile(dbLayer, s, p);

        ft.setSettings(s);
    }

    private static void resolveProfile(InventoryDBLayer dbLayer, Settings s, Profile p) throws Exception {
        if (p == null) {
            return;
        }

        boolean hasAlternativeName = !SOSString.isEmpty(p.getAlternativeProfileName()) && !p.getAlternativeProfileName().equalsIgnoreCase(s
                .getProfileName());
        boolean hasNotificationTriggers = p.getNotificationTriggers() != null;
        boolean isSourceFragmentLocal = FileTransferHelper.isSourceFragmentLocal(p.getSource());
        boolean isTargetFragmentNullOrLocal = FileTransferHelper.isTargetFragmentNullOrLocal(p.getTarget());
        if (!hasAlternativeName && !hasNotificationTriggers && isSourceFragmentLocal && isTargetFragmentNullOrLocal) {
            return;
        }

        // All fragments should already have been released
        // 1) Source
        setSettingsFragment(dbLayer, s, p.getSource().getFragment());

        // 2) Target
        if (!isTargetFragmentNullOrLocal) {
            setSettingsFragment(dbLayer, s, p.getTarget().getFragment());
        }

        // 3) Notification triggers
        if (hasNotificationTriggers) {
            if (p.getNotificationTriggers().getOnError() != null) {
                resolveMailFragment(dbLayer, s, p.getNotificationTriggers().getOnError().getMailFragmentName());
            }
            if (p.getNotificationTriggers().getOnSuccess() != null) {
                resolveMailFragment(dbLayer, s, p.getNotificationTriggers().getOnSuccess().getMailFragmentName());
            }
            if (p.getNotificationTriggers().getOnEmptyFiles() != null) {
                resolveMailFragment(dbLayer, s, p.getNotificationTriggers().getOnSuccess().getMailFragmentName());
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

    private static void setSettingsFragment(InventoryDBLayer dbLayer, Settings s, Fragment f) throws Exception {
        if (FileTransferHelper.isSftp(f)) {
            resolveSftpFragment(dbLayer, s, f.getSftp().getName());
        } else if (FileTransferHelper.isSmb(f)) {
            resolveSmbFragment(dbLayer, s, f.getSmb().getName());
        } else if (FileTransferHelper.isFtp(f)) {
            resolveFtpFragment(dbLayer, s, f.getFtp().getName());
        } else if (FileTransferHelper.isWebdav(f)) {
            resolveWebdavFragment(dbLayer, s, f.getWebdav().getName());
        } else if (FileTransferHelper.isHttp(f)) {
            resolveHttpFragment(dbLayer, s, f.getHttp().getName());
        } else if (FileTransferHelper.isAzureBlobStorage(f)) {
            resolveAzureBlobStorageFragment(dbLayer, s, f.getAzureBlobStorage().getName());
        }
    }

    private static void resolveCommonFragmentReferences(InventoryDBLayer dbLayer, Settings s, AProtocolFragment f) throws Exception {
        resolveCredentialStoreFragment(dbLayer, s, f.getCredentialStoreFragmentName());
        resolveDecryptionFragment(dbLayer, s, f.getDecryptionFragmentName());
    }

    private static void resolveCredentialStoreFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        CredentialStoreFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_CREDENTIAL_STORE, name,
                ConfigurationType.FT_FRAGMENT_CREDENTIAL_STORE, CredentialStoreFragment.class);
        s.getCredentialStoreFragments().getAdditionalProperties().put(name, f);
    }

    private static void resolveDecryptionFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        DecryptionFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_DECRYPTION, name, ConfigurationType.FT_FRAGMENT_DECRYPTION,
                DecryptionFragment.class);
        s.getDecryptionFragments().getAdditionalProperties().put(name, f);
    }

    private static void resolveMailFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        MailFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_MAIL, name, ConfigurationType.FT_FRAGMENT_MAIL, MailFragment.class);
        s.getMailFragments().getAdditionalProperties().put(name, f);
    }

    private static void resolveHttpProxyFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        HttpProxyFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_HTTP_PROXY, name, ConfigurationType.FT_FRAGMENT_HTTP_PROXY,
                HttpProxyFragment.class);
        s.getHttpProxyFragments().getAdditionalProperties().put(name, f);
    }

    private static void resolveSocksProxyFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        SocksProxyFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_SOCKS_PROXY, name, ConfigurationType.FT_FRAGMENT_SOCKS_PROXY,
                SocksProxyFragment.class);
        s.getSocksProxyFragments().getAdditionalProperties().put(name, f);
    }

    private static void resolveJumpFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        JumpFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_JUMP, name, ConfigurationType.FT_FRAGMENT_JUMP, JumpFragment.class);
        s.getJumpFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, s, f);
        resolveHttpProxyFragment(dbLayer, s, f.getHttpProxyFragmentName());
        resolveSocksProxyFragment(dbLayer, s, f.getSocksProxyFragmentName());
        resolveJumpFragment(dbLayer, s, f.getAlternativeJumpFragmentName());
    }

    private static void resolveAzureBlobStorageFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        AzureBlobStorageFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_AZURE_BLOB_STORAGE, name,
                ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE, AzureBlobStorageFragment.class);
        s.getAzureBlobStorageFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, s, f);
        resolveHttpProxyFragment(dbLayer, s, f.getHttpProxyFragmentName());
        resolveJumpFragment(dbLayer, s, f.getJumpFragmentName());

        resolveAzureBlobStorageFragment(dbLayer, s, f.getAlternativeAzureBlobStorageFragmentName());
    }

    private static void resolveFtpFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        FtpFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_FTP, name, ConfigurationType.FT_FRAGMENT_FTP, FtpFragment.class);
        s.getFtpFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, s, f);
        resolveHttpProxyFragment(dbLayer, s, f.getHttpProxyFragmentName());
        resolveSocksProxyFragment(dbLayer, s, f.getSocksProxyFragmentName());

        resolveFtpFragment(dbLayer, s, f.getAlternativeFtpFragmentName());
    }

    private static void resolveHttpFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        HttpFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_HTTP, name, ConfigurationType.FT_FRAGMENT_HTTP, HttpFragment.class);
        s.getHttpFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, s, f);
        resolveHttpProxyFragment(dbLayer, s, f.getHttpProxyFragmentName());
        resolveJumpFragment(dbLayer, s, f.getJumpFragmentName());

        resolveHttpFragment(dbLayer, s, f.getAlternativeHttpFragmentName());
    }

    private static void resolveSftpFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        SftpFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_SFTP, name, ConfigurationType.FT_FRAGMENT_SFTP, SftpFragment.class);
        s.getSftpFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, s, f);
        resolveHttpProxyFragment(dbLayer, s, f.getHttpProxyFragmentName());
        resolveSocksProxyFragment(dbLayer, s, f.getSocksProxyFragmentName());
        resolveJumpFragment(dbLayer, s, f.getJumpFragmentName());

        resolveSftpFragment(dbLayer, s, f.getAlternativeSftpFragmentName());
    }

    private static void resolveSmbFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        SmbFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_SMB, name, ConfigurationType.FT_FRAGMENT_SMB, SmbFragment.class);
        s.getSmbFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, s, f);

        resolveSmbFragment(dbLayer, s, f.getAlternativeSmbFragmentName());
    }

    private static void resolveWebdavFragment(InventoryDBLayer dbLayer, Settings s, String name) throws Exception {
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
        WebdavFragment f = getReleasedFragment(dbLayer, PROPERTY_NAME_FRAGMENT_WEBDAV, name, ConfigurationType.FT_FRAGMENT_WEBDAV,
                WebdavFragment.class);
        s.getWebdavFragments().getAdditionalProperties().put(name, f);

        resolveCommonFragmentReferences(dbLayer, s, f);
        resolveHttpProxyFragment(dbLayer, s, f.getHttpProxyFragmentName());
        resolveJumpFragment(dbLayer, s, f.getJumpFragmentName());

        resolveWebdavFragment(dbLayer, s, f.getAlternativeWebdavFragmentName());
    }

    // TODO remove test
    private static <T extends AFragment> T getReleasedFragment(InventoryDBLayer dbLayer, String propertyName, String name, ConfigurationType type,
            Class<T> clazz) throws Exception {

        if (testMode) {
            List<DBItemInventoryConfiguration> l = dbLayer.getConfigurationByName(name, type.intValue());
            if (SOSCollection.isEmpty(l)) {
                throw new Exception("The assigned " + propertyName + " name  does not exist: " + name);
            }
            return Globals.objectMapper.readValue(l.get(0).getJsonContent(), clazz);
        } else {
            DBItemInventoryReleasedConfiguration item = dbLayer.getReleasedConfiguration(name, type.intValue());
            if (item == null) {
                throw new Exception("The assigned " + propertyName + " name  does not exist: " + name);
            }
            return Globals.objectMapper.readValue(item.getJsonContent(), clazz);
        }
    }

    // String variableValue = "toFile( '{....}', '*.json' )";
    private static String extractJson(String variableValue) {
        if (!variableValue.contains("toFile(")) {
            return variableValue;
        }
        int start = variableValue.indexOf('{'); // first {
        int quoteEnd = variableValue.lastIndexOf('\''); // last '
        int end = variableValue.lastIndexOf('}', quoteEnd); // last } before '
        if (start == -1 || end == -1 || end <= start) {
            return null;
        }
        String json = variableValue.substring(start, end + 1);
        return json.replace("\\r\\n", "\r\n").replace("\\\"", "\"").trim();
    }

}
