
package com.sos.joc.board.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface INoticeResource {

    @POST
    @Path("delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deleteNotice(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("post")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postNotice(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);

}
