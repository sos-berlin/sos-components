package com.sos.joc.note.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface INote {

    @POST
    //@Path("")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse read(@HeaderParam("X-Access-Token") String accessToken, byte[] body);
    
    @POST
    @Path("delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse delete(@HeaderParam("X-Access-Token") String accessToken, byte[] body);
    
    @POST
    @Path("preferences")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse setPreferences(@HeaderParam("X-Access-Token") String accessToken, byte[] body);
}
