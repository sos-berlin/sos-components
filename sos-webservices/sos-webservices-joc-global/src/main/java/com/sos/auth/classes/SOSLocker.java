package com.sos.auth.classes;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.sos.joc.Globals;

public class SOSLocker {

    private static final String KEY = "APLSOSSECRET";
    private Map<String, SOSLockerContent> locker;

    private String encrypt(String keyStr, Object value) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {

        String valueString = (String) value;

        keyStr = keyStr + KEY;
        byte[] key = (keyStr).getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encrypted = cipher.doFinal(valueString.getBytes());

        String encodedValue = Base64.getEncoder().encodeToString(encrypted);
        return encodedValue;
    }

    private String decrypt(String keyStr, Object encodedValue) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        keyStr = keyStr + KEY;
        byte[] key = (keyStr).getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        byte[] decodedBytes = Base64.getDecoder().decode((String) encodedValue);

        Cipher cipher2 = Cipher.getInstance("AES");
        cipher2.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] cipherData2 = cipher2.doFinal(decodedBytes);
        return new String(cipherData2);
    }

    public String addContent(Map<String, Object> content) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        String key = SOSAuthHelper.createAccessToken();
        if (locker == null) {
            locker = new HashMap<String, SOSLockerContent>();
        }
        for (Entry<String, Object> entry : content.entrySet()) {
            content.put(entry.getKey(), encrypt(key, entry.getValue()));
        }
        SOSLockerContent sosLockerContent = new SOSLockerContent();
        sosLockerContent.setContent(content);
        sosLockerContent.setCreated(Instant.now().toEpochMilli());
        locker.put(key, sosLockerContent);
        return key;
    }

    public Map<String, Object> getContent(String key) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (locker == null) {
            locker = new HashMap<String, SOSLockerContent>();
        }
        Map<String, Object> content = null;
        if (locker.get(key) != null) {
            content = locker.get(key).getContent();
        }
        if (content != null) {
            for (Entry<String, Object> entry : content.entrySet()) {
                content.put(entry.getKey(), decrypt(key, entry.getValue()));
            }
        }
        return content;
    }

    public void removeContent(String key) {
        if (locker == null) {
            locker = new HashMap<String, SOSLockerContent>();
        }
        locker.remove(key);
    }

    public void renewContent(String key) {
        if (locker == null) {
            locker = new HashMap<String, SOSLockerContent>();
        }
        SOSLockerContent sosLockerContent = locker.get(key);
        if (sosLockerContent != null) {
            sosLockerContent.setCreated(Instant.now().toEpochMilli());
            locker.put(key, sosLockerContent);
        }
    }

    public Entry<String, SOSLockerContent> getEldestContent() {
        Entry<String, SOSLockerContent> eldest = null;
        Long created = Instant.now().toEpochMilli();
        if (locker == null) {
            locker = new HashMap<String, SOSLockerContent>();
        }
        for (Entry<String, SOSLockerContent> entry : locker.entrySet()) {
            if (entry.getValue().getCreated() < created) {
                eldest = entry;
                created = entry.getValue().getCreated();
            }
        }
        return eldest;
    }

    public boolean isEmpty(String key) {
        if (locker == null) {
            locker = new HashMap<String, SOSLockerContent>();
        }
        Map<String, Object> content = null;
        if (locker.get(key) != null) {
            content = locker.get(key).getContent();
        }

        return (content == null);
    }

    public static void main(String[] args) throws Exception {
        SOSLocker sosLocker = new SOSLocker();
        Map<String, Object> content = new HashMap<String, Object>();
        content.put("a", "1");
        content.put("b", "2");
        String key = sosLocker.addContent(content);
        content = null;
        Map<String, Object> x = sosLocker.getContent(key);
        for (Entry<String, Object> entry : x.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
        sosLocker.removeContent(key);
        sosLocker.removeContent(key);

        x = sosLocker.getContent(key);
        if (x != null) {
            for (Entry<String, Object> entry : x.entrySet()) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }
        }
    }

    public long getCount() {
        if (locker == null) {
            locker = new HashMap<String, SOSLockerContent>();
        }
        return locker.size();
    }
}
