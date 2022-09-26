package com.sos.auth.openid.classes;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.key.KeyUtil;

public class SOSJWTVerifier {

    private static Algorithm getAlgorithm(String alg, RSAPublicKey rsaPublicKey) throws SOSException {
        Algorithm algorithm = null;
        switch (alg) {
        case "RS256":
            algorithm = Algorithm.RSA256(rsaPublicKey, null);
            break;
        case "RS384":
            algorithm = Algorithm.RSA256(rsaPublicKey, null);
            break;
        case "RS512":
            algorithm = Algorithm.RSA256(rsaPublicKey, null);
            break;
        case "HS256":
            algorithm = Algorithm.RSA256(rsaPublicKey, null);
            break;
        case "HS384":
            algorithm = Algorithm.RSA256(rsaPublicKey, null);
            break;
        case "HS512":
            algorithm = Algorithm.RSA256(rsaPublicKey, null);
            break;
        case "ES256":
            algorithm = Algorithm.RSA256(rsaPublicKey, null);
            break;
        case "ES384":
            algorithm = Algorithm.RSA256(rsaPublicKey, null);
            break;
        case "ES512":
            algorithm = Algorithm.RSA256(rsaPublicKey, null);
            break;
        default:
            throw new SOSException("unknown algorithm " + alg);
        }
        return algorithm;

    }

    public static DecodedJWT verify(SOSOpenIdWebserviceCredentials sosOpenIdWebserviceCredentials, String alg, RSAPublicKey rsaPublicKey)
            throws CertificateException, UnsupportedEncodingException, SOSException {

        try {
            Algorithm algorithm = getAlgorithm(alg, rsaPublicKey);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(sosOpenIdWebserviceCredentials.getAuthenticationUrl()).build();
            DecodedJWT jwt = verifier.verify(sosOpenIdWebserviceCredentials.getIdToken());
            return jwt;

        } catch (JWTVerificationException e) {
            throw new SOSException(e);
        }
    }

}
