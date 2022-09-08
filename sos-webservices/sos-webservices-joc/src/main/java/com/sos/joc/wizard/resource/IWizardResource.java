package com.sos.joc.wizard.resource;
 
 
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IWizardResource {
    
    @POST
    @Path("jobs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJobs(@HeaderParam("X-Access-Token") final String accessToken);
    
    @POST
    @Path("job")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJob(@HeaderParam("X-Access-Token") final String accessToken, final byte[] body);

}
