package com.sos.commons.vfs.commons;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.linguafranca.pwdb.Entry;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.encryption.arguments.EncryptionArguments;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.proxy.ProxyConfigArguments;

/** Note: each argument value can contain a different Keepass entry path */
public class ProviderCredentialStoreResolver {

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
        if (args == null || !args.isCredentialStoreEnabled()) {
            return false;
        }
        if (args.getCredentialStore().getKeepassDatabase() == null) {
            setDatabase(args);
            Entry<?, ?, ?, ?> entry = keepass2Arguments(args, proxyArgs, additional2resolve);
            setDecryptionPrivateKey(logger, entry, args);
            return true;
        }
        return false;
    }

    /** YADECientBannerWriter - resolve the host IP address at the DEBUG log level */
    public static String resoveHostNameQuietly(ISOSLogger logger, AProviderArguments args) {
        try {
            if (args.getHost().getValue() == null || !args.getHost().getValue().startsWith(SOSKeePassPath.PATH_PREFIX)) {
                return args.getHost().getValue();
            }

            setDatabase(args);
            SOSArgument<String> tmp = new SOSArgument<>(args.getHost().getName(), false);
            tmp.setValue(args.getHost().getValue());
            keepass2Argument(null, args, tmp, null, null);
            return tmp.getValue();
        } catch (Exception e) {
            logger.debug("[resoveHostNameQuietly]" + e);
            return null;
        } finally {
            args.getCredentialStore().setKeepassDatabase(null);
        }
    }

    /**/
    public static boolean resolveAttachment(AProviderArguments args, SOSArgument<?> arg) throws Exception {
        if (arg.isEmpty()) {
            return false;
        }
        if (args.getCredentialStore() == null) {
            return false;
        }
        if (args.getCredentialStore().getKeepassDatabase() == null) {
            if (args.getCredentialStore().getFile().getValue() == null) {
                return false;
            }
            setDatabase(args);
        }
        SOSKeePassPath keePassPath = new SOSKeePassPath(args.getCredentialStore().getKeepassDatabase().getHandler().isKdbx(), arg.getValue()
                .toString(), args.getCredentialStore().getEntryPath().getValue());
        if (keePassPath.isValid()) {
            Entry<?, ?, ?, ?> entry = getEntry(args, keePassPath.getEntryPath());
            if (entry != null) {
                args.getCredentialStore().setKeepassDatabaseEntry(entry);
                args.getCredentialStore().setKeepassAttachmentPropertyName(keePassPath.getPropertyName());
                return true;
            }
        }
        return false;
    }

    private static void setDatabase(AProviderArguments args) throws Exception {
        args.getCredentialStore().setKeepassDatabase(load(args));
    }

    private static SOSKeePassDatabase load(AProviderArguments args) throws Exception {
        SOSKeePassDatabase kpd = new SOSKeePassDatabase(SOSPath.toAbsolutePath(args.getCredentialStore().getFile().getValue()), SOSKeePassDatabase
                .getModule(args.getCredentialStore().getKeePassModule().getValue()));
        Path keyFile = null;
        if (args.getCredentialStore().getKeyFile().getValue() != null) {
            Path cskf = SOSPath.toAbsolutePath(args.getCredentialStore().getKeyFile().getValue());
            if (Files.notExists(cskf)) {
                throw new Exception(String.format("[%s]key file not found", SOSKeePassDatabase.getFilePath(cskf)));
            }
            keyFile = cskf;
        } else {
            keyFile = SOSKeePassDatabase.getDefaultKeyFile(SOSPath.toAbsolutePath(args.getCredentialStore().getFile().getValue()));
            if (keyFile == null) {
                if (SOSString.isEmpty(args.getCredentialStore().getPassword().getValue())) {
                    throw new Exception(String.format("default key file not found. password is empty"));
                }
            }
        }
        if (keyFile == null) {
            kpd.load(args.getCredentialStore().getPassword().getValue());
        } else {
            kpd.load(args.getCredentialStore().getPassword().getValue(), keyFile);
        }
        return kpd;
    }

    /** Note: each argument value can contain a different Keepass entry path */
    private static Entry<?, ?, ?, ?> keepass2Arguments(AProviderArguments args, ProxyConfigArguments proxyArgs,
            List<SOSArgument<?>> additional2resolve) throws Exception {
        // host(port),user,password
        Entry<?, ?, ?, ?> entry = keepass2Argument(null, args, args.getHost(), args.getPort(), ":");
        entry = keepass2Argument(entry, args, args.getUser());
        entry = keepass2Argument(entry, args, args.getPassword());

        // proxy_host(proxy_port),proxy_user,proxy_password
        ProxyConfig proxyConfig = ProxyConfig.createInstance(proxyArgs);
        if (proxyConfig != null) {
            entry = keepass2Argument(entry, args, proxyArgs.getHost(), proxyArgs.getPort(), ":");
            entry = keepass2Argument(entry, args, proxyArgs.getUser());
            entry = keepass2Argument(entry, args, proxyArgs.getPassword());
        }

        if (additional2resolve != null) {
            for (SOSArgument<?> arg : additional2resolve) {
                // e.g: passphrase, domain
                keepass2Argument(entry, args, arg);
            }
        }
        return entry;
    }

    private static Entry<?, ?, ?, ?> keepass2Argument(Entry<?, ?, ?, ?> lastEntry, AProviderArguments args, final SOSArgument<?> arg)
            throws Exception {
        return keepass2Argument(lastEntry, args, arg, null, null);
    }

    private static Entry<?, ?, ?, ?> keepass2Argument(Entry<?, ?, ?, ?> lastEntry, AProviderArguments args, final SOSArgument<?> mainArg,
            final SOSArgument<?> secondArg, String mainSecondSplitter) throws Exception {
        if (mainArg.getName() == null || mainArg.isEmpty()) {// intern
            return lastEntry;
        }
        SOSKeePassPath keePassPath = new SOSKeePassPath(args.getCredentialStore().getKeepassDatabase().getHandler().isKdbx(), mainArg.getValue()
                .toString(), args.getCredentialStore().getEntryPath().getValue());
        Entry<?, ?, ?, ?> entry = null;
        String fileName = args.getCredentialStore().getFile().getValue().toString();
        String argName = mainArg.getName();
        if (keePassPath.isValid()) {
            if (lastEntry == null || !keePassPath.getEntryPath().equals(lastEntry.getPath())) {
                entry = getEntry(args, keePassPath.getEntry());
            } else {
                entry = lastEntry;
            }
            String value = entry.getProperty(keePassPath.getPropertyName());
            if (value == null) {
                throw new Exception(String.format("[%s][%s][%s]value is null", fileName, argName, keePassPath.toString()));
            }
            if (secondArg == null || EncryptionArguments.hasEncryptedValue(value)) {
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
        }
        return entry == null ? lastEntry : entry;
    }

    private static Entry<?, ?, ?, ?> getEntry(AProviderArguments args, final String entryPath) throws Exception {
        Entry<?, ?, ?, ?> entry = args.getCredentialStore().getKeepassDatabase().getEntryByPath(entryPath);
        if (entry == null) {
            throw new Exception(String.format("[%s][%s]entry not found", args.getCredentialStore().getFile().getValue(), entryPath));
        }
        if (entry.getExpires()) {
            throw new Exception(String.format("[%s][%s]entry is expired (%s)", args.getCredentialStore().getFile().getValue(), entryPath, entry
                    .getExpiryTime()));
        }
        return entry;
    }

    private static void setDecryptionPrivateKey(ISOSLogger logger, Entry<?, ?, ?, ?> lastEntry, AProviderArguments args) throws Exception {
        if (!args.isEncryptionDecryptEnabled()) {
            return;
        }

        SOSKeePassPath keePassPath = new SOSKeePassPath(args.getCredentialStore().getKeepassDatabase().getHandler().isKdbx(), args
                .getEncryptionDecrypt().getPrivateKeyPath().getValue().toString(), args.getCredentialStore().getEntryPath().getValue());
        Entry<?, ?, ?, ?> entry = null;

        if (keePassPath.isValid()) {
            if (lastEntry == null || !keePassPath.getEntryPath().equals(lastEntry.getPath())) {
                entry = getEntry(args, keePassPath.getEntry());
            } else {
                entry = lastEntry;
            }
            boolean readFromFile = false;
            String privateKeyInput = SOSKeePassDatabase.getProperty(entry, keePassPath);
            if (privateKeyInput == null) {
                privateKeyInput = SOSKeePassDatabase.getAttachmentPropertyAsString(args.getCredentialStore().getKeepassDatabase(), entry,
                        keePassPath);
            } else {
                readFromFile = true;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("[%s][setDecryptionPrivateKey][%s]isAttachment=%s", ProviderCredentialStoreResolver.class.getSimpleName(), args
                        .getEncryptionDecrypt().getPrivateKeyPath().getValue(), !readFromFile);
            }

            ProviderEncryptionResolver.setPrivateKey(logger, args.getEncryptionDecrypt(), privateKeyInput, readFromFile);
        }
    }

}
