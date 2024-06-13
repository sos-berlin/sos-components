package com.sos.js7.job.resolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.js7.job.UnitTestJobHelper;
import com.sos.js7.job.helper.TestJob;
import com.sos.js7.job.helper.TestJobArguments;

import js7.data_for_java.order.JOutcome;

public class StandardEncryptionResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardEncryptionResolverTest.class);

    private static final Path ENCRYPTION_RESOURCES_DIR = Paths.get("../../sos-commons/sos-commons-encryption/src/test/resources/encryption");

   
    @Ignore
    @Test
    public void testEncryptionECResolver() throws Exception {
        Path keyPath = ENCRYPTION_RESOURCES_DIR.resolve("ec_test.key");

        Map<String, Object> args = new HashMap<>();
        args.put("log_all_arguments", Boolean.valueOf(true));
        args.put(StandardEncryptionResolver.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, keyPath.toFile().getCanonicalPath());
        args.put("my_arg", StandardEncryptionResolver.getPrefix()
                + "BBFzQJSWD8lVzwf02qkX81PmXGiSxDc9Uf2fs594jjRJLycsBnrYJaUBjjl9a5Fo4IZ6I17mFww6xdGxTj2eZHuKmwKy2+Txae+kvJxImd+ZUdL8Gn2UrUtHdjsS4cOD1n6CysJBckh1EVGecriZPeque/kd KFPflA0MAilZ1KYfPDSiGw== 2sO5Qtw07FD1X4JsQycDhQ==");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testEncryptionECWithCertificateResolver() throws Exception {
        Path certificatePath = ENCRYPTION_RESOURCES_DIR.resolve("ec_test.crt");
        Path keyPath = ENCRYPTION_RESOURCES_DIR.resolve("ec_test.key");

        Map<String, Object> args = new HashMap<>();
        args.put("log_all_arguments", Boolean.valueOf(true));
        args.put(StandardEncryptionResolver.ARG_NAME_ENCIPHERMENT_CERTIFICATE, CertificateUtils.asPEMString(KeyUtil.getX509Certificate(
                certificatePath)));
        args.put(StandardEncryptionResolver.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, keyPath.toFile().getCanonicalPath());
        args.put("my_arg", StandardEncryptionResolver.getPrefix()
                + "BBFzQJSWD8lVzwf02qkX81PmXGiSxDc9Uf2fs594jjRJLycsBnrYJaUBjjl9a5Fo4IZ6I17mFww6xdGxTj2eZHuKmwKy2+Txae+kvJxImd+ZUdL8Gn2UrUtHdjsS4cOD1n6CysJBckh1EVGecriZPeque/kd KFPflA0MAilZ1KYfPDSiGw== 2sO5Qtw07FD1X4JsQycDhQ==");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    // will fail because there is intentionally no support for private keys with password
    public void testEncryptionECPWResolver() throws Exception {
        Path keyPath = ENCRYPTION_RESOURCES_DIR.resolve("ec_test_pw.key");// agent.key

        Map<String, Object> args = new HashMap<>();
        args.put("log_all_arguments", Boolean.valueOf(true));
        args.put(StandardEncryptionResolver.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, keyPath.toFile().getCanonicalPath());
        args.put("my_arg", StandardEncryptionResolver.getPrefix()
                + "BBFzQJSWD8lVzwf02qkX81PmXGiSxDc9Uf2fs594jjRJLycsBnrYJaUBjjl9a5Fo4IZ6I17mFww6xdGxTj2eZHuKmwKy2+Txae+kvJxImd+ZUdL8Gn2UrUtHdjsS4cOD1n6CysJBckh1EVGecriZPeque/kd KFPflA0MAilZ1KYfPDSiGw== 2sO5Qtw07FD1X4JsQycDhQ==");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
