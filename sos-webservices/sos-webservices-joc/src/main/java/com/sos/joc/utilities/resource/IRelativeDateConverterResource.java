
package com.sos.joc.utilities.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
   
public interface IRelativeDateConverterResource {

    @POST
    @Path("convert_relative_dates")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postConvertRelativeDates(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes)  ;
}
