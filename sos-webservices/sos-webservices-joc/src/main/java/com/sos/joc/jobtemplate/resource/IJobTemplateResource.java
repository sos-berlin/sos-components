
package com.sos.joc.jobtemplate.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

 
public interface IJobTemplateResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJobTemplate(@HeaderParam("X-Access-Token") String xAccessToken, byte[] jobTemplateFilter);
    
    @Path("state")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJobTemplateState(@HeaderParam("X-Access-Token") String xAccessToken, byte[] jobTemplateFilter);
}
