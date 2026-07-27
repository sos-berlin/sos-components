package com.sos.joc.bean;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("bean")
public class Register extends JOCResourceImpl {

    @POST
    @Path("register")
    public JOCDefaultResponse register() {
        try {
            JOCMBeanServer.registerOrThrow();
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @POST
    @Path("unregister")
    public JOCDefaultResponse unregister(String accept) {
        try {
            JOCMBeanServer.unregisterOrThrow();
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
