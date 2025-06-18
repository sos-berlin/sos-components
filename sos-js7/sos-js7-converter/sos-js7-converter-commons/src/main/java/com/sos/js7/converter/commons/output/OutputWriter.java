package com.sos.js7.converter.commons.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.commons.JS7ExportObject;
import com.sos.js7.converter.commons.JS7ExportObjects;

public class OutputWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutputWriter.class);

    public static final ObjectMapper OM = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false).configure(
                    SerializationFeature.INDENT_OUTPUT, true);

    public static <T> List<JS7ExportObject<T>> write(Path directory, JS7ExportObjects<T> objects) throws IOException {
        String method = "write";
        if (objects == null) {
            LOGGER.info(String.format("[%s][skip]0 items", method));
            return List.of();
        }
        return write(directory, objects.getItemsToGenerate());
    }

    public static <T> List<JS7ExportObject<T>> write(Path directory, List<JS7ExportObject<T>> items) throws IOException {
        String method = "write";
        if (items == null) {
            LOGGER.info(String.format("[%s][skip]0 items", method));
            return List.of();
        }
        int size = items.size();
        if (size == 0) {
            LOGGER.info(String.format("[%s][skip]0 items", method));
            return List.of();
        }

        // directory = directory.toAbsolutePath();
        // LOGGER.info(String.format("[%s][directory=%s][start]%s items", method, directory, objects.getItems().size()));
        LOGGER.info(String.format("[%s][directory=%s]%s items", method, directory, size));
        for (JS7ExportObject<T> item : items) {
            Path outputPath = directory.resolve(item.getUniquePath().getPath());
            Path parent = outputPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            // LOGGER.info(String.format("[%s]file=%s", method, outputPath));
            OM.writeValue(SOSPath.toFile(outputPath), item.getObject());
        }
        return items;
        // LOGGER.info(String.format("[%s][directory=%s][end]", method, directory));
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
