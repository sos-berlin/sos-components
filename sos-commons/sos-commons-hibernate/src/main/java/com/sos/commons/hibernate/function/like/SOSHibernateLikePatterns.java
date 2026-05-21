package com.sos.commons.hibernate.function.like;

import com.sos.commons.util.SOSString;

public class SOSHibernateLikePatterns {

    public enum CaseMode {
        LOWER, UPPER, NONE
    }

    /** Checks whether the given string contains glob pattern characters.
     *
     * <p>
     * A string is considered a glob pattern if it contains either '*' (multi-character wildcard) or '?' (single-character wildcard).
     * </p>
     *
     * @param glob input string to check
     * @return true if the string contains glob wildcards, otherwise false */
    public static boolean containsGlob(String glob) {
        if (glob == null) {
            return false;
        }
        return glob.contains("*") || glob.contains("?");
    }

    /** Converts a glob-style pattern into a SQL LIKE pattern.
     *
     * <p>
     * This method must be used together with {@link SOSHibernateLike}.
     * </p>
     *
     * <h2>Responsibilities</h2>
     * <ul>
     * <li>Escapes SQL wildcard characters</li>
     * <li>Converts glob syntax to SQL LIKE syntax</li>
     * </ul>
     *
     * <h2>Conversion rules</h2>
     * <ul>
     * <li>{@code \} -> {@code \\} (escaped backslash)</li>
     * <li>{@code _} -> {@code \_} (escaped literal underscore)</li>
     * <li>{@code %} -> {@code \%} (escaped literal percent sign)</li>
     * <li>{@code *} -> {@code %} (SQL LIKE wildcard; consecutive '*' are collapsed into a single '%', e.g. a***b****c -> a%b%c)</li>
     * <li>{@code ?} -> {@code _} (single character wildcard)</li>
     * </ul>
     *
     * <h2>Usage</h2>
     * 
     * <pre>
     * String pattern = SOSHibernateLikePatterns.globToSqlPattern("te_*");
     *
     * query.where("sos_like(lower(name), :p)");
     * query.setParameter("p", pattern);
     * </pre>
     *
     * <h2>Important</h2> This method does NOT apply SQL ESCAPE handling. The {@link SOSHibernateLike} function is responsible for adding:
     *
     * <pre>
    * ESCAPE '\'
     * </pre>
     *
     * <p>
     * Both components must be used together to ensure correct behavior.
     * </p>
     *
     * @param glob glob pattern input
     * @return SQL LIKE compatible pattern (escaped) */
    public static String globToSqlLike(String glob) {
        if (SOSString.isEmpty(glob)) {
            return glob;
        }

        return glob.replace("\\", "\\\\")  // escape backslash
                .replace("_", "\\_") // // make SQL LIKE underscore literal instead of wildcard
                .replace("%", "\\%") // // make SQL LIKE percent sign literal instead of wildcard
                .replaceAll("\\*+", "%") // glob wildcard to SQL wildcard, replaces one or more consecutive * characters with a single %. a***b****c -> a%b%c
                .replace("?", "_"); // single-char wildcard to SQL wildcard

    }

    /** Converts a glob pattern into a SQL LIKE pattern for a "contains" search.
     *
     * <p>
     * This method does NOT add surrounding '%' wildcards. It only performs glob-to-SQL conversion and normalization of the input pattern.
     * </p>
     *
     * <p>
     * The caller is responsible for adding '%' if required by the search semantics.
     * </p>
     *
     * <p>
     * The input is normalized by trimming leading and trailing '*' characters and applying the configured case mode before conversion.
     * </p>
     *
     * @param glob glob-style input pattern
     * @return SQL LIKE compatible pattern for contains search */
    public static String globToSqlLikeContains(String glob, CaseMode mode) {
        String g = applyCase(SOSString.trim(glob, "*"), mode);
        // v = SOSString.trim(v, "%");
        return SOSHibernateLikePatterns.globToSqlLike(g);
    }

    /** Converts a glob pattern into a SQL LIKE pattern for a "starts with" search.
     *
     * <p>
     * This method does NOT add a trailing '%' wildcard. It only performs glob-to-SQL conversion and normalization of the input pattern.
     * </p>
     *
     * <p>
     * The caller is responsible for adding '%' if required by the search semantics.
     * </p>
     *
     * <p>
     * The input is normalized by trimming leading '*' characters and applying the configured case mode before conversion.
     * </p>
     *
     * @param glob glob-style input pattern
     * @return SQL LIKE compatible pattern for prefix search */
    public static String globToSqlLikeStartsWith(String glob, CaseMode mode) {
        String g = applyCase(SOSString.trimStart(glob, "*"), mode);
        // v = SOSString.trim(v, "%");
        return SOSHibernateLikePatterns.globToSqlLike(g);
    }

    /** Converts a glob pattern into a SQL LIKE pattern for an "ends with" search.
     *
     * <p>
     * This method does NOT add a leading '%' wildcard. It only performs glob-to-SQL conversion and normalization of the input pattern.
     * </p>
     *
     * <p>
     * The caller is responsible for adding '%' if required by the search semantics.
     * </p>
     *
     * <p>
     * The input is normalized by trimming trailing '*' characters and applying the configured case mode before conversion.
     * </p>
     *
     * @param glob glob-style input pattern
     * @return SQL LIKE compatible pattern for suffix search */
    public static String globToSqlLikeEndsWith(String glob, CaseMode mode) {
        String g = applyCase(SOSString.trimEnd(glob, "*"), mode);
        // v = SOSString.trim(v, "%");
        return SOSHibernateLikePatterns.globToSqlLike(g);
    }

    private static String applyCase(String s, CaseMode mode) {
        if (s == null)
            return null;

        return switch (mode) {
        case LOWER -> s.toLowerCase();
        case UPPER -> s.toUpperCase();
        case NONE -> s;
        };
    }
}
