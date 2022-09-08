package com.sos.joc.configuration.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

 
public interface IJocConfigurationResource{

    @POST
    @Path("save")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postSaveConfiguration(@HeaderParam("X-Access-Token") String xAccessToken, byte[] configuration);

    @POST
    @Path("delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postDeleteConfiguration(@HeaderParam("X-Access-Token") String xAccessToken, byte[] configuration);

    @POST
    @Path("share")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postShareConfiguration(@HeaderParam("X-Access-Token") String xAccessToken, byte[] configuration);
    
    @POST
    @Path("make_private")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postMakePrivate(@HeaderParam("X-Access-Token") String xAccessToken, byte[] configuration);

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postReadConfiguration(@HeaderParam("X-Access-Token") String xAccessToken, byte[] configuration);

}
