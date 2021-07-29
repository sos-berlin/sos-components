package com.sos.js7.joc.poc.jetty.servlets;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadCertificateTestServlet extends HttpServlet {

    private static final long serialVersionUID = -6942752272134678128L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadCertificateTestServlet.class);
    private X509Certificate clientCertificate;
    private X509Certificate[] clientCertificateChain;
    private String subjectDN;
    private String clientCN;
    private String sslSessionIdHex;
    private String jsonResponse;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * With the SecureRequestCustomizer in place you can access various pieces about the SSL connection from 
         *     HttpServletRequest.getAttribute(String)
         * calls using the following attribute names. 
         *   javax.servlet.request.X509Certificate
         *      an array of java.security.cert.X509Certificate[]
         *   javax.servlet.request.cipher_suite 
         *      the String name of the cipher suite. (same as what is returned from javax.net.ssl.SSLSession.getCipherSuite())
         *   javax.servlet.request.key_size
         *      Integer of the key length in use 
         *   javax.servlet.request.ssl_session_id
         *      String representation (hexified) of the active SSL Session ID
         *      
         */
//        Enumeration<String> reqAttributeNames = request.getAttributeNames();
//        Set<String> attributeNames = new HashSet<String>();
//        while(reqAttributeNames.hasMoreElements()) {
//            attributeNames.add(reqAttributeNames.nextElement());
//        }
//        attributeNames.stream().forEach(item -> LOGGER.info("Attribute Name: " + item));
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
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        StringBuilder strb = new StringBuilder();
        
        if ((clientCertificateChain == null || clientCertificateChain.length == 0) && sslSessionIdHex == null) {
            strb.append("{\n");
            strb.append("  \"message\" : \"no information received from request.\"\n");
            strb.append("}\n");
        } else {
            clientCertificate = clientCertificateChain[0];
            if (clientCertificate != null) {
                subjectDN = clientCertificate.getSubjectDN().getName();
//                clientCN = ((sun.security.x509.X500Name)clientCertificate.getSubjectDN()).getCommonName();
                try {
                    LdapName ldapName = new LdapName(clientCertificate.getSubjectDN().getName().toString());
                    clientCN = ldapName.getRdns().stream().filter(item -> item.getType().equalsIgnoreCase("CN")).findFirst().get().getValue().toString();
                } catch (InvalidNameException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                strb.append("{\n");
                strb.append(String.format("  \"CN\" : \"%1$s\",\n", clientCN));
                strb.append(String.format("  \"IssuerDN\" : \"%1$s\",\n", clientCertificate.getIssuerDN().getName()));
                strb.append(String.format("  \"SubjectDN\" : \"%1$s\",\n", subjectDN));
                boolean[] keyUsages = clientCertificate.getKeyUsage();
        /*        
                KeyUsage ::= BIT STRING {
                    digitalSignature        (0),
                    nonRepudiation          (1),
                    keyEncipherment         (2),
                    dataEncipherment        (3),
                    keyAgreement            (4),
                    keyCertSign             (5),
                    cRLSign                 (6),
                    encipherOnly            (7),
                    decipherOnly            (8)
                    }
        */
                strb.append("  \"keyUsages\" : [\n");
                strb.append(String.format("      {\"digitalSignature\" : \"%1$s\"},\n", keyUsages[0]));
                strb.append(String.format("      {\"nonRepudiation\" : \"%1$s\"},\n", keyUsages[1]));
                strb.append(String.format("      {\"keyEncipherment\" : \"%1$s\"},\n", keyUsages[2]));
                strb.append(String.format("      {\"dataEncipherment\" : \"%1$s\"},\n", keyUsages[3]));
                strb.append(String.format("      {\"keyAgreement\" : \"%1$s\"},\n", keyUsages[4]));
                strb.append(String.format("      {\"keyCertSign\" : \"%1$s\"},\n", keyUsages[5]));
                strb.append(String.format("      {\"cRLSign\" : \"%1$s\"},\n", keyUsages[6]));
                strb.append(String.format("      {\"encipherOnly\" : \"%1$s\"},\n", keyUsages[7]));
                strb.append(String.format("      {\"decipherOnly\" : \"%1$s\"}\n", keyUsages[8]));
                strb.append("  ],\n");
                strb.append(String.format("  \"cipherSuiteName\" : \"%1$s\",\n", cipherSuiteName));
                strb.append(String.format("  \"keySize\" : \"%1$d\",\n", keySize));
                strb.append(String.format("  \"sslSessionId\" : \"%1$s\"\n", sslSessionIdHex));
                strb.append("}");
            }
        }
        response.getWriter().println(strb.toString());

    }

}
