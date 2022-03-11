
package com.sos.joc.security.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
