package com.sos.jitl.jobs.ssh.exception;

import com.sos.jitl.jobs.exception.SOSJobException;

public class SOSJobSSHException extends SOSJobException{

	private static final long serialVersionUID = -8987213800270311709L;

	public SOSJobSSHException(String message) {
		super(message);
	}

	public SOSJobSSHException(String message, Throwable cause) {
		super(message, cause);
	}

}
