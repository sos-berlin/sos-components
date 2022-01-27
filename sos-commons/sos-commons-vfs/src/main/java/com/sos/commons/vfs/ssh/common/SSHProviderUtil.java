package com.sos.commons.vfs.ssh.common;

import java.io.Reader;
import java.io.StringReader;

import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.util.SOSString;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.common.Factory;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

public class SSHProviderUtil {

    public static KeyProvider getKeyProviderFromKeepass(Config config, SSHProviderArguments args) throws Exception {
        org.linguafranca.pwdb.Entry<?, ?, ?, ?> ke = args.getKeepassDatabaseEntry();
        if (ke == null) {
            throw new Exception(String.format("[keepass][can't find database entry]attachment property name=%s", args
                    .getKeepassAttachmentPropertyName()));
        }
        try {
            return getKeyProvider(config, args.getKeepassDatabase().getAttachment(ke, args.getKeepassAttachmentPropertyName()), args.getPassphrase()
                    .getValue());
        } catch (Exception e) {
            String keePassPath = ke.getPath() + SOSKeePassPath.PROPERTY_PREFIX + args.getKeepassAttachmentPropertyName();
            throw new Exception(String.format("[keepass][%s]%s", keePassPath, e.toString()), e);
        }
    }

    public static KeyProvider getKeyProvider(Config config, byte[] privateKey, String passphrase) throws Exception {
        Reader r = null;
        try {
            KeyFormat kf = KeyProviderUtil.detectKeyFileFormat(new StringReader(new String(privateKey, "UTF-8")), false);
            r = new StringReader(new String(privateKey, "UTF-8"));
            FileKeyProvider kp = Factory.Named.Util.create(config.getFileKeyProviderFactories(), kf.toString());
            if (kp == null) {
                throw new SSHException("No provider available for " + kf + " key file");
            }
            kp.init(r, SOSString.isEmpty(passphrase) ? null : getPasswordFinder(passphrase));
            return kp;
        } catch (Throwable e) {
            throw e;
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    public static PasswordFinder getPasswordFinder(String password) {
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
}
