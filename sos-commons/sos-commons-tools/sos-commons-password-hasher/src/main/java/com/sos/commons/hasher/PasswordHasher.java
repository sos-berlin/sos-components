package com.sos.commons.hasher;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordHasher {

    public static void main(String[] args) throws Exception {
        if (args != null && args.length == 1) {
            if (args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("--help")) {
                printUsage();
                System.exit(0);
                return;
            } 
        }        
        if (args[0].equalsIgnoreCase("-s")) {
            if (args.length == 2) {
                System.out.println();
                System.out.println("sha512:" + hash(args[1]));
                System.out.println();
            } else {
                System.out.println();
                System.out.println("No String provided as argument, nothing to do.");
                System.out.println();
            }
        }
    }

    public static String hash(String val) {
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
            throw new RuntimeException(ex);
        }
    }

    private static void printUsage(){
        System.out.println();
        System.out.println("Creates a SHA-512 hashed password string as used in the private.conf.");
        System.out.println();
        System.out.println("create-sha512-pw [Option]");
        System.out.println();
        System.out.printf("  %-29s | %s%n", "-?, -h, --help", "shows this help page, this option is exclusive and has no value");
        System.out.printf("  %-29s | %s%n", "-s", "enter string to be hashed here.");
    }

}
