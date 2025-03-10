package com.sos.commons.vfs.commons;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.arguments.impl.ProxyArguments;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;

public class CredentialStoreResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialStoreResolverTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        LOGGER.info(System.getProperty("user.dir"));

        CredentialStoreArguments csArgs = new CredentialStoreArguments();
        csArgs.getFile().setValue("src/test/resources/test_kdbx.kdbx");
        csArgs.getPassword().setValue("test");

        ProxyArguments proxyArgs = new ProxyArguments();
        proxyArgs.getType().setValue(java.net.Proxy.Type.HTTP);
        proxyArgs.getHost().setValue("cs://server/Proxy/Socks/proxy.sos@url");
        proxyArgs.getUser().setValue("cs://server/Proxy/HTTP/proxy.sos@user");
        proxyArgs.getPassword().setValue("cs://server/Proxy/HTTP/proxy.sos@password");

        SSHProviderArguments args = new SSHProviderArguments();
        args.setCredentialStore(csArgs);
        args.setProxy(proxyArgs);

        args.getHost().setValue("cs://server/SFTP/ssh.sos@url");
        args.getPassword().setValue("cs://server/SFTP/ssh.sos@password");
        args.getPassphrase().setValue("cs://server/SFTP/ssh.sos@user");
        args.getAuthFile().setValue("cs://server/SFTP/ssh.sos@test.txt");

        ProviderCredentialStoreResolver.resolve(args, args.getProxy(), args.getPassphrase());
        ProviderCredentialStoreResolver.resolveAttachment(args, args.getAuthFile());

        LOGGER.info("HOST=" + args.getHost().getValue());
        LOGGER.info("PORT=" + args.getPort().getValue());
        LOGGER.info("PASSWORD=" + args.getPassword().getValue());
        LOGGER.info("PASSPHRASE=" + args.getPassphrase().getValue());
        LOGGER.info("PROXY=" + args.getProxy());

        LOGGER.info("KEEPASSDATABASE=" + csArgs.getKeepassDatabase());
        LOGGER.info("KEEPASSDATABASE ENTRY=" + csArgs.getKeepassDatabaseEntry());
        LOGGER.info("KEEPASSDATABASE ATTACHMENT PROPERTY NAME=" + csArgs.getKeepassAttachmentPropertyName());

    }
}
