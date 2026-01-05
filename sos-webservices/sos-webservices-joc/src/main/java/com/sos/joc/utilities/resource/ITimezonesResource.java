package com.sos.joc.utilities.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface ITimezonesResource {

    public static final String PATH = "timezones";
    public static final String IMPL_PATH = WebservicePaths.getResourceImplPath(WebservicePaths.UTILITIES, PATH);

    @Path(PATH)
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postTimezones();
    
    @Path(PATH)
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getTimezones();

}
