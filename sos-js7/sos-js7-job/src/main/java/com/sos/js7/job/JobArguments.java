package com.sos.js7.job;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.js7.job.JobArgument.Scope;

public class JobArguments {

    public enum LogLevel {
        INFO, DEBUG, TRACE, WARN, ERROR
    }

    public enum MockLevel {
        OFF, INFO, ERROR
    }

    private JobArgument<LogLevel> logLevel = new JobArgument<LogLevel>("log_level", false, LogLevel.INFO);
    private JobArgument<MockLevel> mockLevel = new JobArgument<MockLevel>("mock_level", false, MockLevel.OFF);
    private JobArgument<String> js7WorkflowPath = new JobArgument<String>("js7Workflow.path", false, null, DisplayMode.UNMASKED, null,
            Scope.ORDER_PREPARATION);

    @SuppressWarnings("rawtypes")
    private Map<String, List<JobArgument>> includedArguments;

    @SuppressWarnings("rawtypes")
    private List<JobArgument> dynamicArgumentFields;

    public JobArguments() {
    }

    public JobArguments(ASOSArguments... args) {
        setIncludedArguments(args);
    }

    public void setDynamicArgumentFields(@SuppressWarnings("rawtypes") List<JobArgument> val) {
        dynamicArgumentFields = val;
    }

    @SuppressWarnings("rawtypes")
    public List<JobArgument> getDynamicArgumentFields() {
        return dynamicArgumentFields;
    }

    public boolean hasDynamicArgumentFields() {
        return dynamicArgumentFields != null && dynamicArgumentFields.size() > 0;
    }

    public JobArgument<LogLevel> getLogLevel() {
        return logLevel;
    }

    public JobArgument<MockLevel> getMockLevel() {
        return mockLevel;
    }

    public JobArgument<String> getJS7WorkflowPath() {
        return js7WorkflowPath;
    }

    @SuppressWarnings("rawtypes")
    protected Map<String, List<JobArgument>> getIncludedArguments() {
        return includedArguments;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setIncludedArguments(ASOSArguments... args) {
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
