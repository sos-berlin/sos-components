package com.sos.commons.vfs.commons;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public abstract class AProviderArguments extends ASOSArguments {

    /** see sos.yade.commons.Yade.TransferProtocol<br/>
     * YADE uses its own integer values ​​for storage in the database<br/>
     * TODO - YADE should use these values */
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

    public abstract String getAccessInfo() throws ProviderInitializationException;

    public abstract String getAdvancedAccessInfo();

    private CredentialStoreArguments credentialStore;
    private ProxyConfigArguments proxy;

    // Basic
    private SOSArgument<Protocol> protocol = new SOSArgument<>("protocol", true);
    private SOSArgument<String> host = new SOSArgument<>("host", false);
    private SOSArgument<Integer> port = new SOSArgument<>("port", false);
    private SOSArgument<String> user = new SOSArgument<>("user", false);
    private SOSArgument<String> password = new SOSArgument<>("password", false, DisplayMode.MASKED);
    // Socket connect timeout in seconds based on socket.connect
    // - Maximum time to wait while establishing a connection.
    /** see {@link ASOSArguments#asSeconds(SOSArgument, long) */
    private SOSArgument<String> connectTimeout = new SOSArgument<>("connect_timeout", false, "0");

    private SOSArgument<List<Path>> configurationFiles = new SOSArgument<>("configuration_files", false);

    // JS7
    private SOSArgument<EnumSet<FileType>> validFileTypes = new SOSArgument<>("valid_file_types", false, EnumSet.of(FileType.REGULAR,
            FileType.SYMLINK));

    public void setCredentialStore(CredentialStoreArguments val) {
        credentialStore = val;
    }

    public CredentialStoreArguments getCredentialStore() {
        return credentialStore;
    }

    public ProxyConfigArguments getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfigArguments val) {
        proxy = val;
    }

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

    public SOSArgument<String> getConnectTimeout() {
        return connectTimeout;
    }

    public int getConnectTimeoutAsMillis() {
        return (int) SOSArgumentHelper.asMillis(connectTimeout);
    }

    public SOSArgument<List<Path>> getConfigurationFiles() {
        return configurationFiles;
    }

    public SOSArgument<EnumSet<FileType>> getValidFileTypes() {
        return validFileTypes;
    }

    public void tryRedefineHostPort() {
        if (!host.isEmpty() && host.getValue().contains(":")) {
            String[] arr = host.getValue().split(":");
            host.setValue(arr[0]);
            if (!port.isDirty()) {
                port.setValue(Integer.parseInt(arr[1]));
            }
        }
    }

}
