package com.sos.commons.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSGzipTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSGzipTest.class);

    @Ignore
    @Test
    public void testGzipDirectory() throws IOException {
        Path dir2gzip = Paths.get("./src/test/resources");
        Path unpackDir = Paths.get("./src/test/resources/unpack");
        if (!Files.exists(unpackDir)) {
            Files.createDirectories(unpackDir);
        }
        Path targetTarGz = unpackDir.resolve("test.tar.gz");

        try {
            LOGGER.info("[compress]start");
            byte[] r = SOSGzip.compress(dir2gzip);
            LOGGER.info("[compress]end");

            LOGGER.info("[writeResult]start");
            Files.write(targetTarGz, r, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            LOGGER.info("[writeResult]end");

            LOGGER.info("[decompress]start");
            SOSGzip.decompress(targetTarGz, unpackDir.resolve("tmp"));
            LOGGER.info("[decompress]end");

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

}
