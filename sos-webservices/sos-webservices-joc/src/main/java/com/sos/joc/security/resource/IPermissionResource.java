
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IPermissionResource {

    @POST
    @Path("permission")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postPermissionRead(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("permission/rename")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postPermissionRename(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("permissions/store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postPermissionsStore(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("permissions/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postPermissionsDelete(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("permissions")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postPermissions(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

}
