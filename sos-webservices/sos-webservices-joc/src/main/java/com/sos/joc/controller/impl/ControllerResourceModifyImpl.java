package com.sos.joc.controller.impl;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.controller.model.command.Abort;
import com.sos.controller.model.command.ClusterAction;
import com.sos.controller.model.command.Command;
import com.sos.controller.model.command.Terminate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ModifyControllerAudit;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.controller.resource.IControllerResourceModify;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerNoResponseException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

@Path("controller")
public class ControllerResourceModifyImpl extends JOCResourceImpl implements IControllerResourceModify {

    private static String API_CALL = "./controller/";

    @Override
    public JOCDefaultResponse postJobschedulerTerminate(String accessToken, byte[] filterBytes) {
        try {
            UrlParameter urlParameter = getUrlParameter(filterBytes, accessToken, "terminate");

            boolean permission = getControllerPermissions(urlParameter.getControllerId(), accessToken).getTerminate();
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
            UrlParameter urlParameter = getUrlParameter(filterBytes, accessToken, "restart");

            boolean permission = getControllerPermissions(urlParameter.getControllerId(), accessToken).getRestart();
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
            UrlParameter urlParameter = getUrlParameter(filterBytes, accessToken, "abort");

            boolean permission = getControllerPermissions(urlParameter.getControllerId(), accessToken).getTerminate();
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
            UrlParameter urlParameter = getUrlParameter(filterBytes, accessToken, "abort_and_restart");

            boolean permission = getControllerPermissions(urlParameter.getControllerId(), accessToken).getRestart();
            return executeModifyJobSchedulerCommand("abort_and_restart", new Abort(true), urlParameter, accessToken, permission);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private UrlParameter getUrlParameter(byte[] filterBytes, String accessToken, String request) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + request, filterBytes, accessToken);
        JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
        return Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
    }

    private JOCDefaultResponse executeModifyJobSchedulerCommand(String request, Command cmd, UrlParameter urlParameter, String accessToken,
            boolean permission) throws JsonProcessingException, JocException {
        JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
        if (jocDefaultResponse != null) {
            return jocDefaultResponse;
        }

        try {
            checkRequiredParameter("url", urlParameter.getUrl());
        } catch (JocMissingRequiredParameterException e) {
            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(urlParameter.getControllerId());
            if (controllerInstances == null || controllerInstances.size() > 1) { // is cluster
                throw e;
            } else {
                urlParameter.setUrl(URI.create(controllerInstances.get(0).getUri()));
            }
        }
        checkRequiredComment(urlParameter.getAuditLog());
        logAuditMessage(urlParameter.getAuditLog());
        
        JOCJsonCommand jocJsonCommand = new JOCJsonCommand(urlParameter.getUrl(), accessToken);
        jocJsonCommand.setUriBuilderForCommands();
        String body = new ObjectMapper().writeValueAsString(cmd);
        if (request.contains("abort")) {
            try {
                jocJsonCommand.getJsonObjectFromPost(body);
            } catch (ControllerNoResponseException | ControllerConnectionRefusedException | ControllerConnectionResetException e) {
                // JobScheduler sends always no response if "abort" is called
            }
        } else {
            jocJsonCommand.getJsonObjectFromPost(body);
        }
        // TODO expected answer { "TYPE": "Accepted" }
        ModifyControllerAudit jobschedulerAudit = new ModifyControllerAudit(urlParameter);
        storeAuditLogEntry(jobschedulerAudit);
        return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
    }

}
