package com.sos.yade.engine.commons.helpers;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.yade.engine.exceptions.YADEEngineInvalidExpressionException;

/** Evaluates and replaces expressions like:<br/>
 * 
 * <ul>
 * <li><code>[date:&lt;date format&gt; [timezone:&lt;zone&gt;]]</code>
 * <ul>
 * <li><code>date format</code> must be a valid Java date format string, e.g. yyyyMMddHHmmss, yyyy-MM-dd.HHmmss, etc.<br/>
 * - See {@linkplain https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html}.<br />
 * - Note:<br />
 * -- DateTimeFormatter.ofPattern does not validate unknown pattern letters.<br />
 * -- Any unsupported letters are treated as literal characters.<br/>
 * -- For example, the pattern "yyyy-MM-dd-!!" will be formatted as "2026-01-01-!!" for the date 2026-01-01.</li>
 * <li>The <code>timezone:&lt;zone&gt;</code> part is optional. If omitted, the system default timezone is used. <br/>
 * The timezone can be specified in the following ways:
 * <ul>
 * <li>Region ID:
 * <ul>
 * <li>e.g. <code>Europe/Berlin</code>, <code>Asia/Kolkata</code></li>
 * </ul>
 * </li>
 * <li>Time zone offset:
 * <ul>
 * <li>ISO-8601 compliant formats:
 * <ul>
 * <li>+02:00 – interpreted as +02:00</li>
 * <li>+05:30 – interpreted as +05:30</li>
 * </ul>
 * </li>
 * <li>Supported non-standard shorthand formats:
 * <ul>
 * <li>+2 – interpreted as +02:00</li>
 * <li>+5:30 – interpreted as +05:30</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * Examples:
 * <ul>
 * <li>[date:yyyyMMddHHmmss] - current date using the Java default time zone</li>
 * <li>[date:yyyyMMddHHmmss timezone:Europe/Berlin] - current date in the Europe/Berlin time zone</li>
 * <li>[date:yyyyMMddHHmmss timezone:+02:00] - current date at fixed offset +02:00</li>
 * <li>[date:yyyyMMddHHmmss timezone:+2] - current date at fixed offset +02:00</li>
 * <li>[date:yyyyMMddHHmmss timezone:+05:30] - current date at fixed offset +05:30</li>
 * <li>[date:yyyyMMddHHmmss timezone:+5:30] - current date at fixed offset +05:30</li>
 * <li>[date:yyyyMMddHHmmss timezone:-02:00] - current date at fixed offset -02:00</li>
 * <li>[date:yyyyMMddHHmmss timezone:-2] - current date at fixed offset -02:00</li>
 * </ul>
 * </p>
 * Exceptions are propagated to the caller if the format or timezone is invalid. */
public class YADEExpressionResolver {

    private static final String VAR_DATE_PREFIX = "[date:";

    // Regex:
    // group(1) = date format
    // group(2) = optional timezone (region or offset)
    private static final Pattern DATE_EXPRESSION = Pattern.compile("\\" + VAR_DATE_PREFIX + "([^\\]]+?)(?:\\s+timezone:([^\\]]+))?\\]");

    /** Replaces all <code>[date:&lt;date format&gt; [timezone:&lt;zone&gt;]] expressions in the argument value.
     * <p>
     * See {@link YADEExpressionResolver}.
     * </p>
     * 
     * @see {@link YADEExpressionResolver}
     *
     * @param arg String argument containing date expressions
     * @throws YADEEngineInvalidExpressionException if the format or time zone is invalid */
    public static void replaceDateExpressions(SOSArgument<String> arg) throws YADEEngineInvalidExpressionException {
        try {
            arg.setValue(replaceDateExpressions(arg.getValue()));
        } catch (YADEEngineInvalidExpressionException e) {
            throw new YADEEngineInvalidExpressionException("[" + arg.getName() + "]", e);
        }
    }

    /** Replaces all <code>[date:&lt;date format&gt; [timezone:&lt;zone&gt;]] expressions in the argument value.
     * <p>
     * See {@link YADEExpressionResolver}.
     * </p>
     * 
     * @see {@link YADEExpressionResolver}
     *
     * @param arg List String argument containing date expressions
     * @throws YADEEngineInvalidExpressionException if the format or time zone is invalid */
    public static void replaceDateExpressionsForListArg(SOSArgument<List<String>> arg) throws YADEEngineInvalidExpressionException {
        if (arg == null || arg.getValue() == null) {
            return;
        }
        List<String> result = new ArrayList<>();
        for (String input : arg.getValue()) {
            try {
                result.add(replaceDateExpressions(input));
            } catch (YADEEngineInvalidExpressionException e) {
                throw new YADEEngineInvalidExpressionException("[" + arg.getName() + "][" + input + "]", e);
            }
        }
        arg.setValue(result);
    }

    /** Replaces all <code>[date:&lt;date format&gt; [timezone:&lt;zone&gt;]] expressions in the input string.
     * <p>
     * See {@link YADEExpressionResolver}.
     * </p>
     * 
     * @see {@link YADEExpressionResolver}
     * 
     * @param input String containing date expressions
     * @return String with all date expressions evaluated
     * @throws YADEEngineInvalidExpressionException if the format or time zone is invalid */
    public static String replaceDateExpressions(String input) throws YADEEngineInvalidExpressionException {
        if (input == null || !input.contains(VAR_DATE_PREFIX)) {
            return input;
        }

        Matcher matcher = DATE_EXPRESSION.matcher(input);
        StringBuilder result = new StringBuilder();
        ZonedDateTime nowDefault = null; // default system time zone date time

        while (matcher.find()) {
            String dateFormat = getFromMatcherGroup(matcher.group(1));
            String timezone = getFromMatcherGroup(matcher.group(2));

            try {
                // 1) Determine now - zoned date time
                ZonedDateTime now;
                if (timezone == null) {
                    if (nowDefault == null) { // set the same default now for all while iterations
                        nowDefault = ZonedDateTime.now();
                    }
                    now = nowDefault;
                } else {
                    now = ZonedDateTime.now(resolveZone(timezone));
                }

                // 2) Format date
                // DateTimeFormatter.ofPattern will accept pattern, literal unknown letters are allowed (yyyy-MM-dd-!! -> 2026-01-06-!!)
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
                String replacement = now.format(formatter);

                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));

            } catch (DateTimeException | IllegalArgumentException e) {
                throw new YADEEngineInvalidExpressionException(matcher.group(0), e);
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String getFromMatcherGroup(String group) {
        return group != null ? group.trim() : null;
    }

    private static ZoneId resolveZone(String timezone) {
        if (timezone == null) {
            return ZoneId.systemDefault();
        }

        // tolerate "+2", "-5", "+2:30"
        char firstChar = timezone.charAt(0);
        if (firstChar == '+' || firstChar == '-') {
            String[] parts = timezone.substring(1).split(":");

            int hours = Integer.parseInt(parts[0]);
            int minutes = parts.length == 2 ? Integer.parseInt(parts[1]) : 0;
            if (firstChar == '-') {
                minutes = -minutes;
                hours = -hours;
            }
            return ZoneOffset.ofHoursMinutes(hours, minutes);
        }

        return ZoneId.of(timezone);
    }
}
