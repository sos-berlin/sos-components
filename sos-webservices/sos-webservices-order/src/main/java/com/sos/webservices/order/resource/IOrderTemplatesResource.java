
package com.sos.webservices.order.resource;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.webservices.order.initiator.model.OrderTemplateFilter;

public interface IOrderTemplatesResource {

    @POST
    @Path("list")
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postOrderTemplates(@HeaderParam("X-Access-Token") String accessToken, OrderTemplateFilter orderTemplateFilter);
}
