package com.sos.js7.job;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
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

//    @Ignore
    @Test
    public void testEncryptionResolver() throws Exception {
        Path certificatePath = Paths.get("/sp/tmp/myX509.crt"); 
        X509Certificate cert = KeyUtil.getX509Certificate(certificatePath);
        
        Map<String, Object> args = new HashMap<>();
        args.put("test", DefaultBase64ValueResolver.getPrefix() + "xxx");
        args.put("path", DefaultBase64ValueResolver.getPrefix() + "yyy");
        args.put(DefaultEncryptionResolver.ARG_NAME_ENCIPHERMENT_CERTIFICATE, CertificateUtils.asPEMString(cert));
        args.put(DefaultEncryptionResolver.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, "/sp/tmp/myECprivate.key");

//        args.put("test", DefaultEncryptionResolver.getPrefix() + "zzz");
        args.put("myPasswd", "enc://BPrshdAB5pj4TDmUS/h2gBj1GmHsqiCmzROhuRWvv9t84Cecr6EaKGgJIBNYaRfRwZOSld19ChO5Iuc8rNXkqm7EUpOEVsnRDN5NGjF1LG50A9Li87xWGRZW6PwsjKT4XOeYjUI= JL8/6uX2j5+nvk+SmELs5Q== 8g2unq5vs252FM/U6BGNHw==");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
