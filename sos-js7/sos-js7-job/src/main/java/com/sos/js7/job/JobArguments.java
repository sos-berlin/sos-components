package com.sos.js7.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

/** Represents the declared job arguments for a job execution.
 * <p>
 * This class provides predefined job arguments such as logging and mocking levels. Each argument is represented as a {@link JobArgument} instance.
 * </p>
 */
public class JobArguments {

    /** Log levels supported for job execution. */
    public enum LogLevel {
        INFO, DEBUG, TRACE, WARN, ERROR
    }

    /** Mocking levels supported for job execution.
     * <ul>
     * <li>{@code OFF} – no mocking is applied.</li>
     * <li>{@code INFO} – mocking is active; the step execution will always end as successful, independently of any errors that occur.</li>
     * <li>{@code ERROR} – mocking is active; the step execution can fail.</li>
     * </ul>
     */
    public enum MockLevel {
        OFF, INFO, ERROR
    }

    private JobArgument<LogLevel> logLevel = new JobArgument<>("log_level", false, LogLevel.INFO);
    private JobArgument<MockLevel> mockLevel = new JobArgument<>("mock_level", false, MockLevel.OFF);

    private Map<String, List<JobArgument<?>>> includedArguments;
    private List<JobArgument<?>> dynamicArguments;

    /** Creates a new instance of {@link JobArguments} with default arguments. */
    public JobArguments() {
    }

    /** Creates a new instance of {@link JobArguments} and includes the specified arguments.
     * <p>
     * This constructor allows including arguments whether or not they are declared in this class, making argument definitions reusable across different
     * contexts.
     * </p>
     *
     * @param args the arguments to include */
    public JobArguments(ASOSArguments... args) {
        setIncludedArguments(args);
    }

    /** Sets the dynamic arguments for this job.
     * <p>
     * Dynamic arguments are created on the fly, for example in GraalVM Python or JavaScript jobs.<br/>
     * They cannot be defined using Java {@link JobArguments} (i.e., not represented by Java classes),<br/>
     * but are defined via the respective language scripts as quasi-textual values.
     * </p>
     *
     * @param val the list of dynamic {@link JobArgument} instances */
    public void setDynamicArguments(List<JobArgument<?>> val) {
        dynamicArguments = val;
    }

    /** Returns the list of dynamic arguments for this job.
     *
     * @return the list of dynamic {@link JobArgument} instances, or {@code null} if none */
    public List<JobArgument<?>> getDynamicArguments() {
        return dynamicArguments;
    }

    /** Checks whether there are any dynamic arguments defined.
     *
     * @return {@code true} if there is at least one dynamic argument; {@code false} otherwise */
    public boolean hasDynamicArguments() {
        return dynamicArguments != null && dynamicArguments.size() > 0;
    }

    protected Map<String, List<JobArgument<?>>> getIncludedArguments() {
        return includedArguments;
    }

    private void setIncludedArguments(ASOSArguments... args) {
        if (args == null || args.length == 0) {
            return;
        }
        includedArguments = new HashMap<>();
        for (ASOSArguments arg : args) {
            List<JobArgument<?>> l = arg.getArgumentFields().stream().map(f -> {
                try {
                    f.setAccessible(true);
                    try {
                        SOSArgument<?> sa = (SOSArgument<?>) f.get(arg);
                        if (sa.getName() == null) {// internal usage
                            return null;
                        }
                        return JobArgument.createDeclaredArgumentFromIncluded(sa, f);
                    } catch (Throwable e) {
                        return null;
                    }
                } catch (Throwable e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            includedArguments.put(arg.getIdentifier(), l);
        }
    }

    /** Returns the job argument representing the log level.
     * <p>
     * The log level controls the verbosity of job execution logs.<br/>
     * Its value is a Java {@link LogLevel} enum.<br/>
     * Default is {@link LogLevel#INFO}.
     * </p>
     * 
     * @return the {@link JobArgument} for the log level */
    public JobArgument<LogLevel> getLogLevel() {
        return logLevel;
    }

    /** Returns the job argument representing the mock level.
     * <p>
     * The mock level determines how job execution behaves under mocking.<br/>
     * Its value is a Java {@link MockLevel} enum. .<br/>
     * Default is {@link MockLevel#OFF}.
     * </p>
     * 
     * @return the {@link JobArgument} for the mock level */
    public JobArgument<MockLevel> getMockLevel() {
        return mockLevel;
    }
}
