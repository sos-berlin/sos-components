
package com.sos.joc.tags.resource;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

public interface ITagging {
    
    public static final String PATH_TAGS = "workflow/tags";
    public static final String PATH_TAGGING = PATH_TAGS + "/store";
    public static final String FOLDER_TAGGING = PATH_TAGGING + "/folder";
    public static final String IMPL_PATH_TAGS = JocInventory.getResourceImplPath(PATH_TAGS);
    public static final String IMPL_PATH_TAGGING = JocInventory.getResourceImplPath(PATH_TAGGING);
    public static final String IMPL_FOLDER_TAGGING = JocInventory.getResourceImplPath(FOLDER_TAGGING);
    
    @POST
    @Path(PATH_TAGGING)
    @Produces({ "application/json" })
    public JOCDefaultResponse postTagging(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path(FOLDER_TAGGING)
    @Produces({ "application/json" })
    public JOCDefaultResponse postFolderTagging(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path(PATH_TAGS)
    @Produces({ "application/json" })
    public JOCDefaultResponse postUsed(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
}
