package com.sos.commons.util.keystore;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

import com.sos.commons.util.SOSCollection;

public class AliasForcingKeyManager extends X509ExtendedKeyManager {

    private final X509KeyManager delegate;
    private final List<String> forcedAliases;

    public AliasForcingKeyManager(X509KeyManager delegate, List<String> aliases) {
        this.delegate = Objects.requireNonNull(delegate);
        this.forcedAliases = aliases;
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        String alias = chooseAlias(keyType, issuers);
        if (alias != null) {
            return alias;
        }
        return delegate.chooseClientAlias(keyType, issuers, socket);
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        if (!SOSCollection.isEmpty(forcedAliases)) {
            for (String alias : forcedAliases) {
                String[] serverAliases = delegate.getServerAliases(keyType, issuers);
                if (serverAliases != null && Arrays.asList(serverAliases).contains(alias)) {
                    return alias;
                }
            }
        }
        return delegate.chooseServerAlias(keyType, issuers, socket);
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        return delegate.getCertificateChain(alias);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        return delegate.getPrivateKey(alias);
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        if (SOSCollection.isEmpty(forcedAliases)) {
            return delegate.getClientAliases(keyType, issuers);
        }
        return forcedAliases.toArray(new String[0]);
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        if (SOSCollection.isEmpty(forcedAliases)) {
            return delegate.getServerAliases(keyType, issuers);
        }
        return forcedAliases.toArray(new String[0]);
    }

    @Override
    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        String alias = chooseAlias(keyType, issuers);
        if (alias != null) {
            return alias;
        }

        if (delegate instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager) delegate).chooseEngineClientAlias(keyType, issuers, engine);
        }
        return null;
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        if (!SOSCollection.isEmpty(forcedAliases)) {
            for (String alias : forcedAliases) {
                String[] serverAliases = delegate.getServerAliases(keyType, issuers);
                if (serverAliases != null && Arrays.asList(serverAliases).contains(alias)) {
                    return alias;
                }
            }
        }

        if (delegate instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager) delegate).chooseEngineServerAlias(keyType, issuers, engine);
        }
        return null;
    }

    private String chooseAlias(String[] keyType, Principal[] issuers) {
        if (SOSCollection.isEmpty(forcedAliases)) {
            return null;
        }
        for (String alias : forcedAliases) {
            for (String kt : keyType) {
                String[] clientAliases = delegate.getClientAliases(kt, issuers);
                if (clientAliases != null && Arrays.asList(clientAliases).contains(alias)) {
                    return alias;
                }
                String[] serverAliases = delegate.getServerAliases(kt, issuers);
                if (serverAliases != null && Arrays.asList(serverAliases).contains(alias)) {
                    return alias;
                }
            }
        }
        return forcedAliases.get(0);
    }
}
