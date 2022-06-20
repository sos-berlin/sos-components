package com.sos.js7.converter.commons.output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipCompress {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipCompress.class);

    public static void compress(Path sourceDir, Path zipFile) {
        try {
            ZipOutputStream os = new ZipOutputStream(new FileOutputStream(zipFile.toFile()));
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = sourceDir.relativize(file);
                        os.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        os.write(bytes, 0, bytes.length);
                        os.closeEntry();
                    } catch (IOException e) {
                        LOGGER.error("[zip][" + zipFile + "]" + e.toString(), e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            os.close();
        } catch (IOException e) {
            LOGGER.error("[zip][" + zipFile + "]" + e.toString(), e);
        }
    }
}
