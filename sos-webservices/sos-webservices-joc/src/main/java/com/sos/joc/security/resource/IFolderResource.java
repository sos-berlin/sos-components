
package com.sos.joc.security.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IFolderResource {

    @POST
    @Path("folder")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFolderRead(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("folders")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFolders(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("folder/rename")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFolderRename(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("folders/store")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFoldersStore(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

    @POST
    @Path("folders/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postFoldersDelete(@HeaderParam("X-Access-Token") String accessToken, byte[] body);

 
        
}
