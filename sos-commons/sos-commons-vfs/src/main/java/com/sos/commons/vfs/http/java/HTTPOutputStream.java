package com.sos.commons.vfs.http.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpRequest;

import org.apache.http.entity.EntityTemplate;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.vfs.http.commons.HTTPUtils;
import com.sos.commons.vfs.http.java.HTTPClient.ExecuteResult;

public class HTTPOutputStream extends OutputStream {

    private final HTTPClient client;
    private final HttpRequest request;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public HTTPOutputStream(HTTPClient client, URI uri) throws IOException {
        this.client = client;

        HttpRequest.Builder builder = client.createRequestBuilder(uri);
        builder.header(HTTPClient.HEADER_EXPECT, HTTPClient.HEADER_EXPECT_VALUE);

        this.request = builder.PUT(HttpRequest.BodyPublishers.noBody()).build();
    }

    /** Handling large files – instead of the HTTPOutputStream class, an additional class should be implemented/used for HTTP upload file operations
     * 
     * @param is e.g. InputStream is = new FileInputStream(file)
     * @return */
    @SuppressWarnings("unused")
    private EntityTemplate chunckedFromInputStream(InputStream is) {
        EntityTemplate entity = new EntityTemplate(os -> {
            byte[] buffer = new byte[8 * 1024 * 1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        });
        return entity;
    }

    @Override
    public void write(int b) throws IOException {
        // synchronized (buffer) {
        buffer.write(b);
        // }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        // synchronized (buffer) {
        buffer.write(b, off, len);
        // }
    }

    @Override
    public void close() throws IOException {
        try {
            ExecuteResult result = client.execute(request);
            if (!HTTPUtils.isSuccessful(result.response().statusCode())) {
                throw new IOException(HTTPClient.getResponseStatus(result));
            }
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            SOSClassUtil.closeQuietly(buffer);
            super.close();
        }
    }

}
