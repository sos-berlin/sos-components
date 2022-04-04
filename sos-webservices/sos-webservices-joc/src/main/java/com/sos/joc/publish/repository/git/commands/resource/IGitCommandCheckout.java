package com.sos.joc.publish.repository.git.commands.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IGitCommandCheckout {

    @POST
    @Path("checkout")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCommandCheckout(@HeaderParam("X-Access-Token") String xAccessToken, byte[] checkoutFilter) throws Exception;
}
