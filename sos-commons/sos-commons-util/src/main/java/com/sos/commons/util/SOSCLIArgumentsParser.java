package com.sos.commons.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class SOSCLIArgumentsParser {

    public static Map<String, String> parse(String args) {
        if (args == null) {
            return new LinkedHashMap<>();
        }
        return parse(args.trim().split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
    }

    public static Map<String, String> parse(String[] args) {
        final Map<String, String> arguments = new LinkedHashMap<>();
        if (args == null || args.length == 0) {
            return arguments;
        }
        Arrays.stream(args).filter(arg -> arg.startsWith("-")).forEach(arg -> parseArg(arguments, arg));
        return arguments;
    }

    private static void parseArg(Map<String, String> arguments, String arg) {
        String[] parts = arg.split("=", 2);
        String key = parts[0].replaceFirst("^--?", "");
        String value = parts.length > 1 ? stripQuotes(parts[1]) : "true";
        arguments.put(key, value);
    }

    private static String stripQuotes(String value) {
        return (value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")) ? value.substring(1, value.length()
                - 1) : value;
    }

    public static void main(String[] args) {
        Map<String, String> result = SOSCLIArgumentsParser.parse(new String[] { "-name=\"Fritz Tester\"", "--age=30", "--city=Berlin", "--flag" });
        System.out.println(result);

        result = SOSCLIArgumentsParser.parse("-name=\"Fritz Tester\" --age=30 --city=Berlin      --flag");
        System.out.println(result);
    }
}
