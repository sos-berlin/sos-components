package com.sos.js7.job.resolver.generator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;

public class ResolverGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverGenerator.class);

    private static void generate(Path templateFile, Path outputDir, int number) throws Exception {
        if (!Files.exists(templateFile)) {
            throw new Exception(templateFile + " not found");
        }
        SOSPath.deleteIfExists(outputDir);
        Files.createDirectory(outputDir);

        String content = new String(Files.readAllBytes(templateFile));
        for (int i = 1; i <= number; i++) {
            String className = "GeneratedValueResolver" + i;
            String prefix = "p" + i + ":";

            SOSPath.append(outputDir.resolve(className + ".java"), content.replaceAll("\\$CLASSNAME\\$", className).replaceAll("\\$PREFIX\\$",
                    prefix));
            LOGGER.info("[" + i + "][generated]" + outputDir.resolve(className + ".java"));
        }

    }

    public static void main(String[] args) throws Exception {
        Path baseDir = Paths.get("src/test/java/com/sos/js7/job");
        Path templateFile = baseDir.resolve("resolver/generator").resolve("ResolverTemplate.txt");
        Path outputDirFile = baseDir.resolve("examples/resolver/generated");

        generate(templateFile, outputDirFile, 1_000);
    }

}
