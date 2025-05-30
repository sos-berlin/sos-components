package com.sos.js7.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

public class JobArguments {

    public enum LogLevel {
        INFO, DEBUG, TRACE, WARN, ERROR
    }

    public enum MockLevel {
        OFF, INFO, ERROR
    }

    private JobArgument<LogLevel> logLevel = new JobArgument<>("log_level", false, LogLevel.INFO);
    private JobArgument<MockLevel> mockLevel = new JobArgument<>("mock_level", false, MockLevel.OFF);

    private Map<String, List<JobArgument<?>>> includedArguments;
    private List<JobArgument<?>> dynamicArgumentFields;

    public JobArguments() {
    }

    public JobArguments(ASOSArguments... args) {
        setIncludedArguments(args);
    }

    public void setDynamicArgumentFields(List<JobArgument<?>> val) {
        dynamicArgumentFields = val;
    }

    public List<JobArgument<?>> getDynamicArgumentFields() {
        return dynamicArgumentFields;
    }

    public boolean hasDynamicArgumentFields() {
        return dynamicArgumentFields != null && dynamicArgumentFields.size() > 0;
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
                        return JobArgument.createDeclaredArgumentFromIncluded(sa,f);
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

    public JobArgument<LogLevel> getLogLevel() {
        return logLevel;
    }

    public JobArgument<MockLevel> getMockLevel() {
        return mockLevel;
    }
}
