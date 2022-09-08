
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IRoleResource {

    @POST
    @Path("role")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRoleRead(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("role/rename")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRoleRename(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("role/store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRoleStore(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("roles/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRolesDelete(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("roles/reorder")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRolesReorder(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("roles")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postRoles(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

}
