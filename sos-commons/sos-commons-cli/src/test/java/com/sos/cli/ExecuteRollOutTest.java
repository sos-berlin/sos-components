package com.sos.cli;

import org.junit.Test;

public class ExecuteRollOutTest {

    @Test
    public void testMainWithoutArguments() throws Exception {
        ExecuteRollOut.main(new String[0]);
    }

    @Test
    public void testMainWithArguments() throws Exception {
        String dn = "CN=sp, OU=development, O=SOS, C=DE, L=Berlin, S=Berlin";
        String jocUri = "http://localhost:3333";
        String srcKeystore = "C:/sp/devel/js7/keys/sp-keystore.p12";
        String srcKeystoreType = "PKCS12";
        String srcKeystorePassw = "";
        String srcKeystoreEntryPassw = "";
        String srcTruststore = "C:/sp/devel/js7/keys/sp-truststore.p12";
        String srcTrustoreType = "PKCS12";
        String srcTruststorePassw = "";
        String token = "d6983b2c-24af-403b-8871-70a84f0d0939";
        String[] args = new String[] {
                "--token=" + token,
                "--joc-uri=" + jocUri,
                "--san=" + "sp.sos",
                "--source-keystore=" + srcKeystore,
                "--source-keystore-type=" + srcKeystoreType,
                "--source-keystore-pass=" + srcKeystorePassw,
                "--source-keystore-entry-pass=" + srcKeystoreEntryPassw,
                "--source-truststore=" + srcTruststore,
                "--source-truststore-type=" + srcTrustoreType,
                "--source-truststore-pass=" + srcTruststorePassw,
                "--subject-dn=" + dn
        };
        ExecuteRollOut.main(args);
//        private static final String TRG_KEYSTORE = "--target-keystore";
//        private static final String TRG_KEYSTORE_TYPE = "--target-keystore-type";
//        private static final String TRG_KEYSTORE_PASS = "--target-keystore-pass";
//        private static final String TRG_TRUSTSTORE = "--target-truststore";
//        private static final String TRG_TRUSTSTORE_TYPE = "--target-truststore-type";
//        private static final String TRG_TRUSTSTORE_PASS = "--target-truststore-pass";
    }
}
