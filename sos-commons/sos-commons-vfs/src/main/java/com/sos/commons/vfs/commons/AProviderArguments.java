package com.sos.commons.vfs.commons;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

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

    public enum FileType {
        REGULAR, SYMLINK
    }

    private CredentialStoreArguments credentialStore;

    // Basic
    private SOSArgument<Protocol> protocol = new SOSArgument<>("protocol", true);
    private SOSArgument<String> host = new SOSArgument<>("host", false);
    private SOSArgument<Integer> port = new SOSArgument<>("port", false);
    private SOSArgument<String> user = new SOSArgument<>("user", false);
    private SOSArgument<String> password = new SOSArgument<>("password", false, DisplayMode.MASKED);

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

    public SOSArgument<Integer> getPort() {
        return port;
    }

    public SOSArgument<String> getUser() {
        return user;
    }

    public SOSArgument<String> getPassword() {
        return password;
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

    public static int asMs(SOSArgument<Integer> arg) {
        return arg.getValue() == null ? 0 : arg.getValue() * 1_000;
    }

}
