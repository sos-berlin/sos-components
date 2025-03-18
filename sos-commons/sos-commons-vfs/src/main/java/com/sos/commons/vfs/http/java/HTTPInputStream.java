package com.sos.commons.vfs.http.java;

import java.io.FilterInputStream;
import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;

import com.sos.commons.util.SOSClassUtil;

public class HTTPInputStream extends FilterInputStream {

    private CloseableHttpResponse response;

    public HTTPInputStream(final CloseableHttpResponse response) throws IOException {
        super(response.getEntity().getContent());
        this.response = response;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            SOSClassUtil.closeQuietly(response);
        }
    }

}
