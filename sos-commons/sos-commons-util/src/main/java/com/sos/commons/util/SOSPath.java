package com.sos.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static int deleteFile(final Path file) throws IOException {
        if (Files.exists(file)) {
            if (Files.isDirectory(file)) {
                Set<Path> s = Files.walk(file).sorted(Comparator.reverseOrder()).collect(Collectors.toSet());
                for (Path p : s) {
                    Files.delete(p);
                }
                return s.size();
            } else {
                Files.delete(file);
                return 1;
            }
        } else {
            return 0;
        }
    }

    public static void deleteFolder(final Path folder) throws IOException {
        if (Files.exists(folder)) {
            for (Path p : Files.walk(folder).sorted(Comparator.reverseOrder()).collect(Collectors.toSet())) {
                Files.delete(p);
            }
        }
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

    public static byte[] gzip(Path path) throws Exception {
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
}