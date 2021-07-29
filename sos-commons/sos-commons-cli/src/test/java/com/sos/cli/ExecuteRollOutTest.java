package com.sos.cli;

import org.junit.Ignore;
import org.junit.Test;

public class ExecuteRollOutTest {

    @Test
    public void testMainWithoutArguments() throws Exception {
        ExecuteRollOut.main(new String[0]);
    }

    @Test
    @Ignore
    public void testMainWithArgumentsForController() throws Exception {
        ExecuteRollOut.main(createControllerArgs());
    }
    
    @Test
    @Ignore
    public void testMainWithArgumentsForAgent() throws Exception {
        ExecuteRollOut.main(createAgentArgs());
    }
    private String[] createControllerArgs() {
        String dn = "CN=sp, OU=development, O=SOS, C=DE, L=Berlin, S=Berlin";
        String jocUri = "https://joc-2-0-secondary:4443";
        String srcKeystore = "C:/sp/devel/js7/keys/sp-keystore.p12";
        String srcKeystoreType = "PKCS12";
        String srcKeystorePassw = "";
        String srcKeystoreEntryPassw = "";
        String srcTruststore = "C:/sp/devel/js7/keys/sp-truststore.p12";
        String srcTrustoreType = "PKCS12";
        String srcTruststorePassw = "";
        String targetKeystore = "C:/sp/devel/js7/testing/CLI/controller/https-keystore.p12";
        String targetKeystoreType = "PKCS12";
        String targetKeystorePassw = "jobscheduler";
        String targetKeystoreEntryPassw = "jobscheduler";
        String targetTruststore = "C:/sp/devel/js7/testing/CLI/controller/https-truststore.p12";
        String targetTrustoreType = "PKCS12";
        String targetTruststorePassw = "jobscheduler";
        String token = "efa50e0b-9d49-4487-8b6d-60c74d305ede";
        return new String[] {
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
                "--subject-dn=" + dn,
                "--target-keystore=" + targetKeystore,
                "--target-keystore-type=" + targetKeystoreType,
                "--target-keystore-pass=" + targetKeystorePassw,
                "--target-keystore-entry-pass=" + targetKeystoreEntryPassw,
                "--target-truststore=" + targetTruststore,
                "--target-truststore-type=" + targetTrustoreType,
                "--target-truststore-pass=" + targetTruststorePassw
        };
    }

    private String[] createAgentArgs() {
        String dn = "CN=sp, OU=development, O=SOS, C=DE, L=Berlin, S=Berlin";
        String jocUri = "https://joc-2-0-secondary:4443";
        String srcKeystore = "C:/sp/devel/js7/keys/sp-keystore.p12";
        String srcKeystoreType = "PKCS12";
        String srcKeystorePassw = "";
        String srcKeystoreEntryPassw = "";
        String srcTruststore = "C:/sp/devel/js7/keys/sp-truststore.p12";
        String srcTrustoreType = "PKCS12";
        String srcTruststorePassw = "";
        String targetKeystore = "C:/sp/devel/js7/testing/CLI/agent/https-keystore.p12";
        String targetKeystoreType = "PKCS12";
        String targetKeystorePassw = "jobscheduler";
        String targetKeystoreEntryPassw = "jobscheduler";
        String targetTruststore = "C:/sp/devel/js7/testing/CLI/agent/https-truststore.p12";
        String targetTrustoreType = "PKCS12";
        String targetTruststorePassw = "jobscheduler";
        String token = "92f6d171-1ad1-4990-a73c-edfcfa7bd7c2";
        return new String[] {
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
                "--subject-dn=" + dn,
                "--target-keystore=" + targetKeystore,
                "--target-keystore-type=" + targetKeystoreType,
                "--target-keystore-pass=" + targetKeystorePassw,
                "--target-keystore-entry-pass=" + targetKeystoreEntryPassw,
                "--target-truststore=" + targetTruststore,
                "--target-truststore-type=" + targetTrustoreType,
                "--target-truststore-pass=" + targetTruststorePassw
        };
    }

}
