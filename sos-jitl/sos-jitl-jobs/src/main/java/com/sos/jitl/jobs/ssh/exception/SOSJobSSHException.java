package com.sos.jitl.jobs.ssh.exception;

import com.sos.commons.job.exception.JobException;

public class SOSJobSSHException extends JobException{

	private static final long serialVersionUID = -8987213800270311709L;

	public SOSJobSSHException(String message) {
		super(message);
	}

	public SOSJobSSHException(String message, Throwable cause) {
		super(message, cause);
	}

}
