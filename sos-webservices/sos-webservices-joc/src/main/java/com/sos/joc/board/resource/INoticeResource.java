
package com.sos.joc.board.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
