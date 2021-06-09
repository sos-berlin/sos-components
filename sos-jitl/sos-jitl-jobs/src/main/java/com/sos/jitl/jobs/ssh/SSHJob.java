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

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.commons.vfs.ssh.common.SSHServerInfo;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.ssh.exception.SOSJobSSHException;
import com.sos.jitl.jobs.ssh.util.SSHJobUtil;

import js7.data_for_java.order.JOutcome.Completed;

public class SSHJob extends ABlockingInternalJob<SSHJobArguments> {

    /**
     *	steps
     *
     * - read agent environment variables
     *    -export some of them
     * - execute command, script, remote script
     * - set return values
     * 
     */
	
	private static final String AGENT_ENVVAR_PREFIX = "JS7_AGENT_";
	private static final String YADE_ENVVAR_PREFIX = "JS7_YADE_";
	
//    private static final String DEFAULT_LINUX_GET_PID_COMMAND = "echo $$";
//    private static final String DEFAULT_WINDOWS_GET_PID_COMMAND = "echo Add command to get PID of active shell here!";

	private SOSEnv envVars = new SOSEnv(Collections.emptyMap());
	private JobLogger logger;
    private Map<String, Object> outcomes = new HashMap<String, Object>();
    private SOSParameterSubstitutor parameterSubstitutor = new SOSParameterSubstitutor();
	
	// OLD
    private String returnValuesFileName = null;
    private String resolvedReturnValuesFileName = null;
    private boolean isWindowsShell = false;
    private String delimiter;
    private List<String> tempFilesToDelete = new ArrayList<String>();

    @Override
    public Completed onOrderProcess(JobStep<SSHJobArguments> step) throws Exception {
		logger = step.getLogger();

        SSHProviderArguments providerArgs = step.getAppArguments(SSHProviderArguments.class);
        SSHProvider provider = new SSHProvider(providerArgs);
        SSHJobArguments jobArgs = step.getArguments();
        
		UUID uuid = UUID.randomUUID();
        returnValuesFileName = "sos-ssh-return-values-" + uuid + ".txt";
//        String pidFileName = "sos-ssh-pid-" + uuid + ".txt";

        SOSCommandResult result = null;
        try {
	        SOSEnv envVarsAgent = new SOSEnv(getAgentEnvVars()); 
	        SOSEnv envVarsYade = new SOSEnv(getYadeEnvVars());

        	if(logger.isDebugEnabled()) {
            	logWorkflowCredentials(step);
    	        logger.debug("Systems Environment Variables - Agent");
    	        logSosEnvVars(envVarsAgent);
    	        logger.debug("Systems Environment Variables - Yade");
    	        logSosEnvVars(envVarsYade);
        	}
	        envVars = envVarsAgent.merge(envVarsYade);

            logger.info("[connect]%s:%s ...", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue());
            provider.connect();
            logger.info("[connected][%s:%s]%s", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue(), 
            		provider.getServerInfo().toString());
            
            isWindowsShell = SSHServerInfo.Shell.WINDOWS.equals(provider.getServerInfo().getShell());
            delimiter = isWindowsShell ? SSHJobUtil.DEFAULT_WINDOWS_DELIMITER : SSHJobUtil.DEFAULT_LINUX_DELIMITER;

//            String getPidCommand = addGetPidCommand(step);
//            if (logger.isDebugEnabled()) {
//                logger.debug(String.format("[getPidCommand] %s", getPidCommand));
//            }
           
            String[] commands = new String[]{}; 
            if (!jobArgs.getCommand().isEmpty()) {
            	logger.info("[execute command] %s", jobArgs.getCommand().getDisplayValue());
            	commands = splitCommands(jobArgs);
            } else {
            	commands = new String[1];
            	commands[0] = createRemoteCommandScript(provider, jobArgs);
            }

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("createEnvironmentVariables=%s, simulateShell=%s", jobArgs.getCreateEnvVars().getValue(), 
                		providerArgs.getSimulateShell().getValue()));
            }
            
            for (String command : commands) {
                StringBuilder preCommand = new StringBuilder();
//                StringBuilder sb = new StringBuilder(getPidCommand);
//                sb.append(" >> ").append(pidFileName).append(delimiter);
//                preCommand.append(sb);
//                add2Files2Delete(pidFileName);
                if (step.getArguments().getCreateEnvVars().getValue()) {
                    setReturnValuesEnvVar(envVars, jobArgs);
                    SSHJobUtil.addPreCommand(jobArgs, preCommand, isWindowsShell, delimiter, resolvedReturnValuesFileName);
                }
                command = SSHJobUtil.substituteVariables(parameterSubstitutor, command);
                if (preCommand.length() > 0) {
                    if (logger.isDebugEnabled() && preCommand.length() > 0) {
                        logger.debug(String.format("[preCommand] %s", preCommand));
                    }
                    command = preCommand.append(command).toString();
                }
;
                if (envVars != null) {
            		result = provider.executeCommand(command, envVarsAgent);
            	} else {
            		result = provider.executeCommand(command);
            	}
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
                executePostCommand(jobArgs, provider);
            }
        	deleteTempFiles(jobArgs, provider);
        } catch (Throwable e) {
            if (jobArgs.getRaiseExceptionOnError().getValue()) {
                if (jobArgs.getIgnoreError().getValue()) {
                    logger.debug(e.toString(), e);
                } else {
                    if (e instanceof SOSJobSSHException) {
                        throw e;
                    } else {
                        String msg = "error occurred processing ssh command: " + e.getMessage() + " " + e.getCause();
                        throw new SOSJobSSHException(msg, e);
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
                provider.disconnect();
                logger.info("[disconnected]%s:%s", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue());
            }
            if (result != null) {
                SSHJobUtil.checkStdErr(result.getStdErr(), jobArgs, logger);
                SSHJobUtil.checkExitCode(result.getExitCode(), jobArgs, outcomes, logger);
            }
//                changeExitSignal();
        }
        return step.success(outcomes);
    }

    private String[] splitCommands(SSHJobArguments jobArgs) {
    	logger.info("[execute command]%s", jobArgs.getCommand().getDisplayValue());
    	return jobArgs.getCommand().getValue().split(jobArgs.getCommandDelimiter().getValue());
    }

    private String createRemoteCommandScript(SSHProvider provider, SSHJobArguments jobArgs) throws Exception {
        if(!jobArgs.getCommandScript().isEmpty()) {
            logger.info("[execute command script]%s", jobArgs.getCommandScript().getDisplayValue());
            return putCommandScriptFile(SSHJobUtil.substituteVariables(parameterSubstitutor, jobArgs.getCommandScript().getValue()), provider);
        } else if (!jobArgs.getCommandScriptFile().isEmpty()) {
            logger.info("[execute command script file]%s", jobArgs.getCommandScriptFile().getDisplayValue());
        	String commandScript = new String(Files.readAllBytes(Paths.get(jobArgs.getCommandScriptFile().getValue())));
        	return putCommandScriptFile(SSHJobUtil.substituteVariables(parameterSubstitutor, commandScript), provider);
        }
        return null;
    }
    
    private void logSosEnvVars(SOSEnv env) {
        logger.debug("%-30s | %s", "KEY", "VALUE");
        for (Map.Entry<String, String> entry : env.getEnvVars().entrySet()) {
        	logger.debug("%-30s | %s", entry.getKey(), entry.getValue());
        }
    }

    private void logWorkflowCredentials(JobStep<SSHJobArguments> step) throws Exception {
        logger.debug("Order ID that startet the Job: %s", step.getOrderId());
        logger.debug("Workflow name the Job is running in: %s", step.getWorkflowName());
        logger.debug("Workflow position of the Job: %s", step.getWorkflowPosition());
        logger.debug("CommitID of the workflow: %s", step.getWorkflowVersionId());
    }
    
    private Map<String, String> getAgentEnvVars() {
    	Map<String, String> unfiltered = System.getenv();
    	return unfiltered.entrySet().stream().filter(item -> item.getKey().startsWith(AGENT_ENVVAR_PREFIX))
    			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, String> getYadeEnvVars() {
    	Map<String, String> unfiltered = System.getenv();
    	return unfiltered.entrySet().stream().filter(item -> item.getKey().startsWith(YADE_ENVVAR_PREFIX))
    			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String putCommandScriptFile(String content, SSHProvider provider) throws Exception {
        if (!isWindowsShell) {
            content = content.replaceAll("(?m)\r", "");
        }
        File source = File.createTempFile("sos-ssh-script-", isWindowsShell ? ".cmd" : ".sh");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source)));
        out.write(content);
        out.flush();
        out.close();
        source.deleteOnExit();
        logger.info(String.format("[commandScriptFile][tmp file created][%s]%s", source.getCanonicalPath(), content));
        String target = source.getName();
        if (!isWindowsShell) {
            target = "./" + target;
        }
        provider.put(source.getCanonicalPath(), target);
        addTemporaryFilesToDelete(target);
//        handler.putFile(source, target, 0700);
        return target;
    }

//    public String changeExitSignal(SSHProvider provider, JobStep<SSHJobArguments> step) throws SOSJobSSHException {
//    	// TODO: where to get signal from or is it even still possible 
//        String signal = null; // handler.getExitSignal();
//        if (signal != null && !signal.isEmpty()) {
//        	// set order param
//        	outcomes.put("exit_signal", signal);
//
//            if (step.getArguments().getIgnoreSignal().getValue()) {
//                logger.info("SOS-SSH: exit signal is ignored due to configuration: " + signal);
//            } else {
//                throw new SOSJobSSHException("SOS-SSH: remote command terminated with exit signal: " + signal);
//            }
//        }
//        return signal;
//    }

    public void addTemporaryFilesToDelete(final String filepath) {
        if (!SOSString.isEmpty(filepath)) {
            tempFilesToDelete.add(filepath);
            logger.debug(String.format("file %s marked for deletion", filepath));
        }
    }

    private void setReturnValuesEnvVar(SOSEnv envVars, SSHJobArguments jobArgs) {
        resolvedReturnValuesFileName = SSHJobUtil.resolve(jobArgs, returnValuesFileName, isWindowsShell);
        envVars.getEnvVars().put(SSHJobUtil.JS7_RETURN_VALUES, resolvedReturnValuesFileName);
        addTemporaryFilesToDelete(resolvedReturnValuesFileName);
    }

//    private String addGetPidCommand(JobStep<SSHJobArguments> step) {
//        if (step.getArguments().getGetPidCommand().isDirty() && !step.getArguments().getGetPidCommand().getValue().isEmpty()) {
//            return step.getArguments().getGetPidCommand().getValue();
//        } else {
//            if (isWindowsShell) {
//                return DEFAULT_WINDOWS_GET_PID_COMMAND;
//            } else {
//                return DEFAULT_LINUX_GET_PID_COMMAND;
//            }
//        }
//    }

    private void executePostCommand(SSHJobArguments jobArgs, SSHProvider provider) {
        try {
            String postCommandRead = null;
            if (jobArgs.getPostCommandRead().isDirty()) {
                postCommandRead = String.format(jobArgs.getPostCommandRead().getValue(), resolvedReturnValuesFileName);
            } else {
                if (isWindowsShell) {
                    postCommandRead = String.format(SSHJobUtil.DEFAULT_WINDOWS_POST_COMMAND_READ, resolvedReturnValuesFileName, resolvedReturnValuesFileName);
                } else {
                    postCommandRead = String.format(jobArgs.getPostCommandRead().getDefaultValue(), resolvedReturnValuesFileName, resolvedReturnValuesFileName);
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
                                logger.debug(String.format("[return value]%s=%s", key, value));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // prevent Exception to show in case of postCommandDelete errors
            logger.warn(e.toString(), e);

        }
    }
    
    private void deleteTempFiles(SSHJobArguments jobArgs, SSHProvider provider) {
        if (tempFilesToDelete != null && !tempFilesToDelete.isEmpty()) {
            for (String file : tempFilesToDelete) {
            	if(logger.isDebugEnabled()) {
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
                	if(logger.isDebugEnabled()) {
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
