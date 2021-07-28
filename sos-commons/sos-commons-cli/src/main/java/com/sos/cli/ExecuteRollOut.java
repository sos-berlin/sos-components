package com.sos.cli;

import java.net.URI;
import java.security.KeyStore;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.keyStore.KeyStoreType;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.sign.JocKeyPair;

public class ExecuteRollOut {
    
    private static final String WS_API = "/joc/api/authentication/certificate/create";
    private static final String HELP = "--help";
    private static final String TOKEN = "--token";
    private static final String JOC_URI = "--joc-uri";
    private static final String SAN = "--san";
    private static final String SRC_KEYSTORE = "--source-keystore";
    private static final String SRC_KEYSTORE_TYPE = "--source-keystore-type";
    private static final String SRC_KEYSTORE_PASS = "--source-keystore-pass";
    private static final String SRC_KEYSTORE_ENTRY_PASS = "--source-keystore-entry-pass";
    private static final String SRC_TRUSTSTORE = "--source-truststore";
    private static final String SRC_TRUSTSTORE_TYPE = "--source-truststore-type";
    private static final String SRC_TRUSTSTORE_PASS = "--source-truststore-pass";
    private static final String TRG_KEYSTORE = "--target-keystore";
    private static final String TRG_KEYSTORE_TYPE = "--target-keystore-type";
    private static final String TRG_KEYSTORE_PASS = "--target-keystore-pass";
    private static final String TRG_TRUSTSTORE = "--target-truststore";
    private static final String TRG_TRUSTSTORE_TYPE = "--target-truststore-type";
    private static final String TRG_TRUSTSTORE_PASS = "--target-truststore-pass";
    private static final String SUBJECT_DN = "--subject-dn";
    private static SOSRestApiClient client;
    private static String token;
    private static String subjectDN;
    private static String san;
    private static URI jocUri;
    private static String srcKeystore;
    private static String srcKeystoreType;
    private static String srcKeystorePasswd;
    private static String srcKeystoreEntryPasswd;
    private static String srcTruststore;
    private static String srcTruststoreType;
    private static String srcTruststorePasswd;
    private static String targetKeystore;
    private static String targetKeystoreType;
    private static String targetKeystorePasswd;
    private static String targetTruststore;
    private static String targetTruststoreType;
    private static String targetTruststorePasswd;
    private static ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);


    public static void main(String[] args) throws Exception {
        
        if (args == null || args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("--help"))) {
            printUsage();
//            System.exit(0);
        } else {
            for(int i = 0; i < args.length; i++) {
                String[] split = args[i].split("=", 2);
                if (args[i].startsWith(TOKEN + "=")) {
                    token = split[1];
                } else if (args[i].startsWith(JOC_URI + "=")) {
                    jocUri = URI.create(split[1]);
                } else if (args[i].startsWith(SRC_KEYSTORE + "=")) {
                    srcKeystore = split[1];
                } else if (args[i].startsWith(SRC_KEYSTORE_TYPE + "=")) {
                    srcKeystoreType = split[1];
                } else if (args[i].startsWith(SRC_KEYSTORE_PASS + "=")) {
                    srcKeystorePasswd = split[1];
                } else if (args[i].startsWith(SRC_KEYSTORE_ENTRY_PASS + "=")) {
                    srcKeystoreEntryPasswd = split[1];
                } else if (args[i].startsWith(SRC_TRUSTSTORE + "=")) {
                    srcTruststore = split[1];
                } else if (args[i].startsWith(SRC_TRUSTSTORE_TYPE + "=")) {
                    srcTruststoreType = split[1];
                } else if (args[i].startsWith(SRC_TRUSTSTORE_PASS + "=")) {
                    srcTruststorePasswd = split[1];
                } else if (args[i].startsWith(TRG_KEYSTORE + "=")) {
                    targetKeystore = split[1];
                } else if (args[i].startsWith(TRG_KEYSTORE_TYPE + "=")) {
                    targetKeystoreType = split[1];
                } else if (args[i].startsWith(TRG_KEYSTORE_PASS + "=")) {
                    targetKeystorePasswd = split[1];
                } else if (args[i].startsWith(TRG_TRUSTSTORE + "=")) {
                    targetTruststore = split[1];
                } else if (args[i].startsWith(TRG_TRUSTSTORE_TYPE + "=")) {
                    targetTruststoreType = split[1];
                } else if (args[i].startsWith(TRG_TRUSTSTORE_PASS + "=")) {
                    targetTruststorePasswd = split[1];
                } else if (args[i].startsWith(SUBJECT_DN + "=")) {
                    subjectDN = split[1];
                } else if (args[i].startsWith(SAN + "=")) {
                    san = split[1];
                }
            }
            String response = callWebService();
            closeClient();
            JocKeyPair jocKeyPair = mapper.readValue(response, JocKeyPair.class);
        }
    }
    
    private static void tryCreateClient() throws Exception {
        if (client != null) {
            return;
        }
        KeyStore srcKeyStore = null;
        KeyStore targetKeyStore = null;
        KeyStore srcTrustStore = null;
        KeyStore targetTrustStore = null;
        if (srcKeystore != null && !srcKeystore.isEmpty()) {
            srcKeyStore = KeyStoreUtil.readKeyStore(srcKeystore, KeyStoreType.fromValue(srcKeystoreType), srcKeystorePasswd);
        }
        if (targetKeystore != null && !targetKeystore.isEmpty()) {
            targetKeyStore = KeyStoreUtil.readKeyStore(targetKeystore, KeyStoreType.fromValue(targetKeystoreType), targetKeystorePasswd);
        }
        if (srcTruststore != null && !srcTruststore.isEmpty()) {
            srcTrustStore = KeyStoreUtil.readTrustStore(srcTruststore, KeyStoreType.fromValue(srcTruststoreType), srcTruststorePasswd);
        }
        if (targetTruststore != null && !targetTruststore.isEmpty()) {
            targetTrustStore = KeyStoreUtil.readTrustStore(targetTruststore, KeyStoreType.fromValue(targetTruststoreType), targetTruststorePasswd);
        }

        client = new SOSRestApiClient();
        if (srcKeyStore != null && srcTrustStore != null) {
            client.setSSLContext(srcKeyStore, srcKeystoreEntryPasswd.toCharArray(), srcTrustStore);
        }
    }

    private static void closeClient() {
        if (client != null) {
            client.closeHttpClient();
        }
    }
    
    private static String createRequestBody (String dn) throws InvalidNameException, JsonProcessingException {
        // --subject-dn="CN=sp, OU=IT, O=SOS GmbH, S=Berlin, L=Berlin, C=DE" --token=12345
        LdapName ldapName = new LdapName(dn);
        List<String> cns = ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("CN")).map(rdn -> rdn.getValue().toString())
                .collect(Collectors.toList());
        List<String> ous = ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("OU")).map(rdn -> rdn.getValue().toString())
                .collect(Collectors.toList());
        CreateCSRFilter filter = new CreateCSRFilter();
        filter.setCommonName(cns.get(0));
        filter.setOrganizationUnit(ous.get(0));
        filter.setOrganization(ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("O")).findFirst().get().getValue().toString());
        filter.setCountryCode(ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("C")).findFirst().get().getValue().toString());
        filter.setLocation(ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("L")).findFirst().get().getValue().toString());
        filter.setState(ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("S")).findFirst().get().getValue().toString());
        filter.setSan(san);
        return mapper.writeValueAsString(filter);
    }
    
    private static String callWebService() throws Exception {
        tryCreateClient();
        client.addHeader("X-Onetime-Token", token);
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Accept", "application/json");
        return client.postRestService(jocUri.resolve(WS_API), createRequestBody(subjectDN));
    }

    private static void printUsage(){
        System.out.println();
        System.out.println("Executes a roll out of ssl certificates on a controller or an agent instance.");
        System.out.println();
        System.out.println(" [ExecuteRollOut] [Options]");
        System.out.println();
        System.out.printf("  %-29s | %s%n", HELP, "Shows this help page, this option is exclusive and has no value");
        System.out.printf("  %-29s | %s%n", TOKEN, "UUID of the token for a onetime authentication to JS7 JOC to receive the generated certificates.");
        System.out.printf("  %-29s | %s%n", SUBJECT_DN, "The SubjectDN to be used consisting of [CN, OU, O, C, L, S] where the current hostname has to be set as CN.");
        System.out.printf("  %-29s | %s%n", SAN, "The subject alternative names(SAN) should be set with variation of the hostname e.g. including the domain part. The alternatives are separated by comma.");
        System.out.printf("  %-29s | %s%n", JOC_URI, "URI of the JS7 JOC to receive the generated certificates from.");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE, "Keystore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE_TYPE, "Type of the keystore to connect to JS7 JOC over https. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE_PASS, "Password for the keystore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE_ENTRY_PASS, "Password for the private key entry of the keystore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_TRUSTSTORE, "Truststore holding the trusted certificates to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_TRUSTSTORE_TYPE, "Type of the truststore to connect to JS7 JOC over https. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", SRC_TRUSTSTORE_PASS, "Password for the truststore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE, "Keystore where the generated SSL certificates and keys should be stored.");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE_TYPE, "Type of the keystore to store to. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE_PASS, "Password for the keystore to store to.");
        System.out.printf("  %-29s | %s%n", TRG_TRUSTSTORE, "Truststore where the trusted ca certificate should be stored.");
        System.out.printf("  %-29s | %s%n", TRG_TRUSTSTORE_TYPE, "Type of the truststore to store to. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", TRG_TRUSTSTORE_PASS, "Password for the truststore to store to.");
        System.out.println();
    }

}
