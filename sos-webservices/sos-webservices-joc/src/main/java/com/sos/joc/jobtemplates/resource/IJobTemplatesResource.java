
package com.sos.joc.jobtemplates.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

 
public interface IJobTemplatesResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJobTemplates(@HeaderParam("X-Access-Token") String xAccessToken, byte[] jobTemplatesFilter);
}
