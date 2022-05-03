package com.sos.jitl.jobs.ssh;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.ssh.exception.SOSJobSSHException;
import com.sos.jitl.jobs.ssh.util.SSHJobUtil;

import js7.data_for_java.order.JOutcome.Completed;

public class SSHJob extends ABlockingInternalJob<SSHJobArguments> {

    private SOSParameterSubstitutor parameterSubstitutor = new SOSParameterSubstitutor();

    @Override
    public Completed onOrderProcess(JobStep<SSHJobArguments> step) throws Exception {
        
        SSHProviderArguments providerArgs = step.getAppArguments(SSHProviderArguments.class);
        SSHProvider provider = new SSHProvider(providerArgs, step.getAppArguments(SOSCredentialStoreArguments.class));
        SSHJobArguments jobArgs = step.getArguments();

        SOSEnv envVars = new SOSEnv();
        JobLogger logger = step.getLogger();
        Map<String, Object> outcomes = new HashMap<String, Object>();
        String returnValuesFileName = "sos-ssh-return-values-" + UUID.randomUUID() + ".txt";
        String resolvedReturnValuesFileName = null;
        boolean isWindowsShell = false;
        String delimiter = null;
        List<String> tempFilesToDelete = new ArrayList<String>();
        
        SOSCommandResult result = null;
        /** steps - read agent environment variables - export some of them - execute command, script, remote script - set return values */
        try {
            logger.info("[connect]%s:%s ...", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue());
            provider.connect();
            logger.info("[connected][%s:%s]%s", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue(), provider
                    .getServerInfo().toString());
            isWindowsShell = provider.getServerInfo().hasWindowsShell();
            delimiter = isWindowsShell ? SSHJobUtil.DEFAULT_WINDOWS_DELIMITER : SSHJobUtil.DEFAULT_LINUX_DELIMITER;
            
            Map<String, String> allEnvVars = Collections.emptyMap();
            if (jobArgs.getCreateEnvVars().getValue()) {
                Map<String, String> js7EnvVars = SSHJobUtil.getJS7EnvVars();
                Map<String, String> envVarsOfWorkflowParameters = SSHJobUtil.getWorkflowParamsAsEnvVars(step, jobArgs);
                allEnvVars = Stream.of(js7EnvVars, envVarsOfWorkflowParameters).flatMap(map -> map.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                Map<String,String> resolved = resolveReturnValuesFilename(jobArgs, tempFilesToDelete, returnValuesFileName, isWindowsShell, logger);
                resolvedReturnValuesFileName = resolved.get(resolved.keySet().toArray()[0]);
                allEnvVars.putAll(resolved);
                envVars.setLocalEnvs(allEnvVars);// ??? local?global
                if (logger.isDebugEnabled()) {
                    logWorkflowCredentials(step);
                    logger.debug("Systems Environment Variables - JS7");
                    logSosEnvVars(js7EnvVars, logger);
                    logger.debug("Additional Environment Variables - workflow");
                    logSosEnvVars(envVarsOfWorkflowParameters, logger);
                }
            } else {
                Map<String,String> resolved = resolveReturnValuesFilename(jobArgs, tempFilesToDelete, returnValuesFileName, isWindowsShell, logger);
                resolvedReturnValuesFileName = resolved.get(resolved.keySet().toArray()[0]);
                envVars.setLocalEnvs(resolved);
            }
            String[] commands = new String[] {};
            if (!jobArgs.getCommand().isEmpty()) {
                logger.info("[execute command] %s", jobArgs.getCommand().getDisplayValue());
                commands = splitCommands(jobArgs, logger);
                // new feature 2022-04-05, SP
                if(!jobArgs.getCommandScriptFile().isEmpty()) {
                    String remoteCmdScriptFilepath = createRemoteCommandScript(provider, jobArgs, tempFilesToDelete, isWindowsShell, logger);
                    envVars.getLocalEnvs().put("JS7_SSH_TMP_SCRIPT_FILE", remoteCmdScriptFilepath);
                    if (jobArgs.getCommandScriptParam().isDirty() && !jobArgs.getCommandScriptParam().isEmpty()) {
                        String cmdScriptParams = jobArgs.getCommandScriptParam().getValue();
                        envVars.getLocalEnvs().put("JS7_SSH_SCRIPT_PARAMS", cmdScriptParams);
                    }
                }
            } else {
                commands = new String[1];
                commands[0] = createRemoteCommandScript(provider, jobArgs, tempFilesToDelete, isWindowsShell, logger);
                if (jobArgs.getCommandScriptParam().isDirty() && !jobArgs.getCommandScriptParam().isEmpty()) {
                    commands[0] += " " + jobArgs.getCommandScriptParam().getValue();
                }
            }
            logger.info("command: %s", commands[0]);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("createEnvironmentVariables=%s, simulateShell=%s", jobArgs.getCreateEnvVars().getValue(), providerArgs
                        .getSimulateShell().getValue()));
            }
            for (String command : commands) {
                StringBuilder preCommand = new StringBuilder();
                SSHJobUtil.addPreCommand(jobArgs, preCommand, isWindowsShell, delimiter, resolvedReturnValuesFileName);
                command = SSHJobUtil.substituteVariables(parameterSubstitutor, command);
                if (preCommand.length() > 0) {
                    if (logger.isDebugEnabled() && preCommand.length() > 0) {
                        logger.debug(String.format("[preCommand] %s", preCommand));
                    }
                    command = preCommand.append(command).toString();
                    if (logger.isTraceEnabled() && preCommand.length() > 0) {
                        logger.trace(String.format("[full command] %s", command));
                    }
                }

                result = provider.executeCommand(command, envVars);

                if (!SOSString.isEmpty(result.getStdOut())) {
                    outcomes.put("std_out", result.getStdOut());
                    logger.info("[stdOut]%s", result.getStdOut());
                }
                if (!SOSString.isEmpty(result.getStdErr())) {
                    outcomes.put("std_err", result.getStdErr());
                    logger.error("[stdErr]%s", result.getStdErr());
                }
                logger.info("[exitCode]%s", result.getExitCode());
                outcomes.put("exit_code", result.getExitCode());
                if (result.getException() != null) {
                    outcomes.put("exception", result.getException());
                    logger.info("[exception]%s", result.getException().getCause());
                }
            }
            if (resolvedReturnValuesFileName != null) {
                outcomes.putAll(executePostCommand(jobArgs, provider, resolvedReturnValuesFileName, isWindowsShell, logger));
            }
        } catch (Throwable e) {
            if (jobArgs.getRaiseExceptionOnError().getValue()) {
                if (jobArgs.getIgnoreError().getValue()) {
                    logger.debug(e.toString(), e);
                } else {
                    if (e instanceof SOSJobSSHException) {
                        throw e;
                    } else {
                        // String msg = "error occurred processing ssh command: " + e.getMessage() + " " + e.getCause();
                        StringBuilder sb = new StringBuilder(e.getClass().getSimpleName());
                        sb.append(": ").append(e.getMessage());
                        if (e.getCause() != null) {
                            sb.append(" ").append(e.getCause());
                        }
                        throw new SOSJobSSHException(sb.toString(), e);
                    }
                }
            }
            if (!jobArgs.getIgnoreStdErr().getValue()) {
                if (e instanceof SOSJobSSHException) {
                    throw e;
                } else {
                    String msg = "error occurred processing ssh command: " + e.getMessage() + " " + e.getCause();
                    throw new SOSJobSSHException(msg, e);
                }
            }
            throw e;
        } finally {
            if (provider != null) {
                deleteTempFiles(jobArgs, provider, tempFilesToDelete, isWindowsShell, logger);
                provider.disconnect();
                logger.info("[disconnected]%s:%s", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue());
            }
            if (result != null) {
                SSHJobUtil.checkStdErr(result.getStdErr(), jobArgs, logger);
                SSHJobUtil.checkExitCode(result.getExitCode(), jobArgs, outcomes, logger);
            }
        }
        return step.success(outcomes);
    }

    private String[] splitCommands(SSHJobArguments jobArgs, JobLogger logger) {
        logger.info("[execute command]%s", jobArgs.getCommand().getDisplayValue());
        return jobArgs.getCommand().getValue().split(jobArgs.getCommandDelimiter().getValue());
    }

    private String createRemoteCommandScript(SSHProvider provider, SSHJobArguments jobArgs, List<String> tempFilesToDelete,
            boolean isWindowsShell, JobLogger logger) throws Exception {
        if (!jobArgs.getCommandScript().isEmpty()) {
            logger.info("[execute command script]%s", jobArgs.getCommandScript().getDisplayValue());
            return putCommandScriptFile(SSHJobUtil.substituteVariables(parameterSubstitutor, jobArgs.getCommandScript().getValue()), provider,
                    jobArgs, tempFilesToDelete, isWindowsShell, logger);
        } else if (!jobArgs.getCommandScriptFile().isEmpty()) {
            logger.info("[execute command script file]%s", jobArgs.getCommandScriptFile().getDisplayValue());
            String commandScript = new String(Files.readAllBytes(Paths.get(jobArgs.getCommandScriptFile().getValue())));
            return putCommandScriptFile(SSHJobUtil.substituteVariables(parameterSubstitutor, commandScript), provider, jobArgs,
                    tempFilesToDelete, isWindowsShell, logger);
        }
        return null;
    }

    private void logSosEnvVars(Map<String, String> env, JobLogger logger) {
        logger.debug("%-30s | %s", "KEY", "VALUE");
        for (Map.Entry<String, String> entry : env.entrySet()) {
            logger.debug("%-30s | %s", entry.getKey(), entry.getValue());
        }
    }

    private void logWorkflowCredentials(JobStep<SSHJobArguments> step) throws Exception {
        step.getLogger().debug("Order ID that startet the Job: %s", step.getOrderId());
        step.getLogger().debug("Workflow name the Job is running in: %s", step.getWorkflowName());
        step.getLogger().debug("Workflow position of the Job: %s", step.getWorkflowPosition());
        step.getLogger().debug("CommitID of the workflow: %s", step.getWorkflowVersionId());
    }

    private String putCommandScriptFile(String content, SSHProvider provider, SSHJobArguments jobArgs, List<String> tempFilesToDelete, 
            boolean isWindowsShell, JobLogger logger) throws Exception {
        if (!isWindowsShell) {
            content = content.replaceAll("(?m)\r", "");
        }
        File source = File.createTempFile("sos-ssh-script-", isWindowsShell ? ".cmd" : ".sh");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source)));
        out.write(content);
        out.flush();
        out.close();
        source.deleteOnExit();
        String target = source.getName();
        if (jobArgs.getTmpDir().isDirty()) {
            target = Paths.get(jobArgs.getTmpDir().getValue()).resolve(target).toString().replace('\\', '/');
        } else {
            if (!isWindowsShell) {
                target = "./" + target;
            }
        }
        logger.info(String.format("[tmp commandScript file locally][tmp command script file remote] [%s] : [%s]", source.getCanonicalPath(), target));
        provider.put(source.getCanonicalPath(), target, 0700);
        addTemporaryFilesToDelete(target, tempFilesToDelete, logger);
        // handler.putFile(source, target, 0700);
        return target;
    }

    private void addTemporaryFilesToDelete(final String filepath, List<String> tempFilesToDelete, JobLogger logger) {
        if (!SOSString.isEmpty(filepath)) {
            tempFilesToDelete.add(filepath);
            logger.debug(String.format("file %s marked for deletion", filepath));
        }
    }

    private Map<String, String> resolveReturnValuesFilename(SSHJobArguments jobArgs, List<String> tempFilesToDelete, String returnValuesFileName,
            boolean isWindowsShell, JobLogger logger) {
        String resolvedReturnValuesFileName = SSHJobUtil.resolve(jobArgs, returnValuesFileName, isWindowsShell);
        addTemporaryFilesToDelete(resolvedReturnValuesFileName, tempFilesToDelete, logger);
        Map<String, String> retVal = new HashMap<String, String>();
        retVal.put(SSHJobUtil.JS7_RETURN_VALUES, resolvedReturnValuesFileName);
        return retVal;
    }

    private Map<String, Object> executePostCommand(SSHJobArguments jobArgs, SSHProvider provider, String resolvedReturnValuesFileName,
            boolean isWindowsShell, JobLogger logger) {
        Map<String, Object> outcomes = new HashMap<String, Object>();
        try {
            String postCommandRead = null;
            if (jobArgs.getPostCommandRead().isDirty()) {
                postCommandRead = String.format(jobArgs.getPostCommandRead().getValue(), resolvedReturnValuesFileName);
            } else {
                if (isWindowsShell) {
                    postCommandRead = String.format(SSHJobUtil.DEFAULT_WINDOWS_POST_COMMAND_READ, resolvedReturnValuesFileName,
                            resolvedReturnValuesFileName);
                } else {
                    postCommandRead = String.format(jobArgs.getPostCommandRead().getDefaultValue(), resolvedReturnValuesFileName,
                            resolvedReturnValuesFileName);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("[postCommandRead] %s", postCommandRead));
            }
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("[postCommandRead] %s", postCommandRead));
            }
            SOSCommandResult result = provider.executeCommand(postCommandRead);
            if (result.getExitCode() == 0) {
                if (!result.getStdOut().toString().isEmpty()) {
                    BufferedReader reader = new BufferedReader(new StringReader(new String(result.getStdOut())));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        Matcher regExMatcher = Pattern.compile("^([^=]+)=(.*)").matcher(line);
                        if (regExMatcher.find()) {
                            String key = regExMatcher.group(1).trim();
                            String value = regExMatcher.group(2).trim();
                            outcomes.put(key, value);
                            if (logger.isDebugEnabled()) {
                                logger.debug(String.format("[return value added to outcomes] Key: %1$s = Value: %2$s", key, value));
                            }
                        }
                    }
                }
            }
            return outcomes;
        } catch (Exception e) {
            // prevent Exception to show in case of postCommandDelete errors
            logger.warn(e.toString(), e);
            return outcomes;
        }
    }

    private void deleteTempFiles(SSHJobArguments jobArgs, SSHProvider provider, List<String> tempFilesToDelete, boolean isWindowsShell,
            JobLogger logger) {
        if (tempFilesToDelete != null && !tempFilesToDelete.isEmpty()) {
            for (String file : tempFilesToDelete) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[deleteTempFiles]" + file);
                }
                String cmd = null;
                if (jobArgs.getPostCommandDelete().isDirty()) {
                    cmd = String.format(jobArgs.getPostCommandDelete().getValue(), file);
                } else {
                    if (isWindowsShell) {
                        cmd = String.format(SSHJobUtil.DEFAULT_WINDOWS_POST_COMMAND_DELETE, file);
                    } else {
                        cmd = String.format(jobArgs.getPostCommandDelete().getDefaultValue(), file, file);
                    }
                }
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[deleteTempFiles]" + cmd);
                    }
                    provider.executeCommand(cmd);
                } catch (Exception e) {
                    logger.warn(String.format("error ocurred deleting %1$s: ", file), e);
                }
            }
            tempFilesToDelete.clear();
        }
    }
}
