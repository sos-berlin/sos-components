package com.sos.commons.hasher;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class StringHasher {

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            printUsage();
            System.exit(0);
            return;
        } else if (args != null && args.length > 0) {
            String value = String.join(" ", args);
            System.out.println();
            System.out.println("sha512:" + hash(value));
            System.out.println();            
        }
    }

    private static String hash(String val) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(val.getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    private static void printUsage(){
        System.out.println();
        System.out.println("Creates a SHA-512 hash of a string with a prefix \"sha512:\". Strings with leading spaces have to be quoted.");
        System.out.println();
        System.out.println("sos-commons-hash-[VERSION] [String]");
        System.out.println();
        System.out.printf("  %-29s | %s%n", "no argument", "shows this help page, this option is exclusive and has no value");
        System.out.printf("  %-29s | %s%n", "String", "the String to be hashed.");
    }

}
