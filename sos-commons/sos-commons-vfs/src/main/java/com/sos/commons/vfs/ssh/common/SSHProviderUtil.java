package com.sos.commons.vfs.ssh.common;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.util.SOSString;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

public class SSHProviderUtil {

    public static KeyProvider getKeyProviderFromKeepass(SSHClient sshClient, SSHProviderArguments args) throws Exception {
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
            return sshClient.loadKeys(pk, null, SOSString.isEmpty(args.getPassphrase().getValue()) ? null : getPasswordFinder(args.getPassphrase()
                    .getValue()));
        } catch (Exception e) {
            String keePassPath = ke.getPath() + SOSKeePassPath.PROPERTY_PREFIX + args.getKeepassAttachmentPropertyName();
            throw new Exception(String.format("[keepass][%s]%s", keePassPath, e.toString()), e);
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
