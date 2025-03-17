package com.sos.commons.vfs.commons;

import java.nio.file.Files;
import java.nio.file.Path;

import org.linguafranca.pwdb.Entry;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.impl.ProxyArguments;
import com.sos.commons.vfs.commons.proxy.ProxyProvider;

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
    public static boolean resolve(AProviderArguments args, ProxyArguments proxyArgs, SOSArgument<?>... additional2resolve) throws Exception {
        if (args == null || args.getCredentialStore() == null) {
            return false;
        }
        if (args.getCredentialStore().getFile().getValue() != null && args.getCredentialStore().getKeepassDatabase() == null) {
            setDatabase(args);
            keepass2Arguments(args, proxyArgs, additional2resolve);
            return true;
        }
        return false;
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
        args.getCredentialStore().setKeepassDatabase(kpd);
    }

    /** Note: each argument value can contain a different Keepass entry path */
    private static Entry<?, ?, ?, ?> keepass2Arguments(AProviderArguments args, ProxyArguments proxyArgs, SOSArgument<?>... additional2resolve)
            throws Exception {
        // host(port),user,password
        Entry<?, ?, ?, ?> entry = keepass2Argument(null, args, args.getHost(), args.getPort(), ":");
        entry = keepass2Argument(entry, args, args.getUser());
        entry = keepass2Argument(entry, args, args.getPassword());

        // proxy_host(proxy_port),proxy_user,proxy_password
        ProxyProvider proxyProvider = ProxyProvider.createInstance(proxyArgs);
        if (proxyProvider != null) {
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

}
