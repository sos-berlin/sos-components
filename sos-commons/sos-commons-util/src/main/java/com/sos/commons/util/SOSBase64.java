package com.sos.commons.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SOSBase64 {

    public static String encode(final String s) {
        if (s == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(final String s) {
        if (s == null) {
            return null;
        }
        return new String(Base64.getDecoder().decode(s.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        int exitStatus = 0;

        final String OP_ENCODE = "-encode";
        final String OP_DECODE = "-decode";
        try {
            if (args.length == 0 || (args.length > 0 && args[0].length() == 0)) {
                System.err.println();
                System.err.println("Usage:");
                System.err.println("       <value> - required, string to encode/decode");
                System.err.println("       -encode - optional (default), encode string");
                System.err.println("         or ");
                System.err.println("       -decode - optional, decode string");
                System.err.println();
                exitStatus = 1;
                return;
            }

            String op = OP_ENCODE;
            if (args.length > 1 && args[1].equals(OP_DECODE)) {
                op = OP_DECODE;
            }

            if (op.equals(OP_ENCODE)) {
                System.out.println(SOSBase64.encode(args[0]));
            } else {
                System.out.println(SOSBase64.decode(args[0]));
            }
        } catch (Throwable t) {
            exitStatus = 99;
            t.printStackTrace();
        } finally {
            System.exit(exitStatus);
        }
    }
}
