package com.sos.commons.util;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static SOSPathResult cleanupDirectory(final Path dir) throws IOException {
        SOSPathResult result = (new SOSPath()).new SOSPathResult();
        if (dir == null) {
            return result;
        }
        Path sourceDir = dir.toAbsolutePath();
        if (Files.exists(sourceDir)) {
            try (Stream<Path> stream = Files.walk(sourceDir)) {
                for (Path p : stream.sorted(Comparator.reverseOrder()).filter(f -> !f.equals(sourceDir)).collect(Collectors.toList())) {
                    try {
                        File f = p.toFile();
                        if (f.isDirectory()) {
                            result.addDirectory(p);
                        } else {
                            result.addFile(p);
                        }
                    } catch (Throwable e) {
                    }
                    Files.delete(p);
                }
            }
        }
        result.finisch();
        return result;
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

    public static File getMostRecentFile(Path dir) {
        return Arrays.stream(toFile(dir).listFiles()).filter(f -> f.isFile()).max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
                .orElse(null);
    }

    public static void setLastModifiedTime(Path path, Date date) throws Exception {
        if (path == null || date == null) {
            return;
        }
        Files.setLastModifiedTime(path, FileTime.fromMillis(date.getTime()));
    }

    public static long getCountSubfolders(Path dir, int maxDepth) throws IOException {
        long count = 0;
        Path sourceDir = dir.toAbsolutePath();
        try (Stream<Path> stream = Files.find(sourceDir, maxDepth, (path, attributes) -> attributes.isDirectory())) {
            count = stream.count() - 1;// -1 - without parent sourceDir
        }
        return count;
    }

    public static boolean isDirectoryEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return !entries.findFirst().isPresent();
            }
        }
        return false;
    }

    public class SOSPathResult {

        private final Instant start;

        private Instant end;
        // TODO Set<Path> check performance
        private Set<String> files = new HashSet<>();
        private Set<String> directories = new HashSet<>();

        protected SOSPathResult() {
            start = Instant.now();
        }

        protected void finisch() {
            end = Instant.now();
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
            if (end != null) {
                sb.append(",duration=").append(SOSDate.getDuration(start, end));
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        Path input = null;
        int sleep = 1;

        if (args.length > 1) {
            input = Paths.get(args[0]);
            sleep = Integer.parseInt(args[1]);
        } else {
            return;
        }

        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            String name = runtimeBean.getName();
            String pid = name.split("@")[0];
            System.out.println("PID=" + pid);

            SOSPath.getMostRecentFile(input);

            Thread.sleep(sleep * 1_000);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

}