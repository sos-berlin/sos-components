package com.sos.joc.jobscheduler.impl;

import java.net.URI;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerLogResource;
import com.sos.joc.model.jobscheduler.UrlParameter;

@Path("jobscheduler")
public class JobSchedulerLogImpl extends JOCResourceImpl implements IJobSchedulerLogResource {

    private static final String LOG_API_CALL = "./jobscheduler/log";

    public JOCDefaultResponse getLog(String accessToken, UrlParameter urlParamSchema) {
        try {
            JOCDefaultResponse jocDefaultResponse = init(LOG_API_CALL, urlParamSchema, accessToken, urlParamSchema.getJobschedulerId(),
                    getPermissonsJocCockpit(urlParamSchema.getJobschedulerId(), accessToken).getJobschedulerMaster().getView().isMainlog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            checkRequiredParameter("jobschedulerId", urlParamSchema.getJobschedulerId());
            try {
                checkRequiredParameter("url", urlParamSchema.getUrl());
            } catch (JocMissingRequiredParameterException e) {
                if (dbItemInventoryInstance.getIsCluster()) {
                    throw e;
                } else {
                    urlParamSchema.setUrl(URI.create(dbItemInventoryInstance.getUri()));
                }
            }

            // increase timeout for large log files
            int socketTimeout = Math.max(Globals.httpSocketTimeout, 30000);
            JOCJsonCommand jocJsonCommand = new JOCJsonCommand(urlParamSchema.getUrl(), getAccessToken());
            jocJsonCommand.setAutoCloseHttpClient(false);
            jocJsonCommand.setSocketTimeout(socketTimeout);
            jocJsonCommand.setUriBuilderForMainLog(true);
            
            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(jocJsonCommand.getStreamingOutputFromGet("text/plain,application/octet-stream", true), "master.log.gz");
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse getDebugLog(String accessToken, String queryAccessToken, String jobschedulerId, String url) {

        UrlParameter urlParams = new UrlParameter();
        urlParams.setJobschedulerId(jobschedulerId);
        urlParams.setUrl(URI.create(url));

        if (accessToken == null) {
            accessToken = queryAccessToken;
        }

        return getLog(accessToken, urlParams);
    }

    @Override
    public JOCDefaultResponse getDebugLog(String xAccessToken, UrlParameter urlParamSchema) {
        return getLog(xAccessToken, urlParamSchema);
    }

}
