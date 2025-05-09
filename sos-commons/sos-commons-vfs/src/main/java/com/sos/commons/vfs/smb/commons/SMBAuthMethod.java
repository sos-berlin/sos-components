package com.sos.commons.vfs.smb.commons;

public enum SMBAuthMethod {

    /** <br/>
     * - ANONYMOUS: Used for situations where no authentication is required, such as accessing public SMB shares or other unauthenticated services.<br/>
     * - GUEST: Represents unauthenticated access where no credentials are required. The username is set to "Guest", and the password is not needed (null).<br/>
     * - NTLM: NT LAN Manager, an older authentication protocol from Microsoft, typically used when Kerberos is unavailable.<br/>
     * -- Simple, no special configuration required. Less secure, no SSO(Single Sign-On). Recommended For: Standalone servers, workgroups */
    ANONYMOUS(null, null, null), GUEST(null, null, null), NTLM(null, null, null)

    /** GSSAPI(Generic Security Service Application Program Interface) mechanisms:<br/>
     * - KERBEROS<br/>
     * - SPNEGO */

    /** Kerberos: The standard authentication protocol used in many enterprise environments like Active Directory. */
    /** Secure, SSO, no passwords transmitted over the network. Works only in Windows domains. Recommended For: Windows domains (Active Directory)<br/>
     * 
     * OID: "1.2.840.113554.1.2.2" – This is the standard identifier for Kerberos GSSAPI and must remain fixed.<br/>
     * LoginContextName: Configurable, allows referencing a custom JAAS login entry (default is "KerberosLogin").<br/>
     */
    ,KERBEROS("Kerberos", "1.2.840.113554.1.2.2", "KerberosLogin")

    /** SPNEGO: The Simple and Protected GSSAPI Negotiation Mechanism, used to negotiate between different authentication mechanisms (e.g., Kerberos, NTLM).<br>
     * 
     * OID: "1.3.6.1.5.5.2" – This is the standard identifier for SPNEGO and must remain fixed.<br/>
     * LoginContextName: Configurable, allows referencing a custom JAAS login entry (default is "SPNEGOLogin"). */
    ,SPNEGO("SPNEGO", "1.3.6.1.5.5.2", "SPNEGOLogin");

    /** DIGEST_MD5, LDAP, OAUTH, SCRAM, and X509 are not GSSAPI mechanisms in the classic sense.<br/>
     * While some of these mechanisms could potentially use a GSSAPI mechanism (e.g., LDAP over Kerberos),<br/>
     * they are not directly supported by the {@link AuthenticationContextFactory#createGSSAPIContext} (smbj based) method.<br/>
     * Note: X509 - it may be possible to use X509 in specific scenarios with GSSAPI, but it is not directly supported in all cases... */
    // DIGEST-MD5: A challenge-response authentication mechanism using MD5 hashing, often used in protocols like IMAP or HTTP.
    // , DIGEST_MD5("DIGEST-MD5", "1.2.840.113549.2.5", "DigestMD5Login")
    // LDAP: Lightweight Directory Access Protocol authentication mechanism, commonly used in environments that rely on LDAP directories for user information.
    // , LDAP("LDAP", "1.3.6.1.4.1.4203.1.5.1", "LDAPLogin")
    // X.509: Certificate-based authentication mechanism using X.509 certificates, widely used in secure communication protocols like TLS/SSL.
    // , X509("X.509", "1.2.840.113549.1.9.1", "X509Login")
    // OAuth: An open-standard protocol used for token-based authorization. Commonly used in APIs and web services for secure delegated access.
    // , OAUTH("OAuth", "1.3.6.1.4.1.11591.4.1", "OAuthLogin")
    // SCRAM: A secure authentication mechanism using a salted challenge-response model, commonly used for database and XMPP authentication.
    // , SCRAM("SCRAM-SHA-256", "1.3.6.1.4.1.11591.4.2", "SCRAMLogin");

    private final String name;
    private final String oid;
    private final String loginContextName;

    SMBAuthMethod(String name, String oid, String loginContextName) {
        this.name = name;
        this.oid = oid;
        this.loginContextName = loginContextName;
    }

    public String getName() {
        return name;
    }

    public String getOid() {
        return oid;
    }

    public String getLoginContextName() {
        return loginContextName;
    }

    @Override
    public String toString() {
        return name + (oid == null ? "" : " (" + oid + ")");
    }
}