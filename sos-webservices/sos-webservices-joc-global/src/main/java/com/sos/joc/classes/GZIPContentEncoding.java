package com.sos.joc.classes;

import java.io.IOException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import com.sos.joc.annotation.CompressedAlready;

@Provider
@CompressedAlready
public class GZIPContentEncoding implements WriterInterceptor {
    
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        
        MultivaluedMap<String, Object> headers = context.getHeaders();
        if (headers.containsKey("X-Uncompressed-Length")) {
            headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        }  
        context.proceed();
    }

}
