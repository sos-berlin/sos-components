package com.sos.joc.classes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import com.sos.joc.annotation.Compress;

@Provider
@Compress
public class GZIPContentEncodingCompress implements WriterInterceptor {

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

        MultivaluedMap<String, Object> headers = context.getHeaders();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        
        if (!headers.containsKey("X-Uncompressed-Length")) {
            final OutputStream outputStream = context.getOutputStream();
            context.setOutputStream(new GZIPOutputStream(outputStream));
        }

        context.proceed();
    }

}
