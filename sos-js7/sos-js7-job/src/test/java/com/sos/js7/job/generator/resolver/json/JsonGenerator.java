package com.sos.js7.job.generator.resolver.json;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;

public class JsonGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonGenerator.class);

    public static void main(String[] args) throws Exception {
        Path baseDir = Paths.get("src/test/java/com/sos/js7/job");
        Path currentDir = baseDir.resolve("generator/json");
        Path outputDir = currentDir.resolve("output");

        boolean generateWorkflow = true;
        if (generateWorkflow) {
            generateWorkflowWithMaps(outputDir, "agent_name", "com.sos.jitl.jobs.examples.EmptyJob", 100, 10, "p");
        } else {
            generateJobResource(outputDir, 1000, "p");
        }
    }

    private static void generateJobResource(Path outputDir, int numberOfArguments, String prefix) throws Exception {
        Path outputPath = outputDir.resolve("jr_generated.json");
        if (!createDirectoryIfNotExists(outputDir)) {
            SOSPath.deleteIfExists(outputPath);
        }

        int maxLen = String.valueOf(numberOfArguments).length();
        JsonObjectBuilder args = Json.createObjectBuilder();
        for (int i = 1; i <= numberOfArguments; i++) {
            String formatted = String.format("%0" + maxLen + "d", i);
            String value = "\"" + getArgumentValuePrefix(prefix, i) + "val_" + formatted + "\"";
            args.add("generated_arg_" + formatted, value);
        }
        JsonObjectBuilder b = Json.createObjectBuilder();
        b.add("arguments", args);

        writeToFile(outputPath, b);
        LOGGER.info("[generateJobResource][created]" + outputPath);
    }

    private static void generateWorkflowWithMaps(Path outputDir, String agentName, String className, int numberOfMaps, int numberOfMapArguments,
            String prefix) throws Exception {
        Path outputPath = outputDir.resolve("workflow_generated_with_maps.json");
        if (!createDirectoryIfNotExists(outputDir)) {
            SOSPath.deleteIfExists(outputPath);
        }

        int maxLenMaps = String.valueOf(numberOfMaps).length();
        int maxLenMapArguments = String.valueOf(numberOfMapArguments).length();

        // ORDER PREPARATION
        JsonObjectBuilder ps = Json.createObjectBuilder();
        for (int i = 1; i <= numberOfMaps; i++) {
            JsonObjectBuilder lp = Json.createObjectBuilder();
            for (int j = 1; j <= numberOfMapArguments; j++) {
                String formatted = String.format("%0" + maxLenMapArguments + "d", j);
                String value = getArgumentValuePrefix(prefix, j) + "val_" + formatted;

                JsonObjectBuilder ac = Json.createObjectBuilder();
                ac.add("type", "String");
                ac.add("default", value);
                lp.add(formatted, ac);
            }
            JsonObjectBuilder p = Json.createObjectBuilder();
            p.add("type", "Map");
            p.add("listParameters", lp);
            ps.add(String.format("%0" + maxLenMaps + "d", i), p);
        }
        JsonObjectBuilder op = Json.createObjectBuilder();
        op.add("parameters", ps);
        op.add("allowUndeclared", false);

        // INSTRUCTIONS
        String jobName = "job";
        JsonObjectBuilder ino = Json.createObjectBuilder();
        ino.add("TYPE", "Execute.Named");
        ino.add("jobName", jobName);
        ino.add("label", jobName);
        JsonArrayBuilder in = Json.createArrayBuilder();
        in.add(ino);

        // JOBS
        JsonObjectBuilder ex = Json.createObjectBuilder();
        ex.add("TYPE", "InternalExecutable");
        ex.add("className", className);
        ex.add("internalType", "JITL");
        JsonObjectBuilder j = Json.createObjectBuilder();
        j.add("agentName", agentName);
        j.add("withSubagentClusterIdExpr", false);
        j.add("skipIfNoAdmissionForOrderDay", false);
        j.add("parallelism", 1);
        j.add("graceTimeout", 1);
        j.add("failOnErrWritten", 1);
        j.add("warnOnErrWritten", false);
        j.add("executable", ex);
        JsonObjectBuilder js = Json.createObjectBuilder();
        js.add(jobName, j);

        // WORKFLOW
        JsonObjectBuilder b = Json.createObjectBuilder();
        b.add("timeZone", "Europe/Berlin");
        b.add("title", "Workflow with " + numberOfMaps + " OrderPreparation MAP parameters");
        b.add("orderPreparation", op);
        b.add("instructions", in);
        b.add("jobs", js);

        writeToFile(outputPath, b);
        LOGGER.info("[generateWorkflowWithMaps][created]" + outputPath);
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
        LOGGER.info(SOSPath.readFile(outputFile));
    }

    private static JsonWriterFactory createWriterFactory() {
        Map<String, Boolean> config = new HashMap<>();
        config.put(javax.json.stream.JsonGenerator.PRETTY_PRINTING, true);
        return Json.createWriterFactory(config);
    }

    private static boolean createDirectoryIfNotExists(Path dir) throws Exception {
        if (!Files.exists(dir)) {
            Files.createDirectory(dir);
            return true;
        }
        return false;
    }

}
