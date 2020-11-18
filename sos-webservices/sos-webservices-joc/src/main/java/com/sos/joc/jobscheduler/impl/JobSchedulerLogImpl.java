package com.sos.joc.jobscheduler.impl;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerLogResource;
import com.sos.joc.model.jobscheduler.UrlParameter;
import com.sos.schema.JsonValidator;

@Path("controller")
public class JobSchedulerLogImpl extends JOCResourceImpl implements IJobSchedulerLogResource {

    private static final String LOG_API_CALL = "./controller/log";

    public JOCDefaultResponse getLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(LOG_API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParamSchema = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(urlParamSchema.getControllerId(), getPermissonsJocCockpit(urlParamSchema
                    .getControllerId(), accessToken).getJS7Controller().getView().isMainlog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("controllerId", urlParamSchema.getControllerId());
            try {
                checkRequiredParameter("url", urlParamSchema.getUrl());
            } catch (JocMissingRequiredParameterException e) {
                List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(urlParamSchema.getControllerId());
                if (controllerInstances.size() > 1) { // is cluster
                    throw e;
                } else {
                    urlParamSchema.setUrl(URI.create(controllerInstances.get(0).getUri()));
                }
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
    public JOCDefaultResponse getDebugLog(String accessToken, String queryAccessToken, String jobschedulerId, String url) {

        if (accessToken == null) {
            accessToken = queryAccessToken;
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if(jobschedulerId != null) {
            builder.add("jobschedulerId", jobschedulerId);
        }
        if (url != null) {
            builder.add("url", url);
        }
        return getLog(accessToken, builder.build().toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public JOCDefaultResponse getDebugLog(String xAccessToken, byte[] filterBytes) {
        return getLog(xAccessToken, filterBytes);
    }

}
