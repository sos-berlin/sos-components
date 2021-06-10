package com.sos.jitl.jobs.ssh;

import java.util.List;

import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class SSHJobArguments extends JobArguments {

    private JobArgument<String> command = new JobArgument<>("command", false);
    private JobArgument<String> commandScript = new JobArgument<>("command_script", false);
    private JobArgument<String> commandScriptFile = new JobArgument<>("command_script_file", false);
    private JobArgument<String> commandScriptParam = new JobArgument<>("command_script_param", false);
    private JobArgument<String> commandDelimiter = new JobArgument<>("command_delimiter", false, "%%");
    private JobArgument<Boolean> raiseExceptionOnError = new JobArgument<>("raise_exception_on_error", false, true);
    private JobArgument<Boolean> ignoreError = new JobArgument<>("ignore_error", false, false);
    private JobArgument<List<Integer>> exitCodesToIgnore = new JobArgument<>("exit_codes_to_ignore", false);
    private JobArgument<Boolean> ignoreStdErr = new JobArgument<>("ignore_stderr", false, false);
    private JobArgument<String> tmpDir = new JobArgument<>("tmp_dir", false);
    private JobArgument<Boolean> createEnvVars = new JobArgument<>("create_env_vars", false, false);
    private JobArgument<String> preCommand = new JobArgument<>("pre_command", false, "export %s='%s'");
    private JobArgument<String> postCommandRead = new JobArgument<>("post_command_read", false, "test -r %s && cat %s; exit 0");
    private JobArgument<String> postCommandDelete = new JobArgument<>("post_command_delete", false, "test -r %s && rm %s; exit 0");
    

    public SSHJobArguments() {
        super(new SSHProviderArguments());
    }

    public JobArgument<String> getCommand() {
        return command;
    }

	public JobArgument<String> getCommandScript() {
		return commandScript;
	}

	public JobArgument<String> getCommandScriptFile() {
		return commandScriptFile;
	}

	public JobArgument<String> getCommandScriptParam() {
		return commandScriptParam;
	}

	public JobArgument<String> getCommandDelimiter() {
		return commandDelimiter;
	}

	public JobArgument<Boolean> getRaiseExceptionOnError() {
		return raiseExceptionOnError;
	}

	public JobArgument<Boolean> getIgnoreError() {
		return ignoreError;
	}

	public JobArgument<List<Integer>> getExitCodesToIgnore() {
		return exitCodesToIgnore;
	}

	public JobArgument<Boolean> getIgnoreStdErr() {
		return ignoreStdErr;
	}

	public JobArgument<String> getTmpDir() {
		return tmpDir;
	}

	public JobArgument<Boolean> getCreateEnvVars() {
		return createEnvVars;
	}

	public JobArgument<String> getPreCommand() {
		return preCommand;
	}

	public JobArgument<String> getPostCommandRead() {
		return postCommandRead;
	}

	public JobArgument<String> getPostCommandDelete() {
		return postCommandDelete;
	}

}
