package com.sos.commons.vfs.common;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.commons.vfs.common.proxy.Proxy;

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

    // Basic
    private SOSArgument<Protocol> protocol = new SOSArgument<Protocol>("protocol", true);
    private SOSArgument<String> host = new SOSArgument<String>("host", true);
    private SOSArgument<String> user = new SOSArgument<String>("user", false);
    private SOSArgument<String> password = new SOSArgument<String>("password", false, DisplayMode.MASKED);

    // Proxy
    private Proxy proxy;
    private SOSArgument<java.net.Proxy.Type> proxyType = new SOSArgument<java.net.Proxy.Type>("proxy_type", false);
    private SOSArgument<String> proxyHost = new SOSArgument<String>("proxy_host", false);
    private SOSArgument<Integer> proxyPort = new SOSArgument<Integer>("proxy_port", false, -1);
    private SOSArgument<String> proxyUser = new SOSArgument<String>("proxy_user", false);
    private SOSArgument<String> proxyPassword = new SOSArgument<String>("proxy_password", false, DisplayMode.MASKED);
    // Socket connect timeout in seconds based on socket.connect
    private SOSArgument<Integer> proxyConnectTimeout = new SOSArgument<Integer>("proxy_connect_timeout", false, 30);

    // Internal/Keepass
    private SOSArgument<SOSKeePassDatabase> keepassDatabase = new SOSArgument<SOSKeePassDatabase>(null, false);
    private SOSArgument<org.linguafranca.pwdb.Entry<?, ?, ?, ?>> keepassDatabaseEntry = new SOSArgument<org.linguafranca.pwdb.Entry<?, ?, ?, ?>>(null,
            false);
    private SOSArgument<String> keepassAttachmentPropertyName = new SOSArgument<String>(null, false);

    private SOSCredentialStoreArguments credentialStore;

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

    public Proxy getProxy() {
        if (proxy != null) {
            return proxy;
        }
        tryCreateProxy();
        return proxy;
    }

    protected Proxy recreateProxy() {
        if (proxy == null) {
            return proxy;
        }
        tryCreateProxy();
        return proxy;
    }

    private void tryCreateProxy() {
        if (proxyType.getValue() != null && proxyHost.getValue() != null) {
            proxy = new Proxy(proxyType.getValue(), proxyHost.getValue(), proxyPort.getValue(), proxyUser.getValue(), proxyPassword.getValue(), asMs(
                    proxyConnectTimeout));
        }
    }

    public void setProxy(Proxy val) {
        proxy = val;
    }

    protected SOSArgument<java.net.Proxy.Type> getProxyType() {
        return proxyType;
    }

    protected SOSArgument<String> getProxyHost() {
        return proxyHost;
    }

    protected SOSArgument<Integer> getProxyPort() {
        return proxyPort;
    }

    protected SOSArgument<String> getProxyUser() {
        return proxyUser;
    }

    protected SOSArgument<String> getProxyPassword() {
        return proxyPassword;
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

    protected void setCredentialStore(SOSCredentialStoreArguments val) {
        credentialStore = val;
    }

    public int asMs(SOSArgument<Integer> arg) {
        return arg.getValue() == null ? 0 : arg.getValue() * 1_000;
    }

    public SOSCredentialStoreArguments getCredentialStore() {
        return credentialStore;
    }
}
