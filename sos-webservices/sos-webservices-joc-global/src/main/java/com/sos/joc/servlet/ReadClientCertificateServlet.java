package com.sos.joc.servlet;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReadClientCertificateServlet extends HttpServlet {

    private static final long serialVersionUID = -6942752272134678128L;
    private X509Certificate clientCertificate;
    private X509Certificate[] clientCertificateChain;
    private String subjectDN;
    private String clientCN;
    private String sslSessionIdHex;
    private String jsonResponse200;
    private String jsonResponse420;
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        readClientCertificateInfo(req, resp);
        super.doPost(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        readClientCertificateInfo(req, resp);
        super.doGet(req, resp);
    }


    private void readClientCertificateInfo (HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        clientCertificateChain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        String cipherSuiteName = (String) request.getAttribute("javax.servlet.request.cipher_suite");
        Integer keySize = (Integer) request.getAttribute("javax.servlet.request.key_size");
        sslSessionIdHex = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
//        response.setContentType("application/json");
//        response.setStatus(HttpServletResponse.SC_OK);
        if ((clientCertificateChain == null || clientCertificateChain.length == 0) && sslSessionIdHex == null) {
            StringBuilder jsonStrb = new StringBuilder();
            jsonStrb.append("{\n");
            jsonStrb.append("  \"message\" : \"no SSL information received from request.\"\n");
            jsonStrb.append("}\n");
            jsonResponse420 = jsonStrb.toString();
        } else {
            clientCertificate = clientCertificateChain[0];
            subjectDN = clientCertificate.getSubjectDN().getName();
            clientCN = ((sun.security.x509.X500Name)clientCertificate.getSubjectDN()).getCommonName();
            jsonResponse200 = createResponse200(cipherSuiteName, keySize);
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
        jsonStrb.append(String.format("  \"SSLSessionId\" : \"%1$s\"\n", sslSessionIdHex));
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
    
    public String getJsonResponse200() {
        return jsonResponse200;
    }
    
    public String getJsonResponse420() {
        return jsonResponse420;
    }

}
