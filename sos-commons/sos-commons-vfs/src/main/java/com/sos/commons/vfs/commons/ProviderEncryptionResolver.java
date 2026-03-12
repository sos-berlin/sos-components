package com.sos.commons.vfs.commons;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;

import com.sos.commons.encryption.arguments.EncryptionArguments;
import com.sos.commons.encryption.common.EncryptedValue;
import com.sos.commons.encryption.decrypt.Decrypt;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.proxy.ProxyConfigArguments;

public class ProviderEncryptionResolver {

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
    public static boolean resolve(AProviderArguments args, ProxyConfigArguments proxyArgs, SOSArgument<?>... additional2resolve) throws Exception {
        if (args == null || !args.isEncryptionDecryptEnabled()) {
            return false;
        }
        resolveArguments(args, proxyArgs, additional2resolve);
        return true;
    }

    private static void resolveArguments(AProviderArguments args, ProxyConfigArguments proxyArgs, SOSArgument<?>... additional2resolve)
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
        if (!EncryptionArguments.hasEncryptedValue(mainArg) || mainArg.getName() == null) { // name=null - internal argument ...
            return;
        }

        try {
            String value = decrypt(mainArg.getValue().toString(), args.getEncryptionDecrypt().getPrivateKeyPath().getValue());
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

    private static String decrypt(String encryptedValue, String pathToPrivateKey) throws Exception {
        Path privateKeyPath = Paths.get(pathToPrivateKey);
        PrivateKey priv = KeyUtil.getPrivateKeyFromString(Files.readString(privateKeyPath));
        EncryptedValue encVal = EncryptedValue.getInstance("decrypt", encryptedValue);
        return Decrypt.decrypt(encVal, priv);
    }
}
