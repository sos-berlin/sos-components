package com.sos.joc.jobscheduler.impl;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jobscheduler.model.command.Abort;
import com.sos.jobscheduler.model.command.ClusterAction;
import com.sos.jobscheduler.model.command.Command;
import com.sos.jobscheduler.model.command.Terminate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ModifyJobSchedulerAudit;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerNoResponseException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceModifyJobScheduler;
import com.sos.joc.model.jobscheduler.UrlParameter;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

@Path("jobscheduler")
public class JobSchedulerResourceModifyJobSchedulerImpl extends JOCResourceImpl implements IJobSchedulerResourceModifyJobScheduler {

    private static String API_CALL = "./jobscheduler/";

    @Override
    public JOCDefaultResponse postJobschedulerTerminate(String accessToken, byte[] filterBytes) {
        try {
            UrlParameter urlParameter = getUrlParameter(filterBytes);

            boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken).getJS7Controller().getExecute()
                    .isTerminate();
            Terminate terminateCommand = new Terminate();
            if (urlParameter.getWithFailover() != null && urlParameter.getWithFailover()) {
                terminateCommand.setClusterAction(new ClusterAction());
            }
            return executeModifyJobSchedulerCommand("terminate", terminateCommand, urlParameter, accessToken, permission);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postJobschedulerRestartTerminate(String accessToken, byte[] filterBytes) {
        try {
            UrlParameter urlParameter = getUrlParameter(filterBytes);

            boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken).getJS7Controller().getExecute()
                    .getRestart().isTerminate();
            Terminate terminateCommand = new Terminate(true, null);
            if (urlParameter.getWithFailover() != null && urlParameter.getWithFailover()) {
                terminateCommand.setClusterAction(new ClusterAction());
            }
            return executeModifyJobSchedulerCommand("restart", terminateCommand, urlParameter, accessToken, permission);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postJobschedulerAbort(String accessToken, byte[] filterBytes) {
        try {
            UrlParameter urlParameter = getUrlParameter(filterBytes);

            boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken).getJS7Controller().getExecute()
                    .isAbort();
            return executeModifyJobSchedulerCommand("abort", new Abort(), urlParameter, accessToken, permission);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postJobschedulerRestartAbort(String accessToken, byte[] filterBytes) {
        try {
            UrlParameter urlParameter = getUrlParameter(filterBytes);

            boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken).getJS7Controller().getExecute()
                    .getRestart().isAbort();
            return executeModifyJobSchedulerCommand("abort_and_restart", new Abort(true), urlParameter, accessToken, permission);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private UrlParameter getUrlParameter(byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
        return Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
    }

    private JOCDefaultResponse executeModifyJobSchedulerCommand(String request, Command cmd, UrlParameter urlParameter, String accessToken,
            boolean permission) throws JsonProcessingException, JocException {
        JOCDefaultResponse jocDefaultResponse = init(API_CALL + request, urlParameter, accessToken, urlParameter.getJobschedulerId(), permission);
        if (jocDefaultResponse != null) {
            return jocDefaultResponse;
        }

        try {
            checkRequiredParameter("url", urlParameter.getUrl());
        } catch (JocMissingRequiredParameterException e) {
            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(urlParameter.getJobschedulerId());
            if (controllerInstances == null || controllerInstances.size() > 1) { // is cluster
                throw e;
            } else {
                urlParameter.setUrl(URI.create(controllerInstances.get(0).getUri()));
            }
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
            } catch (JobSchedulerNoResponseException | JobSchedulerConnectionRefusedException | JobSchedulerConnectionResetException e) {
                // JobScheduler sends always no response if "abort" is called
            }
        } else {
            jocJsonCommand.getJsonObjectFromPost(body);
        }
        // TODO expected answer { "TYPE": "Accepted" }
        storeAuditLogEntry(jobschedulerAudit);
        return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
    }

}
