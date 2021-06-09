package com.sos.commons.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collector;
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

import com.sos.commons.exception.SOSNoSuchFileException;

public class SOSPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPath.class);
    static final int BUFF_SIZE = 100000;
    static final byte[] buffer = new byte[BUFF_SIZE];

    public static boolean canReadFile(final Path file) throws IOException, InterruptedException {
        boolean rep = true;
        int repeatCount = 5;
        while (rep && repeatCount >= 0) {
            try {
                if (!Files.exists(file)) {
                    throw new FileNotFoundException("..file does not exist: " + file.toString());
                }
                OutputStream f = Files.newOutputStream(file, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                f.close();
                rep = false;
            } catch (FileNotFoundException e) {
                throw e;
            } catch (IOException e) {
                if (repeatCount == 0) {
                    throw e;
                }
                repeatCount--;
                LOGGER.debug("trial " + (5 - repeatCount) + " of 5 to access file: " + file.toString());
                Thread.sleep(1000);
            }
        }
        return !rep;
    }

    public static void appendFile(final Path source, final Path dest) throws IOException {
        copyFile(source, dest, true);
    }

    public static void copyFile(final Path source, final Path dest) throws IOException {
        copyFile(source, dest, false);
    }

    public static void copyFile(final Path source, final Path dest, final boolean append) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            LOGGER.debug("Copying file " + source.toString() + " with buffer of " + BUFF_SIZE + " bytes");

            in = Files.newInputStream(source);
            if (append) {
                out = Files.newOutputStream(dest, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            } else {
                out = Files.newOutputStream(dest);
            }
            while (true) {
                synchronized (buffer) {
                    int amountRead = in.read(buffer);
                    if (amountRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, amountRead);
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static void copyFile(final String source, final String dest) throws IOException {
        copyFile(source, dest, false);
    }

    public static void copyFile(final String source, final String dest, final boolean append) throws IOException {
        copyFile(Paths.get(source), Paths.get(dest), append);
    }

    public static void delete(final Path path) throws IOException, SOSNoSuchFileException {
        if (path == null || !Files.exists(path)) {
            throw new SOSNoSuchFileException(path);
        }
        if (Files.isDirectory(path)) {
            try (Stream<Path> stream = Files.walk(path)) {
                for (Path p : stream.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                    Files.delete(p);
                }
            }
        } else {
            Files.delete(path);
        }
    }

    public static boolean deleteIfExists(final Path path) throws Exception {
        try {
            delete(path);
            return true;
        } catch (SOSNoSuchFileException e) {
            return false;
        }
    }

    public static boolean cleanupDirectory(final Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (Stream<Path> stream = Files.walk(dir)) {
                for (Path p : stream.sorted(Comparator.reverseOrder()).filter(f -> !f.equals(dir)).collect(Collectors.toList())) {
                    Files.delete(p);
                }
                return true;
            }
        }
        return false;
    }

    public static long getLineCount(final Path file) throws IOException {
        long result;
        try (Stream<String> stream = Files.lines(file)) {
            result = stream.count();
        }
        return result;
    }

    public static Stream<Path> getFilesStream(final String folder, final String regexp, final int flag) throws IOException {
        return getFilesStream(folder, regexp, flag, false);
    }

    public static Stream<Path> getFilesStream(final Path folder, final String regexp, final int flag) throws IOException {
        return getFilesStream(folder, regexp, flag, false);
    }

    public static Stream<Path> getFilesStream(final String folder, final String regexp, final int flag, final boolean withSubFolder)
            throws IOException {
        if (folder == null || folder.isEmpty()) {
            throw new FileNotFoundException("empty directory not allowed!!");
        }
        return getFilesStream(Paths.get(folder), regexp, flag, withSubFolder);
    }

    public static Stream<Path> getFilesStream(final Path folder, final String regexp, final int flag, final boolean withSubFolder)
            throws IOException {
        if (folder == null) {
            throw new FileNotFoundException("empty directory not allowed!!");
        }
        if (!Files.isDirectory(folder)) {
            throw new FileNotFoundException("directory does not exist: " + folder.toString());
        }
        Predicate<Path> predicate = p -> !Files.isDirectory(p);
        if (regexp != null) {
            final Pattern pattern = Pattern.compile(regexp, flag);
            predicate = p -> !Files.isDirectory(p) && pattern.matcher(p.getFileName().toString()).find();
        }
        if (withSubFolder) {
            return Files.walk(folder).filter(predicate);
        } else {
            return Files.list(folder).filter(predicate);
        }
    }

    public static List<Path> getFileList(final String folder) throws IOException {
        return getFilesStream(folder, null, 0).collect(Collectors.toList());
    }

    public static List<Path> getFileList(final Path folder) throws IOException {
        return getFilesStream(folder, null, 0).collect(Collectors.toList());
    }

    public static List<Path> getFileList(final String folder, final String regexp, final int flag) throws IOException {
        return getFilesStream(folder, regexp, flag).collect(Collectors.toList());
    }

    public static List<Path> getFileList(final Path folder, final String regexp, final int flag) throws IOException {
        return getFilesStream(folder, regexp, flag).collect(Collectors.toList());
    }

    public static List<Path> getFileList(final String folder, final String regexp, final int flag, final boolean withSubFolder) throws IOException {
        return getFilesStream(folder, regexp, flag, withSubFolder).collect(Collectors.toList());
    }

    public static List<Path> getFileList(final Path folder, final String regexp, final int flag, final boolean withSubFolder) throws IOException {
        return getFilesStream(folder, regexp, flag, withSubFolder).collect(Collectors.toList());
    }

    public static Stream<Path> getFolderStream(final String folder) throws IOException {
        return getFolderStream(folder, null, 0, false);
    }

    public static Stream<Path> getFolderStream(final Path folder) throws IOException {
        return getFolderStream(folder, null, 0, false);
    }

    public static Stream<Path> getFolderStream(final String folder, final String regexp, final int flag) throws IOException {
        return getFolderStream(folder, regexp, flag, false);
    }

    public static Stream<Path> getFolderStream(final Path folder, final String regexp, final int flag) throws IOException {
        return getFolderStream(folder, regexp, flag, false);
    }

    public static Stream<Path> getFolderStream(final String folder, final String regexp, final int flag, final boolean withSubFolder)
            throws IOException {
        if (folder == null || folder.isEmpty()) {
            throw new FileNotFoundException("empty directory not allowed!!");
        }
        return getFolderStream(Paths.get(folder), regexp, flag, withSubFolder);
    }

    public static Stream<Path> getFolderStream(final Path folder, final String regexp, final int flag, final boolean withSubFolder)
            throws IOException {
        if (folder == null) {
            throw new FileNotFoundException("empty directory not allowed!!");
        }
        if (!Files.isDirectory(folder)) {
            throw new FileNotFoundException("directory does not exist: " + folder.toString());
        }
        Predicate<Path> predicate = p -> !".".equals(p.getFileName().toString()) && !"..".equals(p.getFileName().toString());
        if (regexp != null) {
            final Pattern pattern = Pattern.compile(regexp, flag);
            predicate = p -> pattern.matcher(p.getFileName().toString()).find() && !".".equals(p.getFileName().toString()) && !"..".equals(p
                    .getFileName().toString());
        }
        if (withSubFolder) {
            return Files.walk(folder).filter(predicate);
        } else {
            return Files.list(folder).filter(predicate);
        }
    }

    public static List<Path> getFolderList(final String folder, final String regexp, final int flag) throws IOException {
        return getFolderStream(folder, regexp, flag).collect(Collectors.toList());
    }

    public static List<Path> getFolderList(final Path folder, final String regexp, final int flag) throws IOException {
        return getFolderStream(folder, regexp, flag).collect(Collectors.toList());
    }

    public static List<Path> getFolderList(final String folder, final String regexp, final int flag, final boolean withSubFolder) throws IOException {
        return getFolderStream(folder, regexp, flag, withSubFolder).collect(Collectors.toList());
    }

    public static List<Path> getFolderList(final Path folder, final String regexp, final int flag, final boolean withSubFolder) throws IOException {
        return getFolderStream(folder, regexp, flag, withSubFolder).collect(Collectors.toList());
    }

    public static byte[] readFile(final Path source) throws IOException {
        return Files.readAllBytes(source);
    }

    public static String readFile(final Path source, Charset charset) throws IOException {
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        return new String(Files.readAllBytes(source), charset);
    }

    public static String readFile(final Path file, Collector<? super String, ?, String> collector) throws IOException {
        String result;
        try (Stream<String> stream = Files.lines(file)) {
            result = stream.collect(collector);
        }
        return result;
    }

    public static void renameTo(final Path source, final Path dest) throws IOException {
        LOGGER.debug("..trying to move File " + source.toString() + " to " + dest.toString());
        if (!Files.exists(dest)) {
            try {
                Files.move(source, dest, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(source, dest);
            }
        } else {
            Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void renameTo(final String source, final String dest) throws IOException {
        renameTo(Paths.get(source), Paths.get(dest));
    }

    public static String subFileMask(final String filespec, final String substitute) throws IOException {
        if (filespec == null) {
            throw new IOException("file specification is null.");
        }
        String retVal = new String();
        int ipos1 = filespec.indexOf("[");
        int ipos2 = filespec.lastIndexOf("]");
        if (ipos1 == -1 || ipos2 == -1) {
            return filespec;
        }
        String midStr = new String();
        String startStr = new String();
        String endStr = new String();
        if (ipos1 != 0) {
            startStr = filespec.substring(0, ipos1);
        }
        midStr = substitute.concat(filespec.substring(ipos2 + 1, filespec.length()));
        retVal = startStr.concat(midStr).concat(endStr);
        return retVal;
    }

    public static String getFileNameWithoutExtension(Path filename) {
        if (filename != null) {
            return filename.toString().replaceFirst("\\.[^.]$", "");
        }
        return null;
    }

    public static File toFile(Path file) {
        return file.isAbsolute() ? file.toFile() : file.toAbsolutePath().toFile();
    }

    public static boolean endsWithNewLine(Path file) throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(toFile(file), "r");
            long len = raf.length() - 1L;
            if (len < 0L) {
                return true;
            }
            raf.seek(len);
            byte readByte = raf.readByte();
            // LF 10 \n
            // CR 13 \r
            if (readByte == 10 || readByte == 13) {
                return true;
            }
        } finally {
            if (raf != null)
                try {
                    raf.close();
                } catch (IOException iOException) {
                }
        }
        return false;
    }

    public static boolean endsWithNewLine(String content) throws IOException {
        if (SOSString.isEmpty(content)) {
            return false;
        }
        return content.endsWith("\n") || content.endsWith("\r");
    }

    public static byte[] gzipFile(Path path) throws Exception {// TODO use commons.compress implementation
        byte[] uncompressedData = Files.readAllBytes(path);
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

    public static byte[] gzipDirectory(Path path) throws Exception {
        // TODO file filter, gzip sub directories etc...
        // try (FileOutputStream fos = new FileOutputStream(outputFile); BufferedOutputStream bos = new BufferedOutputStream(fos);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); BufferedOutputStream bos = new BufferedOutputStream(baos);
                GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(bos); TarArchiveOutputStream tos = new TarArchiveOutputStream(gcos,
                        "UTF-8")) {

            tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path currentFile : stream) {
                    TarArchiveEntry entry = new TarArchiveEntry(currentFile.getFileName().toString());
                    entry.setSize(Files.size(currentFile));
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[gzip][entry]%s", entry.getName()));
                    }
                    tos.putArchiveEntry(entry);
                    tos.write(Files.readAllBytes(currentFile));
                    tos.closeArchiveEntry();
                }
            }
            tos.close();
            return baos.toByteArray();
        }
    }

    public static void ungzipDirectory(Path targz, Path outputDirectory) throws IOException {
        ungzipDirectory(Files.newInputStream(targz), outputDirectory);
    }

    public static void ungzipDirectory(byte[] targz, Path outputDirectory) throws IOException {
        ungzipDirectory(new ByteArrayInputStream(targz), outputDirectory);
    }

    public static void ungzipDirectory(InputStream targz, Path outputDirectory) throws IOException {
        // TODO ungzip sub directories etc ...
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new GZIPInputStream(new BufferedInputStream(targz)));

            try (TarArchiveInputStream tis = new TarArchiveInputStream(inputStream)) {
                for (TarArchiveEntry entry = tis.getNextTarEntry(); entry != null;) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[ungzip][entry]%s", entry.getName()));
                    }
                    if (entry.isDirectory()) {
                        continue;
                    } else {
                        Path outputfile = outputDirectory.resolve(entry.getName());
                        // outputFile.getParentFile().mkdirs();
                        try (OutputStream out = Files.newOutputStream(outputfile)) {
                            IOUtils.copy(tis, out);
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
            if (targz != null) {
                try {
                    targz.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    public static File getMostRecentFile(Path dir) {
        return Arrays.stream(toFile(dir).listFiles()).filter(f -> f.isFile()).max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
                .orElse(null);
    }

    public static void main(String[] args) throws Exception {
        Path input = null;
        Path output = null;
        int sleep = 1;

        if (args.length > 2) {
            input = Paths.get(args[0]);
            output = Paths.get(args[1]);
            sleep = Integer.parseInt(args[2]);
        } else {
            return;
        }

        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            String name = runtimeBean.getName();
            String pid = name.split("@")[0];
            System.out.println("PID=" + pid);

            SOSPath.ungzipDirectory(SOSPath.gzipDirectory(input), output);

            SOSPath.getMostRecentFile(input);

            Thread.sleep(sleep * 1_000);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

}