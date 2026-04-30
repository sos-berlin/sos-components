package com.sos.joc.classes.logs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;
import reactor.core.publisher.Flux;

public class FluxStreamingOutput implements StreamingOutput {

    private final boolean compressed;
    private final Flux<byte[]> flux;
    private final byte[] header;
    private static final Logger LOGGER = LoggerFactory.getLogger(FluxStreamingOutput.class);

    public FluxStreamingOutput(boolean compressed, Flux<byte[]> flux, byte[] header) {
        this.compressed = compressed;
        this.flux = flux;
        this.header = header;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        try {
            OutputStream out = compressed ? new GZIPOutputStream(output) : output;
            flux.doOnComplete(() -> onComplete(out))
                .doOnError(t -> onError(t, out))
                .doFirst(() -> writeLine(header, out))
                .doOnNext(l -> writeLine(l, out))
                .blockLast();
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

    private static void onError(Throwable t, OutputStream out) {
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
