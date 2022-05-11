package com.sos.js7.converter.commons.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.commons.JS7ExportObjects;

public class OutputWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutputWriter.class);

    public static final ObjectMapper OM = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false).configure(
                    SerializationFeature.INDENT_OUTPUT, true);

    public static <T> void write(Path directory, JS7ExportObjects<T> objects) throws IOException {
        String method = "write";
        if (objects == null || objects.getItems().size() == 0) {
            LOGGER.info(String.format("[%s][skip]missing objects", method));
            return;
        }
        // directory = directory.toAbsolutePath();
        LOGGER.info(String.format("[%s][directory=%s][start]%s objects", method, directory, objects.getItems().size()));
        for (JS7ExportObjects<T>.JS7ExportObject item : objects.getItems()) {
            Path outputPath = directory.resolve(item.getUniquePath().getPath());
            Path parent = outputPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            LOGGER.info(String.format("[%s]file=%s", method, outputPath));
            OM.writeValue(SOSPath.toFile(outputPath), item.getObject());
        }
        LOGGER.info(String.format("[%s][directory=%s][end]", method, directory));
    }

    public static void prepareDirectory(Path directory) throws IOException {
        Path dir = directory.toAbsolutePath();
        if (Files.exists(dir)) {
            LOGGER.info(String.format("[checkDirectory][cleanup]%s", dir));
            SOSPath.cleanupDirectory(dir);
        } else {
            LOGGER.info(String.format("[checkDirectory][create]%s", dir));
            Files.createDirectories(dir);
        }
    }
}
