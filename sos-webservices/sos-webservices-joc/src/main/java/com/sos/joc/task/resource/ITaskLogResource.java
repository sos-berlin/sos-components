
package com.sos.joc.task.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sos.joc.annotation.CompressedAlready;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.model.job.TaskFilter;

public interface ITaskLogResource {

    @POST
    @Path("log")
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, TaskFilter taskFilter);

    @GET
    @Path("log/html")
    @CompressedAlready
    // @Produces({ MediaType.TEXT_HTML })
    public JOCDefaultResponse getTaskLogHtml(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String queryAccessToken,
            @QueryParam("jobschedulerId") String jobschedulerId, @QueryParam("taskId") Long taskId, @QueryParam("filename") String filename);

    @GET
    @Path("log/download")
    @CompressedAlready
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse downloadTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, @QueryParam("accessToken") String queryAccessToken,
            @QueryParam("jobschedulerId") String jobschedulerId, @QueryParam("taskId") Long taskId, @QueryParam("filename") String filename);

    @POST
    @Path("log/download")
    @CompressedAlready
    @Consumes("application/json")
    // @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public JOCDefaultResponse downloadTaskLog(@HeaderParam("X-Access-Token") String xAccessToken, TaskFilter taskFilter);

    @POST
    @Path("log/info")
    @Consumes("application/json")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getLogInfo(@HeaderParam("X-Access-Token") String xAccessToken, TaskFilter taskFilter);
}
