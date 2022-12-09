package com.sos.joc.controller.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.controller.resource.IControllerLogResource;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("controller")
public class ControllerLogImpl extends JOCResourceImpl implements IControllerLogResource {

    private static final String LOG_API_CALL = "./controller/log";
    private static final String isUrlPattern = "^https?://[^\\s]+$";
    private static final Predicate<String> isUrl = Pattern.compile(isUrlPattern).asPredicate();

    @Override
    public JOCDefaultResponse getDebugLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(LOG_API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParamSchema = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(urlParamSchema.getControllerId(), accessToken)
                    .getGetLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(urlParamSchema.getControllerId());
            if (controllerInstances.size() > 1) { // is cluster
                checkRequiredParameter("url", urlParamSchema.getUrl());
                if (!isUrl.test(urlParamSchema.getUrl())) {
                    throw new JocBadRequestException("$.url: does not match the url pattern " + isUrlPattern);
                }
            } else {
                urlParamSchema.setUrl(controllerInstances.get(0).getUri());
            }

            // increase timeout for large log files
            int socketTimeout = Math.max(Globals.httpSocketTimeout, 30000);
            JOCJsonCommand jocJsonCommand = new JOCJsonCommand(urlParamSchema.getUrl(), getAccessToken());
            jocJsonCommand.setAutoCloseHttpClient(false);
            jocJsonCommand.setSocketTimeout(socketTimeout);
            jocJsonCommand.setUriBuilderForMainLog(true);

            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(jocJsonCommand.getStreamingOutputFromGet(
                    "text/plain,application/octet-stream", true), "controller.log.gz");
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse getDebugLog(String accessToken, String queryAccessToken, String controllerId, String url) {

        if (accessToken == null) {
            accessToken = queryAccessToken;
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if(controllerId != null) {
            builder.add("controllerId", controllerId);
        }
        if (url != null) {
            builder.add("url", url);
        }
        return getDebugLog(accessToken, builder.build().toString().getBytes(StandardCharsets.UTF_8));
    }

}
