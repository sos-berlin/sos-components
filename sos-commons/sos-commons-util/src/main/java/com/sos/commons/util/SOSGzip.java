package com.sos.commons.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSGzip {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSGzip.class);

    public static byte[] compress(Path source) throws Exception {
        if (source == null) {
            throw new Exception("missing path");
        }
        if (SOSPath.toFile(source).isFile()) {
            return compressFile(source);
        }
        return compressDirectory(source);
    }

    private static byte[] compressFile(Path source) throws Exception {
        // TODO use commons.compress implementation
        // merge into compressDirectory
        byte[] uncompressedData = Files.readAllBytes(source);
        byte[] result = new byte[] {};
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressedData.length); GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {
            gzipOS.write(uncompressedData);
            gzipOS.close();
            result = bos.toByteArray();
        } catch (IOException e) {
            throw e;
        }
        return result;
    }

    private static byte[] compressDirectory(Path source) throws Exception {
        if (!SOSPath.toFile(source).isDirectory()) {
            throw new Exception(String.format("[%s]is not a directory", source));
        }
        // try (FileOutputStream fos = new FileOutputStream(outputFile); BufferedOutputStream bos = new BufferedOutputStream(fos);
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); BufferedOutputStream bos = new BufferedOutputStream(baos);
                GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(bos); TarArchiveOutputStream tos = new TarArchiveOutputStream(gcos,
                        "UTF-8")) {

            tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    if (attributes.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    }
                    Path targetFile = source.relativize(file);
                    try {
                        TarArchiveEntry entry = new TarArchiveEntry(file.toFile(), targetFile.toString());
                        entry.setSize(Files.size(file));
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[gzip][file]%s", entry.getName()));
                        }
                        tos.putArchiveEntry(entry);
                        Files.copy(file, tos);
                        tos.closeArchiveEntry();
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[gzip][visitFile][%s]%s", file, e.toString()), e);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    LOGGER.error(String.format("[gzip][visitFileFailed][%s]%s", file, e.toString()), e);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) {
                    try {
                        Path targetDir = source.relativize(dir);
                        if (targetDir.toString().trim().length() > 0 && dir.toFile().list().length == 0) {
                            try {
                                TarArchiveEntry entry = new TarArchiveEntry(targetDir.toString() + "/");
                                entry.setSize(0);
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("[gzip][dir]%s", entry.getName()));
                                }
                                tos.putArchiveEntry(entry);
                                tos.closeArchiveEntry();
                            } catch (Throwable e) {
                                LOGGER.error(String.format("[gzip][preVisitDirectory][%s]%s", dir, e.toString()), e);
                            }
                        }
                    } catch (Throwable ex) {
                        LOGGER.error(ex.toString(), ex);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            tos.close();
            return baos.toByteArray();
        }
    }

    /** Unpack a tar.gz file to the given directory
     * 
     * @param source tar.gz file
     * @param targetDir
     * @throws IOException */
    public static void decompress(Path source, Path targetDir) throws Exception {
        if (source == null || !SOSPath.toFile(source).isFile()) {
            throw new Exception(String.format("[%s]is not a file", source));
        }
        decompress(Files.newInputStream(source), targetDir);
    }

    /** Unpack the tar.gz bytes to the given directory
     * 
     * @param source tar.gz bytes
     * @param targetDir
     * @throws IOException */
    public static void decompress(byte[] source, Path targetDir) throws Exception {
        decompress(new ByteArrayInputStream(source), targetDir);
    }

    /** Unpack a tar.gz input stream to the given directory
     * 
     * @param source source tar.gz input stream
     * @param targetDir
     * @throws IOException */
    public static void decompress(InputStream source, Path targetDir) throws Exception {
        InputStream inputStream = null;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        try {
            inputStream = new BufferedInputStream(new GZIPInputStream(new BufferedInputStream(source)));

            try (TarArchiveInputStream tis = new TarArchiveInputStream(inputStream)) {
                for (TarArchiveEntry entry = tis.getNextTarEntry(); entry != null;) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[ungzip][entry]%s", entry.getName()));
                    }
                    Path outputPath = targetDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(outputPath);
                    } else {
                        Path parent = outputPath.getParent();
                        if (parent != null) {
                            if (Files.notExists(parent)) {
                                Files.createDirectories(parent);
                            }
                        }
                        try (OutputStream out = Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                            IOUtils.copy(tis, out);
                        }
                        if (entry.getLastModifiedDate() != null) {
                            try {
                                Files.setLastModifiedTime(outputPath, FileTime.fromMillis(entry.getLastModifiedDate().getTime()));
                            } catch (Throwable e) {
                                LOGGER.warn(String.format("[ungzip][setLastModifiedTime][%s=%s]%s", entry.getName(), entry.getLastModifiedDate(), e
                                        .toString()), e);
                            }
                        }
                    }
                    entry = tis.getNextTarEntry();
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.toString(), e);
                }
            }
            if (source != null) {
                try {
                    source.close();
                } catch (Throwable e) {
                }
            }
        }
    }
}
