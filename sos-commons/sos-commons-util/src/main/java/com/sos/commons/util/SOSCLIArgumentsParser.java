package com.sos.commons.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SOSCLIArgumentsParser {

    /** Parses a single string of CLI-style arguments (e.g. from a shell input).<br/>
     * Quoted sections are preserved (e.g. -cp "lib/*" or multiple values: -java-options "-Xms32m" "-Xmx64m").
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

            String key;
            if (arg.contains("=")) {// --key=value or -k=value
                String[] parts = arg.split("=", 2);
                key = parts[0].replaceFirst("^--?", "");
                arguments.put(key, stripQuotes(parts[1]));
            } else {
                key = arg.replaceFirst("^--?", "");
                List<String> values = new ArrayList<>();
                // e.g. -cp "lib/*" or multiple values: -java-options "-Xms32m" "-Xmx64m"
                while (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    values.add(stripQuotes(args[++i]));
                }
                if (values.isEmpty()) { // it's a flag
                    arguments.put(key, "true");
                } else {// single/multiple values
                    // TODO multiple values as List?
                    arguments.put(key, String.join(" ", values));
                }
            }
        }
        return arguments;
    }

    private static String stripQuotes(String value) {
        return (value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")) ? value.substring(1, value.length()
                - 1) : value;
    }

    private static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        // - unquoted sequences (e.g. abc, --flag)
        // - double-quoted strings (e.g. "some value")
        // - single-quoted strings (e.g. 'some value')
        // it ignores whitespace between tokens
        Matcher m = Pattern.compile("([^\"]\\S*|\".*?\"|'.*?')\\s*").matcher(input);
        while (m.find()) {
            tokens.add(m.group(1).trim());
        }
        return tokens;
    }

    public static void main(String[] args) {
        // args: --age=30 --city=Berlin -name "Fritz Tester" -java-options "-Xms32m" "-Xmx64m" --flag
        for (String a : args) {
            // [ARG]--age=30
            // [ARG]--city=Berlin
            // [ARG]-name
            // [ARG]Fritz Tester
            // [ARG]-java-options
            // [ARG]-Xms32m
            // [ARG]-Xmx64m
            // [ARG]--flag
            System.out.println("[ARG]" + a);
        }

        List<String> l = new ArrayList<>();
        l.add("--age=30");
        l.add("--city=\"Berlin\"");
        l.add("-name");
        l.add("\"Fritz Tester\"");
        l.add("-java-options");
        l.add("\"-Xms32m\"");
        l.add("\"-Xmx64m\"");
        l.add("--flag");
        Map<String, String> result = SOSCLIArgumentsParser.parse(l.toArray(new String[0]));
        System.out.println(result);

        result = SOSCLIArgumentsParser.parse(" --age=30 --city=Berlin -name \"Fritz Tester\" -java-options \"-Xms32m\" \"-Xmx64m\"     --flag");
        System.out.println(result);
    }
}
