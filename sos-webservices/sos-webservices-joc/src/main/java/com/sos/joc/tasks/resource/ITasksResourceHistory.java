package com.sos.joc.tasks.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
