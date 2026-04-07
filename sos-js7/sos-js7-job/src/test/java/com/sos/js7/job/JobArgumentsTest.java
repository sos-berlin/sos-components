package com.sos.js7.job;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.helper.TestJobArguments;

public class JobArgumentsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobArgumentsTest.class);

    @Ignore
    @Test
    public void testDecrypt() throws Exception {
        String enc =
                "enc:BBQHBPIu3jKSrjSTsVgwLs3l4v4p6F67+XBevixcT/ujsFGco2B5KtWQll0j8513CZ5M1hQX8hP/vFx+hWWQiTh8fxUxulH0hkH6la7TlIbD2LN0Sp9+3202IXZCgw8Ny0s2Q64= K+Yg1BG0WEe+ipbsDeHuEg== QGzGIFCBRaEiDEhA5EK+OPpf7erJzL9MI68S0CXbk6M=";
        TestJobArguments args = new TestJobArguments();
        args.getTest().setValue(enc);
        executeDecrypt(args.getTest());

        args.getTest().setValue("{a:true, endpoint: decrypt(" + enc + ")}");
        executeDecrypt(args.getTest());
    }

    private void executeDecrypt(JobArgument<?> arg) throws Exception {
        LOGGER.info("[testDecrypt][before]" + arg.getName() + "=" + arg.getValue());
        JobArguments.decryptIfNeeded(arg, "ec_test_not_exists.key");
        LOGGER.info("[testDecrypt][after]" + arg.getName() + "=" + arg.getValue());
    }
}
