package com.sos.commons.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    private static final int BUFFER_SIZE = 8_192; // 8*1_024

    /** compress file or directory to a byte array<br/>
     * Note: Memory usage
     * 
     * @param source
     * @param setAllFileAttributes
     * @return
     * @throws Exception */
    public static SOSGzipResult compress(Path source, boolean setAllFileAttributes) throws Exception {
        if (source == null) {
            throw new Exception("missing path");
        }
        if (SOSPath.toFile(source).isFile()) {
            return compressFile(source);
        }
        return compressDirectory(source, setAllFileAttributes);
    }

    /** compress file or directory to a byte array<br/>
     * Note: Memory usage<br/>
     * - source - 1,5GB txt file ~ 8MB memory<br />
     * - source - 1,5GB binary file ~ 1,5GB memory<br />
     * 
     * @param source
     * @return
     * @throws Exception */
    private static SOSGzipResult compressFile(Path source) throws Exception {
        SOSGzipResult result = (new SOSGzip()).new SOSGzipResult();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); InputStream fis = Files.newInputStream(source); GZIPOutputStream gos =
                new GZIPOutputStream(bos)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                gos.write(buffer, 0, len);
            }
            gos.close();

            result.addFile(source);
            result.setCompressed(bos.toByteArray());
            result.finisch();
        } catch (IOException e) {
            throw e;
        }
        return result;
    }

    /** compressing without empty folders
     * 
     * @param source
     * @param setAllFileAttributes<br/>
     *            true - sets last modified, file size, user, permissions etc<br/>
     *            false - sets last modified, file size<br />
     *            better performing when large list of files, e.g<br/>
     *            87895 files - true ~ 50s, false - 31s
     * @return
     * @throws Exception */
    private static SOSGzipResult compressDirectory(Path source, boolean setAllFileAttributes) throws Exception {
        SOSGzipResult result = (new SOSGzip()).new SOSGzipResult();
        if (source == null) {
            return result;
        }
        File sourceDirAsFile = SOSPath.toFile(source);
        if (!sourceDirAsFile.isDirectory()) {
            throw new Exception(String.format("[%s]is not a directory", source));
        }
        Path sourceDir = sourceDirAsFile.toPath();
        // try (FileOutputStream fos = new FileOutputStream(outputFile); BufferedOutputStream bos = new BufferedOutputStream(fos);
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); BufferedOutputStream bos = new BufferedOutputStream(baos);
                GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(bos); TarArchiveOutputStream tos = new TarArchiveOutputStream(gcos,
                        "UTF-8")) {

            tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            try (Stream<Path> stream = Files.walk(sourceDir)) {
                for (Path p : stream.collect(Collectors.toList())) {
                    File f = p.toFile();
                    if (f.isFile()) {
                        if (Files.isSymbolicLink(p)) {
                            continue;
                        }

                        Path targetFile = sourceDir.relativize(p);
                        try {
                            TarArchiveEntry entry = null;
                            if (setAllFileAttributes) {
                                entry = new TarArchiveEntry(f, targetFile.toString());
                            } else {
                                entry = new TarArchiveEntry(targetFile.toString());
                                entry.setSize(f.length());
                                entry.setModTime(f.lastModified());
                            }
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[compress][file]%s", entry.getName()));
                            }
                            tos.putArchiveEntry(entry);
                            Files.copy(p, tos);
                            tos.closeArchiveEntry();

                            result.addFile(p);
                        } catch (Throwable e) {
                            LOGGER.error(String.format("[compress][file][%s]%s", p, e.toString()), e);
                        }
                    } else if (f.isDirectory()) {
                        if (!p.equals(sourceDir)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[compress][directory]%s", p.getFileName()));
                            }
                            result.addDirectory(p);
                        }
                    }
                }
            }
            tos.close();
            result.setCompressed(baos.toByteArray());
            result.finisch();
            return result;
        }
    }

    /** Unpack a tar.gz file to the given directory
     * 
     * @param source tar.gz file
     * @param targetDir
     * @throws IOException */
    public static SOSGzipResult decompress(Path source, Path targetDir, boolean setLastModifiedTime) throws Exception {
        if (source == null || !SOSPath.toFile(source).isFile()) {
            throw new Exception(String.format("[%s]is not a file", source));
        }
        return decompress(Files.newInputStream(source), targetDir, setLastModifiedTime);
    }

    /** Unpack the tar.gz bytes to the given directory
     * 
     * @param source tar.gz bytes
     * @param targetDir
     * @throws IOException */
    public static SOSGzipResult decompress(byte[] source, Path targetDir, boolean setLastModifiedTime) throws Exception {
        return decompress(new ByteArrayInputStream(source), targetDir, setLastModifiedTime);
    }

    /** Unpack a tar.gz input stream to the given directory
     * 
     * @param source source tar.gz input stream
     * @param targetDir
     * @throws IOException */
    public static SOSGzipResult decompress(InputStream source, Path targetDir, boolean setLastModifiedTime) throws Exception {
        InputStream inputStream = null;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        SOSGzipResult result = (new SOSGzip()).new SOSGzipResult();
        try {
            Path target = targetDir.toAbsolutePath();

            inputStream = new BufferedInputStream(new GZIPInputStream(new BufferedInputStream(source)));
            result.addDirectory(target);

            try (TarArchiveInputStream tis = new TarArchiveInputStream(inputStream)) {
                for (TarArchiveEntry entry = tis.getNextTarEntry(); entry != null;) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[decompress][entry]%s", entry.getName()));
                    }
                    Path outputPath = target.resolve(entry.getName());

                    if (entry.isDirectory()) {// empty directory
                        Files.createDirectories(outputPath);
                        result.addDirectory(outputPath);
                    } else {
                        Path parent = outputPath.getParent();
                        if (parent != null) {
                            String p = parent.toString();
                            if (!result.getDirectories().contains(p)) {
                                // an exception is not thrown if the directory could not be created because it already exists.
                                Files.createDirectories(parent);
                                result.addDirectory(parent);
                            }
                        }
                        try (OutputStream out = Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                            IOUtils.copy(tis, out);
                        }
                        result.addFile(outputPath);

                        if (setLastModifiedTime) {
                            try {
                                SOSPath.setLastModifiedTime(outputPath, entry.getLastModifiedDate());
                            } catch (Throwable e) {
                                LOGGER.warn(String.format("[decompress][setLastModifiedTime][%s=%s]%s", entry.getName(), entry.getLastModifiedDate(),
                                        e.toString()), e);
                            }
                        }
                    }

                    entry = tis.getNextTarEntry();
                }
            }
            result.getDirectories().remove(target.toString());
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
        result.finisch();
        return result;
    }

    public class SOSGzipResult {

        private final Instant start;

        private Instant end;
        private byte[] compressed = null;
        // TODO Set<Path> check performance
        private Set<String> files = new HashSet<>();
        private Set<String> directories = new HashSet<>();

        protected SOSGzipResult() {
            start = Instant.now();
        }

        protected void finisch() {
            end = Instant.now();
        }

        protected void setCompressed(byte[] val) {
            compressed = val;
        }

        public byte[] getCompressed() {
            return compressed;
        }

        protected void addFile(Path p) {
            files.add(p.toString());
        }

        public Set<String> getFiles() {
            return files;
        }

        protected void addDirectory(Path p) {
            directories.add(p.toString());
        }

        public Set<String> getDirectories() {
            return directories;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("directories=").append(directories.size());
            sb.append(",files=").append(files.size());
            if (compressed != null) {
                sb.append(",compressed=").append(compressed.length).append("b");
            }
            if (end != null) {
                sb.append(",duration=").append(SOSDate.getDuration(start, end));
            }
            return sb.toString();
        }
    }
}