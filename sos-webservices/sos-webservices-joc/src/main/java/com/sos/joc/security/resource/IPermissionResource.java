
package com.sos.joc.security.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
