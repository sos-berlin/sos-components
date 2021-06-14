package com.sos.jitl.jobs.ssh.util;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.ssh.SSHJobArguments;
import com.sos.jitl.jobs.ssh.exception.SOSJobSSHException;

public class SSHJobUtil {

    private static final String AGENT_ENVVAR_PREFIX = "JS7_AGENT_";
    private static final String YADE_ENVVAR_PREFIX = "JS7_YADE_";

    private static final String DEFAULT_WINDOWS_PRE_COMMAND = "set \"%s=%s\"";
    private static final String DEFAULT_LINUX_PRE_COMMAND = "export %s='%s'";
    public static final String DEFAULT_WINDOWS_POST_COMMAND_READ = "if exist \"%s\" type \"%s\"";
    public static final String DEFAULT_WINDOWS_POST_COMMAND_DELETE = "del \"%s\"";
    public static final String DEFAULT_LINUX_DELIMITER = ";";
    public static final String DEFAULT_WINDOWS_DELIMITER = "&";
    public static final String JS7_RETURN_VALUES = "JS7_RETURN_VALUES";

    public static String resolve(SSHJobArguments jobArgs, String filename, boolean windowsShell) {
        if (jobArgs.getTmpDir().isDirty()) {
            return resolveTempFileName(jobArgs.getTmpDir().getValue(), filename, windowsShell);
        } else {
            return filename;
        }
    }

    public static void addPreCommand(SSHJobArguments jobArgs, StringBuilder sb, boolean windowsShell, String delimiter, String filepath) {
        String preCommand = jobArgs.getPreCommand().getValue();
        if (!jobArgs.getPreCommand().isDirty()) {
            preCommand = windowsShell ? DEFAULT_WINDOWS_PRE_COMMAND : DEFAULT_LINUX_PRE_COMMAND;
        }
        sb.append(String.format(preCommand, JS7_RETURN_VALUES, filepath));
        sb.append(delimiter);
    }

    private static String resolveTempFileName(String tempDir, String filename, boolean windowsShell) {
        if (windowsShell) {
            return Paths.get(tempDir, filename).toString().replace('/', '\\');
        } else {
            return Paths.get(tempDir, filename).toString().replace('\\', '/');
        }
    }

    public static String substituteVariables(SOSParameterSubstitutor parameterSubstitutor, String source) {
        String result = source;
        if (parameterSubstitutor == null) {
            parameterSubstitutor = createParameterSubstitutor();
        }
        if (source.matches("(?s).*\\$\\{[^{]+\\}.*")) {
            parameterSubstitutor.setOpenTag("${");
            parameterSubstitutor.setCloseTag("}");
            result = parameterSubstitutor.replace(source);
        }
        if (result.contains("%")) {
            parameterSubstitutor.setOpenTag("%");
            parameterSubstitutor.setCloseTag("%");
            result = parameterSubstitutor.replace(result);
        }
        return result;
    }

    public static SOSParameterSubstitutor createParameterSubstitutor() {
        SOSParameterSubstitutor parameterSubstitutor = new SOSParameterSubstitutor();
        for (Entry<String, String> entry : System.getenv().entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                parameterSubstitutor.addKey(name, value);
            }
        }
        return parameterSubstitutor;
    }

    public static void checkStdErr(String stdErr, SSHJobArguments jobArgs, JobLogger logger) throws SOSJobSSHException {
        if (jobArgs.getRaiseExceptionOnError().getValue()) {
            if (stdErr.length() > 0) {
                if (jobArgs.getIgnoreStdErr().getValue()) {
                    logger.info("output to stderr is ignored.");
                } else {
                    throw new SOSJobSSHException(stdErr);
                }
            }
        }
    }

    public static void checkExitCode(Integer exitCode, SSHJobArguments jobArgs, Map<String, Object> outcomes, JobLogger logger)
            throws SOSJobSSHException {
        if (exitCode != null) {
            outcomes.put("exit_code", exitCode);
            if (!exitCode.equals(new Integer(0))) {
                if (jobArgs.getIgnoreError().getValue() || (jobArgs.getExitCodesToIgnore().getValue() != null && jobArgs.getExitCodesToIgnore()
                        .getValue().contains(exitCode))) {
                    outcomes.put("exit_code_ignored", true);
                    logger.info("SOS-SSH: exit code is ignored due to configuration: " + exitCode);
                } else {
                    outcomes.put("exit_code_ignored", false);
                    if (jobArgs.getRaiseExceptionOnError().getValue()) {
                        throw new SOSJobSSHException("remote command terminated with exit code: " + exitCode);
                    }
                }
            }
        }
    }

    public static Map<String, String> getAgentEnvVars() {
        Map<String, String> unfiltered = System.getenv();
        return unfiltered.entrySet().stream().filter(item -> item.getKey().startsWith(AGENT_ENVVAR_PREFIX)).collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, String> getYadeEnvVars() {
        Map<String, String> unfiltered = System.getenv();
        return unfiltered.entrySet().stream().filter(item -> item.getKey().startsWith(YADE_ENVVAR_PREFIX)).collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue));
    }

}
