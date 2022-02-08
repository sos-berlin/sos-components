package com.sos.auth.client;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientCertificateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCertificateHandler.class);
    private X509Certificate clientCertificate;
    private X509Certificate[] clientCertificateChain;
    private String subjectDN;
    private String clientCN;
    private String sslSessionIdHex;
    
    public ClientCertificateHandler(HttpServletRequest req) throws IOException, CertificateEncodingException, InvalidNameException {
        readClientCertificateInfo(req);
    }
    
    private void readClientCertificateInfo (HttpServletRequest request) throws IOException, CertificateEncodingException, InvalidNameException {
        /*
         * With the SecureRequestCustomizer in place you can access various pieces about the SSL connection from 
         *     HttpServletRequest.getAttribute(String) calls using the following attribute names. 
         *   javax.servlet.request.X509Certificate - an array of java.security.cert.X509Certificate[]
         *   javax.servlet.request.cipher_suite    - the String name of the cipher suite. 
         *                                           (same as what is returned from javax.net.ssl.SSLSession.getCipherSuite())
         *   javax.servlet.request.key_size        - Integer of the key length in use 
         *   javax.servlet.request.ssl_session_id  - String representation (hexified) of the active SSL Session ID
         *      
         */
        String cipherSuiteName = "";
        Integer keySize = 0;
        Enumeration<String> attributes = request.getAttributeNames();
        while(attributes.hasMoreElements()) {
            String attributeName = attributes.nextElement();
            if("javax.servlet.request.X509Certificate".equals(attributeName)) {
                this.clientCertificateChain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
            } else if("javax.servlet.request.cipher_suite".equals(attributeName)) {
                cipherSuiteName = (String) request.getAttribute("javax.servlet.request.cipher_suite");
            } else if("javax.servlet.request.key_size".equals(attributeName)) {
                keySize = (Integer) request.getAttribute("javax.servlet.request.key_size");
            } else if("javax.servlet.request.ssl_session_id".equals(attributeName)) {
                this.sslSessionIdHex = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
            }
        }
        if (clientCertificateChain == null || clientCertificateChain.length == 0) {
            LOGGER.debug("No certificate information received from request.");
        } else {
            this.clientCertificate = clientCertificateChain[0];
            if (clientCertificate != null) {
                this.subjectDN = clientCertificate.getSubjectDN().getName();
                
                // deprecated usage of all sun.* classes in Javas rt.jar
                // the rt.jar is  present in Javas JRE 1.8, not present in Javas JDK 11, 15, 17
//                this.clientCN = ((sun.security.x509.X500Name)clientCertificate.getSubjectDN()).getCommonName();

                // same with bouncy castle
//                X500Name x500Name = new JcaX509CertificateHolder(clientCertificate).getSubject();
//                RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
//                this.clientCN = IETFUtils.valueToString(cn.getFirst().getValue());

                
                // same with LDAP (preferred implementation, no third party jar needed)
                LdapName ldapName = new LdapName(subjectDN);
                ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("CN")).findFirst().ifPresent(o -> this.clientCN = o.getValue().toString());

                LOGGER.debug("Client SubjectDN read from request: " + subjectDN);
                LOGGER.debug("Client CN read from request for comparison with shiro account: " + clientCN);
            }
            LOGGER.debug("Certificate infos as JSON: \n" + createResponse200(cipherSuiteName, keySize));
        }
    }
    
    private String createResponse200 (String cipherSuiteName, Integer keySize) {
        StringBuilder jsonStrb = new StringBuilder();
        jsonStrb.append("{\n");
        jsonStrb.append(String.format("  \"IssuerDN\" : \"%1$s\",\n", clientCertificate.getIssuerDN().getName()));
        jsonStrb.append(String.format("  \"SubjectDN\" : \"%1$s\",\n", subjectDN));
        /*        
         *      KeyUsage ::= BIT STRING {
         *          digitalSignature        (0),
         *          nonRepudiation          (1),
         *          keyEncipherment         (2),
         *          dataEncipherment        (3),
         *          keyAgreement            (4),
         *          keyCertSign             (5),
         *          cRLSign                 (6),
         *          encipherOnly            (7),
         *          decipherOnly            (8)
         *      }
         */
        boolean[] keyUsages = clientCertificate.getKeyUsage();
        jsonStrb.append("  \"keyUsages\" : [\n");
        jsonStrb.append(String.format("      {\"digitalSignature\" : \"%1$s\"},\n", keyUsages[0]));
        jsonStrb.append(String.format("      {\"nonRepudiation\" : \"%1$s\"},\n", keyUsages[1]));
        jsonStrb.append(String.format("      {\"keyEncipherment\" : \"%1$s\"},\n", keyUsages[2]));
        jsonStrb.append(String.format("      {\"dataEncipherment\" : \"%1$s\"},\n", keyUsages[3]));
        jsonStrb.append(String.format("      {\"keyAgreement\" : \"%1$s\"},\n", keyUsages[4]));
        jsonStrb.append(String.format("      {\"keyCertSign\" : \"%1$s\"},\n", keyUsages[5]));
        jsonStrb.append(String.format("      {\"cRLSign\" : \"%1$s\"},\n", keyUsages[6]));
        jsonStrb.append(String.format("      {\"encipherOnly\" : \"%1$s\"},\n", keyUsages[7]));
        jsonStrb.append(String.format("      {\"decipherOnly\" : \"%1$s\"}\n", keyUsages[8]));
        jsonStrb.append("  ],\n");
        jsonStrb.append(String.format("  \"cipherSuiteName\" : \"%1$s\",\n", cipherSuiteName));
        jsonStrb.append(String.format("  \"keySize\" : \"%1$d\",\n", keySize));
        if (sslSessionIdHex != null) {
            jsonStrb.append(String.format("  \"SSLSessionId\" : \"%1$s\"\n", sslSessionIdHex));
        }
        jsonStrb.append(String.format("  \"CN\" : \"%1$s\"\n", clientCN));
        jsonStrb.append("}");
        return jsonStrb.toString();
    }
        
    public X509Certificate getClientCertificate() {
        return clientCertificate;
    }
    
    public X509Certificate[] getClientCertificateChain() {
        return clientCertificateChain;
    }
    
    public String getSubjectDN() {
        return subjectDN;
    }
    
    public String getClientCN() {
        return clientCN;
    }
    
}
