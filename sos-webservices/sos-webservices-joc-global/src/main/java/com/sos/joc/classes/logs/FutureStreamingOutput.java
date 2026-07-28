package com.sos.joc.classes.logs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;

public class FutureStreamingOutput implements StreamingOutput {

    private final boolean compressed;
    private final CompletableFuture<List<byte[]>> future;
    private final byte[] header;
    private static final Logger LOGGER = LoggerFactory.getLogger(FutureStreamingOutput.class);

    public FutureStreamingOutput(boolean compressed, CompletableFuture<List<byte[]>> future, byte[] header) {
        this.compressed = compressed;
        this.future = future;
        this.header = header;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        try {
            OutputStream out = compressed ? new GZIPOutputStream(output) : output;
            future.thenAccept(list -> {
                writeLine(header, out);
                list.forEach(l -> writeLine(l, out));
            }).exceptionally(t -> onError(t, out)).thenAccept(v -> onComplete(out)).join();
        } catch (Exception e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    private static void writeLine(byte[] logLine, OutputStream out) {
        if (logLine == null) {
            return;
        }
        try {
            out.write(logLine);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Void onError(Throwable t, OutputStream out) {
        close(out);
        throw new RuntimeException(t);
    }

    private static void onComplete(OutputStream out) {
        try {
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            close(out);
        }
    }

    private static void close(OutputStream out) {
        try {
            out.close();
        } catch (IOException e) {
            //
        }
    }

}
