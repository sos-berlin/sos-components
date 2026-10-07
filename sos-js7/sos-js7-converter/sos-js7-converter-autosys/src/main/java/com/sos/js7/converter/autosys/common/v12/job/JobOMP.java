package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public class JobOMP extends ACommonMachineJob {

    private static final String ATTR_PROCESS_NAME = "process_name"; // /opt/asys/progress/openedge/bin/_progres
    private static final String ATTR_PROCESS_STATUS = "process_status"; // STOPPED
    private static final String ATTR_MONITOR_MODE = "monitor_mode"; // WAIT, EXIT <- ???

    private SOSArgument<String> processName = new SOSArgument<>(ATTR_PROCESS_NAME, false);
    private SOSArgument<String> processStatus = new SOSArgument<>(ATTR_PROCESS_STATUS, false);
    private SOSArgument<String> monitorMode = new SOSArgument<>(ATTR_MONITOR_MODE, false);

    // ---------------------------------------------------------------------------------------------------------------------
    public JobOMP(Path source, boolean reference) {
        super(source, ConverterJobType.OMP, reference);
    }

    public SOSArgument<String> getProcessName() {
        return processName;
    }

    @ArgumentSetter(name = ATTR_PROCESS_NAME)
    public void setProcessName(String val) {
        processName.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getProcessStatus() {
        return processStatus;
    }

    @ArgumentSetter(name = ATTR_PROCESS_STATUS)
    public void setProcessStatus(String val) {
        processStatus.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getMonitorMode() {
        return monitorMode;
    }

    @ArgumentSetter(name = ATTR_MONITOR_MODE)
    public void setMonitorMode(String val) {
        monitorMode.setValue(JS7ConverterHelper.stringValue(val));
    }

    public boolean isMonitorModeWait() {
        return monitorMode.getValue() != null && monitorMode.getValue().toUpperCase().equals("WAIT");
    }

    public boolean isMonitorModeExit() {
        return monitorMode.getValue() != null && monitorMode.getValue().toUpperCase().equals("EXIT");
    }

    public String toCMDEnvVars(boolean isUnix, String commentBegin) {
        StringBuilder sb = new StringBuilder();

        // UNIX
        sb.append(getLine("PROCESS=\"" + processName.getValue() + "\"", commentBegin));
        sb.append(getLine("EXPECTED_STATUS=\"" + processStatus.getValue() + "\"", commentBegin));
        sb.append(getLine("TIMEOUT=" + getTermRunTimeAsSeconds(), commentBegin));
        sb.append(getLine("", commentBegin));
        return sb.toString();
    }

    public String toCMD(boolean isUnix, String commentBegin) {
        StringBuilder sb = new StringBuilder();

        // UNIX
        // sb.append(getLine("PROCESS=\"$1\"", commentBegin));
        // sb.append(getLine("EXPECTED_STATUS=\"$2\"", commentBegin));
        // sb.append(getLine("TIMEOUT=\"${3:-900}\"", commentBegin));
        // sb.append(getLine("INTERVAL=\"${4:-5}\"", commentBegin));
        // sb.append(getLine("", commentBegin));

        sb.append(getLine("INTERVAL=5", commentBegin));
        sb.append(getLine("", commentBegin));

        sb.append(getLine("if [ -z \"$PROCESS\" ] || [ -z \"$EXPECTED_STATUS\" ]; then", commentBegin));
        sb.append(getLine("    echo \"Missing <PROCESS> <RUNNING|STOPPED>\"", commentBegin));
        sb.append(getLine("    exit 2", commentBegin));
        sb.append(getLine("fi", commentBegin));
        sb.append(getLine("", commentBegin));

        sb.append(getLine("ELAPSED=0", commentBegin));
        sb.append(getLine("", commentBegin));

        sb.append(getLine("while true", commentBegin));
        sb.append(getLine("do", commentBegin));
        sb.append(getLine("    if pgrep -f \"$PROCESS\" > /dev/null 2>&1; then", commentBegin));
        sb.append(getLine("        CURRENT_STATUS=\"RUNNING\"", commentBegin));
        sb.append(getLine("    else", commentBegin));
        sb.append(getLine("        CURRENT_STATUS=\"STOPPED\"", commentBegin));
        sb.append(getLine("    fi", commentBegin));
        sb.append(getLine("", commentBegin));
        sb.append(getLine("    if [ \"$CURRENT_STATUS\" = \"$EXPECTED_STATUS\" ]; then", commentBegin));
        sb.append(getLine("        echo \"OK: Process [$PROCESS] is $CURRENT_STATUS\"", commentBegin));
        sb.append(getLine("        exit 0", commentBegin));
        sb.append(getLine("    fi", commentBegin));
        sb.append(getLine("", commentBegin));
        sb.append(getLine("    if [ \"$ELAPSED\" -ge \"$TIMEOUT\" ]; then", commentBegin));
        sb.append(getLine("        echo \"ERROR: Timeout. Process [$PROCESS] is $CURRENT_STATUS, expected $EXPECTED_STATUS\"", commentBegin));
        sb.append(getLine("        exit 1", commentBegin));
        sb.append(getLine("    fi", commentBegin));
        sb.append(getLine("", commentBegin));
        if (isMonitorModeWait()) {
            sb.append(getLine("    echo \"Waiting: process [$PROCESS] is $CURRENT_STATUS, expected $EXPECTED_STATUS\"", commentBegin));
            sb.append(getLine("", commentBegin));
            sb.append(getLine("    sleep \"$INTERVAL\"", commentBegin));
            sb.append(getLine("    ELAPSED=$((ELAPSED + INTERVAL))", commentBegin));
        }
        sb.append(getLine("done", commentBegin));

        return sb.toString();
    }

    private String getLine(String val, String commentBegin) {
        return getComment(commentBegin) + val + JS7ConverterHelper.JS7_NEW_LINE;
    }

    private String getComment(String commentBegin) {
        if (SOSString.isEmpty(commentBegin)) {
            return "";
        }
        return commentBegin;
    }

}
