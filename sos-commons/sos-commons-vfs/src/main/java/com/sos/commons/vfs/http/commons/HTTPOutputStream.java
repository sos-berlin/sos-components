package com.sos.commons.vfs.http.commons;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HTTP;

import com.sos.commons.util.SOSClassUtil;

public class HTTPOutputStream extends OutputStream {

    private final PipedOutputStream os;
    private final PipedInputStream is;
    private final HTTPClient client;
    private final HttpPut request;

    public HTTPOutputStream(HTTPClient client, HttpPut request) throws IOException {
        this.os = new PipedOutputStream();
        this.is = new PipedInputStream(os);

        this.client = client;
        this.request = request;

        this.request.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
        this.request.setEntity(new InputStreamEntity(is));
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine sl = response.getStatusLine();
            if (!HTTPClient.isSuccessful(sl)) {
                throw new IOException(HTTPClient.getResponseStatus(request.getURI(), sl));
            }
        } finally {
            SOSClassUtil.closeQuietly(is);
            SOSClassUtil.closeQuietly(os);
            super.close();
        }
    }
}
