
package com.sos.joc.jobtemplates.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

 
public interface IJobTemplatesResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJobTemplates(@HeaderParam("X-Access-Token") String xAccessToken, byte[] jobTemplatesFilter);
}