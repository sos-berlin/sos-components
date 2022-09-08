
package com.sos.joc.cluster.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.cluster.impl.ClusterResourceImpl;

public interface IClusterResource {

    @POST
    @Path(ClusterResourceImpl.IMPL_PATH_RESTART)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse restart(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);

    @POST
    @Path(ClusterResourceImpl.IMPL_PATH_SWITCH_MEMBER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse switchMember(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
    
    @POST
    @Path(ClusterResourceImpl.IMPL_PATH_DELETE_MEMBER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse deleteMember(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes);
}
