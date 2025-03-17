package com.sos.commons.vfs.http.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
                synchronized (buffer) {
                    buffer.writeTo(os);
                    os.flush();
                }
            }
        });
        entity.setContentType(ContentType.APPLICATION_OCTET_STREAM.toString());
        request.setEntity(entity);
    }

    @Override
    public void write(int b) throws IOException {
        synchronized (buffer) {
            buffer.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        synchronized (buffer) {
            buffer.write(b, off, len);
        }
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
