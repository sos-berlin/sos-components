package com.sos.azure;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;

public class AzureSignature {

    public static String getHMAC256(String key, String data) {
        String hash = null;
        Mac sha256_HMAC;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            Encoder encoder = Base64.getEncoder();
            hash = new String(encoder.encode(sha256_HMAC.doFinal(data.getBytes("UTF-8"))));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return hash;
    }

   
    public static String createSignature(String operation, String container, String blob, String account, String accessKey, String dateString, String version,
            Integer contentLength) throws Exception {

        String contentLengthValue = "";
        if (contentLength != -1) {
            contentLengthValue = String.valueOf(contentLength);
        }

       

        String partToSign = "\n" + "\n" + "\n" + contentLengthValue + "\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n"
                + "x-ms-blob-type:BlockBlob" + "\n" + "x-ms-date:" + dateString + "\n" + "x-ms-version:" + version + "\n";

        String stringToSign = "";

        switch (operation) {
        case "GET": {
            stringToSign = "GET" + partToSign + "/" + account + "/" + container + "/" + blob;
            break;
        }

        case "PUT": {
            stringToSign = "PUT" + partToSign + "/" + account + "/" + container + "/" + blob;
            break;
        }

        case "LIST": {
            stringToSign = "GET" + partToSign + "/" + account + "/" + container + "\n" + "comp:list" + "\n" + "restype:container";
            break;
        }

        default: {
            throw new Exception("invalid operation: " + operation);
        }
        }

        return getHMAC256(accessKey, stringToSign);
    }

    public static String invokeBlobRequest(URI uri, String account, String signature, String dateString, String version, String method, String filePath)
            throws Exception {
 

        SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
        try {

            KeyStore truststore = KeyStoreUtil.readTrustStore("C:/Program Files/Java/jdk-11.0.16/lib/security/cacerts", KeystoreType.valueOf("JKS"),
                    "changeit");

            sosRestApiClient.setSSLContext(null, null, truststore);
            sosRestApiClient.addHeader("x-ms-blob-type", "BlockBlob");
            sosRestApiClient.addHeader("x-ms-date", dateString);
            sosRestApiClient.addHeader("x-ms-version", version);
            sosRestApiClient.addHeader("Authorization", "SharedKey " + account + ":" + signature);

            return sosRestApiClient.executeRestServiceCommand("GET", uri);
        } catch (SocketException | SOSException e) {
            e.printStackTrace();
        }
        return "";
    }

}
