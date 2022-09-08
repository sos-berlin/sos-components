
package com.sos.joc.security.resource;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IOidcResource {

    @POST
    @Path("identityproviders")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postIdentityproviders();
 
}
