package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jobscheduler.model.command.Abort;
import com.sos.jobscheduler.model.command.Command;
import com.sos.jobscheduler.model.command.Terminate;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ModifyJobSchedulerAudit;
import com.sos.joc.exceptions.JobSchedulerNoResponseException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceModifyJobScheduler;
import com.sos.joc.model.jobscheduler.UrlParameter;

@Path("jobscheduler")
public class JobSchedulerResourceModifyJobSchedulerImpl extends JOCResourceImpl
		implements IJobSchedulerResourceModifyJobScheduler {

	private static String API_CALL = "./jobscheduler/";

	@Override
	public JOCDefaultResponse postJobschedulerTerminate(String accessToken, UrlParameter urlParameter) {
		try {
			boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken)
					.getJobschedulerMaster().getExecute().isTerminate();
			return executeModifyJobSchedulerCommand("terminate", new Terminate(), urlParameter, accessToken, permission);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

	//@Override
	public JOCDefaultResponse postJobschedulerRestartTerminate(String accessToken, UrlParameter urlParameter) {
		try {
			boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken)
					.getJobschedulerMaster().getExecute().getRestart().isTerminate();
			return executeModifyJobSchedulerCommand("restart", new Terminate(true), urlParameter, accessToken, permission);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

	@Override
	public JOCDefaultResponse postJobschedulerAbort(String accessToken, UrlParameter urlParameter) {
		try {
			boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken)
					.getJobschedulerMaster().getExecute().isAbort();
			return executeModifyJobSchedulerCommand("abort", new Abort(), urlParameter, accessToken, permission);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

	//@Override
	public JOCDefaultResponse postJobschedulerRestartAbort(String accessToken, UrlParameter urlParameter) {
		try {
			boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken)
					.getJobschedulerMaster().getExecute().getRestart().isAbort();
			return executeModifyJobSchedulerCommand("abort_and_restart", new Abort(true), urlParameter, accessToken, permission);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

	private JOCDefaultResponse executeModifyJobSchedulerCommand(String request, Command cmd, UrlParameter urlParameter,
			String accessToken, boolean permission) throws JsonProcessingException, JocException {
		JOCDefaultResponse jocDefaultResponse = init(API_CALL + request, urlParameter, accessToken,
				urlParameter.getJobschedulerId(), permission);
		if (jocDefaultResponse != null) {
			return jocDefaultResponse;
		}

		checkRequiredComment(urlParameter.getAuditLog());
		ModifyJobSchedulerAudit jobschedulerAudit = new ModifyJobSchedulerAudit(urlParameter);
		logAuditMessage(jobschedulerAudit);

		JOCJsonCommand jocJsonCommand = new JOCJsonCommand(urlParameter.getUrl(), accessToken);
		jocJsonCommand.setUriBuilderForCommands();
		String body = new ObjectMapper().writeValueAsString(cmd);
		if (request.contains("abort")) {
			try {
				jocJsonCommand.getJsonObjectFromPost(body);
			} catch (JobSchedulerNoResponseException e) {
				// JobScheduler sends always no response if "abort" is called
			}
		} else {
			jocJsonCommand.getJsonObjectFromPost(body);
		}
		//TODO expected answer { "TYPE": "Accepted" }
		storeAuditLogEntry(jobschedulerAudit);
		return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
	}

}
