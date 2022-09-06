
package com.sos.joc.security.resource;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOidcResource {

    @POST
    @Path("identityproviders")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityproviders();
 
}
