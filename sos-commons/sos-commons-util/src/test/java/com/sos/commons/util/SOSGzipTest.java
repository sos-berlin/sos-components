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

import com.sos.commons.util.SOSGzip.SOSGzipResult;

public class SOSGzipTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSGzipTest.class);

    @Ignore
    @Test
    public void testGzipDirectory() throws IOException {
        Path dir2gzip = Paths.get("./src/test/java");
        Path gzipTargetDir = Paths.get("./src/test/resources/gzip");

        Path decompressDir = gzipTargetDir.resolve("decompress");
        if (Files.exists(decompressDir)) {
            SOSPath.cleanupDirectory(decompressDir);
        } else {
            Files.createDirectories(decompressDir);
        }
        Path targetTarGz = gzipTargetDir.resolve("test.tar.gz");

        SOSGzipResult r;
        try {

            LOGGER.info("[compress]start");
            r = SOSGzip.compress(dir2gzip, false);
            LOGGER.info("[compress][end]" + r.toString());

            LOGGER.info("[writeResult]start");
            Files.write(targetTarGz, r.getCompressed(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            LOGGER.info("[writeResult]end");

            LOGGER.info("[decompress]start");
            r = SOSGzip.decompress(targetTarGz, decompressDir, true);
            LOGGER.info("[decompress][end]" + r.toString());

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

}
