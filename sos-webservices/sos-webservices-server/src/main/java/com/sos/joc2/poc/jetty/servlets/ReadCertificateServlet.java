package com.sos.joc2.poc.jetty.servlets;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReadCertificateServlet extends HttpServlet {

    private static final long serialVersionUID = -6942752272134678128L;

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
        Enumeration<String> reqAttributeNames = request.getAttributeNames();
        Set<String> attributeNames = new HashSet<String>();
        while(reqAttributeNames.hasMoreElements()) {
            attributeNames.add(reqAttributeNames.nextElement());
        }
        X509Certificate[] certificates = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        String cipherSuiteName = (String) request.getAttribute("javax.servlet.request.cipher_suite");
        Integer keySize = (Integer) request.getAttribute("javax.servlet.request.key_size");
        String sessionId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        StringBuilder strb = new StringBuilder();
        
        if ((certificates == null || certificates.length == 0) && cipherSuiteName == null && keySize == null && sessionId == null) {
            strb.append("{\n");
            strb.append("  \"message\" : \"no information received from request.\"\n");
            strb.append("}\n");
        } else {
            strb.append("{\n");
            strb.append(String.format("  \"IssuerDN\" : \"%1$s\",\n",certificates[0].getIssuerDN().getName()));
            strb.append(String.format("  \"SubjectDN\" : \"%1$s\",\n",certificates[0].getSubjectDN().getName()));
            boolean[] keyUsages = certificates[0].getKeyUsage();
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
            strb.append(String.format("  \"sessionId\" : \"%1$s\"\n", sessionId));
            strb.append("}");
        }
        response.getWriter().println(strb.toString());

    }

}
