package com.sos.commons.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SOSMapVariableReplacer {

    // ${VAR},$VAR
    private static final String REGEX_ENV_VAR_UNIX = "\\$\\{([\\p{L}\\p{N}_.-]+)\\}|\\$([\\p{L}\\p{N}_.-]+)";
    private static final Pattern PATTERN_ENV_VAR_UNIX = Pattern.compile(REGEX_ENV_VAR_UNIX);
    // %VAR%,%{VAR}
    private static final String REGEX_ENV_VAR_WINDOWS = "%([\\p{L}\\p{N}_.-]+)%|%\\{([\\p{L}\\p{N}_.-]+)\\}";
    private static final Pattern PATTERN_ENV_VAR_WINDOWS = Pattern.compile(REGEX_ENV_VAR_WINDOWS);
    // ${VAR},$VAR,%VAR%,%{VAR}
    private static final Pattern PATTERN_ENV_VAR_ALL = Pattern.compile(REGEX_ENV_VAR_UNIX + "|" + REGEX_ENV_VAR_WINDOWS);

    private final Map<String, String> map;
    private final boolean caseSensitive;
    private final boolean keepUnresolved;

    /** If a variable can't be replaced, this variable will not be modified and will return the original value<br/>
     * -- Example: input=Hi %USERNAME% - %NOT_EXISTS, output=Hi Fritz Tester - %NOT_EXISTS%<br/>
     */
    public SOSMapVariableReplacer(Map<String, String> map, boolean caseSensitive) {
        this(map, caseSensitive, true);
    }

    /** @param map
     * @param caseSensitive
     * @param keepUnresolved <br/>
     *            - true - (Windows-like) If a variable can't be replaced, this variable will not be modified and will return the original value<br/>
     *            -- Example: input=Hi %USERNAME% - %NOT_EXISTS, output=Hi Fritz Tester - %NOT_EXISTS%<br/>
     *            - false - (Unix-like) If a variable can't be replaced, this variable will be modified to empty<br/>
     *            -- Example: input=Hi %USERNAME% - %NOT_EXISTS, output=Hi Fritz Tester - <br/>
     */
    public SOSMapVariableReplacer(Map<String, String> map, boolean caseSensitive, boolean keepUnresolved) {
        this.map = caseSensitive ? map : prepareEnvMapForCaseInsensitive(map);
        this.caseSensitive = caseSensitive;
        this.keepUnresolved = keepUnresolved;
    }

    /** Replaces only Unix-style environment variables like ${VAR} and $VAR */
    public String replaceUnixVars(String input) {
        return replaceVars(input, PATTERN_ENV_VAR_UNIX);
    }

    /** Replaces only Windows-style environment variables like %VAR% and %{VAR} */
    public String replaceWindowsVars(String input) {
        return replaceVars(input, PATTERN_ENV_VAR_WINDOWS);
    }

    /** Replaces Unix and Windows-style environment variables like ${VAR}, $VAR, %VAR% and %{VAR} */
    public String replaceAllVars(String input) {
        return replaceVars(input, PATTERN_ENV_VAR_ALL);
    }

    private static Map<String, String> prepareEnvMapForCaseInsensitive(Map<String, String> originalMap) {
        return originalMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue));
    }

    private String replaceVars(String input, Pattern pattern) {
        if (input == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String varName = getVarName(matcher);
            String replacement = caseSensitive ? map.get(varName) : map.get(varName.toLowerCase());
            if (replacement == null) {
                if (keepUnresolved) {
                    // If the variable is not found in the map, return the original value (e.g., %NOT_EXISTS%)
                    replacement = matcher.group(0);
                } else {
                    replacement = "";
                }
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String getVarName(Matcher matcher) {
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String varName = matcher.group(i);
            if (varName != null) {
                return varName;
            }
        }
        return null;
    }

}
