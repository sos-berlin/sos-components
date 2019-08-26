package com.sos.commons.hibernate;

import java.util.Collection;
import java.util.stream.Collectors;

public class SearchStringHelper {

    public static String getSearchOperator(String s) {
        if (s.contains("%")) {
            return " like ";
        } else {
            return "=";
        }
    }

    public static String getSearchPathOperator(String s) {
        s = getSearchPathValue(s);
        if (s.contains("%")) {
            return "like";
        } else {
            return "=";
        }
    }

    public static String getSearchPathValue(String s) {
        if (s.startsWith("/") || s.startsWith("%")) {
            return s;
        } else {
            return "%" + s;
        }
    }

    public static String getIntegerSetSql(final Collection<Integer> values, final String fieldName) {

        String clause = values.stream().map(value -> fieldName + "=" + value).collect(Collectors.joining(" or "));
        if (values.size() > 1) {
            clause = "(" + clause + ")";
        }
        return clause;
    }

    public static String getStringListSql(final Collection<String> values, final String fieldName) {
        
        String clause = values.stream().map(value -> fieldName + getSearchOperator(value) + "'" + value + "'").collect(Collectors.joining(" or "));
        if (values.size() > 1) {
            clause = "(" + clause + ")";
        }
        return clause;
    }

    public static String getStringListPathSql(final Collection<String> values, final String fieldName) {
        
        String clause = values.stream().map(value -> getSearchPathValue(value)).map(value -> fieldName + getSearchOperator(value) + "'" + value + "'")
                .collect(Collectors.joining(" or "));
        if (values.size() > 1) {
            clause = "(" + clause + ")";
        }
        return clause;
    }

    public static boolean isDBWildcardSearch(String regex) {
        return (regex != null && (regex.contains("%") || regex.contains(",")));
    }

    public static String getRegexValue(String sourceFilesRegex) {
        if (sourceFilesRegex != null) {
            return sourceFilesRegex.replaceAll("%", ".*");
        }
        return null;
    }
}
