
package com.sos.joc.jobtemplate.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

 
public interface IJobTemplateResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJobTemplate(@HeaderParam("X-Access-Token") String xAccessToken, byte[] jobTemplateFilter);
}
