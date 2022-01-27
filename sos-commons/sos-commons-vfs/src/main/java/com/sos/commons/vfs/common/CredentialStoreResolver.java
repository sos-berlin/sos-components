package com.sos.commons.vfs.common;

import java.nio.file.Files;
import java.nio.file.Path;

import org.linguafranca.pwdb.Entry;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;

public class CredentialStoreResolver {

    /** @param args default arguments to resolve:<br/>
     *            host (port when host:port)<br/>
     *            user<br/>
     *            password<br/>
     *            proxyHost (proxyPort when host:port)<br/>
     *            proxyUser<br/>
     *            proxyPassword
     * @param additional2resolve<br/>
     *            e.g. passphrase, domain
     * @return
     * @throws Exception */
    public static boolean resolve(AProviderArguments args, SOSArgument<?>... additional2resolve) throws Exception {
        if (args.getCredentialStore() == null) {
            return false;
        }
        if (args.getCredentialStore().getCredentialStoreFile().getValue() != null && args.getKeepassDatabase() == null) {
            setDatabase(args);
            keepass2Arguments(args, additional2resolve);
            return true;
        }
        return false;
    }

    /**/
    public static boolean resolveAttachment(AProviderArguments args, SOSArgument<?> arg) throws Exception {
        if (args.getCredentialStore() == null) {
            return false;
        }
        if (args.getKeepassDatabase() == null) {
            if (args.getCredentialStore().getCredentialStoreFile().getValue() == null) {
                return false;
            }
            setDatabase(args);
        }
        if (arg.getValue() == null) {
            return false;
        }

        SOSKeePassPath keePassPath = new SOSKeePassPath(args.getKeepassDatabase().isKDBX(), arg.getValue().toString(), args.getCredentialStore()
                .getCredentialStoreEntryPath().getValue());
        if (keePassPath.isValid()) {
            Entry<?, ?, ?, ?> entry = getEntry(args, keePassPath.getEntryPath());
            if (entry != null) {
                args.setKeepassDatabaseEntry(entry);
                args.setKeepassAttachmentPropertyName(keePassPath.getPropertyName());
                return true;
            }
        }
        return false;
    }

    private static void setDatabase(AProviderArguments args) throws Exception {
        SOSKeePassDatabase kpd = new SOSKeePassDatabase(SOSPath.toAbsolutePath(args.getCredentialStore().getCredentialStoreFile().getValue()));
        Path keyFile = null;
        if (args.getCredentialStore().getCredentialStoreKeyFile().getValue() != null) {
            Path cskf = SOSPath.toAbsolutePath(args.getCredentialStore().getCredentialStoreKeyFile().getValue());
            if (Files.notExists(cskf)) {
                throw new Exception(String.format("[%s]key file not found", SOSKeePassDatabase.getFilePath(cskf)));
            }
            keyFile = cskf;
        } else {
            Path defaultKeyFile = SOSKeePassDatabase.getDefaultKeyFile(SOSPath.toAbsolutePath(args.getCredentialStore().getCredentialStoreFile()
                    .getValue()));
            if (Files.exists(defaultKeyFile)) {
                keyFile = defaultKeyFile;
            } else {
                if (SOSString.isEmpty(args.getCredentialStore().getCredentialStorePassword().getValue())) {
                    throw new Exception(String.format("[%s]key file not found. password is empty", SOSKeePassDatabase.getFilePath(defaultKeyFile)));
                }
            }
        }
        if (keyFile == null) {
            kpd.load(args.getCredentialStore().getCredentialStorePassword().getValue());
        } else {
            kpd.load(args.getCredentialStore().getCredentialStorePassword().getValue(), keyFile);
        }
        args.setKeepassDatabase(kpd);
    }

    private static Entry<?, ?, ?, ?> keepass2Arguments(AProviderArguments args, SOSArgument<?>... additional2resolve) throws Exception {
        Entry<?, ?, ?, ?> entry = keepass2Argument(args, args.getHost(), null);
        entry = keepass2Argument(args, args.getUser(), entry);
        entry = keepass2Argument(args, args.getPassword(), entry);

        if (args.getProxy() != null) {
            entry = keepass2Argument(args, args.getProxyHost(), entry);
            entry = keepass2Argument(args, args.getProxyUser(), entry);
            entry = keepass2Argument(args, args.getProxyPassword(), entry);
            args.recreateProxy();
        }

        if (additional2resolve != null) {
            for (SOSArgument<?> arg : additional2resolve) {
                // e.g: passphrase, domain
                keepass2Argument(args, arg, entry);
            }
        }
        return entry;
    }

    private static Entry<?, ?, ?, ?> keepass2Argument(AProviderArguments args, final SOSArgument<?> arg, Entry<?, ?, ?, ?> lastEntry)
            throws Exception {
        if (arg.getName() == null || arg.getValue() == null) {// intern
            return lastEntry;
        }
        SOSKeePassPath keePassPath = new SOSKeePassPath(args.getKeepassDatabase().isKDBX(), arg.getValue().toString(), args.getCredentialStore()
                .getCredentialStoreEntryPath().getValue());
        Entry<?, ?, ?, ?> entry = null;
        String fileName = args.getCredentialStore().getCredentialStoreFile().getValue().toString();
        String argName = arg.getName();
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
            boolean setMultipleValue = false;

            if (argName.equals(args.getHost().getName()) || argName.equals(args.getProxyHost().getName())) {
                String[] arr = value.split(":");
                switch (arr.length) {
                case 1:
                    break;
                default:
                    setMultipleValue = true;
                    if (argName.equals(args.getHost().getName())) {
                        args.getHost().setValue(arr[0]);
                        if (args instanceof SSHProviderArguments) {
                            SSHProviderArguments sshArgs = (SSHProviderArguments) args;
                            sshArgs.getPort().setValue(Integer.parseInt(arr[1]));
                        }
                    } else {
                        args.getProxyHost().setValue(arr[0]);
                        args.getProxyPort().setValue(Integer.parseInt(arr[1]));
                    }
                }
            }
            if (!setMultipleValue) {
                arg.fromString(value);
            }
        }
        return entry == null ? lastEntry : entry;
    }

    private static Entry<?, ?, ?, ?> getEntry(AProviderArguments args, final String entryPath) throws Exception {
        Entry<?, ?, ?, ?> entry = args.getKeepassDatabase().getEntryByPath(entryPath);
        if (entry == null) {
            throw new Exception(String.format("[%s][%s]entry not found", args.getCredentialStore().getCredentialStoreFile().getValue(), entryPath));
        }
        if (entry.getExpires()) {
            throw new Exception(String.format("[%s][%s]entry is expired (%s)", args.getCredentialStore().getCredentialStoreFile().getValue(),
                    entryPath, entry.getExpiryTime()));
        }
        return entry;
    }

}
