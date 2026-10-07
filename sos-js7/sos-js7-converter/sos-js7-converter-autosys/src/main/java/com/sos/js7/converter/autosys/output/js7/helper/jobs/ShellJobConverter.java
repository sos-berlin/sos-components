package com.sos.js7.converter.autosys.output.js7.helper.jobs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.JobReturnCode;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.JobOMP;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.helper.Report;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.Platform;
import com.sos.js7.converter.commons.config.json.JS7Agent;

public class ShellJobConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellJobConverter.class);

    public static Job setExecutable(Autosys2JS7Converter converter, Workflow w, Path workflowPath, Job j, JobCMD jilJob, JS7Agent js7Agent) {
        boolean isMock = Autosys2JS7Converter.CONFIG.getMockConfig().hasForcedScript();
        String platform = js7Agent.getPlatform();
        boolean isUnix = platform.equals(Platform.UNIX.name());

        if (jilJob.isWindowsStyleCommand() && isUnix) {
            LOGGER.info("[" + js7Agent.getJS7AgentName() + "][maybe windows]" + jilJob.getCommand().getValue());
        }

        String commentBegin = isUnix ? "# " : "REM ";

        StringBuilder header = new StringBuilder();
        if (isMock) {
            header.append(getScriptBegin("", isUnix)).append(commentBegin).append("Mock mode").append(JS7ConverterHelper.JS7_NEW_LINE);
        }

        String command = jilJob.getCommand().getValue();

        if (header.length() == 0) {
            header.append(getScriptBegin(command, isUnix));
        }
        StringBuilder script = new StringBuilder(header);
        if (!SOSString.isEmpty(jilJob.getProfile().getValue())) {
            if (isMock) {
                script.append(commentBegin);
            }
            String commandPrefix = isUnix ? Autosys2JS7Converter.CONFIG.getJobConfig().getForcedUnixCommandPrefix() : Autosys2JS7Converter.CONFIG
                    .getJobConfig().getForcedWindowsCommandPrefix();
            if (!SOSString.isEmpty(commandPrefix)) {
                script.append(commandPrefix).append(" ");
            }
            script.append(jilJob.getProfile().getValue()).append(JS7ConverterHelper.JS7_NEW_LINE);
        }
        if (isMock) {
            script.append(commentBegin);
        }
        script.append(command);

        if (isMock) {
            script.append(JS7ConverterHelper.JS7_NEW_LINE);
            String mockScript = isUnix ? Autosys2JS7Converter.CONFIG.getMockConfig().getForcedUnixScript() : Autosys2JS7Converter.CONFIG
                    .getMockConfig().getForcedWindowsScript();
            if (!SOSString.isEmpty(mockScript)) {
                script.append(mockScript);
                script.append(JS7ConverterHelper.JS7_NEW_LINE);
            }
        }

        ExecutableScript es = new ExecutableScript();
        es.setScript(script.toString());
        es.setV1Compatible(Autosys2JS7Converter.CONFIG.getJobConfig().getForcedV1Compatible());
        // TODO Check
        if (jilJob.getFailCodes().getValue() != null) {
            JobReturnCode rc = new JobReturnCode();
            rc.setFailure(JS7ConverterHelper.integerListValue(jilJob.getFailCodes().getValue(), ","));
            es.setReturnCodeMeaning(rc);
        } else {
            if (jilJob.getSuccessCodes().getValue() != null) {
                if (!jilJob.getSuccessCodes().getValue().equals("0")) {
                    JobReturnCode rc = new JobReturnCode();
                    rc.setSuccess(JS7ConverterHelper.integerListValue(jilJob.getFailCodes().getValue(), ","));
                    es.setReturnCodeMeaning(rc);
                }
            } else if (jilJob.getMaxExitSuccess().getValue() != null) {
                try {
                    List<Integer> l = new ArrayList<>();
                    for (int i = 0; i <= jilJob.getMaxExitSuccess().getValue().intValue(); i++) {
                        l.add(Integer.valueOf(i));
                    }
                    if (l.size() > 0) {
                        JobReturnCode rc = new JobReturnCode();
                        rc.setSuccess(l);
                        es.setReturnCodeMeaning(rc);
                    }
                } catch (Throwable e) {
                    LOGGER.error("[" + jilJob + "][getMaxExitSuccess]" + e, e);
                }
            }
        }

        Set<String> enVars = extractEnvironmentVariables(jilJob, command.toString());
        if (!SOSCollection.isEmpty(enVars)) {
            String jobPath = SOSPathUtils.toUnixStyle(workflowPath.toString().replace(".workflow.json", "")) + "/" + jilJob.getBaseName();
            String enVarsJoined = SOSString.join(enVars);
            if (!jilJob.isReference()) {
                LOGGER.info("[COMMAND][DETECTED CUSTOM ENV VARS][" + jobPath + "]" + enVarsJoined);
                Report.writePerJobDetectedCustomEnvVarReport(converter.getAnalyzer().getReportDir(), jilJob, jobPath, enVarsJoined);
            }

            Environment env = null;
            for (String v : enVars) {
                switch (v) {
                case "AUTO_JOB_NAME":
                    env = es.getEnv();
                    if (env == null) {
                        env = new Environment();
                    }
                    env.getAdditionalProperties().put("AUTO_JOB_NAME", "$js7JobName");
                    break;
                case "AUTORUN":
                    env = es.getEnv();
                    if (env == null) {
                        env = new Environment();
                    }
                    env.getAdditionalProperties().put("AUTORUN", "$js7EpochMilli");
                    break;
                default:
                    Map<String, String> jr = converter.getJobResources().get(Autosys2JS7Converter.JOBRESOURCE_JOB_ENVIRONMENT);
                    if (jr == null) {
                        jr = new HashMap<>();
                    }
                    if (!jr.containsKey(v)) {
                        jr.put(v, "");
                    }
                    converter.getJobResources().put(Autosys2JS7Converter.JOBRESOURCE_JOB_ENVIRONMENT, jr);

                    List<String> names = w.getJobResourceNames();
                    if (names == null) {
                        names = new ArrayList<>();
                        names.add(Autosys2JS7Converter.JOBRESOURCE_JOB_ENVIRONMENT);
                        w.setJobResourceNames(names);
                    } else {
                        if (!names.contains(Autosys2JS7Converter.JOBRESOURCE_JOB_ENVIRONMENT)) {
                            names.add(Autosys2JS7Converter.JOBRESOURCE_JOB_ENVIRONMENT);
                            w.setJobResourceNames(names);
                        }
                    }
                    // w.setJobResourceNames(names.stream().distinct().collect(Collectors.toList()));

                    break;
                }
            }
            if (env != null) {
                es.setEnv(env);
            }
        }

        j.setExecutable(es);
        return j;
    }

    public static Job setExecutable(Autosys2JS7Converter converter, Job j, JobOMP jilJob, String platform) {
        boolean isMock = Autosys2JS7Converter.CONFIG.getMockConfig().hasForcedScript();
        boolean isUnix = platform.equals(Platform.UNIX.name());
        String commentBegin = isUnix ? "# " : "REM ";

        StringBuilder header = new StringBuilder();
        if (isMock) {
            header.append(getScriptBegin("", isUnix)).append(commentBegin).append("Mock mode").append(JS7ConverterHelper.JS7_NEW_LINE);
        }

        String scriptInclude = Autosys2JS7Converter.SCRIPT_INCLUDE_CHECK_PROCESS_STATUS;
        if (!converter.getScriptIncludes().containsKey(scriptInclude)) {
            converter.getScriptIncludes().put(scriptInclude, jilJob.toCMD(isUnix, null));
        }

        StringBuilder command = new StringBuilder();
        command.append(jilJob.toCMDEnvVars(isUnix, isMock ? commentBegin : null));
        if (isMock) {
            command.append(commentBegin + "##!include ").append(scriptInclude);
        } else {
            command.append("##!include ").append(scriptInclude);
        }
        command.append(JS7ConverterHelper.JS7_NEW_LINE);

        if (header.length() == 0) {
            header.append(getScriptBegin(command.toString(), isUnix));
        }
        StringBuilder script = new StringBuilder(header);
        if (!SOSString.isEmpty(jilJob.getProfile().getValue())) {
            if (isMock) {
                script.append(commentBegin);
            }
            String commandPrefix = isUnix ? Autosys2JS7Converter.CONFIG.getJobConfig().getForcedUnixCommandPrefix() : Autosys2JS7Converter.CONFIG
                    .getJobConfig().getForcedWindowsCommandPrefix();
            if (!SOSString.isEmpty(commandPrefix)) {
                script.append(commandPrefix).append(" ");
            }
            script.append(jilJob.getProfile().getValue()).append(JS7ConverterHelper.JS7_NEW_LINE);
        }
        if (isMock) {
            script.append(commentBegin);
        }
        script.append(command);

        if (isMock) {
            script.append(JS7ConverterHelper.JS7_NEW_LINE);
            String mockScript = isUnix ? Autosys2JS7Converter.CONFIG.getMockConfig().getForcedUnixScript() : Autosys2JS7Converter.CONFIG
                    .getMockConfig().getForcedWindowsScript();
            if (!SOSString.isEmpty(mockScript)) {
                script.append(mockScript);
                script.append(JS7ConverterHelper.JS7_NEW_LINE);
            }
        }

        ExecutableScript es = new ExecutableScript();
        es.setScript(script.toString());
        es.setV1Compatible(Autosys2JS7Converter.CONFIG.getJobConfig().getForcedV1Compatible());

        j.setExecutable(es);

        return j;
    }

    public static Set<String> extractEnvironmentVariables(JobCMD jilJob, String script) {
        Set<String> variables = new HashSet<>();

        if (SOSString.isEmpty(script) || !SOSString.isEmpty(jilJob.getProfile().getValue())) {
            return variables;
        }
        if (script.contains(".profile")) {
            return variables;
        }

        // Variablen, die im Script selbst gesetzt werden
        Set<String> localVariables = new HashSet<>();
        // Unix: VAR=..., export VAR=...
        Pattern unixAssignment = Pattern.compile("(?m)^(?:\\s*(?:export\\s+)?)?([A-Za-z_][A-Za-z0-9_]*)\\s*=");
        Matcher assignmentMatcher = unixAssignment.matcher(script);
        while (assignmentMatcher.find()) {
            localVariables.add(assignmentMatcher.group(1));
        }

        // Windows: SET VAR=..., SET "VAR=..."
        Pattern windowsAssignment = Pattern.compile("(?im)^\\s*set\\s+\"?([A-Za-z_][A-Za-z0-9_]*)\\s*=");
        Matcher windowsMatcher = windowsAssignment.matcher(script);
        while (windowsMatcher.find()) {
            localVariables.add(windowsMatcher.group(1));
        }

        // Unix: $VAR or ${VAR}
        Pattern unixVariable = Pattern.compile("\\$(?:\\{([A-Za-z_][A-Za-z0-9_]*)\\}|([A-Za-z_][A-Za-z0-9_]*))");
        Matcher unixMatcher = unixVariable.matcher(script);
        while (unixMatcher.find()) {
            String variable = unixMatcher.group(1) != null ? unixMatcher.group(1) : unixMatcher.group(2);

            if (!"HOME".equalsIgnoreCase(variable) && !localVariables.contains(variable)) {
                variables.add(variable);
            }
        }

        // Windows: %VAR%
        Pattern windowsVariable = Pattern.compile("%([A-Za-z_][A-Za-z0-9_]*)%");
        Matcher windowsVariableMatcher = windowsVariable.matcher(script);
        while (windowsVariableMatcher.find()) {
            String variable = windowsVariableMatcher.group(1);
            if (!localVariables.contains(variable)) {
                // command: zip /prod/post/1.0/OUT/ARCHIVES/ASPHERIA/MDP3NDNC_`date +%Y%m%d_%H%M`.zip /prod/appl/prodappl/post/1.0/OUT/MDP3NDNC
                if (!"Y".equals(variable) && !"d_".equals(variable)) {
                    variables.add(variable);
                }
            }
        }

        return variables;
    }

    private static String getScriptBegin(String command, boolean isUnix) {
        if (isUnix) {
            if (command != null && !command.toString().startsWith("#!/")) {
                StringBuilder sb = new StringBuilder();

                if (!SOSString.isEmpty(Autosys2JS7Converter.CONFIG.getJobConfig().getDefaultUnixKshShebang())) {
                    boolean isKsh = command.toLowerCase().endsWith(".ksh");
                    if (isKsh) {
                        sb.append(Autosys2JS7Converter.CONFIG.getJobConfig().getDefaultUnixKshShebang());
                        sb.append(JS7ConverterHelper.JS7_NEW_LINE);
                        return sb.toString();
                    }
                }

                if (!SOSString.isEmpty(Autosys2JS7Converter.CONFIG.getJobConfig().getDefaultUnixShebang())) {
                    sb.append(Autosys2JS7Converter.CONFIG.getJobConfig().getDefaultUnixShebang());
                    sb.append(JS7ConverterHelper.JS7_NEW_LINE);
                    return sb.toString();
                }
            }
        }
        return "";
    }

}
