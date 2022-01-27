package com.sos.commons.vfs.common;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;

public class CredentialStoreResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialStoreResolverTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        LOGGER.info(System.getProperty("user.dir"));

        SOSCredentialStoreArguments csArgs = new SOSCredentialStoreArguments();
        csArgs.getCredentialStoreFile().setValue("src/test/resources/test_kdbx.kdbx");
        csArgs.getCredentialStorePassword().setValue("test");

        SSHProviderArguments args = new SSHProviderArguments();
        args.setCredentialStore(csArgs);

        args.getHost().setValue("cs://server/SFTP/ssh.sos@url");
        args.getPassword().setValue("cs://server/SFTP/ssh.sos@password");
        args.getPassphrase().setValue("cs://server/SFTP/ssh.sos@user");
        args.getAuthFile().setValue("cs://server/SFTP/ssh.sos@test.txt");

        args.getProxyType().setValue(java.net.Proxy.Type.HTTP);
        args.getProxyHost().setValue("cs://server/Proxy/Socks/proxy.sos@url");
        args.getProxyUser().setValue("cs://server/Proxy/HTTP/proxy.sos@user");
        args.getProxyPassword().setValue("cs://server/Proxy/HTTP/proxy.sos@password");

        CredentialStoreResolver.resolve(args, args.getPassphrase());
        CredentialStoreResolver.resolveAttachment(args, args.getAuthFile());

        LOGGER.info("HOST=" + args.getHost().getValue());
        LOGGER.info("PORT=" + args.getPort().getValue());
        LOGGER.info("PASSWORD=" + args.getPassword().getValue());
        LOGGER.info("PASSPHRASE=" + args.getPassphrase().getValue());
        LOGGER.info("PROXY=" + args.getProxy());

        LOGGER.info("KEEPASSDATABASE=" + args.getKeepassDatabase());
        LOGGER.info("KEEPASSDATABASE ENTRY=" + args.getKeepassDatabaseEntry());
        LOGGER.info("KEEPASSDATABASE ATTACHMENT PROPERTY NAME=" + args.getKeepassAttachmentPropertyName());

    }
}
