package com.sos.joc.controller.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.controller.model.command.Abort;
import com.sos.controller.model.command.ClusterAction;
import com.sos.controller.model.command.Command;
import com.sos.controller.model.command.Terminate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.controller.resource.IControllerResourceModify;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerNoResponseException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import jakarta.ws.rs.Path;

@Path("controller")
public class ControllerResourceModifyImpl extends JOCResourceImpl implements IControllerResourceModify {

    private static String API_CALL = "./controller/";
    private static final String isUrlPattern = "^https?://[^\\s]+$";
    private static final Predicate<String> isUrl = Pattern.compile(isUrlPattern).asPredicate();

    @Override
    public JOCDefaultResponse postJobschedulerTerminate(String accessToken, byte[] filterBytes) {
        try {
            UrlParameter urlParameter = getUrlParameter(filterBytes, accessToken, "terminate");

            Stream<Boolean> permission = getControllerPermissions(urlParameter.getControllerId(), accessToken).map(p -> p.getTerminate());
            Terminate terminateCommand = new Terminate();
            if (urlParameter.getWithSwitchover() == Boolean.TRUE) {
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

            Stream<Boolean> permission = getControllerPermissions(urlParameter.getControllerId(), accessToken).map(p -> p.getRestart());
            Terminate terminateCommand = new Terminate(true, null);
            if (urlParameter.getWithSwitchover() == Boolean.TRUE) {
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

            Stream<Boolean> permission = getControllerPermissions(urlParameter.getControllerId(), accessToken).map(p -> p.getTerminate());
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

            Stream<Boolean> permission = getControllerPermissions(urlParameter.getControllerId(), accessToken).map(p -> p.getRestart());
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
            Stream<Boolean> permission) throws JsonProcessingException, JocException {
        JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
        if (jocDefaultResponse != null) {
            return jocDefaultResponse;
        }

        List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(urlParameter.getControllerId());
        if (controllerInstances == null || controllerInstances.size() > 1) { // is cluster
            checkRequiredParameter("url", urlParameter.getUrl());
            if (!isUrl.test(urlParameter.getUrl())) {
                throw new JocBadRequestException("$.url: does not match the url pattern " + isUrlPattern);
            }
        } else {
            urlParameter.setUrl(controllerInstances.get(0).getUri());
        }
        storeAuditLog(urlParameter.getAuditLog(), urlParameter.getControllerId(), CategoryType.CONTROLLER);
        
        JOCJsonCommand jocJsonCommand = new JOCJsonCommand(urlParameter.getUrl(), accessToken);
        jocJsonCommand.setUriBuilderForCommands();
        String body = Globals.objectMapper.writeValueAsString(cmd);
        if (request.contains("abort")) {
            try {
                jocJsonCommand.getJsonObjectFromPost(body);
            } catch (ControllerNoResponseException | ControllerConnectionRefusedException | ControllerConnectionResetException e) {
                // JobScheduler sends always no response if "abort" is called
            }
        } else {
            jocJsonCommand.getJsonObjectFromPost(body);
        }
        return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
    }

}
