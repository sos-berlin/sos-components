package com.sos.js7.job;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.helper.TestJob;
import com.sos.js7.job.helper.TestJobArguments;
import com.sos.js7.job.resolver.DefaultBase64ValueResolver;
import com.sos.js7.job.resolver.DefaultEncryptionResolver;

import js7.data_for_java.order.JOutcome;

public class JobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobTest.class);

    @Ignore
    @Test
    public void testBase64Resolver() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("test", DefaultBase64ValueResolver.getPrefix() + "xxx");
        args.put("path", DefaultBase64ValueResolver.getPrefix() + "yyy");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testEncryptionECResolver() throws Exception {
        Path resoucesDir = Paths.get("../../sos-commons/sos-commons-hibernate").resolve("src/test/resources/encryption");
        Path keyPath = resoucesDir.resolve("ec_test.key");

        Map<String, Object> args = new HashMap<>();
        args.put("log_all_arguments", Boolean.valueOf(true));
        args.put(DefaultEncryptionResolver.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, keyPath.toFile().getCanonicalPath());
        args.put("my_arg", DefaultEncryptionResolver.getPrefix()
                + "BBFzQJSWD8lVzwf02qkX81PmXGiSxDc9Uf2fs594jjRJLycsBnrYJaUBjjl9a5Fo4IZ6I17mFww6xdGxTj2eZHuKmwKy2+Txae+kvJxImd+ZUdL8Gn2UrUtHdjsS4cOD1n6CysJBckh1EVGecriZPeque/kd KFPflA0MAilZ1KYfPDSiGw== 2sO5Qtw07FD1X4JsQycDhQ==");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testEncryptionECPWResolver() throws Exception {
        Path resoucesDir = Paths.get("../../sos-commons/sos-commons-hibernate").resolve("src/test/resources/encryption");
        // Path certificatePath = resoucesDir.resolve("ec_test.crt");
        Path keyPath = resoucesDir.resolve("ec_test_pw.key");// agent.key

        // X509Certificate cert = KeyUtil.getX509Certificate(certificatePath);
        Map<String, Object> args = new HashMap<>();
        args.put("log_all_arguments", Boolean.valueOf(true));
        // args.put(DefaultEncryptionResolver.ARG_NAME_ENCIPHERMENT_CERTIFICATE, CertificateUtils.asPEMString(cert));
        args.put(DefaultEncryptionResolver.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, keyPath.toFile().getCanonicalPath());
        args.put(DefaultEncryptionResolver.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PASSWORD, "jobscheduler");
        args.put(DefaultEncryptionResolver.ARG_NAME_ENCIPHERMENT_FAIL_ON_ERROR, "false");
        args.put("my_arg", DefaultEncryptionResolver.getPrefix()
                + "BBFzQJSWD8lVzwf02qkX81PmXGiSxDc9Uf2fs594jjRJLycsBnrYJaUBjjl9a5Fo4IZ6I17mFww6xdGxTj2eZHuKmwKy2+Txae+kvJxImd+ZUdL8Gn2UrUtHdjsS4cOD1n6CysJBckh1EVGecriZPeque/kd KFPflA0MAilZ1KYfPDSiGw== 2sO5Qtw07FD1X4JsQycDhQ==");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }
}
