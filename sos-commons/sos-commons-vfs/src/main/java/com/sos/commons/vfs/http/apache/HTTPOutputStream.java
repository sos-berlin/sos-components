package com.sos.commons.vfs.http.apache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HTTP;

import com.sos.commons.util.SOSClassUtil;

public class HTTPOutputStream extends OutputStream {

    private final HTTPClient client;
    private final HttpEntityEnclosingRequestBase request;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public HTTPOutputStream(HTTPClient client, URI uri) throws IOException {
        this.client = client;
        this.request = new HttpPut(uri);
        this.request.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);

        EntityTemplate entity = new EntityTemplate(new ContentProducer() {

            @Override
            public void writeTo(OutputStream os) throws IOException {
                // synchronized (buffer) {
                buffer.writeTo(os);
                os.flush();
                // }
            }
        });

        entity.setContentType(ContentType.APPLICATION_OCTET_STREAM.toString());
        request.setEntity(entity);
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
        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine sl = response.getStatusLine();
            if (!HTTPClient.isSuccessful(sl)) {
                throw new IOException(HTTPClient.getResponseStatus(request, response));
            }
        } finally {
            SOSClassUtil.closeQuietly(buffer);
            super.close();
        }
    }

}
