package com.sos.commons.util.loggers.base;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.sos.commons.util.SOSString;

/** Logging interface for applications that can be used in JS7 jobs (job logger) or standalone (slf4j API), etc.<br />
 * Represents a logger which supports five log levels:<br/>
 * INFO, DEBUG, TRACE, WARN, and ERROR. */
public interface ISOSLogger {

    public enum LogLevel {
        INFO, DEBUG, TRACE, WARN, ERROR
    }

    public void info(final Object msg);

    public void info(final String format, final Object... args);

    public void info(final String msg, Throwable t);

    public void debug(final Object msg);

    public void debug(final String format, final Object... args);

    public void debug(final String msg, Throwable t);

    public void trace(final Object msg);

    public void trace(final String format, final Object... args);

    public void trace(final String msg, Throwable t);

    public void warn(final Object msg);

    public void warn(final String format, final Object... args);

    public void warn(final String msg, Throwable t);

    public void error(final Object msg);

    public void error(final String format, final Object... args);

    public void error(final String msg, Throwable t);

    public void error(Throwable e);

    public boolean isInfoEnabled();

    public boolean isDebugEnabled();

    public boolean isTraceEnabled();

    /**
     * <ul>
     * <li>Formats a message using SLF4J-style placeholders ({@code {}}) if the format string contains them.
     * <li>Otherwise, formats a message using {@link String#format(String, Object...)} with Java format specifiers.</li>
     * <li>Note: Mixing SLF4J-style placeholders {} and Java format specifiers (such as %s) is not supported</li>
     * </ul>
     *
     * @param format the message format string
     * @param args the arguments used to format the message
     * @return a {@link FormattingTuple} containing the formatted message and, for SLF4J-style formatting, any trailing {@link Throwable}
     * @throws java.util.IllegalFormatException if Java formatting fails */
    public static FormattingTuple format(String format, Object... args) {
        if (args == null || args.length == 0 || format == null) {
            return new FormattingTuple(format);
        }
        if (format.contains("{}")) {
            return MessageFormatter.arrayFormat(format, args);
        }
        return new FormattingTuple(String.format(format, args));
    }

    public static String getMessage(String msg, Throwable t) {
        if (t == null) {
            return msg;
        }
        if (msg == null) {
            return SOSString.toString(t);
        }
        return msg + "\n" + SOSString.toString(t);
    }

}
