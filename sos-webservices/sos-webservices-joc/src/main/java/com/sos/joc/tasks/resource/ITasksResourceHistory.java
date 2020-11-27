package com.sos.joc.tasks.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

public interface ITasksResourceHistory {

    public static final String PATH = "history";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.TASKS, PATH);

    @POST
    @Path(PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postTasksHistory(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
