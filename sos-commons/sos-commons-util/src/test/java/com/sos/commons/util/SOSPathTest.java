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

public class SOSPathTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPathTest.class);

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
            LOGGER.info("[gzipDirectory]start");
            byte[] r = SOSPath.gzipDirectory(dir2gzip);
            LOGGER.info("[gzipDirectory]end");

            LOGGER.info("[writeResult]start");
            Files.write(targetTarGz, r, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            LOGGER.info("[writeResult]end");

            LOGGER.info("[unpackGzip]start");
            SOSPath.unpackGzip(targetTarGz, unpackDir.resolve("tmp"));
            LOGGER.info("[unpackGzip]end");

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

}
