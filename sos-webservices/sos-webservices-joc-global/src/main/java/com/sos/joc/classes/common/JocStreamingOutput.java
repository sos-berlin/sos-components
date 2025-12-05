package com.sos.joc.classes.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import com.sos.joc.exceptions.JocException;

import jakarta.ws.rs.core.StreamingOutput;

public class JocStreamingOutput implements StreamingOutput {

    private final boolean withGzipEncoding;
    private final byte[] content;


    public JocStreamingOutput(boolean withGzipEncoding, byte[] content) throws JocException, IOException {
        this.withGzipEncoding = withGzipEncoding;
        this.content = content;
    }

    @Override
    public void write(OutputStream output) throws IOException {
        try {
            if (withGzipEncoding) {
                output = new GZIPOutputStream(output);
            }
            InputStream in = new ByteArrayInputStream(content);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
        } finally {
            try {
                output.close();
            } catch (Exception e) {}
        }
    }
    
}
