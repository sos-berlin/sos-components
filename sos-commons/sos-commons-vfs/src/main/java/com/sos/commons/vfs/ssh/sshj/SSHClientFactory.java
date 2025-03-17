package com.sos.commons.vfs.ssh.sshj;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.commons.proxy.ProxyProvider;
import com.sos.commons.vfs.commons.proxy.ProxySocketFactory;
import com.sos.commons.vfs.exceptions.ProviderAuthenticationException;
import com.sos.commons.vfs.ssh.commons.SSHAuthMethod;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.Service;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.PasswordResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

public class SSHClientFactory {

    protected static SSHClient createAuthenticatedClient(SSHProviderArguments args, ProxyProvider proxyProvider) throws Exception {
        /** 1) Create */
        SSHClient client = create(args, proxyProvider);
        /** 2) Connect */
        client.connect(args.getHost().getValue(), args.getPort().getValue());
        /** 3) Authenticate */
        authenticate(args, client);
        /** 4) Post-Connect Keep Alive */
        if (!args.getServerAliveInterval().isEmpty()) {
            client.getConnection().getKeepAlive().setKeepAliveInterval(args.getServerAliveInterval().getValue());
        }
        return client;
    }

    private static SSHClient create(SSHProviderArguments args, ProxyProvider proxyProvider) throws Exception {
        Config config = new DefaultConfig();
        // Keep Alive Provider
        if (!args.getServerAliveInterval().isEmpty()) {
            config.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
        }
        // SSH Client
        SSHClient client = new SSHClient(config);
        setHostKeyVerifier(args, client);
        setCompression(args, client);
        // CHARSET
        client.setRemoteCharset(args.getRemoteCharset().getValue());
        // TIMEOUT
        client.setTimeout(args.getSocketTimeoutAsMs());
        client.setConnectTimeout(args.getConnectTimeoutAsMs());
        // PROXY
        if (proxyProvider != null) {
            client.setSocketFactory(new ProxySocketFactory(proxyProvider));
        }
        return client;
    }

    private static void authenticate(SSHProviderArguments args, SSHClient client) throws ProviderAuthenticationException {
        try {
            if (!args.getPreferredAuthentications().isEmpty()) {
                usePreferredAuthentications(args, client);
            } else if (!args.getRequiredAuthentications().isEmpty()) {
                useRequiredAuthentications(args, client);
            } else {
                useAuthMethodAuthentication(args, client);
            }
        } catch (ProviderAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderAuthenticationException(e);
        }
    }

    private static void usePreferredAuthentications(SSHProviderArguments args, SSHClient client) throws Exception {
        List<net.schmizz.sshj.userauth.method.AuthMethod> methods = new LinkedList<>();
        for (SSHAuthMethod am : args.getPreferredAuthentications().getValue()) {
            switch (am) {
            case PUBLICKEY:
                methods.add(getAuthPublickey(args, client));
                break;
            case PASSWORD:
                methods.add(getAuthPassword(args));
                break;
            case KEYBOARD_INTERACTIVE:
                methods.add(getAuthKeyboardInteractive(args));
                break;
            }
        }
        client.auth(args.getUser().getValue(), methods);
    }

    /** ssh(d)_config AuthenticationMethods */
    private static void useRequiredAuthentications(SSHProviderArguments args, SSHClient client) throws Exception {
        for (SSHAuthMethod am : args.getRequiredAuthentications().getValue()) {
            switch (am) {
            case PUBLICKEY:
                partialAuthentication(args, client, getAuthPublickey(args, client));
                break;
            case PASSWORD:
                partialAuthentication(args, client, getAuthPassword(args));
                break;
            case KEYBOARD_INTERACTIVE:
                partialAuthentication(args, client, getAuthKeyboardInteractive(args));
                break;
            }
        }
    }

    private static void partialAuthentication(SSHProviderArguments args, SSHClient client, net.schmizz.sshj.userauth.method.AuthMethod method)
            throws ProviderAuthenticationException, UserAuthException, TransportException {
        if (!client.getUserAuth().authenticate(args.getUser().getValue(), (Service) client.getConnection(), method, client.getTransport()
                .getTimeoutMs())) {
            if (!client.getUserAuth().hadPartialSuccess()) {
                throw new ProviderAuthenticationException();
            }
        }
    }

    private static void useAuthMethodAuthentication(SSHProviderArguments args, SSHClient client) throws Exception {
        if (args.getAuthMethod().getValue() == null) {
            throw new SOSRequiredArgumentMissingException(args.getAuthMethod().getName());
        }
        net.schmizz.sshj.userauth.method.AuthMethod method = null;
        switch (args.getAuthMethod().getValue()) {
        case PUBLICKEY:
            method = getAuthPublickey(args, client);
            break;
        case PASSWORD:
            method = getAuthPassword(args);
            break;
        case KEYBOARD_INTERACTIVE:
            method = getAuthKeyboardInteractive(args);
            break;
        }
        client.auth(args.getUser().getValue(), method);
    }

    private static AuthPassword getAuthPassword(SSHProviderArguments args) throws Exception {
        if (args.getPassword().isEmpty()) {
            throw new SOSRequiredArgumentMissingException(args.getPassword().getName());
        }
        return new AuthPassword(getPasswordFinder(args.getPassword().getValue()));
    }

    private static AuthKeyboardInteractive getAuthKeyboardInteractive(SSHProviderArguments args) throws Exception {
        if (args.getPassword().isEmpty()) {
            throw new SOSRequiredArgumentMissingException(args.getPassword().getName());
        }
        return new AuthKeyboardInteractive(new PasswordResponseProvider(getPasswordFinder(args.getPassword().getValue())));
    }

    private static AuthPublickey getAuthPublickey(SSHProviderArguments args, SSHClient client) throws Exception {
        // TODO Agent support getArguments().getUseKeyAgent().getValue()
        KeyProvider keyProvider = null;
        // from KeePass attachment
        if (useKeyProviderFromKeepass(args)) {
            keyProvider = getKeyProviderFromKeepass(client, args.getCredentialStore(), args.getPassphrase().getValue());
        } else {// from File
            if (SOSString.isEmpty(args.getAuthFile().getValue())) {
                throw new SOSRequiredArgumentMissingException(args.getAuthFile().getName());
            }
            Path authFile = Paths.get(args.getAuthFile().getValue());
            if (args.getPassphrase().isEmpty()) {
                keyProvider = client.loadKeys(authFile.toFile().getCanonicalPath());
            } else {
                keyProvider = client.loadKeys(authFile.toFile().getCanonicalPath(), args.getPassphrase().getValue());
            }
        }
        return new AuthPublickey(keyProvider);
    }

    private static void setHostKeyVerifier(SSHProviderArguments args, SSHClient client) throws IOException {
        // default HostKeyVerifier -> OpenSSHKnownHosts
        if (args.getStrictHostkeyChecking().getValue()) {
            if (args.getHostkeyLocation().isEmpty()) {
                client.loadKnownHosts();// default search in <user.home>/.ssh/known_hosts|known_hosts2
            } else {
                client.loadKnownHosts(args.getHostkeyLocation().getValue().toFile());
            }
        } else {
            client.addHostKeyVerifier(new PromiscuousVerifier());
        }
    }

    private static void setCompression(SSHProviderArguments args, SSHClient client) throws Exception {
        if (args.getUseZlibCompression().getValue()) {
            // JCRAFT compression com.jcraft.jzlib-1.1.3.jar
            // uses default compression_level=6 (1-best speed, 9-best compression)
            // see JZlib.Z_DEFAULT_COMPRESSION(-1) and com.jcraft.jzlib.Deplate.deflateInit(with Z_DEFAULT_COMPRESSION))
            client.useCompression();
        }
    }

    private static PasswordFinder getPasswordFinder(String password) {
        return new PasswordFinder() {

            @Override
            public char[] reqPassword(Resource<?> resource) {
                return password.toCharArray().clone();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }

        };
    }

    private static boolean useKeyProviderFromKeepass(SSHProviderArguments args) {
        if (args.getCredentialStore() == null) {
            return false;
        }
        CredentialStoreArguments cs = args.getCredentialStore();
        return cs.getKeepassDatabase() != null && cs.getKeepassDatabaseEntry() != null && !SOSString.isEmpty(cs.getKeepassAttachmentPropertyName());
    }

    private static KeyProvider getKeyProviderFromKeepass(SSHClient sshClient, CredentialStoreArguments args, String passphrase) throws Exception {
        SOSKeePassDatabase kd = (SOSKeePassDatabase) args.getKeepassDatabase();
        if (kd == null) {
            throw new Exception("[keepass]keepass_database property is null");
        }
        org.linguafranca.pwdb.Entry<?, ?, ?, ?> ke = args.getKeepassDatabaseEntry();
        if (ke == null) {
            throw new Exception(String.format("[keepass][can't find database entry]attachment property name=%s", args
                    .getKeepassAttachmentPropertyName()));
        }
        try {
            String pk = new String(kd.getAttachment(ke, args.getKeepassAttachmentPropertyName()), "UTF-8");
            return sshClient.loadKeys(pk, null, SOSString.isEmpty(passphrase) ? null : getPasswordFinder(passphrase));
        } catch (Exception e) {
            String keePassPath = ke.getPath() + SOSKeePassPath.PROPERTY_PREFIX + args.getKeepassAttachmentPropertyName();
            throw new Exception(String.format("[keepass][%s]%s", keePassPath, e.toString()), e);
        }
    }

}
