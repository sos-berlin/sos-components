package com.sos.commons.httpclient.commons.mulitpart;

import java.io.Closeable;
import java.io.IOException;

public class HttpFormDataCloseable extends HttpFormData implements Closeable {

    /**
     * Closes all {@link AutoCloseable} {@link FormData}s.
     * @throws IOException Contains a message with all {@link IOException}s that might have occurred.
     */
    public void close() throws IOException {

        var closeFailed = false;
        var closeExceptions = new StringBuilder("HttpFormData close exceptions - ");
        for (var part : getParts()) {
            if (part instanceof AutoCloseable partA) {
                try {
                    partA.close();
                } catch (Exception e) {
                    if (closeFailed) {
                        closeExceptions.append("\n");
                    } else {
                        closeFailed = true;
                    }
                    closeExceptions.append(e.toString());
                }
            }
        }
        if (closeFailed) {
            throw new IOException(closeExceptions.toString());
        }
    }
}
