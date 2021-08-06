package com.sos.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.keyStore.KeyStoreType;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.publish.RolloutResponse;

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
    private static final String TRG_KEYSTORE_ENTRY_PASS = "--target-keystore-entry-pass";
    private static final String TRG_TRUSTSTORE = "--target-truststore";
    private static final String TRG_TRUSTSTORE_TYPE = "--target-truststore-type";
    private static final String TRG_TRUSTSTORE_PASS = "--target-truststore-pass";
    private static final String SUBJECT_DN = "--subject-dn";
    private static final String KS_ALIAS = "--key-alias";
    private static final String TS_ALIAS = "--ca-alias";
    private static final String SRC_PRIVATE_KEY = "--source-private-key";
    private static final String SRC_CERT = "--source-certificate";
    private static final String SRC_CA_CERT = "--source-ca-cert";
    private static SOSRestApiClient client;
    private static String token;
    private static String subjectDN;
    private static String san;
    private static URI jocUri;
    private static String srcKeystore;
    private static String srcKeystoreType = "PKCS12";
    private static String srcKeystorePasswd;
    private static String srcKeystoreEntryPasswd;
    private static String srcTruststore;
    private static String srcTruststoreType = "PKCS12";
    private static String srcTruststorePasswd;
    private static String targetKeystore;
    private static String targetKeystoreType = "PKCS12";
    private static String targetKeystorePasswd;
    private static String targetKeystoreEntryPasswd;
    private static String targetTruststore;
    private static String targetTruststoreType = "PKCS12";
    private static String targetTruststorePasswd;
    private static String keyAlias;
    private static String caAlias;
    private static String srcPrivateKeyPath;
    private static String srcCertPath;
    private static String srcCaCertPath;
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
                } else if (args[i].startsWith(TRG_KEYSTORE_ENTRY_PASS + "=")) {
                    targetKeystoreEntryPasswd = split[1];
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
                } else if (args[i].startsWith(KS_ALIAS + "=")) {
                    keyAlias = split[1];
                } else if (args[i].startsWith(TS_ALIAS + "=")) {
                    caAlias = split[1];
                } else if (args[i].startsWith(SRC_PRIVATE_KEY + "=")) {
                    srcPrivateKeyPath = split[1];
                } else if (args[i].startsWith(SRC_CERT + "=")) {
                    srcCertPath = split[1];
                } else if (args[i].startsWith(SRC_CA_CERT + "=")) {
                    srcCaCertPath = split[1];
                }
            }
            String response = callWebService();
            closeClient();
            RolloutResponse rollout = mapper.readValue(response, RolloutResponse.class);
            addKeyAndCertToStore(rollout);
            
        }
    }
    
    private static void addKeyAndCertToStore(RolloutResponse rolloutResponse) throws Exception {
        KeyStore targetKeyStore = null;
        KeyStore targetTrustStore = null;
        try {
            X509Certificate certificate = KeyUtil.getX509Certificate(rolloutResponse.getJocKeyPair().getCertificate());
            PrivateKey privKey = KeyUtil.getPrivateECDSAKeyFromString(rolloutResponse.getJocKeyPair().getPrivateKey());
            X509Certificate rootCaCertificate = KeyUtil.getX509Certificate(rolloutResponse.getCaCert());
            Certificate[] chain = new Certificate[] {certificate, rootCaCertificate}; 
            if (targetKeystore != null && !targetKeystore.isEmpty()) {
                targetKeyStore = KeyStoreUtil.readKeyStore(targetKeystore, KeyStoreType.fromValue(targetKeystoreType), targetKeystorePasswd);
                if(keyAlias != null && !keyAlias.isEmpty()) {
                    targetKeyStore.setKeyEntry(keyAlias, privKey, targetKeystoreEntryPasswd.toCharArray(), chain);
                } else {
                    System.err.println("no alias provided for the certificate and its private key. Parameter --key-alias is required.");
                }
                if (caAlias != null && !caAlias.isEmpty()) {
                    targetKeyStore.setCertificateEntry(caAlias, rootCaCertificate);
                } else {
                    System.err.println("no alias provided for the CA certificate. Parameter --ca-alias is required.");
                }
                targetKeyStore.store(new FileOutputStream(new File(targetKeystore)), targetKeystorePasswd.toCharArray());

            }
            if (targetTruststore != null && !targetTruststore.isEmpty()) {
                targetTrustStore = KeyStoreUtil.readTrustStore(targetTruststore, KeyStoreType.fromValue(targetTruststoreType), targetTruststorePasswd);
                if (caAlias != null && !caAlias.isEmpty()) {
                    targetTrustStore.setCertificateEntry(caAlias, rootCaCertificate);
                } else {
                    System.err.println("no alias provided for the CA certificate. Parameter --ca-alias is required.");
                }
                targetTrustStore.store(new FileOutputStream(new File(targetTruststore)), targetTruststorePasswd.toCharArray());
            }
        } catch (CertificateException | IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println(e.toString());
        }
        
    }
    
    private static void tryCreateClient() throws Exception {
        if (client != null) {
            return;
        }
        client = new SOSRestApiClient();
        KeyStore srcKeyStore = null;
        KeyStore srcTrustStore = null;
        if (srcKeystore != null && !srcKeystore.isEmpty() && srcTruststore != null && !srcTruststore.isEmpty()) {
            srcKeyStore = KeyStoreUtil.readKeyStore(srcKeystore, KeyStoreType.fromValue(srcKeystoreType), srcKeystorePasswd);
            srcTrustStore = KeyStoreUtil.readTrustStore(srcTruststore, KeyStoreType.fromValue(srcTruststoreType), srcTruststorePasswd);
        } else if (srcPrivateKeyPath != null && !srcPrivateKeyPath.isEmpty()
                && srcCertPath != null && !srcCertPath.isEmpty()
                && srcCaCertPath != null && !srcCaCertPath.isEmpty()) {
            PrivateKey privKey = null;
            X509Certificate cert = null;
            X509Certificate caCert = null;
            String pk = new String (Files.readAllBytes(Paths.get(srcPrivateKeyPath)), StandardCharsets.UTF_8);
            if (pk != null && !pk.isEmpty()) {
                if (pk.contains(SOSKeyConstants.RSA_ALGORITHM_NAME)) {
                    privKey = KeyUtil.getPrivateRSAKeyFromString(pk);
                } else {
                    privKey = KeyUtil.getPrivateECDSAKeyFromString(pk);
                }
            }
            cert = (X509Certificate)KeyUtil.getCertificate(Paths.get(srcCertPath));
            Certificate[] chain = null;
            Certificate[] caChain = null;
            if (srcCaCertPath.contains(",")) {
                String[] caCertPaths = srcCaCertPath.split(",");
                caChain = new Certificate [caCertPaths.length];
                chain = new Certificate[caChain.length + 1];
                chain[0] = cert;
                for (int i=0; i < caCertPaths.length; i++) {
                    X509Certificate caCertficate = (X509Certificate)KeyUtil.getCertificate(Paths.get(caCertPaths[i].trim()));
                    caChain[i] = caCertficate;
                    chain[i+1] = caCertficate;
                }
            } else {
                caCert = (X509Certificate)KeyUtil.getCertificate(srcCaCertPath);
                chain = new Certificate[] {cert, caCert};
            }
            srcKeyStore = KeyStore.getInstance("PKCS12");
            srcKeyStore.load(null, null);
            srcKeyStore.setKeyEntry(keyAlias, privKey, "".toCharArray(), chain);
            srcTrustStore = KeyStore.getInstance("PKCS12");
            srcTrustStore.load(null, null);
            if (caChain.length != 0) {
                for (int i=0; i < caChain.length; i++) {
                    srcTrustStore.setCertificateEntry(caAlias + (i+1), caChain[i]);
                }
            } else {
                srcTrustStore.setCertificateEntry(caAlias, caCert);
            }
        }
        if (srcKeyStore != null && srcTrustStore != null) {
            if (srcKeystoreEntryPasswd != null) {
                client.setSSLContext(srcKeyStore, srcKeystoreEntryPasswd.toCharArray(), srcTrustStore);
            } else {
                client.setSSLContext(srcKeyStore, "".toCharArray(), srcTrustStore);
            }
        }
    }

    private static void closeClient() {
        if (client != null) {
            client.closeHttpClient();
        }
    }
    
    private static String createRequestBody (String dn) throws InvalidNameException, JsonProcessingException {
        // --subject-dn="CN=sp, OU=IT, O=SOS GmbH, S=Berlin, L=Berlin, C=DE"
        LdapName ldapName = new LdapName(dn);
        CreateCSRFilter filter = new CreateCSRFilter();
        List<String> cns = null;
        List<String> ous = null;
        if (dn.contains("CN=")) {
            cns = ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("CN")).map(rdn -> rdn.getValue().toString())
                .collect(Collectors.toList());
            filter.setCommonName(cns.get(0));
        }
        if (dn.contains("OU=")) {
            ous = ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("OU")).map(rdn -> rdn.getValue().toString())
                .collect(Collectors.toList());
            filter.setOrganizationUnit(ous.get(0));
        }
        if (dn.contains("O=")) {
            filter.setOrganization(ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("O")).findFirst().get().getValue().toString());
        }
        if (dn.contains("C=")) {
            filter.setCountryCode(ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("C")).findFirst().get().getValue().toString());
        }
        if (dn.contains("L=")) {
            filter.setLocation(ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("L")).findFirst().get().getValue().toString());
        }
        if (dn.contains("S=")) {
            filter.setState(ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("S")).findFirst().get().getValue().toString());
        }
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
        System.out.printf("  %-29s | %s%n", TOKEN + "=", "UUID of the token for a onetime authentication to JS7 JOC to receive the generated certificates.");
        System.out.printf("  %-29s | %s%n", SUBJECT_DN + "=", "The SubjectDN to be used consisting of [CN, OU, O, C, L, S] where the current hostname has to be set as CN.");
        System.out.printf("  %-29s | %s%n", SAN + "=", "The subject alternative names(SAN) should be set with variation of the hostname e.g. including the domain part. The alternatives are separated by comma.");
        System.out.printf("  %-29s | %s%n", JOC_URI + "=", "URI of the JS7 JOC to receive the generated certificates from.");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE + "=", "Path to the Keystore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE_TYPE + "=", "Type of the keystore to connect to JS7 JOC over https. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE_PASS + "=", "Password for the keystore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE_ENTRY_PASS + "=", "Password for the private key entry of the keystore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_TRUSTSTORE + "=", "Path to the Truststore holding the trusted certificates to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_TRUSTSTORE_TYPE + "=", "Type of the truststore to connect to JS7 JOC over https. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", SRC_TRUSTSTORE_PASS + "=", "Password for the truststore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_PRIVATE_KEY + "=", "Path to the private Key file used to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_CERT + "=", "Path to the certificate file used to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_CA_CERT + "=", "Path to the CA certificate file(s) used to connect to JS7 JOC over https. (Comma separated)");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE + "=", "Path to the Keystore where the generated SSL certificates and keys should be stored.");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE_TYPE + "=", "Type of the keystore to store to. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE_PASS + "=", "Password for the keystore to store to.");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE_ENTRY_PASS + "=", "Password for the private key/certificate entry of the keystore holding the keys for mutual https.");
        System.out.printf("  %-29s | %s%n", TRG_TRUSTSTORE + "=", "Path to the Truststore where the trusted ca certificate should be stored.");
        System.out.printf("  %-29s | %s%n", TRG_TRUSTSTORE_TYPE + "=", "Type of the truststore to store to. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", TRG_TRUSTSTORE_PASS + "=", "Password for the truststore to store to.");
        System.out.printf("  %-29s | %s%n", KS_ALIAS + "=", "Alias used to store the certificate and its private key in the target keystore.");
        System.out.printf("  %-29s | %s%n", TS_ALIAS + "=", "Alias used to store the ca certificate in both, the target keystore and truststore.");
        System.out.println();
    }

}
