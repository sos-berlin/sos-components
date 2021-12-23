package com.sos.auth.classes;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.sos.joc.Globals;

public class SOSAuthHelper {
    
    public static final String EMERGENCY_ROLE = "all";
    public static final String EMERGENCY_PERMISSION = "sos:products";
    public static final String EMERGENY_KEY = "sos_emergency_key";
    public static final String CONFIGURATION_TYPE_IAM = "IAM";
    public static final String OBJECT_TYPE_IAM_GENERAL = "GENERAL";

    public static String getSHA512(String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (pwd.length() == 128){
            return pwd;
        }
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        // random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        String hashedPwd = String.format("%0128x", new BigInteger(1, hash));
        return hashedPwd;
    }

    public static String createAccessToken() {

        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();

        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String s = base64Encoder.encodeToString(randomBytes);
        s = s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16);
        return s;
    }

    public static String getIdentityServiceAccessToken(String accessToken) {
        if (Globals.jocWebserviceDataContainer != null && Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null) {
            SOSAuthCurrentAccount sosAuthCurrentAccount = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccount(accessToken);
            if (sosAuthCurrentAccount != null && sosAuthCurrentAccount.getIdentityServices() != null) {
                return sosAuthCurrentAccount.getAccessToken(sosAuthCurrentAccount.getIdentityServices().getIdentityServiceName());
            }
        }
        return null;
    }

    public static boolean emergencyKeyExist() {
        if (Globals.sosCockpitProperties != null) {
            Path p = Globals.sosCockpitProperties.resolvePath(EMERGENY_KEY);
            File f = new File(p.toString().replace('\\', '/'));
            return f.exists();
        }
        return false;
    }
    
    public static void removeEmergencyKey(){
        if (Globals.sosCockpitProperties != null) {
            Path p = Globals.sosCockpitProperties.resolvePath(EMERGENY_KEY);
            File f = new File(p.toString().replace('\\', '/'));
            if (f.exists()){
                f.delete();
            }
        }
    }

}
