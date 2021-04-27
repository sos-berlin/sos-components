package com.sos.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OutputPipe implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutputPipe.class);

    private final InputStream in;
    private final PrintStream out;

    OutputPipe(final InputStream is, final PrintStream ps) {
        in = is;
        out = ps;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            for (int n = 0; n != -1; n = in.read(buffer)) {
                out.write(buffer, 0, n);
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}