package com.sos.joc.classes;

import java.io.IOException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import com.sos.joc.annotation.CompressedAlready;

@Provider
@CompressedAlready
public class GZIPContentEncoding implements WriterInterceptor {
    
    @Context
    private UriInfo uriInfo;
    
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        
        try {
            MultivaluedMap<String, Object> headers = context.getHeaders();
            if (headers.containsKey("X-Uncompressed-Length")) {
                headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
            }  
            context.proceed();
        } catch (Exception e) {
            if (uriInfo != null) {
                System.out.println("-------------------Request-URI--------------------------------------------");
                System.out.println(uriInfo.getRequestUri());
            }
            throw e;
        }
    }

}
