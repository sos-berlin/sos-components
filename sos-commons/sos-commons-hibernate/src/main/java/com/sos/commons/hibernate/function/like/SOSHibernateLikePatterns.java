package com.sos.commons.hibernate.function.like;

import com.sos.commons.util.SOSString;

public class SOSHibernateLikePatterns {

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
     * <li>{@code \} - {@code \\} (escaped backslash)</li>
     * <li>{@code _} - {@code \_} (escaped literal underscore)</li>
     * <li>{@code *} - {@code %} (SQL wildcard)</li>
     * <li>{@code ?} - {@code _} (single character wildcard)</li>
     * 
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
     * @param globglob pattern input
     * @return SQL LIKE compatible pattern (escaped) */
    public static String globToSqlLike(String glob) {
        if (SOSString.isEmpty(glob)) {
            return glob;
        }

        return glob.replace("\\", "\\\\")  // escape backslash
                .replace("_", "\\_") // // make SQL LIKE underscore literal instead of wildcard
                .replace("*", "%") // glob wildcard to SQL wildcard
                .replace("?", "_"); // single-char wildcard to SQL wildcard

    }
}
