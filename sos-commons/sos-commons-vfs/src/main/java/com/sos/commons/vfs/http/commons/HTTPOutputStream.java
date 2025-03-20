package com.sos.commons.vfs.http.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpRequest;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.vfs.http.commons.HTTPClient.ExecuteResult;

public class HTTPOutputStream extends OutputStream {

    private final HTTPClient client;
    private final URI uri;
    private final boolean isWebDAV;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public HTTPOutputStream(HTTPClient client, URI uri, boolean isWebDAV) throws IOException {
        this.client = client;
        this.uri = uri;
        this.isWebDAV = isWebDAV;
    }

    @Override
    public void write(int b) throws IOException {
        buffer.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        buffer.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        try {
            byte[] bytes = buffer.toByteArray();
            HttpRequest.Builder builder = client.createRequestBuilder(uri)
                    // Expect: 100-continue
                    .expectContinue(true);

            // Content-Type
            builder.header(HTTPUtils.HEADER_CONTENT_TYPE, HTTPUtils.HEADER_CONTENT_TYPE_BINARY);
            HTTPClient.withWebDAVOverwrite(builder, isWebDAV);

            if (!client.isChunkedTransfer()) {
                // Note: Not works - throws an Exception ...
                // set the HEADER_CONTENT_LENGTH to avoid Chunked transfer
                builder.header(HTTPUtils.HEADER_CONTENT_LENGTH, String.valueOf(bytes.length));
            }

            HttpRequest request = builder.PUT(HttpRequest.BodyPublishers.ofByteArray(bytes)).build();
            ExecuteResult<Void> result = client.executeWithoutResponseBody(request);
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
