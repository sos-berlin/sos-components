package com.sos.joc.tags.resource;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.sos.joc.classes.JOCDefaultResponse;

public interface ITagsModify {

    @POST
    @Path("add")
    @Produces({ "application/json" })
    public JOCDefaultResponse postTagsAdd(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("delete")
    @Produces({ "application/json" })
    public JOCDefaultResponse postTagsDelete(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
    
    @POST
    @Path("ordering")
    @Produces({ "application/json" })
    public JOCDefaultResponse postTagsOrdering(@HeaderParam("X-Access-Token") String xAccessToken, byte[] filterBytes);
}
