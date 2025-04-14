package com.sos.commons.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SOSCLIArgumentsParser {

    /** Parses a single string of CLI-style arguments (e.g. from a shell input).<br/>
     * Quoted sections are preserved (e.g. -cp "lib/*;dist/*").
     *
     * @param args CLI argument string
     * @return parsed arguments as key-value pairs */
    public static Map<String, String> parse(String args) {
        if (args == null || args.trim().isEmpty()) {
            return new LinkedHashMap<>();
        }
        return parse(tokenize(args).toArray(new String[0]));
    }

    /** Parses a pre-split array of CLI arguments (e.g. from main(String[] args)).
     *
     * @param args CLI argument array
     * @return parsed arguments as key-value pairs */
    public static Map<String, String> parse(String[] args) {
        final Map<String, String> arguments = new LinkedHashMap<>();
        if (args == null || args.length == 0) {
            return arguments;
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("-")) {
                continue;
            }

            String key, value;
            // Handle --key=value or -k=value
            if (arg.contains("=")) {
                String[] parts = arg.split("=", 2);
                key = parts[0].replaceFirst("^--?", "");
                value = stripQuotes(parts[1]);
            } else {
                key = arg.replaceFirst("^--?", "");
                // Check if next element is a value (and not another flag)
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    value = stripQuotes(args[++i]);
                } else {
                    value = "true"; // it's a flag
                }
            }
            arguments.put(key, value);
        }
        return arguments;
    }

    private static String stripQuotes(String value) {
        return (value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")) ? value.substring(1, value.length()
                - 1) : value;
    }

    private static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".*?\"|'.*?')\\s*").matcher(input);
        while (m.find()) {
            String token = m.group(1).trim();
            tokens.add(token);
        }
        return tokens;
    }

    public static void main(String[] args) {
        // args: -name "Fritz Tester" -b="a" --c="d" --e
        for (String a : args) {
            // [ARG]-name
            // [ARG]Fritz Tester
            // [ARG]-b=a
            // [ARG]--c=d
            // [ARG]--e
            System.out.println("[ARG]" + a);
        }

        List<String> l = new ArrayList<>();
        l.add("--age=30");
        l.add("--city=\"Berlin\"");
        l.add("-name");
        l.add("\"Fritz Tester\"");
        l.add("--flag");
        Map<String, String> result = SOSCLIArgumentsParser.parse(l.toArray(new String[0]));
        System.out.println(result);

        result = SOSCLIArgumentsParser.parse(" --age=30 --city=Berlin -name \"Fritz Tester\"      --flag");
        System.out.println(result);
    }
}
