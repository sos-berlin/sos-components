package com.sos.commons.httpclient.azure.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sos.commons.httpclient.azure.AzureBlobStorageClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.http.HttpUtils;

public class AzureBlobStorageOutputStream extends OutputStream {

    private final AzureBlobStorageClient client;
    private final String containerName;
    private final String blobPath;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public AzureBlobStorageOutputStream(AzureBlobStorageClient client, String containerName, String blobPath) throws IOException {
        this.client = client;
        this.containerName = containerName;
        this.blobPath = blobPath;
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
        try {
            HttpExecutionResult<String> result = client.executePUTBlob(containerName, blobPath, buffer.toByteArray(),
                    HttpUtils.HEADER_CONTENT_TYPE_BINARY);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                throw new Exception(AzureBlobStorageClient.formatExecutionResult(result));
            }
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            SOSClassUtil.closeQuietly(buffer);
            super.close();
        }
    }

}
