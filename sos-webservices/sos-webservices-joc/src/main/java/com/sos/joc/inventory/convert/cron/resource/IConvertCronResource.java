package com.sos.joc.inventory.convert.cron.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.sos.joc.classes.JOCDefaultResponse;

public interface IConvertCronResource {

    @Path("convert/cron")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON) 
    public JOCDefaultResponse postConvertCron(
            @HeaderParam("X-Access-Token") String xAccessToken, 
            @FormDataParam("file") FormDataBodyPart body,
            @FormDataParam("folder") String targetFolder,
            @FormDataParam("calendarName") String calendarName,
            @FormDataParam("agentName") String agentName,
            @FormDataParam("subagentClusterId") String subagentClusterId,
            @FormDataParam("systemCrontab") Boolean systemCrontab,
            @FormDataParam("timeSpent") String timeSpent,
            @FormDataParam("ticketLink") String ticketLink,
            @FormDataParam("comment") String comment) throws Exception;
}
