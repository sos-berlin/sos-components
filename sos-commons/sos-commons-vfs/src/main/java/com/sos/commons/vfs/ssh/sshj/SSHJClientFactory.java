package com.sos.commons.vfs.ssh.sshj;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.proxy.SOSProxyProvider;
import com.sos.commons.util.proxy.socket.ProxySocketFactory;
import com.sos.commons.vfs.exceptions.ProviderAuthenticationException;
import com.sos.commons.vfs.ssh.commons.SSHAuthMethod;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.keepalive.KeepAliveRunner;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.Service;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.PasswordResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

public class SSHJClientFactory {

    private static final String SSH_MSG_UNIMPLEMENTED = "SSH_MSG_UNIMPLEMENTED";

    protected static SSHClient createAuthenticatedClient(ISOSLogger logger, SSHProviderArguments args, SOSProxyProvider proxyProvider) throws Exception {
        /** 1) Create */
        SSHClient client = create(args, proxyProvider);
        /** 2) Connect */
        client.connect(args.getHost().getValue(), args.getPort().getValue());
        /** 3) Authenticate */
        authenticate(logger, args, client);
        /** 4) Post-Connect Keep Alive */
        if (!args.getServerAliveInterval().isEmpty()) {
            client.getConnection().getKeepAlive().setKeepAliveInterval(args.getServerAliveIntervalAsSeconds());
            if (!args.getServerAliveCountMax().isEmpty()) {
                // NOTE - KeepAliveRunner - is only available if KeepAliveProvider.KEEP_ALIVE is used, otherwise it is an instance of SSHJ Heartbeater
                ((KeepAliveRunner) client.getConnection().getKeepAlive()).setMaxAliveCount(args.getServerAliveCountMax().getValue().intValue());
            }
        }
        return client;
    }

    private static SSHClient create(SSHProviderArguments args, SOSProxyProvider proxyProvider) throws Exception {
        Config config = new DefaultConfig();
        // Keep Alive Provider - see NOTE above about KeepAliveRunner
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
        client.setTimeout(args.getSocketTimeoutAsMillis());
        client.setConnectTimeout(args.getConnectTimeoutAsMillis());
        // PROXY
        if (proxyProvider != null) {
            client.setSocketFactory(new ProxySocketFactory(proxyProvider));
        }
        return client;
    }

    private static void authenticate(ISOSLogger logger, SSHProviderArguments args, SSHClient client) throws ProviderAuthenticationException {
        try {
            if (!args.getPreferredAuthentications().isEmpty()) {
                usePreferredAuthentications(args, client);
            } else if (!args.getRequiredAuthentications().isEmpty()) {
                useRequiredAuthentications(logger, args, client);
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
    private static void useRequiredAuthentications(ISOSLogger logger, SSHProviderArguments args, SSHClient client) throws Exception {
        List<SSHAuthMethod> used = new ArrayList<>();
        Map<SSHAuthMethod, SSHJRequiredAuthenticationPartialResult> result = new LinkedHashMap<>();
        boolean hadPartialSuccess = false;
        String configured = SOSString.join(args.getRequiredAuthentications().getValue(), ",", m -> m.toString()).toLowerCase();
        try {
            for (SSHAuthMethod method : args.getRequiredAuthentications().getValue()) {
                switch (method) {
                case PUBLICKEY:
                    used.add(method);
                    result.put(method, partialRequiredAuthentication(args, client, getAuthPublickey(args, client)));
                    break;
                case PASSWORD:
                    used.add(method);
                    result.put(method, partialRequiredAuthentication(args, client, getAuthPassword(args)));
                    break;
                case KEYBOARD_INTERACTIVE:
                    used.add(method);
                    result.put(method, partialRequiredAuthentication(args, client, getAuthKeyboardInteractive(args)));
                    break;
                }
            }
            // Using multiple authentication methods in a single call (e.g. client.auth(user, methods))
            // is avoided here because OpenSSH servers that do not explicitly configure 'AuthenticationMethods'
            // (i.e. no enforced multi-factor auth) will treat the listed methods as alternatives.
            // In that case, if the first supported method (e.g. password) succeeds, other methods (e.g. publickey) are ignored silently.
            // To properly detect which methods are accepted or required, each method should be attempted individually.
            // client.auth(args.getUser().getValue(), methods);
        } catch (Exception e) {
            // Exception occurs if one of the methods is not configured on the server or e.g private key decoding failed
            StringBuilder sb = new StringBuilder("Required authentication is configured (").append(configured).append("). ");
            if (args.getRequiredAuthentications().getValue().size() > 1) {
                if (used.size() != result.size()) {
                    if (hadPartialSuccess) {

                    } else {
                        List<SSHAuthMethod> failed = used.stream().filter(m -> !result.containsKey(m)).collect(Collectors.toList());
                        sb.append("Not accepted (").append(SOSString.join(failed, ",", m -> m.toString()).toLowerCase()).append("). ");
                        if (e.getMessage() != null && e.getMessage().contains(SSH_MSG_UNIMPLEMENTED)) {
                            sb.append("The server does not appear to support multiple authentications.");
                        }// else: e.g. private key decoding failed
                    }
                }
            }
            throw new ProviderAuthenticationException(sb.toString(), e);
        } finally {
            hadPartialSuccess = client.getUserAuth().hadPartialSuccess();
            if (logger.isDebugEnabled()) {
                logger.debug("[useRequiredAuthentications][used]%s", used);
                logger.debug("[useRequiredAuthentications][result]%s", result);
            }
        }
        if (client.isAuthenticated()) {
            // Authentication succeeded, but not all configured required methods may have been validated (see comment above)
            if (args.getRequiredAuthentications().getValue().size() > 1) {
                if (!hadPartialSuccess) {
                    StringBuilder sb = new StringBuilder("Required authentication is configured (").append(configured).append("). ");
                    List<SSHAuthMethod> failed = result.entrySet().stream().filter(e -> !e.getValue().isAuthenticated()).map(e -> e.getKey()).collect(
                            Collectors.toList());
                    sb.append("Not accepted (").append(SOSString.join(failed, ",", m -> m.toString()).toLowerCase()).append("). ");
                    sb.append("The server does not appear to support multiple authentications.");
                    throw new ProviderAuthenticationException(sb.toString());
                }
            }
        } else {
            // One or more methods failed
            StringBuilder sb = new StringBuilder("Required authentication is configured (").append(configured).append("). ");
            if (hadPartialSuccess) {
                // If the server enforces multiple authentication methods, but not all were successfully authenticated

                // client.getUserAuth().getAllowedMethods() doc:
                // - The available authentication methods. This is only defined once an unsuccessful authentication has taken place.
                String serverResponded = String.join(", ", client.getUserAuth().getAllowedMethods());
                if (!SOSString.isEmpty(serverResponded)) {
                    sb.append("Server responded: ").append(serverResponded).append(". ");
                }
                // TODO:
                // The current 'Note:' is always appended when all configured authentication methods fail.
                // This should be refined to only appear when the failure may actually be related to incorrect method ordering
                // (i.e., when partial success was achieved but full authentication failed).
                // Ideally, add logic to differentiate between wrong method order and unsupported/misconfigured server setup.
                sb.append("Note: The client might have tried the methods in a different order than expected by the server.");
            } else {
                List<SSHAuthMethod> failed = result.entrySet().stream().filter(e -> !e.getValue().isAuthenticated()).map(e -> e.getKey()).collect(
                        Collectors.toList());
                sb.append("Not accepted (").append(SOSString.join(failed, ",", m -> m.toString()).toLowerCase()).append("). ");
            }
            throw new ProviderAuthenticationException(sb.toString());
        }
    }

    private static SSHJRequiredAuthenticationPartialResult partialRequiredAuthentication(SSHProviderArguments args, SSHClient client,
            net.schmizz.sshj.userauth.method.AuthMethod methodImpl) throws Exception {
        boolean authenticate = client.getUserAuth().authenticate(args.getUser().getValue(), (Service) client.getConnection(), methodImpl, client
                .getTransport().getTimeoutMs());
        return new SSHJRequiredAuthenticationPartialResult(authenticate, client.getUserAuth().hadPartialSuccess());
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
