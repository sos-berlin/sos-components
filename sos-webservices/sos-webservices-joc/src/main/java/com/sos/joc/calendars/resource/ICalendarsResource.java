
package com.sos.joc.calendars.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

 
public interface ICalendarsResource {

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCalendars(@HeaderParam("X-Access-Token") String xAccessToken, byte[] calendarsFilter);
    
//    @POST
//    @Path("used")
//    @Produces({ MediaType.APPLICATION_JSON })
//    public JOCDefaultResponse postUsedBy(@HeaderParam("X-Access-Token") String xAccessToken, byte[] calendarsFilter);
}
