
package com.sos.joc.board.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface INoticesModify {

    @POST
    @Path("delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deleteNotices(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
    @POST
    @Path("post")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postNotices(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
    @POST
    @Path("post/expected")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postExpectedNotices(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);

}
