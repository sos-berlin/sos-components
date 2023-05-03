package com.sos.js7.converter.js1.common.jobchain.node;

import java.nio.file.Path;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JobChainNodeFileOrderSource extends AJobChainNode {

    private static final String ATTR_DIRECTORY = "directory";
    private static final String ATTR_REGEX = "regex";
    private static final String ATTR_NEXT_STATE = "next_state";
    private static final String ATTR_MAX = "max";
    private static final String ATTR_REPEAT = "repeat";
    private static final String ATTR_ALERT_WHEN_DIRECTORY_MISSING = "alert_when_directory_missing";
    private static final String ATTR_DELAY_AFTER_ERROR = "delay_after_error";

    private String directory; // directory_path
    private String regex;
    private String nextState;
    private Integer max;

    private String repeat; // no|seconds

    private Boolean alertWhenDirectoryMissing;
    private String delayAfterError; // seconds

    protected JobChainNodeFileOrderSource(Path jobChainPath, JobChainNodeType type, Node node) {
        super(jobChainPath, type, node);
        directory = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_DIRECTORY));
        regex = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_REGEX));
        nextState = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_NEXT_STATE));
        max = JS7ConverterHelper.integerValue(getAttributes().get(ATTR_MAX));
        repeat = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_REPEAT));
        alertWhenDirectoryMissing = JS7ConverterHelper.booleanValue(getAttributes().get(ATTR_ALERT_WHEN_DIRECTORY_MISSING));
        delayAfterError = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_DELAY_AFTER_ERROR));
    }

    public String getDirectory() {
        return directory;
    }

    public String getRegex() {
        return regex;
    }

    public String getNextState() {
        return nextState;
    }

    public Integer getMax() {
        return max;
    }

    public String getRepeat() {
        return repeat;
    }

    public boolean isAlertWhenDirectoryMissing() {
        return alertWhenDirectoryMissing == null ? true : alertWhenDirectoryMissing;
    }

    public String getDelayAfterError() {
        return delayAfterError;
    }

}
