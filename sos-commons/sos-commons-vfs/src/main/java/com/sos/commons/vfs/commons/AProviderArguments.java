package com.sos.commons.vfs.commons;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.encryption.arguments.EncryptionDecryptArguments;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public abstract class AProviderArguments extends ASOSArguments {

    /** see sos.yade.commons.Yade.TransferProtocol<br/>
     * -- YADE uses its own integer values ​​for storage in the database<br/>
     * TODO - YADE should use these values */
    public enum Protocol {

        UNKNOWN(0), LOCAL(10), FTP(20), FTPS(21), SFTP(30), SSH(31), HTTP(40), HTTPS(41), WEBDAV(50), WEBDAVS(51), SMB(60), AZURE_BLOB_STORAGE(70);

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

    private EncryptionDecryptArguments encryptionDecrypt;
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
    /** Path or file content */
    private SOSArgument<List<String>> configurationFiles = new SOSArgument<>("configuration_files", false);

    // JS7
    private SOSArgument<EnumSet<FileType>> validFileTypes = new SOSArgument<>("valid_file_types", false, EnumSet.of(FileType.REGULAR,
            FileType.SYMLINK));

    // Arguments identifier - can be set to handle YADE alternatives (protocol fragment name)
    private SOSArgument<String> key = new SOSArgument<>(null, false);
    private List<AProviderArguments> alternatives;
    private Set<String> visitedAlternatives;

    private Boolean isHTTP = null;
    private Boolean isFTP = null;
    private boolean connectivityFaultSimulationEnabled;

    public void setEncryptionDecrypt(EncryptionDecryptArguments val) {
        encryptionDecrypt = val;
    }

    public EncryptionDecryptArguments getEncryptionDecrypt() {
        return encryptionDecrypt;
    }

    public boolean isEncryptionDecryptEnabled() {
        return encryptionDecrypt != null && !encryptionDecrypt.getPrivateKeyPath().isEmpty();
    }

    public void setCredentialStore(CredentialStoreArguments val) {
        credentialStore = val;
    }

    public CredentialStoreArguments getCredentialStore() {
        return credentialStore;
    }

    public boolean isCredentialStoreEnabled() {
        return credentialStore != null && !credentialStore.getFile().isEmpty();
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

    public SOSArgument<List<String>> getConfigurationFiles() {
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

    public boolean isHTTP() {
        evaluateIsProtocol();
        return isHTTP == null ? false : isHTTP;
    }

    public boolean isFTP() {
        evaluateIsProtocol();
        return isFTP == null ? false : isFTP;
    }

    public void setConnectivityFaultSimulationEnabled(boolean val) {
        connectivityFaultSimulationEnabled = val;
    }

    public boolean isConnectivityFaultSimulationEnabled() {
        return connectivityFaultSimulationEnabled;
    }

    public SOSArgument<String> getKey() {
        return key;
    }

    public boolean keyEquals(String key) {
        if (this.key.getValue() == null || key == null) {
            return false;
        }
        return this.key.getValue().equals(key);
    }

    public List<AProviderArguments> getAlternatives() {
        return alternatives;
    }

    public boolean hasAlternatives() {
        return !SOSCollection.isEmpty(alternatives);
    }

    public void mergeNestedAlternatives(AProviderArguments args) {
        if (alternatives == null) {
            alternatives = new ArrayList<>();
        }
        if (visitedAlternatives == null) {
            visitedAlternatives = new HashSet<>();
        }
        appendAlternaties(args, alternatives, visitedAlternatives);
    }

    private void appendAlternaties(AProviderArguments node, List<AProviderArguments> target, Set<String> visited) {
        if (node.getKey().getValue() == null) {
            return;
        }
        if (visited.add(node.getKey().getValue())) {
            target.add(node);
        } else {
            return;
        }

        // if (node.getKey().getValue() == null || visited.add(node.getKey().getValue())) {
        // target.add(node);
        // } else {
        // return;
        // }

        if (node.getAlternatives() != null) {
            List<AProviderArguments> children = new ArrayList<>(node.getAlternatives());
            for (AProviderArguments child : children) {
                appendAlternaties(child, target, visited);
            }
            node.clearAlternatives();
        }
    }

    private void clearAlternatives() {
        alternatives = null;
        visitedAlternatives = null;
    }

    private void evaluateIsProtocol() {
        if (isHTTP != null && isFTP != null) {
            return;
        }

        if (!protocol.isEmpty()) {
            switch (protocol.getValue()) {
            case AZURE_BLOB_STORAGE:
            case HTTP:
            case HTTPS:
            case WEBDAV:
            case WEBDAVS:
                isHTTP = true;
                isFTP = false;
                break;
            case FTP:
            case FTPS:
                isHTTP = false;
                isFTP = true;
                break;
            case LOCAL:
            case SFTP:
            case SMB:
            case SSH:
            case UNKNOWN:
                isHTTP = false;
                isFTP = false;
                break;
            }
        }
    }

}
