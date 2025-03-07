package com.sos.commons.vfs.commons;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;

public abstract class AProviderArguments extends ASOSArguments {

    // TODO see sos.yade.commons.Yade.TransferProtocol
    public enum Protocol {

        UNKNOWN(0), LOCAL(10), FTP(20), FTPS(21), SFTP(30), SSH(31), HTTP(40), HTTPS(41), WEBDAV(50), WEBDAVS(51), SMB(60);

        private final Integer value;

        private Protocol(Integer val) {
            value = val;
        }

        public Integer getValue() {
            return value;
        }
    }

    public enum KeyStoreType {
        JKS, JCEKS, PKCS12, PKCS11, DKS
    }

    public enum FileType {
        REGULAR, SYMLINK
    }

    // Basic
    private SOSArgument<Protocol> protocol = new SOSArgument<Protocol>("protocol", true);
    private SOSArgument<String> host = new SOSArgument<String>("host", true);
    private SOSArgument<String> user = new SOSArgument<String>("user", false);
    private SOSArgument<String> password = new SOSArgument<String>("password", false, DisplayMode.MASKED);

    // Internal/Keepass
    private SOSArgument<SOSKeePassDatabase> keepassDatabase = new SOSArgument<SOSKeePassDatabase>(null, false);
    private SOSArgument<org.linguafranca.pwdb.Entry<?, ?, ?, ?>> keepassDatabaseEntry = new SOSArgument<org.linguafranca.pwdb.Entry<?, ?, ?, ?>>(null,
            false);
    private SOSArgument<String> keepassAttachmentPropertyName = new SOSArgument<String>(null, false);

    private CredentialStoreArguments credentialStore;

    private SOSArgument<List<Path>> systemPropertyFiles = new SOSArgument<>("system_property_files", false);
    private SOSArgument<List<Path>> configurationFiles = new SOSArgument<>("configuration_files", false);

    // JS7
    private SOSArgument<EnumSet<FileType>> validFileTypes = new SOSArgument<>("valid_file_types", false, EnumSet.of(FileType.REGULAR,
            FileType.SYMLINK));

    public SOSArgument<Protocol> getProtocol() {
        return protocol;
    }

    public SOSArgument<String> getHost() {
        return host;
    }

    public SOSArgument<String> getUser() {
        return user;
    }

    public SOSArgument<String> getPassword() {
        return password;
    }

    public SOSKeePassDatabase getKeepassDatabase() {
        return keepassDatabase.getValue();
    }

    protected void setKeepassDatabase(SOSKeePassDatabase val) {
        keepassDatabase.setValue(val);
    }

    public org.linguafranca.pwdb.Entry<?, ?, ?, ?> getKeepassDatabaseEntry() {
        return keepassDatabaseEntry.getValue();
    }

    protected void setKeepassDatabaseEntry(org.linguafranca.pwdb.Entry<?, ?, ?, ?> val) {
        keepassDatabaseEntry.setValue(val);
    }

    public String getKeepassAttachmentPropertyName() {
        return keepassAttachmentPropertyName.getValue();
    }

    protected void setKeepassAttachmentPropertyName(String val) {
        keepassAttachmentPropertyName.setValue(val);
    }

    public void setCredentialStore(CredentialStoreArguments val) {
        credentialStore = val;
    }

    public CredentialStoreArguments getCredentialStore() {
        return credentialStore;
    }

    public SOSArgument<List<Path>> getSystemPropertyFiles() {
        return systemPropertyFiles;
    }

    public SOSArgument<List<Path>> getConfigurationFiles() {
        return configurationFiles;
    }

    public SOSArgument<EnumSet<FileType>> getValidFileTypes() {
        return validFileTypes;
    }

    public int asMs(SOSArgument<Integer> arg) {
        return arg.getValue() == null ? 0 : arg.getValue() * 1_000;
    }

}
