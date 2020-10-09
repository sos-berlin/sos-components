package com.sos.commons.sign.pgp;


public class SOSPGPConstants {
    public static final String PRIVKEY_KEY = "PRIV";
    public static final String PUBKEY_KEY = "PUB";
    public static final String DEFAULT_ALGORYTHM = "SHA256WithRSA";
    public static final String DEFAULT_ALGORYTHM_NAME = "RSA";
    public static final String PGP_ALGORYTHM_NAME = "PGP";
    public static final String ECDSA_ALGORYTHM = "SHA512withECDSA";
    public static final String ECDSA_ALGORYTHM_NAME = "ECDSA";
    public static final String EC_ALGORYTHM_NAME = "EC";
    public static final int DEFAULT_ALGORYTHM_BIT_LENGTH = 4096;
    public static final int DEFAULT_ECDSA_ALGORYTHM_BIT_LENGTH = 256;
    public static final String PRIVATE_KEY_HEADER     = "-----BEGIN PRIVATE KEY-----";
    public static final String PRIVATE_KEY_FOOTER     = "-----END PRIVATE KEY-----";
    public static final String PRIVATE_RSA_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
    public static final String PRIVATE_RSA_KEY_FOOTER = "-----END RSA PRIVATE KEY-----";
    public static final String PRIVATE_EC_KEY_HEADER  = "-----BEGIN EC PRIVATE KEY-----";
    public static final String PRIVATE_EC_KEY_FOOTER  = "-----END EC PRIVATE KEY-----";
    public static final String PRIVATE_ECDSA_KEY_HEADER  = "-----BEGIN ECDSA PRIVATE KEY-----";
    public static final String PRIVATE_ECDSA_KEY_FOOTER  = "-----END ECDSA PRIVATE KEY-----";
    public static final String PRIVATE_PGP_KEY_HEADER = "-----BEGIN PGP PRIVATE KEY BLOCK-----";
    public static final String PRIVATE_PGP_KEY_FOOTER = "-----END PGP PRIVATE KEY BLOCK-----";
    public static final String PUBLIC_KEY_HEADER      = "-----BEGIN PUBLIC KEY-----";
    public static final String PUBLIC_KEY_FOOTER      = "-----END PUBLIC KEY-----";
    public static final String PUBLIC_RSA_KEY_HEADER  = "-----BEGIN RSA PUBLIC KEY-----";
    public static final String PUBLIC_RSA_KEY_FOOTER  = "-----END RSA PUBLIC KEY-----";
    public static final String PUBLIC_EC_KEY_HEADER   = "-----BEGIN EC PUBLIC KEY-----";
    public static final String PUBLIC_EC_KEY_FOOTER   = "-----END EC PUBLIC KEY-----";
    public static final String PUBLIC_ECDSA_KEY_HEADER   = "-----BEGIN ECDSA PUBLIC KEY-----";
    public static final String PUBLIC_ECDSA_KEY_FOOTER   = "-----END ECDSA PUBLIC KEY-----";
    public static final String PUBLIC_PGP_KEY_HEADER  = "-----BEGIN PGP PUBLIC KEY BLOCK-----";
    public static final String PUBLIC_PGP_KEY_FOOTER  = "-----END PGP PUBLIC KEY BLOCK-----";
    public static final String CERTIFICATE_HEADER     = "-----BEGIN CERTIFICATE-----";
    public static final String CERTIFICATE_FOOTER     = "-----END CERTIFICATE-----";
    public static final String SIGNATURE_HEADER       = "-----BEGIN SIGNATURE-----";
    public static final String SIGNATURE_FOOTER       = "-----END SIGNATURE-----";
    public static final String PGP_SIGNATURE_HEADER   = "-----BEGIN PGP SIGNATURE-----";
    public static final String PGP_SIGNATURE_FOOTER   = "-----END PGP SIGNATURE-----";
    public static final String X509_SIGNATURE_HEADER  = "-----BEGIN X.509 SIGNATURE-----";
    public static final String X509_SIGNATURE_FOOTER  = "-----END X.509 SIGNATURE-----";
}
