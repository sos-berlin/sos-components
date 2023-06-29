package com.sos.jitl.jobs.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.jitl.jobs.common.JobLogger.LogLevel;

public class JobArguments {

    public enum MockLevel {
        OFF, INFO, ERROR
    }

    private JobArgument<LogLevel> logLevel = new JobArgument<LogLevel>("log_level", false, LogLevel.INFO);
    private JobArgument<MockLevel> mockLevel = new JobArgument<MockLevel>("mock_level", false, MockLevel.OFF);

    @SuppressWarnings("rawtypes")
    private Map<String, List<JobArgument>> includedArguments;

    public JobArguments(ASOSArguments... args) {
        setAppArguments(args);
    }

    public JobArgument<LogLevel> getLogLevel() {
        return logLevel;
    }

    public JobArgument<MockLevel> getMockLevel() {
        return mockLevel;
    }

    @SuppressWarnings("rawtypes")
    protected Map<String, List<JobArgument>> getIncludedArguments() {
        return includedArguments;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setAppArguments(ASOSArguments... args) {
        if (args == null || args.length == 0) {
            return;
        }
        includedArguments = new HashMap<>();
        for (ASOSArguments arg : args) {
            List<JobArgument> l = arg.getArgumentFields().stream().map(f -> {
                try {
                    f.setAccessible(true);
                    try {
                        SOSArgument sa = (SOSArgument) f.get(arg);
                        if (sa.getName() == null) {// internal usage
                            return null;
                        }
                        Type type = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
                        return new JobArgument(sa, type);
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
}
