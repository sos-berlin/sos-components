
package com.sos.joc.security.resource;

import com.sos.joc.classes.JOCDefaultResponse;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public interface ILockerResource {

    @POST
    @Path("locker/get")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postLockerGet(byte[] body);

    @POST
    @Path("locker/put")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postLockerPut(byte[] body);

    @POST
    @Path("locker/renew")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postLockerRenew(byte[] body);

    @POST
    @Path("locker/remove")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postLockerRemove(byte[] body);

 
}
