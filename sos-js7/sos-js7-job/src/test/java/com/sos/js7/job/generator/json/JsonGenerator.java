package com.sos.js7.job.generator.json;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;

public class JsonGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonGenerator.class);

    private static void generateJobResource(Path outputDir, int numberOfArguments, String prefix) throws Exception {
        SOSPath.deleteIfExists(outputDir);
        Files.createDirectory(outputDir);

        int d = String.valueOf(numberOfArguments).length();

        JsonObjectBuilder args = Json.createObjectBuilder();
        for (int i = 1; i <= numberOfArguments; i++) {
            String si = String.format("%0" + d + "d", i);
            String val = "\"" + getArgumentValuePrefix(prefix, i) + "val_" + si + "\"";
            args.add("generated_arg_" + si, val);
        }
        JsonObjectBuilder b = Json.createObjectBuilder();
        b.add("arguments", args);

        Path outPath = outputDir.resolve("jr_generated_" + numberOfArguments + ".json");
        writeToFile(outPath, b);
        LOGGER.info("[generateJobResource][created]" + outPath);
    }

    private static String getArgumentValuePrefix(String prefix, int currentNumber) {
        if (prefix == null) {
            return "";
        }
        if (prefix.endsWith(":")) {
            return prefix;
        }
        // generated resolvers p1 - p1000
        return prefix + currentNumber + ":";
    }

    private static void writeToFile(Path outputFile, JsonObjectBuilder b) throws IOException {
        JsonObject o = b.build();

        JsonWriterFactory f = createWriterFactory();
        try (FileWriter writer = new FileWriter(outputFile.toFile())) {
            f.createWriter(writer).write(o);
        }
    }

    private static JsonWriterFactory createWriterFactory() {
        Map<String, Boolean> config = new HashMap<>();
        config.put(javax.json.stream.JsonGenerator.PRETTY_PRINTING, true);
        return Json.createWriterFactory(config);

    }

    public static void main(String[] args) throws Exception {
        Path baseDir = Paths.get("src/test/java/com/sos/js7/job");
        Path currentDir = baseDir.resolve("generator/json");
        Path outputDir = currentDir.resolve("output");

        generateJobResource(outputDir, 1000, "p");
    }
}
