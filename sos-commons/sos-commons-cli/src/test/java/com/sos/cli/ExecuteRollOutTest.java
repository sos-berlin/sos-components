package com.sos.cli;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

public class ExecuteRollOutTest {

    @Test
    public void testMainWithoutArguments() throws Exception {
        ExecuteRollOut.main(new String[0]);
    }

    @Test
    @Ignore
    public void testShiftMainArguments() throws Exception {
        String[] args = createSkriptArgsWithSourceKeyStore();
        String[] newArgsStream = Arrays.stream(args).skip(1).toArray(String[]::new);
        String[] newArgsCopy = Arrays.copyOfRange(args, 1, args.length);
        System.out.println("***** Original *****");
        for (String arg : args) {
            System.out.print(arg + " | ");
        }
        System.out.println();
        System.out.println("***** shifted with stream *****");
        for (String arg : newArgsStream) {
            System.out.print(arg + " | ");
        }
        System.out.println();
        System.out.println("***** shifted with copyOfRange *****");
        for (String arg : newArgsCopy) {
            System.out.print(arg + " | ");
        }
        System.out.println();
    }

    @Test
    @Ignore
    public void testMainWithSourceKeyStoreArgumentsForController() throws Exception {
        ExecuteRollOut.main(createControllerArgsWithSourceKeyStore());
    }

    @Test
    @Ignore
    public void testMainWithSourceKeyStoreArgumentsForAgent() throws Exception {
        ExecuteRollOut.main(createAgentArgsWithSourceKeyStore());
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

    @Test
    @Ignore
    public void testMainWithHttpArgumentsForController() throws Exception {
        ExecuteRollOut.main(createControllerArgsHttp());
    }

    @Test
    @Ignore
    public void testMainWithMinArgumentsForController() throws Exception {
        System.setProperty("js7.config-directory", "C:/sp/devel/js7/testing/CLI/controller");
        ExecuteRollOut.main(createMinimalControllerArgsHttp());
    }

    private String[] createControllerArgsWithSourceKeyStore() {
        String dn = "CN=agent, OU=development, O=SOS, C=DE, L=Berlin, S=Berlin";
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
        String token = "73bfc4b8-3f15-44b9-a75b-cdb44aec8f4b";
        String keystoreAlias = "controller";
        String truststoreAlias = "sp root ca";
        String san = "controller.sos, controller, sp.sos, sp";
        return new String[] { 
                "--token=" + token, 
                "--joc-uri=" + jocUri, 
                "--san=" + san, 
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
                "--target-truststore-pass=" + targetTruststorePassw, 
                "--key-alias=" + keystoreAlias, 
                "--ca-alias=" + truststoreAlias };
    }

    private String[] createAgentArgsWithSourceKeyStore() {
        String dn = "CN=agent, OU=development, O=SOS, C=DE, L=Berlin, S=Berlin";
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
        String token = "a1f7edb7-e37e-4501-a97b-055e896c984d";
        String keystoreAlias = "agent";
        String truststoreAlias = "sp root ca";
        String san = "agent.sos, agent, sp.sos, sp";
        return new String[] {
                "--token=" + token, 
                "--joc-uri=" + jocUri, 
                "--san=" + san, 
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
                "--target-truststore-pass=" + targetTruststorePassw, 
                "--key-alias=" + keystoreAlias, 
                "--ca-alias=" + truststoreAlias };
    }

    private String[] createControllerArgs() {
        String dn = "CN=sp, OU=development, O=SOS, C=DE, L=Berlin, S=Berlin";
        String jocUri = "https://joc-2-0-secondary:4443";
        String srcPrivKey = "C:/sp/devel/js7/keys/sp/sp.key";
        String srcCert = "C:/sp/devel/js7/keys/sp/sp.cer";
        String srcCaCert = "C:/sp/devel/js7/keys/sp/sos_intermediate_ca.cer, C:/sp/devel/js7/keys/sp/sos_root_ca.cer";
        String targetKeystore = "C:/sp/devel/js7/testing/CLI/controller/withSourceKeys/https-keystore.p12";
        String targetKeystoreType = "PKCS12";
        String targetKeystorePassw = "jobscheduler";
        String targetKeystoreEntryPassw = "jobscheduler";
        String targetTruststore = "C:/sp/devel/js7/testing/CLI/controller/withSourceKeys/https-truststore.p12";
        String targetTrustoreType = "PKCS12";
        String targetTruststorePassw = "jobscheduler";
        String token = "73bfc4b8-3f15-44b9-a75b-cdb44aec8f4b";
        String keystoreAlias = "sp";
        String truststoreAlias = "sp root ca";
        String san = "sp.sos, sp";
        return new String[] { 
                "--token=" + token, 
                "--joc-uri=" + jocUri, 
                "--san=" + san, 
                "--source-private-key=" + srcPrivKey, 
                "--source-certificate=" + srcCert, 
                "--source-ca-cert=" + srcCaCert, 
                "--subject-dn=" + dn, 
                "--target-keystore=" + targetKeystore, 
                "--target-keystore-type=" + targetKeystoreType, 
                "--target-keystore-pass=" + targetKeystorePassw, 
                "--target-keystore-entry-pass=" + targetKeystoreEntryPassw, 
                "--target-truststore=" + targetTruststore, 
                "--target-truststore-type=" + targetTrustoreType, 
                "--target-truststore-pass=" + targetTruststorePassw, 
                "--key-alias=" + keystoreAlias,
                "--ca-alias=" + truststoreAlias };
    }

    private String[] createAgentArgs() {
        String dn = "CN=agent, OU=development, O=SOS, C=DE, L=Berlin, S=Berlin";
        String jocUri = "https://joc-2-0-secondary:4443";
        String srcPrivKey = "C:/sp/devel/js7/keys/sp/sp.key";
        String srcCert = "C:/sp/devel/js7/keys/sp/sp.cer";
        String srcCaCert = "C:/sp/devel/js7/keys/sp/sos_intermediate_ca.cer, C:/sp/devel/js7/keys/sp/sos_root_ca.cer";
        String targetKeystore = "C:/sp/devel/js7/testing/CLI/agent/withSourceKeys/https-keystore.p12";
        String targetKeystoreType = "PKCS12";
        String targetKeystorePassw = "jobscheduler";
        String targetKeystoreEntryPassw = "jobscheduler";
        String targetTruststore = "C:/sp/devel/js7/testing/CLI/agent/withSourceKeys/https-truststore.p12";
        String targetTrustoreType = "PKCS12";
        String targetTruststorePassw = "jobscheduler";
        String token = "a1f7edb7-e37e-4501-a97b-055e896c984d";
        String keystoreAlias = "My Agent Key";
        String truststoreAlias = "sp root ca";
        String san = "agent.sos, agent, sp.sos, sp";
        return new String[] { 
                "--token=" + token, 
                "--joc-uri=" + jocUri, 
                "--san=" + san, 
                "--source-private-key=" + srcPrivKey, 
                "--source-certificate=" + srcCert, 
                "--source-ca-cert=" + srcCaCert, 
                "--subject-dn=" + dn, 
                "--target-keystore=" + targetKeystore, 
                "--target-keystore-type=" + targetKeystoreType, 
                "--target-keystore-pass=" + targetKeystorePassw, 
                "--target-keystore-entry-pass=" + targetKeystoreEntryPassw, 
                "--target-truststore=" + targetTruststore, 
                "--target-truststore-type=" + targetTrustoreType, 
                "--target-truststore-pass=" + targetTruststorePassw, 
                "--key-alias=" + keystoreAlias,
                "--ca-alias=" + truststoreAlias };
    }

    private String[] createControllerArgsHttp() {
        String dn = "CN=sp, OU=development, O=SOS, C=DE, L=Berlin, S=Berlin";
        String jocUri = "http://sp.sos:3333";
        String targetKeystore = "C:/sp/devel/js7/testing/CLI/controller/withHTTP/https-keystore.p12";
        String targetKeystoreType = "PKCS12";
        String targetKeystorePassw = "jobscheduler";
        String targetKeystoreEntryPassw = "jobscheduler";
        String targetTruststore = "C:/sp/devel/js7/testing/CLI/controller/withHTTP/https-truststore.p12";
        String targetTrustoreType = "PKCS12";
        String targetTruststorePassw = "jobscheduler";
        String token = "73bfc4b8-3f15-44b9-a75b-cdb44aec8f4b";
        String keystoreAlias = "sp";
        String truststoreAlias = "sp root ca";
        String san = "sp.sos, sp";
        return new String[] { 
                "--token=" + token, 
                "--joc-uri=" + jocUri, 
                "--san=" + san, 
                "--subject-dn=" + dn, 
                "--target-keystore=" + targetKeystore, 
                "--target-keystore-type=" + targetKeystoreType, 
                "--target-keystore-pass=" + targetKeystorePassw, 
                "--target-keystore-entry-pass=" + targetKeystoreEntryPassw, 
                "--target-truststore=" + targetTruststore, 
                "--target-truststore-type=" + targetTrustoreType, 
                "--target-truststore-pass=" + targetTruststorePassw, 
                "--key-alias=" + keystoreAlias,
                "--ca-alias=" + truststoreAlias };
    }

    private String[] createMinimalControllerArgsHttp() {
        String jocUri = "http://sp.sos:3333";
        String token = "0f15b55a-3521-4836-8f95-bbfb6be93652";
        return new String[] { 
                "--token=" + token, 
                "--joc-uri=" + jocUri };
    }

    private String[] createSkriptArgsWithSourceKeyStore() {
        return new String[] {"cert", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th"};
    }
    
    private String[] createTest20221209Arguments () {
        // ./bin/controller_instance.cmd cert test 
        return new String[] {
                "--token=ced55cc0-0bd5-4716-b314-ddf6b837607d",
                "--joc-uri=http://sp.sos:4444",
                "--san=\"sp.sos, sp\"",
                "--subject-dn=CN=sp, OU=Development, O=SOS, C=DE, L=Berlin, ST=Berlin",
                "--key-alias=sp2",
                "--ca-alias=SP Root CA",
                "--target-keystore=C:/sp/devel/js7/testing/2022-12-09/controller_primary_keystore.p12",
                "--target-keystore-pass=jobscheduler",
                "--target-keystore-entry-pass=jobscheduler",
                "--target-truststore=C:/sp/devel/js7/testing/2022-12-09/controller_primary_truststore.p12",
                "--target-truststore-pass=jobscheduler"};
    }

    @Test
    @Ignore
    public void test20221209MainForController() throws Exception {
//        System.setProperty(ExecuteRollOut.PRIVATE_CONF_JS7_PARAM_CONFDIR, "C:/ProgramData/sos-berlin.com/js7/controller/controller/config");
        ExecuteRollOut.main(createTest20221209Arguments());
    }
}
