package com.sos.js7.converter.commons.output;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
        } catch (Throwable e) {
            LOGGER.error("[zip][compress][" + zipFile + "]" + e.toString(), e);
        }
    }

    public static void decompress(Path zipFile, Path outputDir) {
        try {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    Path path = normalizePath(zipEntry, outputDir);
                    // boolean isDirectory = false;
                    // if (zipEntry.getName().endsWith(File.separator)) {
                    // isDirectory = true;
                    // }
                    // if (isDirectory) {
                    if (zipEntry.isDirectory()) {
                        Files.createDirectories(path);
                    } else {
                        if (path.getParent() != null) {
                            if (Files.notExists(path.getParent())) {
                                Files.createDirectories(path.getParent());
                            }
                        }
                        Files.copy(zis, path, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
        } catch (Throwable e) {
            LOGGER.error("[zip][decompress][" + zipFile + "]" + e.toString(), e);
        }
    }

    public static Path normalizePath(ZipEntry zipEntry, Path outputDir) throws IOException {
        Path path = outputDir.resolve(zipEntry.getName()).normalize();
        if (!path.startsWith(outputDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }
        return path;
    }

}
