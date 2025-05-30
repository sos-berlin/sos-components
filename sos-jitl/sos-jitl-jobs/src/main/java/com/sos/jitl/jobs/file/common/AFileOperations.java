package com.sos.jitl.jobs.file.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.jitl.jobs.file.exception.SOSFileOperationsException;

public abstract class AFileOperations {

    protected static final int CREATE_DIR = 0x01;
    protected static final int GRACIOUS = 0x02;
    protected static final int NOT_OVERWRITE = 0x04;
    protected static final int RECURSIVE = 0x08;
    protected static final int REMOVE_DIR = 0x10;
    protected static final int WIPE = 0x20;

    private final int BUFF_SIZE = 100000;
    private final byte[] buffer = new byte[BUFF_SIZE];
    private final ISOSLogger logger;

    private List<File> resultList = null;

    public AFileOperations(ISOSLogger logger) {
        this.logger = logger;
        this.resultList = new ArrayList<File>();
    }

    protected abstract boolean handleOneFile(File sourceFile, File targetFile, boolean overwrite, boolean gracious) throws Exception;

    public boolean canWrite(File file, final String fileSpec, final int fileSpecFlags) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(getDebugMain(fileSpecFlags));
        }
        file = new File(substituteAllDate(file.getPath()));
        if (!file.exists()) {
            logger.info("[%s]no such file or directory", file.getCanonicalPath());
            return true;
        } else {
            if (!file.isDirectory()) {
                logger.info("[%s]file exists", file.getCanonicalPath());
                boolean writable = false;
                RandomAccessFile f = null;
                try {
                    f = new RandomAccessFile(file.getAbsolutePath(), "rw");
                    f.close();
                    f = null;
                    writable = true;
                } catch (Exception e) {
                    //
                } finally {
                    if (f != null) {
                        f.close();
                    }
                }
                if (!writable) {
                    logger.info("[%s]cannot be written", file.getCanonicalPath());
                    return false;
                } else {
                    return true;
                }
            } else {
                if (fileSpec == null || fileSpec.isEmpty()) {
                    logger.info("[%s]directory exists", file.getCanonicalPath());
                    return true;
                }
                List<File> fileList = getFilelist(file.getPath(), fileSpec, fileSpecFlags, false, 0, 0, -1, -1, 0, 0);
                if (fileList.isEmpty()) {
                    logger.info("[%s]directory contains no files matching %s", file.getCanonicalPath(), fileSpec);
                    return false;
                } else {
                    logger.info("[%s]directory contains %s file(s) matching %s", file.getCanonicalPath(), fileList.size(), fileSpec);
                    for (int i = 0; i < fileList.size(); i++) {
                        File checkFile = fileList.get(i);
                        logger.info("[%s]found file", checkFile.getCanonicalPath());
                        boolean writable = false;
                        RandomAccessFile f = null;
                        try {
                            f = new RandomAccessFile(file.getAbsolutePath(), "rw");
                            f.close();
                            f = null;
                            writable = true;
                        } catch (Exception e) {
                            //
                        } finally {
                            if (f != null) {
                                f.close();
                            }
                        }
                        if (!writable) {
                            logger.info("[%s]cannot be written", checkFile.getCanonicalPath());
                            return false;
                        }
                    }
                    resultList.addAll(fileList);
                    return true;
                }
            }
        }

    }

    public boolean existsFile(File file, final String fileSpec, final boolean recursive, final int fileSpecFlags, final String minFileAge,
            final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles)
            throws Exception {
        long minAge = SOSDate.getTimeAsMillis(minFileAge);
        long maxAge = SOSDate.getTimeAsMillis(maxFileAge);
        long minSize = calculateFileSize(minFileSize);
        long maxSize = calculateFileSize(maxFileSize);

        if (logger.isDebugEnabled()) {
            debug(getDebugMain(fileSpecFlags), null, minFileAge, maxFileAge, minFileSize, maxFileSize);
        }

        if (skipFirstFiles < 0) {
            throw new SOSFileOperationsException("[" + skipFirstFiles + "] is no valid value for skipFirstFiles");
        }
        if (skipLastFiles < 0) {
            throw new SOSFileOperationsException("[" + skipLastFiles + "] is no valid value for skipLastFiles");
        }
        if (skipFirstFiles > 0 && skipLastFiles > 0) {
            throw new SOSFileOperationsException("skip only either first files or last files");
        }
        if ((skipFirstFiles > 0 || skipLastFiles > 0) && minAge == 0 && maxAge == 0 && minSize == -1 && maxSize == -1) {
            throw new SOSFileOperationsException("missed constraint for file skipping (minFileAge, maxFileAge, minFileSize, maxFileSize)");
        }
        file = new File(substituteAllDate(file.getPath()));
        if (!file.exists()) {
            logger.info("[%s]no such file or directory", file.getCanonicalPath());
            return false;
        } else {
            if (!file.isDirectory()) {
                long currentTime = System.currentTimeMillis();
                if (minAge > 0) {
                    long lastModified = file.lastModified();
                    long interval = currentTime - lastModified;
                    if (interval < 0) {
                        throw new SOSFileOperationsException("Cannot filter by file age. File [" + file.getCanonicalPath()
                                + "] was modified in the future.");
                    }
                    if (interval < minAge) {
                        logger.info("[%s][lastModified=%s]minimum age required is %s", file.getCanonicalPath(), getDateTime(lastModified),
                                minFileAge);
                        return false;
                    }
                }
                if (maxAge > 0) {
                    long lastModified = file.lastModified();
                    long interval = currentTime - lastModified;
                    if (interval < 0) {
                        throw new SOSFileOperationsException("Cannot filter by file age. File [" + file.getCanonicalPath()
                                + "] was modified in the future.");
                    }
                    if (interval > maxAge) {
                        logger.info("[%s][lastModified=%s]maximum age required is %s", file.getCanonicalPath(), getDateTime(lastModified),
                                maxFileAge);
                        return false;
                    }
                }
                if (minSize > -1 && minSize > file.length()) {
                    logger.info("[%s][checking file size][%s]minimum size required is %s", file.getCanonicalPath(), file.length(), minFileSize);
                    return false;
                }
                if (maxSize > -1 && maxSize < file.length()) {
                    logger.info("[%s][checking file size][%s]maximum size required is %s", file.getCanonicalPath(), file.length(), maxFileSize);
                    return false;
                }
                if (skipFirstFiles > 0 || skipLastFiles > 0) {
                    logger.info("[%s]skipped", file.getCanonicalPath());
                    return false;
                }
                logger.info(file.getCanonicalPath());
                resultList.add(file);
                return true;
            } else {
                if (fileSpec == null || fileSpec.isEmpty()) {
                    logger.info("[%s]directory exists", file.getCanonicalPath());
                    return true;
                }
                List<File> fileList = getFilelist(file.getPath(), fileSpec, fileSpecFlags, recursive, minAge, maxAge, minSize, maxSize,
                        skipFirstFiles, skipLastFiles);
                if (fileList.isEmpty()) {
                    logger.info("[%s]directory contains no files matching %s", file.getCanonicalPath(), fileSpec);
                    return false;
                } else {
                    logger.info("[%s]directory contains %s file(s) matching %s", file.getCanonicalPath(), fileList.size(), fileSpec);
                    for (File f : fileList) {
                        logger.info(f.getCanonicalPath());
                    }
                    resultList.addAll(fileList);
                    return true;
                }
            }
        }
    }

    public int removeFileCnt(final String file, final String fileSpec, final int flags, final int fileSpecFlags, final String minFileAge,
            final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles,
            String sortCriteria, String sortOrder) throws Exception {
        return removeFileCnt(new File(file), fileSpec, flags, fileSpecFlags, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles,
                skipLastFiles, sortCriteria, sortOrder);
    }

    private int removeFileCnt(final File file, final String fileSpec, final int flags, final int fileSpecFlags, final String minFileAge,
            final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles,
            String sortCriteria, String sortOrder) throws Exception {

        int removedFiles = 0;
        int removedDirectories = 0;

        boolean recursive = has(flags, RECURSIVE);
        boolean gracious = has(flags, GRACIOUS);
        boolean wipe = has(flags, WIPE);
        boolean removeDir = has(flags, REMOVE_DIR);
        long minAge = SOSDate.getTimeAsMillis(minFileAge);
        long maxAge = SOSDate.getTimeAsMillis(maxFileAge);
        long minSize = calculateFileSize(minFileSize);
        long maxSize = calculateFileSize(maxFileSize);

        if (logger.isDebugEnabled()) {
            debug(getDebugMain(fileSpecFlags), getDebugRemoveFlags(flags), minFileAge, maxFileAge, minFileSize, maxFileSize);
        }

        if (skipFirstFiles < 0) {
            throw new SOSFileOperationsException("[" + skipFirstFiles + "] is no valid value for skipFirstFiles");
        }
        if (skipLastFiles < 0) {
            throw new SOSFileOperationsException("[" + skipLastFiles + "] is no valid value for skipLastFiles");
        }
        if (skipFirstFiles > 0 && skipLastFiles > 0) {
            throw new SOSFileOperationsException("skip only either first files or last files");
        }
        if ((skipFirstFiles > 0 || skipLastFiles > 0) && minAge == 0 && maxAge == 0 && minSize == -1 && maxSize == -1) {
            throw new SOSFileOperationsException("missed constraint for file skipping (minFileAge, maxFileAge, minFileSize, maxFileSize)");
        }
        if (!file.exists()) {
            if (gracious) {
                logger.info("[%s]cannot remove file, file does not exist", file.getCanonicalPath());
                return 0;
            } else {
                throw new SOSFileOperationsException("file does not exist: " + file.getCanonicalPath());
            }
        }
        List<File> fileList;
        if (file.isDirectory()) {
            if (!file.canRead()) {
                throw new SOSFileOperationsException("directory is not readable: " + file.getCanonicalPath());
            }
            logger.info("[%s]remove [%s] %s", file.getCanonicalPath(), fileSpec, (recursive ? " (recursive)" : ""));
            fileList = getFilelist(file.getPath(), fileSpec, fileSpecFlags, has(flags, RECURSIVE), minAge, maxAge, minSize, maxSize, 0, 0);
        } else {
            fileList = new ArrayList<File>();
            fileList.add(file);
            fileList = filelistFilterAge(fileList, minAge, maxAge);
            fileList = filelistFilterSize(fileList, minSize, maxSize);
            if (skipFirstFiles > 0 || skipLastFiles > 0) {
                fileList.clear();
            }
        }

        if (sortCriteria.equalsIgnoreCase("age")) {
            fileList.sort(new FileComparatorAge());
        } else if (sortCriteria.equalsIgnoreCase("size")) {
            fileList.sort(new FileComparatorSize());
        } else {
            fileList.sort(new FileComparatorName());
        }

        File currentFile;

        for (int i = skipFirstFiles; i < fileList.size() - skipLastFiles; i++) {
            currentFile = fileList.get(i);
            resultList.add(currentFile);
            logger.info("[%s]remove file", currentFile.getCanonicalPath());
            if (wipe) {
                if (!wipe(currentFile)) {
                    throw new SOSFileOperationsException("cannot remove file: " + currentFile.getCanonicalPath());
                }
            } else {
                delete(currentFile);
            }
            removedFiles++;
        }
        if (removeDir) {
            int firstSize = listFolders(file.getPath(), ".*", 0, recursive).size();
            if (recursive) {
                recDeleteEmptyDir(file, fileSpec, fileSpecFlags);
            } else {
                List<File> list = listFolders(file.getPath(), fileSpec, fileSpecFlags);
                File f;
                for (int i = 0; i < list.size(); i++) {
                    f = list.get(i);
                    if (f.isDirectory()) {
                        if (!f.canRead()) {
                            throw new SOSFileOperationsException("directory is not readable: " + f.getCanonicalPath());
                        }
                        if (f.list().length == 0) {
                            delete(f);
                            logger.info("[%s]remove directory", f.getCanonicalPath());
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("[%s]directory cannot be removed because it is not empty", f.getCanonicalPath());
                            }
                            String lst = f.list()[0];
                            for (int n = 1; n < f.list().length; n++) {
                                lst += ", " + f.list()[n];
                            }
                            if (logger.isDebugEnabled()) {
                                logger.debug("          contained files " + f.list().length + ": " + lst);
                            }
                        }
                    }
                }
            }
            removedDirectories = firstSize - listFolders(file.getPath(), ".*", 0, recursive).size();
        }
        String msg = "";
        if (removeDir) {
            if (removedDirectories == 1) {
                msg = " + 1 directory removed";
            } else {
                msg = " + " + removedDirectories + " directories removed";
            }
        }
        logger.info(removedFiles + " file(s) removed" + msg);
        return removedFiles + removedDirectories;
    }

    private void delete(File f) throws Exception {
        Path path = FileSystems.getDefault().getPath(f.getAbsolutePath());
        try {
            Files.delete(path);
        } catch (NoSuchFileException x) {
            throw new SOSFileOperationsException(path + "no exists");
        } catch (DirectoryNotEmptyException x) {
            throw new SOSFileOperationsException(path + "directory not empty");
        } catch (IOException x) {
            throw new SOSFileOperationsException(path + "file permission issue");
        }
    }

    private boolean recDeleteEmptyDir(final File dir, final String fileSpec, final int fileSpecFlags) throws Exception {
        if (dir.isDirectory()) {
            if (!dir.canRead()) {
                throw new SOSFileOperationsException("directory is not readable: " + dir.getCanonicalPath());
            }
        } else {
            return false;
        }
        File[] list = dir.listFiles();
        if (list.length == 0) {
            return true;
        }
        Pattern p = Pattern.compile(fileSpec, fileSpecFlags);
        File f;
        for (File element : list) {
            f = element;
            if (recDeleteEmptyDir(f, fileSpec, fileSpecFlags)) {
                if (p.matcher(f.getName()).matches()) {
                    delete(f);
                    logger.info("[%s]remove directory", f.getCanonicalPath());
                }
            } else {
                if (f.isDirectory()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[%s]directory cannot be removed because it is not empty", f.getCanonicalPath());
                    }
                    String lst = f.list()[0];
                    for (int n = 1; n < f.list().length; n++) {
                        lst += ", " + f.list()[n];
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("          contained files " + f.list().length + ": " + lst);
                    }
                }
            }
        }
        return dir.list().length == 0;
    }

    private List<File> listFolders(final String folder, final String regexp, final int flag, final boolean withSubFolder) throws Exception {
        List<File> result = new ArrayList<File>();
        result.addAll(listFolders(folder, regexp, flag));

        if (withSubFolder) {
            File[] subDir = new File(folder).listFiles();
            for (File element : subDir) {
                if (element.isDirectory()) {
                    result.addAll(listFolders(element.getPath(), regexp, flag, true));
                }
            }
        }
        return result;
    }

    private List<File> listFolders(final String folder, final String regexp, final int flag) throws Exception {
        if (folder == null || folder.isEmpty()) {
            throw new FileNotFoundException("empty directory not allowed!!");
        }
        File f = new File(folder);
        if (!f.exists()) {
            throw new FileNotFoundException("directory does not exist: " + folder);
        }
        File[] files = f.listFiles(new FilelistFilter(regexp, flag));
        List<File> result = new ArrayList<File>();
        for (int i = 0; i < files.length; i++) {
            if (!".".equals(files[i].getName()) && !"..".equals(files[i].getName())) {
                result.add(files[i]);
            }
        }
        return result;
    }

    private List<File> listFiles(final String folder, final String regexp, final int flag) throws Exception {
        if (folder == null || folder.isEmpty()) {
            throw new FileNotFoundException("empty directory not allowed!!");
        }
        File f = new File(folder);
        if (!f.exists()) {
            throw new FileNotFoundException("directory does not exist: " + folder);
        }
        List<File> result = new ArrayList<File>();
        File[] files = f.listFiles(new FilelistFilter(regexp, flag));
        for (File file : files) {
            if (file.isFile()) {
                result.add(file);
            }
        }
        return result;
    }

    private List<File> getFilelist(final String folder, final String regexp, final int flag, final boolean withSubFolder, final long minFileAge,
            final long maxFileAge, final long minFileSize, final long maxFileSize, final int skipFirstFiles, final int skipLastFiles)
            throws Exception {
        List<File> temp = new ArrayList<File>();
        temp = listFiles(folder, regexp, flag);
        temp = filelistFilterAge(temp, minFileAge, maxFileAge);
        temp = filelistFilterSize(temp, minFileSize, maxFileSize);
        if ((minFileSize != -1 || maxFileSize != -1) && minFileAge == 0 && maxFileAge == 0) {
            temp = filelistSkipFiles(temp, skipFirstFiles, skipLastFiles, "sort_size");
        } else if (minFileAge != 0 || maxFileAge != 0) {
            temp = filelistSkipFiles(temp, skipFirstFiles, skipLastFiles, "sort_age");
        }

        List<File> result = new ArrayList<File>();
        result.addAll(temp);
        if (withSubFolder) {
            File[] subDir = new File(folder).listFiles();
            for (File element : subDir) {
                if (element.isDirectory()) {
                    result.addAll(getFilelist(element.getPath(), regexp, flag, true, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles,
                            skipLastFiles));
                }
            }
        }
        return result;
    }

    public int copyFileCnt(final String source, final String target, final String fileSpec, final int flags, final int fileSpecFlags,
            final String replacing, final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize,
            final String maxFileSize, final int skipFirstFiles, final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return transferFileCnt(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge, maxFileAge, minFileSize,
                maxFileSize, skipFirstFiles, skipLastFiles, sortCriteria, sortOrder);
    }

    public boolean copyFile(final File source, final File dest, final boolean append) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest, append);
            while (true) {
                synchronized (buffer) {
                    int amountRead = in.read(buffer);
                    if (amountRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, amountRead);
                }
            }
            return true;
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public int renameFileCnt(final String source, final String target, final String fileSpec, final int flags, final int fileSpecFlags,
            final String replacing, final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize,
            final String maxFileSize, final int skipFirstFiles, final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return transferFileCnt(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge, maxFileAge, minFileSize,
                maxFileSize, skipFirstFiles, skipLastFiles, sortCriteria, sortOrder);
    }

    private int transferFileCnt(final File source, File target, final String fileSpec, final int flags, final int fileSpecFlags,
            final String replacing, final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize,
            final String maxFileSize, final int skipFirstFiles, final int skipLastFiles, String sortCriteria, String sortOrder) throws Exception {

        if (sortCriteria == null || sortCriteria.isEmpty()) {
            sortCriteria = "name";
        }
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "asc";
        }

        boolean replace = false;
        boolean createDir = has(flags, CREATE_DIR);
        boolean gracious = has(flags, GRACIOUS);
        boolean overwrite = !has(flags, NOT_OVERWRITE);
        long minAge = SOSDate.getTimeAsMillis(minFileAge);
        long maxAge = SOSDate.getTimeAsMillis(maxFileAge);
        long minSize = calculateFileSize(minFileSize);
        long maxSize = calculateFileSize(maxFileSize);

        if (logger.isDebugEnabled()) {
            debug(getDebugMain(fileSpecFlags), getDebugCopyFlags(flags), minFileAge, maxFileAge, minFileSize, maxFileSize);
        }

        String targetFilename;
        int transferedFiles = 0;
        if (skipFirstFiles < 0) {
            throw new SOSFileOperationsException("[" + skipFirstFiles + "] is no valid value for skipFirstFiles");
        }
        if (skipLastFiles < 0) {
            throw new SOSFileOperationsException("[" + skipLastFiles + "] is no valid value for skipLastFiles");
        }

        if (replacing != null || replacement != null) {
            if (replacing == null) {
                throw new SOSFileOperationsException("replacing cannot be null if replacement is set");
            }
            if (replacement == null) {
                throw new SOSFileOperationsException("replacement cannot be null if replacing is set");
            }
            if (!"".equals(replacing)) {
                try {
                    Pattern.compile(replacing);
                } catch (PatternSyntaxException pse) {
                    throw new SOSFileOperationsException("invalid pattern '" + replacing + "'");
                }
                replace = true;
            }
        }
        if (!source.exists()) {
            if (gracious) {
                logger.info(transferedFiles + " file(s) renamed");
                return transferedFiles;
            } else {
                throw new SOSFileOperationsException("file or directory does not exist: " + source.getCanonicalPath());
            }
        }
        if (!source.canRead()) {
            throw new SOSFileOperationsException("file or directory is not readable: " + source.getCanonicalPath());
        }
        if (target != null) {
            targetFilename = substituteAllDate(target.getPath());
            targetFilename = substituteAllDirectory(targetFilename, source.getPath());

            target = new File(targetFilename);
        }
        if (createDir && target != null && !target.exists()) {
            if (target.mkdirs()) {
                logger.info("[%s]target directory created", target.getCanonicalPath());
            } else {
                throw new SOSFileOperationsException("cannot create directory " + target.getCanonicalPath());
            }
        }
        List<File> list = null;
        if (source.isDirectory()) {
            if (target != null) {
                if (!target.exists()) {
                    throw new SOSFileOperationsException("target directory does not exist: " + target.getCanonicalPath());
                }
                if (!target.isDirectory()) {
                    throw new SOSFileOperationsException("target is no directory: " + target.getCanonicalPath());
                }
            }
            list = getFilelist(source.getPath(), fileSpec, fileSpecFlags, has(flags, RECURSIVE), minAge, maxAge, minSize, maxSize, 0, 0);
            if (logger.isDebugEnabled()) {
                logger.debug("[source=%s][fileSpec=%s]found %s files", source.getPath(), fileSpec, list.size());
            }
        } else {
            list = new ArrayList<File>();
            list.add(source);
            list = filelistFilterAge(list, minAge, maxAge);
            list = filelistFilterSize(list, minSize, maxSize);
            if (skipFirstFiles > 0 || skipLastFiles > 0) {
                list.clear();
            }
        }
        File sourceFile;
        File targetFile;
        File dir;
        if (sortCriteria.equalsIgnoreCase("age")) {
            list.sort(new FileComparatorAge());
        } else if (sortCriteria.equalsIgnoreCase("size")) {
            list.sort(new FileComparatorSize());
        } else {
            list.sort(new FileComparatorName());
        }
        if (sortOrder.equalsIgnoreCase("desc")) {
            Collections.reverse(list);
        }
        for (int i = skipFirstFiles; i < list.size() - skipLastFiles; i++) {
            sourceFile = list.get(i);
            resultList.add(sourceFile);
            if (target != null) {
                if (target.isDirectory()) {
                    String root = source.isDirectory() ? source.getPath() : source.getParent();
                    targetFilename = target.getPath() + sourceFile.getPath().substring(root.length());
                } else {
                    targetFilename = target.getPath();
                }
            } else {
                if (source.isDirectory()) {
                    String root = source.isDirectory() ? source.getPath() : source.getParent();
                    targetFilename = source.getPath() + sourceFile.getPath().substring(root.length());
                } else {
                    targetFilename = source.getParent() + "/" + sourceFile.getName();
                }
            }
            targetFile = new File(targetFilename);
            try {
                if (replace) {
                    targetFilename = targetFile.getName();
                    targetFilename = replaceGroups(targetFilename, replacing, replacement);
                    targetFilename = substituteAllDate(targetFilename);
                    targetFilename = substituteAllFilename(targetFilename, targetFile.getName());

                    targetFile = new File(targetFile.getParent() + "/" + targetFilename);
                }
            } catch (Exception re) {
                throw new SOSFileOperationsException("replacement error in file " + targetFilename + ": " + re.getMessage());
            }
            dir = new File(targetFile.getParent());
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    logger.info("[%s]create directory", dir.getCanonicalPath());
                } else {
                    throw new SOSFileOperationsException("cannot create directory " + dir.getCanonicalPath());
                }
            }
            if (!handleOneFile(sourceFile, targetFile, overwrite, gracious)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[skip][sourceFile=%s][targetFile=%s]overwrite=%s,gracious=%s", sourceFile.getPath(), targetFile.getPath(),
                            overwrite, gracious);
                }
                continue;
            }
            transferedFiles++;
        }
        logger.info(transferedFiles + " file(s)");
        return transferedFiles;
    }

    private String getDuration(long millis) {
        try {
            return SOSDate.getDuration(Duration.ofMillis(millis));
        } catch (Throwable e) {
            return millis + "ms";
        }
    }

    private String getDateTime(long millis) {
        try {
            return SOSDate.getDateTimeAsString(millis);
        } catch (Throwable e) {
            return millis + "ms";
        }
    }

    private List<File> filelistFilterAge(List<File> filelist, final long minAge, final long maxAge) throws Exception {
        long currentTime = System.currentTimeMillis();
        if (minAge != 0) {
            String minAgeDef = getDuration(minAge);
            File file;
            List<File> newlist = new ArrayList<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file = filelist.get(i);
                long lastModified = file.lastModified();
                long interval = currentTime - lastModified;
                if (interval < 0) {
                    throw new SOSFileOperationsException("Cannot filter by file age. File [" + file.getCanonicalPath()
                            + "] was modified in the future.");
                }
                if (interval >= minAge) {
                    newlist.add(file);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("[%s][lastModified=%s]minimum age required is %s", file.getCanonicalPath(), getDateTime(
                                lastModified), minAgeDef));
                    }
                }
            }
            filelist = newlist;
        }
        if (maxAge != 0) {
            String maxAgeDef = getDuration(maxAge);
            File file;
            List<File> newlist = new ArrayList<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file = filelist.get(i);
                long lastModified = file.lastModified();
                long interval = currentTime - lastModified;
                if (interval < 0) {
                    throw new SOSFileOperationsException("Cannot filter by file age. File [" + file.getCanonicalPath()
                            + "] was modified in the future.");
                }
                if (interval <= maxAge) {
                    newlist.add(file);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("[%s][lastModified=%s]maximum age required is %s", file.getCanonicalPath(), getDateTime(
                                lastModified), maxAgeDef));
                    }
                }
            }
            filelist = newlist;
        }
        return filelist;
    }

    private List<File> filelistFilterSize(List<File> filelist, final long minSize, final long maxSize) throws Exception {
        if (minSize > -1) {
            File file;
            List<File> newlist = new ArrayList<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file = filelist.get(i);
                if (file.length() >= minSize) {
                    newlist.add(file);
                }
            }
            filelist = newlist;
        }
        if (maxSize > -1) {
            File file;
            List<File> newlist = new ArrayList<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file = filelist.get(i);
                if (file.length() <= maxSize) {
                    newlist.add(file);
                }
            }
            filelist = newlist;
        }
        return filelist;
    }

    private List<File> filelistSkipFiles(List<File> filelist, final int skipFirstFiles, final int skipLastFiles, final String sorting)
            throws Exception {
        if ("sort_size".equals(sorting)) {
            filelist.sort(new FileComparatorSize());
        } else if ("sort_age".equals(sorting)) {
            filelist.sort(new FileComparatorAge());
        }
        return filelist;
    }

    private long calculateFileSize(final String filesize) throws Exception {
        long size;
        if (filesize == null || filesize.trim().isEmpty()) {
            return -1;
        }
        if (filesize.matches("-1")) {
            return -1;
        }
        if (filesize.matches("[\\d]+")) {
            size = Long.parseLong(filesize);
        } else {
            if (filesize.matches("^[\\d]+[kK][bB]$")) {
                size = Long.parseLong(filesize.substring(0, filesize.length() - 2)) * 1024;
            } else if (filesize.matches("^[\\d]+[mM][bB]$")) {
                size = Long.parseLong(filesize.substring(0, filesize.length() - 2)) * 1024 * 1024;
            } else if (filesize.matches("^[\\d]+[gG][bB]$")) {
                size = Long.parseLong(filesize.substring(0, filesize.length() - 2)) * 1024 * 1024 * 1024;
            } else {
                throw new SOSFileOperationsException("[" + filesize + "] is no valid file size");
            }
        }
        return size;
    }

    private String substituteFirstFilename(String targetFilename, final String original) throws Exception {
        Matcher matcher = Pattern.compile("\\[filename:([^\\]]*)\\]").matcher(targetFilename);
        if (matcher.find()) {
            if ("".equals(matcher.group(1))) {
                targetFilename = targetFilename.replaceFirst("\\[filename:\\]", original);
            } else if ("lowercase".equals(matcher.group(1))) {
                targetFilename = targetFilename.replaceFirst("\\[filename:lowercase\\]", original.toLowerCase());
            } else if ("uppercase".equals(matcher.group(1))) {
                targetFilename = targetFilename.replaceFirst("\\[filename:uppercase\\]", original.toUpperCase());
            }
        }
        return targetFilename;
    }

    private String substituteAllFilename(String targetFilename, final String original) throws Exception {
        String temp = substituteFirstFilename(targetFilename, original);
        while (!targetFilename.equals(temp)) {
            targetFilename = temp;
            temp = substituteFirstFilename(targetFilename, original);
        }
        return temp;
    }

    private String substituteFirstDate(String targetFilename) throws Exception {
        final String conVarName = "[date:";
        try {
            if (targetFilename.matches("(.*)(\\" + conVarName + ")([^\\]]*)(\\])(.*)")) {
                int posBegin = targetFilename.indexOf(conVarName);
                if (posBegin > -1) {
                    int posEnd = targetFilename.indexOf("]", posBegin + 6);
                    if (posEnd > -1) {
                        String dateTimeFormat = targetFilename.substring(posBegin + 6, posEnd);
                        if (dateTimeFormat.isEmpty()) {
                            dateTimeFormat = new String(SOSDate.DATETIME_FORMAT);
                        }
                        String dateTime = SOSDate.format(new Date(), dateTimeFormat);
                        String strT = (posBegin > 0 ? targetFilename.substring(0, posBegin) : "") + dateTime;
                        if (targetFilename.length() > posEnd) {
                            strT += targetFilename.substring(posEnd + 1);
                        }
                        targetFilename = strT;
                    }
                }
            }
            return targetFilename;
        } catch (Exception e) {
            throw new SOSFileOperationsException("error substituting [date:]: " + e.getMessage());
        }
    }

    private String substituteAllDate(String targetFilename) throws Exception {
        String temp = substituteFirstDate(targetFilename);
        while (!targetFilename.equals(temp)) {
            targetFilename = temp;
            temp = substituteFirstDate(targetFilename);
        }
        return temp;
    }

    private String substituteFirstDirectory(String target, String source) throws Exception {
        try {
            File sourceFile = new File(source);
            if (!sourceFile.isDirectory()) {
                source = sourceFile.getParent();
            }
            source = source.replaceAll("\\\\", "/");
            target = target.replaceAll("\\\\", "/");
            Pattern p = Pattern.compile("\\[directory:(-[\\d]+|[\\d]*)\\]");
            Matcher m = p.matcher(target);
            if (m.find()) {
                String substitute = "";
                if (m.group(1).isEmpty() || "0".equals(m.group(1)) || "-0".equals(m.group(1))) {
                    substitute = source;
                } else {
                    int depth = Integer.valueOf(m.group(1)).intValue();
                    StringTokenizer st = new StringTokenizer(source, "/");
                    int absDepth = depth < 0 ? -depth : depth;
                    if (absDepth >= st.countTokens()) {
                        substitute = source;
                    } else {
                        String[] dirs = new String[st.countTokens()];
                        int n = 0;
                        while (st.hasMoreTokens()) {
                            dirs[n++] = st.nextToken();
                        }
                        if (depth > 0) {
                            while (depth > 0) {
                                substitute = dirs[--depth] + "/" + substitute;
                            }
                        } else if (depth < 0) {
                            while (depth < 0) {
                                substitute = substitute + dirs[dirs.length + depth++] + "/";
                            }
                        }
                    }
                }
                if (substitute.charAt(substitute.length() - 1) == '/') {
                    substitute = substitute.substring(0, substitute.length() - 1);
                }
                target = target.replaceFirst("\\[directory:[^\\]]*\\]", substitute);
            }
            return target;
        } catch (Exception e) {
            throw new SOSFileOperationsException("error substituting [directory]: " + e.getMessage());
        }
    }

    private String substituteAllDirectory(String target, final String source) throws Exception {
        String temp = substituteFirstDirectory(target, source);
        while (!target.equals(temp)) {
            target = temp;
            temp = substituteFirstDirectory(target, source);
        }
        return temp;
    }

    private String replaceGroups(final String input, final String replacing, final String replacements) throws Exception {
        if (replacements == null) {
            throw new SOSFileOperationsException("replacements missing: 0 replacements defined");
        }
        return replaceGroups(input, replacing, replacements.split(";"));
    }

    private String replaceGroups(final String source, final String replacing, final String[] replacements) throws Exception {
        if (replacements == null) {
            throw new SOSFileOperationsException("replacements missing: 0 replacements defined");
        }
        Pattern p = Pattern.compile(replacing);
        Matcher m = p.matcher(source);
        if (!m.find()) {
            return source;
        }
        int groupCount = m.groupCount();
        if (replacements.length < groupCount) {
            throw new SOSFileOperationsException("replacements missing: " + replacements.length + " replacement(s) defined but " + groupCount
                    + " groups found");
        }

        StringBuilder result = new StringBuilder();
        if (groupCount == 0) {
            result.append(source.substring(0, m.start()) + replacements[0] + source.substring(m.end()));
        } else {
            int index = 0;
            for (int i = 1; i <= groupCount; i++) {
                int start = m.start(i);
                if (start >= 0) {
                    String repl = replacements[i - 1].trim();
                    if (!repl.isEmpty()) {
                        if (repl.contains("\\")) {
                            repl = repl.replaceAll("\\\\-", "");
                            for (int j = 1; j <= groupCount; j++) {
                                repl = repl.replaceAll("\\\\" + j, m.group(j));
                            }
                        }
                        result.append(source.substring(index, start)).append(repl);
                    }
                }
                index = m.end(i);
            }
            result.append(source.substring(index));
        }
        return result.toString();
    }

    private boolean has(final int flags, final int f) {
        return (flags & f) > 0;
    }

    private boolean wipe(final File file) {
        RandomAccessFile rwFile = null;
        try {
            rwFile = new RandomAccessFile(file, "rw");
            byte[] bytes = new byte[(int) rwFile.length()];
            int i = 0;
            while ((bytes[i++] = (byte) rwFile.read()) != -1) {
                //
            }
            rwFile.seek(0);
            for (i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (Math.random() * 10 % 9);
            }
            rwFile.write(bytes);
            rwFile.close();
            boolean rc = file.delete();
            if (logger.isDebugEnabled()) {
                logger.debug("[%s][deleting file]%s", file.getCanonicalPath(), rc);
            }
            rwFile = null;
            return rc;
        } catch (Exception e) {
            logger.warn("Failed to wipe file: " + e.toString(), e);
            return false;
        } finally {
            if (rwFile != null) {
                try {
                    rwFile.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private StringBuilder getDebugMain(int fileSpecFlags) {
        return getDebugFileSpecFlags(fileSpecFlags);
    }

    private StringBuilder getDebugFileSpecFlags(final int flags) {
        StringBuilder sb = new StringBuilder("fileSpecFlags=");
        if (has(flags, Pattern.CANON_EQ)) {
            sb.append("CANON_EQ");
        }
        if (has(flags, Pattern.CASE_INSENSITIVE)) {
            sb.append("CASE_INSENSITIVE");
        }
        if (has(flags, Pattern.COMMENTS)) {
            sb.append("COMMENTS");
        }
        if (has(flags, Pattern.DOTALL)) {
            sb.append("DOTALL");
        }
        if (has(flags, Pattern.MULTILINE)) {
            sb.append("MULTILINE");
        }
        if (has(flags, Pattern.UNICODE_CASE)) {
            sb.append("UNICODE_CASE");
        }
        if (has(flags, Pattern.UNIX_LINES)) {
            sb.append("UNIX_LINES");
        }
        return sb;
    }

    private StringBuilder getDebugRemoveFlags(final int flags) {
        StringBuilder sb = new StringBuilder("flags=");
        if (has(flags, GRACIOUS)) {
            sb.append("GRACIOUS ");
        }
        if (has(flags, RECURSIVE)) {
            sb.append("RECURSIVE ");
        }
        if (has(flags, REMOVE_DIR)) {
            sb.append("REMOVE_DIR ");
        }
        if (has(flags, WIPE)) {
            sb.append("WIPE ");
        }
        return sb;
    }

    private StringBuilder getDebugCopyFlags(final int flags) {
        StringBuilder sb = new StringBuilder("flags=");
        if (has(flags, CREATE_DIR)) {
            sb.append("CREATE_DIR ");
        }
        if (has(flags, GRACIOUS)) {
            sb.append("GRACIOUS ");
        }
        if (has(flags, NOT_OVERWRITE)) {
            sb.append("NOT_OVERWRITE ");
        }
        if (has(flags, RECURSIVE)) {
            sb.append("RECURSIVE ");
        }
        return sb;
    }

    private void debug(StringBuilder main, StringBuilder flags, String minFileAge, String maxFileAge, String minFileSize, String maxFileSize) {
        StringBuilder sb = new StringBuilder(main);
        if (flags != null) {
            sb.append(",").append(flags);
        }
        sb.append(",").append("minFileAge=").append(minFileAge);
        sb.append(",").append("maxFileAge=").append(maxFileAge);
        sb.append(",").append("minFileSize=").append(minFileSize);
        sb.append(",").append("maxFileSize=").append(maxFileSize);
        logger.debug(sb);
    }

    public List<File> getResultList() {
        return resultList;
    }

    public ISOSLogger getLogger() {
        return logger;
    }

}