package com.sos.commons.vfs.commons;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.sos.commons.encryption.arguments.EncryptionArguments;
import com.sos.commons.encryption.arguments.EncryptionDecryptArguments;
import com.sos.commons.encryption.common.EncryptedValue;
import com.sos.commons.encryption.decrypt.Decrypt;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.proxy.ProxyConfigArguments;

public class ProviderEncryptionResolver {

    /** resolves credential store password argument
     * 
     * @param logger
     * @param args
     * @return
     * @throws Exception */
    public static boolean resolveCredentialStorePass(ISOSLogger logger, AProviderArguments args) throws Exception {
        if (args == null || !args.isEncryptionDecryptEnabled() || !args.isCredentialStoreEnabled()) {
            return false;
        }

        SOSArgument<?> arg = args.getCredentialStore().getPassword();
        if (!hasEncryptedValue(arg)) {
            return false;
        }

        setPrivateKey(logger, args.getEncryptionDecrypt(), args.getEncryptionDecrypt().getPrivateKeyPath().getValue(), true);
        try {
            resolveArgument(args, arg);
        } finally {
            // not really necessary, since the credential store cannot contain another private key... ...
            cleanup(args.getEncryptionDecrypt());
        }
        return true;
    }

    /** @param args default arguments to resolve:<br/>
     *            args.host (args.port when host:port)<br/>
     *            args.user<br/>
     *            args.password<br/>
     *            proxyArgs.host (proxyArgs.port when host:port)<br/>
     *            proxyArgs.user<br/>
     *            proxyArgs.password
     * @param additional2resolve<br/>
     *            e.g. passphrase, domain
     * @return
     * @throws Exception */
    public static boolean resolve(ISOSLogger logger, AProviderArguments args, ProxyConfigArguments proxyArgs, List<SOSArgument<?>> additional2resolve)
            throws Exception {
        if (args == null || !args.isEncryptionDecryptEnabled()) {
            return false;
        }
        // The private key may already be set by the ProviderCredentialStoreResolver.
        // If not, load it from the path specified in privateKeyPath and set it.
        setPrivateKey(logger, args.getEncryptionDecrypt(), args.getEncryptionDecrypt().getPrivateKeyPath().getValue(), true);
        try {
            resolveArguments(args, proxyArgs, additional2resolve);
        } finally {
            cleanup(args.getEncryptionDecrypt());
        }
        return true;
    }

    public static void setPrivateKey(ISOSLogger logger, EncryptionDecryptArguments decryptArgs, String privateKeyInput, boolean readFromFile)
            throws Exception {
        if (decryptArgs.getPrivateKey().getValue() != null) {
            return;
        }
        if (privateKeyInput == null) {
            throw new Exception("[EnciphermentPrivateKey=" + decryptArgs.getPrivateKeyPath().getValue() + "]The private key could not be resolved.");
        }

        String content = privateKeyInput;
        if (readFromFile) {
            if (logger.isDebugEnabled()) {
                logger.debug("[%s][setPrivateKey]%s", ProviderEncryptionResolver.class.getSimpleName(), privateKeyInput);
            }
            try {
                content = Files.readString(Paths.get(privateKeyInput));
            } catch (Exception e) {
                throw new Exception("[EnciphermentPrivateKey=" + decryptArgs.getPrivateKeyPath().getValue() + "]" + e, e);
            }
        }
        decryptArgs.getPrivateKey().setValue(KeyUtil.getPrivateKeyFromString(content));
    }

    private static void cleanup(EncryptionDecryptArguments decryptArgs) {
        decryptArgs.getPrivateKey().setValue(null);
    }

    private static void resolveArguments(AProviderArguments args, ProxyConfigArguments proxyArgs, List<SOSArgument<?>> additional2resolve)
            throws Exception {
        // host(port),user,password
        resolveArgument(args, args.getHost(), args.getPort(), ":");
        resolveArgument(args, args.getUser());
        resolveArgument(args, args.getPassword());

        // proxy_host(proxy_port),proxy_user,proxy_password
        ProxyConfig proxyConfig = ProxyConfig.createInstance(proxyArgs);
        if (proxyConfig != null) {
            resolveArgument(args, proxyArgs.getHost(), proxyArgs.getPort(), ":");
            resolveArgument(args, proxyArgs.getUser());
            resolveArgument(args, proxyArgs.getPassword());
        }

        if (additional2resolve != null) {
            for (SOSArgument<?> arg : additional2resolve) {
                // e.g: passphrase, domain
                resolveArgument(args, arg);
            }
        }
    }

    private static void resolveArgument(AProviderArguments args, final SOSArgument<?> arg) throws Exception {
        resolveArgument(args, arg, null, null);
    }

    private static void resolveArgument(AProviderArguments args, final SOSArgument<?> mainArg, final SOSArgument<?> secondArg,
            String mainSecondSplitter) throws Exception {
        if (!hasEncryptedValue(mainArg)) {
            return;
        }

        try {
            String value = decrypt(mainArg.getValue().toString(), args.getEncryptionDecrypt());
            if (secondArg == null) {
                mainArg.applyValue(value);
            } else {
                String[] arr = value.split(mainSecondSplitter);
                switch (arr.length) {
                case 1:
                    mainArg.applyValue(value);
                    break;
                default:
                    mainArg.applyValue(arr[0]);
                    secondArg.applyValue(arr[1]);
                    break;
                }
            }
        } catch (Exception e) {
            throw new Exception(String.format("[%s]%s", mainArg.getName(), e.toString()), e);
        }
    }

    private static String decrypt(String encryptedValue, EncryptionDecryptArguments decryptArgs) throws Exception {
        if (encryptedValue == null) {
            return null;
        }
        EncryptedValue encrypted = EncryptedValue.getInstance("decrypt", encryptedValue);
        return Decrypt.decrypt(encrypted, decryptArgs.getPrivateKey().getValue());
    }

    // name=null - internal argument ...
    private static boolean hasEncryptedValue(final SOSArgument<?> arg) {
        return arg.getName() != null && EncryptionArguments.hasEncryptedValue(arg);
    }
}
