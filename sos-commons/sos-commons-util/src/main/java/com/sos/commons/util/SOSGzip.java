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

    public static SOSGzipResult compress(Path source, boolean setAllFileAttributes) throws Exception {
        if (source == null) {
            throw new Exception("missing path");
        }
        if (SOSPath.toFile(source).isFile()) {
            return compressFile(source);
        }
        return compressDirectory(source, setAllFileAttributes);
    }

    private static SOSGzipResult compressFile(Path source) throws Exception {
        // TODO use commons.compress implementation
        // merge into compressDirectory
        byte[] uncompressedData = Files.readAllBytes(source);
        SOSGzipResult result = (new SOSGzip()).new SOSGzipResult();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressedData.length); GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {
            gzipOS.write(uncompressedData);
            gzipOS.close();

            result.addFile(source);
            result.setCompressed(bos.toByteArray());
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
        if (!SOSPath.toFile(source).isDirectory()) {
            throw new Exception(String.format("[%s]is not a directory", source));
        }
        // try (FileOutputStream fos = new FileOutputStream(outputFile); BufferedOutputStream bos = new BufferedOutputStream(fos);
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        SOSGzipResult result = (new SOSGzip()).new SOSGzipResult();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); BufferedOutputStream bos = new BufferedOutputStream(baos);
                GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(bos); TarArchiveOutputStream tos = new TarArchiveOutputStream(gcos,
                        "UTF-8")) {

            tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            try (Stream<Path> stream = Files.walk(source)) {
                for (Path p : stream.collect(Collectors.toList())) {
                    File f = p.toFile();
                    if (f.isFile()) {
                        if (Files.isSymbolicLink(p)) {
                            continue;
                        }

                        Path targetFile = source.relativize(p);
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
                        if (!p.equals(source)) {
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
            inputStream = new BufferedInputStream(new GZIPInputStream(new BufferedInputStream(source)));
            result.addDirectory(targetDir);

            try (TarArchiveInputStream tis = new TarArchiveInputStream(inputStream)) {
                for (TarArchiveEntry entry = tis.getNextTarEntry(); entry != null;) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[decompress][entry]%s", entry.getName()));
                    }
                    Path outputPath = targetDir.resolve(entry.getName());

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
            result.getDirectories().remove(targetDir.toString());
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
        return result;
    }

    public class SOSGzipResult {

        private byte[] compressed = null;
        private Set<String> files = new HashSet<>();
        private Set<String> directories = new HashSet<>();

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
                sb.append(",compressed=" + compressed.length).append("b");
            }
            return sb.toString();
        }
    }
}
