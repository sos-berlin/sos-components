package com.sos.commons.httpclient.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpRequest;

import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.http.HttpUtils;

public class HttpOutputStream extends OutputStream {

    private final BaseHttpClient client;
    private final URI uri;
    private final boolean isWebDAV;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public HttpOutputStream(BaseHttpClient client, URI uri, boolean isWebDAV) throws IOException {
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
    public void flush() throws IOException {
        buffer.flush();
    }

    @Override
    public void close() throws IOException {
        IOException exception = null;
        try {
            byte[] bytes = buffer.toByteArray();
            HttpRequest.Builder builder = client.createRequestBuilder(uri)
                    // Expect: 100-continue
                    .expectContinue(true);

            // Content-Type
            builder.header(HttpUtils.HEADER_CONTENT_TYPE, HttpUtils.HEADER_CONTENT_TYPE_BINARY);
            BaseHttpClient.withWebDAVOverwrite(builder, isWebDAV);

            if (!client.isChunkedTransfer()) {
                // Note: Not works - throws an Exception ...
                // set the HEADER_CONTENT_LENGTH to avoid Chunked transfer
                builder.header(HttpUtils.HEADER_CONTENT_LENGTH, String.valueOf(bytes.length));
            }

            HttpRequest request = builder.PUT(HttpRequest.BodyPublishers.ofByteArray(bytes)).build();
            HttpExecutionResult<Void> result = client.executeNoResponseBody(request);
            if (!HttpUtils.isSuccessful(result.response().statusCode())) {
                throw new IOException(BaseHttpClient.formatExecutionResult(result));
            }
        } catch (IOException e) {
            exception = e;
        } catch (Exception e) {
            exception = new IOException(e);
        } finally {
            // 1) close buffer
            try {
                SOSClassUtil.close(buffer);
            } catch (IOException ex) {
                exception = SOSException.mergeException(exception, ex);
            }
            // 2) super.close() is called for completeness.
            // OutputStream.close() is a no-op today, but subclasses may override it.
            try {
                super.close();
            } catch (IOException ex) {
                exception = SOSException.mergeException(exception, ex);
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

}
