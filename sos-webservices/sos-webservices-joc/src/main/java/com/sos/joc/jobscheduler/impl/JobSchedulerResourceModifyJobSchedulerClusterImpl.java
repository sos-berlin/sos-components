package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jobscheduler.model.command.ClusterSwitchOver;
import com.sos.jobscheduler.model.command.Command;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ModifyJobSchedulerClusterAudit;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceModifyJobSchedulerCluster;
import com.sos.joc.model.jobscheduler.UrlParameter;

@Path("jobscheduler")
public class JobSchedulerResourceModifyJobSchedulerClusterImpl extends JOCResourceImpl
		implements IJobSchedulerResourceModifyJobSchedulerCluster {

	private static String API_CALL = "./jobscheduler/cluster";

	@Override
	public JOCDefaultResponse postJobschedulerSwitchOver(String accessToken, UrlParameter urlParameter) {
		try {
		    //TODO permission
		    boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken).getJobschedulerMasterCluster()
                    .getExecute().isTerminate();
			return executeModifyJobSchedulerCommand("switchover", new ClusterSwitchOver(), urlParameter, accessToken, permission);
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
		ModifyJobSchedulerClusterAudit jobschedulerAudit = new ModifyJobSchedulerClusterAudit(urlParameter);
		logAuditMessage(jobschedulerAudit);

		JOCJsonCommand jocJsonCommand = new JOCJsonCommand(urlParameter.getUrl(), accessToken);
		//TODO check the active node
		jocJsonCommand.setUriBuilderForCommands();
		String body = new ObjectMapper().writeValueAsString(cmd);
		jocJsonCommand.getJsonObjectFromPost(body);
		//TODO expected answer { "TYPE": "Accepted" }
		storeAuditLogEntry(jobschedulerAudit);
		return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
	}

}
