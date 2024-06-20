package com.sos.js7.job.generator.resolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;

public class ResolverGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverGenerator.class);

    public static void main(String[] args) throws Exception {
        Path baseDir = Paths.get("src/test/java/com/sos/js7/job");
        Path templateFile = baseDir.resolve("generator/resolver").resolve("ResolverTemplate.txt");
        Path outputDir = baseDir.resolve("examples/resolver/generated");

        generate(templateFile, outputDir, 1_000);
    }

    private static void generate(Path templateFile, Path outputDir, int numberOfArguments) throws Exception {
        if (!Files.exists(templateFile)) {
            throw new Exception(templateFile + " not found");
        }
        SOSPath.deleteIfExists(outputDir);
        Files.createDirectory(outputDir);

        int maxLen = String.valueOf(numberOfArguments).length();
        String content = new String(Files.readAllBytes(templateFile));
        for (int i = 1; i <= numberOfArguments; i++) {
            String formatted = String.format("%0" + maxLen + "d", i);
            String className = "GeneratedValueResolver" + formatted;
            String prefix = "p" + i + ":";

            SOSPath.append(outputDir.resolve(className + ".java"), content.replaceAll("\\$CLASSNAME\\$", className).replaceAll("\\$PREFIX\\$",
                    prefix));
            LOGGER.info("[" + i + "][generated]" + outputDir.resolve(className + ".java"));
        }
    }

}
