package com.sos.jitl.jobs.file.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.file.exception.SOSFileOperationsException;

import js7.data_for_java.order.JOutcome;

public abstract class AFileOperationsJob extends ABlockingInternalJob<FileOperationsJobArguments> {

    public AFileOperationsJob(JobContext jobContext) {
        super(jobContext);
    }

    public void checkArguments(FileOperationsJobArguments args) throws Exception {
        if (args.getSourceFile().getValue() == null) {
            throw new SOSFileOperationsException(String.format("missing '%s'", args.getSourceFile().getName()));
        }

        if (SOSString.isEmpty(args.getReplacing().getValue()) && !SOSString.isEmpty(args.getReplacement().getValue())) {
            throw new SOSFileOperationsException(String.format("'%s' is missing but required for '%s'", args.getReplacing().getName(), args
                    .getReplacement().getName()));
        }
        setFlags(args);
    }

    private void setFlags(FileOperationsJobArguments args) {
        int flags = 0;
        if (args.getCreateFile().getValue()) {
            flags |= AFileOperations.CREATE_DIR;
        }
        if (args.getGracious().getValue()) {
            flags |= AFileOperations.GRACIOUS;
        }
        if (!args.getOverwrite().getValue()) {
            flags |= AFileOperations.NOT_OVERWRITE;
        }
        if (args.getRecursive().getValue()) {
            flags |= AFileOperations.RECURSIVE;
        }
        if (args.getRemoveDir().getValue()) {
            flags |= AFileOperations.REMOVE_DIR;
        }
        args.setFlags(flags);
    }

    public boolean checkSteadyStateOfFiles(JobLogger logger, FileOperationsJobArguments args, List<File> files) throws Exception {
        if (files == null || files.size() == 0 || !args.getCheckSteadyStateOfFiles().getValue()) {
            return true;
        }
        long interval = Job.getTimeAsSeconds(args.getSteadyStateInterval());

        logger.debug("checking file(s) for steady state, interval=%ss", interval);
        List<FileDescriptor> list = new ArrayList<FileDescriptor>();
        for (File file : files) {
            list.add(new FileDescriptor(file));
        }
        try {
            TimeUnit.SECONDS.sleep(interval);
        } catch (InterruptedException e) {
            logger.error(e.toString(), e);
        }

        boolean result = true;
        for (int i = 0; i < args.getSteadyStateCount().getValue(); i++) {
            result = true;
            for (FileDescriptor fd : list) {
                File file = new File(fd.getFileName());
                logger.debug("result is : " + file.lastModified() + ", " + fd.getLastModificationDate() + ", " + file.length() + ", " + fd
                        .getLastFileLength());
                if (args.getUseFileLock().getValue()) {
                    try {
                        RandomAccessFile raf = new RandomAccessFile(file, "rw");
                        FileChannel channel = raf.getChannel();
                        FileLock lock = channel.lock();
                        try {
                            lock = channel.tryLock();
                            logger.debug(String.format("lock for file '%1$s' ok", file.getAbsolutePath()));
                            break;
                        } catch (OverlappingFileLockException e) {
                            result = false;
                            logger.info(String.format("File '%1$s' is open by someone else", file.getAbsolutePath()));
                            break;
                        } finally {
                            lock.release();
                            logger.debug(String.format("release lock for '%1$s'", file.getAbsolutePath()));
                            if (raf != null) {
                                channel.close();
                                raf.close();
                                raf = null;
                            }
                        }
                    } catch (FileNotFoundException e) {
                        logger.error(e.getMessage(), e);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (file.lastModified() != fd.getLastModificationDate() || file.length() != fd.getLastFileLength()) {
                    result = false;
                    fd.update(file);
                    fd.setSteady(false);
                    logger.info(String.format("File '%1$s' changed during checking steady state", file.getAbsolutePath()));
                    break;
                } else {
                    fd.setSteady(true);
                }
            }
            if (!result) {
                try {
                    TimeUnit.SECONDS.sleep(interval);
                } catch (InterruptedException e) {
                    logger.error(e.toString(), e);
                }
            } else {
                break;
            }
        }
        if (!result) {
            logger.error("not all files are steady");
            for (FileDescriptor fd : list) {
                if (!fd.isSteady()) {
                    logger.info(String.format("File '%1$s' is not steady", fd.getFileName()));
                }
            }
            throw new SOSFileOperationsException("not all files are steady");
        }
        return result;
    }

    public JOutcome.Completed handleResult(JobLogger logger, FileOperationsJobArguments args, List<File> files, boolean result) throws Exception {
        int size = 0;
        StringBuilder fileList = new StringBuilder();
        if (files != null && files.size() > 0) {
            size = files.size();
            for (File f : files) {
                fileList.append(f.getAbsolutePath() + ";");
            }
        }
        args.getReturnResultSet().setValue(fileList.toString());
        args.getReturnResultSetSize().setValue(size);

        if (args.getResultSetFile().getValue() != null && fileList.length() > 0) {
            logger.debug("..try to write file:" + args.getResultSetFile().getValue());
            if (Files.isWritable(args.getResultSetFile().getValue())) {
                Files.write(args.getResultSetFile().getValue(), fileList.toString().getBytes("UTF-8"));
            } else {
                throw new SOSFileOperationsException(String.format("file '%s'(%s) is not writable", args.getResultSetFile().getValue(), args
                        .getResultSetFile().getName()));
            }
        }
        if (!SOSString.isEmpty(args.getRaiseErrorIfResultSetIs().getValue())) {
            if (compareIntValues(args.getRaiseErrorIfResultSetIs().getValue(), size, args.getExpectedSizeOfResultSet().getValue())) {
                String msg = String.format("no of hits in result set '%s'  is '%s' expected '%s'", size, args.getRaiseErrorIfResultSetIs().getValue(),
                        args.getExpectedSizeOfResultSet().getValue());
                return Job.failed(msg, args.getReturnResultSet(), args.getReturnResultSetSize());
            }
        }
        return Job.success(args.getReturnResultSet(), args.getReturnResultSetSize());
    }

    private boolean compareIntValues(final String comparator, final int left, final int right) throws Exception {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("eq", 1);
        map.put("equal", 1);
        map.put("==", 1);
        map.put("=", 1);
        map.put("ne", 2);
        map.put("not equal", 2);
        map.put("!=", 2);
        map.put("<>", 2);
        map.put("lt", 3);
        map.put("less than", 3);
        map.put("<", 3);
        map.put("le", 4);
        map.put("less or equal", 4);
        map.put("<=", 4);
        map.put("ge", 5);
        map.put("greater or equal", 5);
        map.put(">=", 5);
        map.put("gt", 6);
        map.put("greater than", 6);
        map.put(">", 6);

        boolean result = false;
        Integer val = map.get(comparator.toLowerCase());
        if (val != null) {
            switch (val) {
            case 1:
                result = left == right;
                break;
            case 2:
                result = left != right;
                break;
            case 3:
                result = left < right;
                break;
            case 4:
                result = left <= right;
                break;
            case 5:
                result = left >= right;
                break;
            case 6:
                result = left > right;
                break;
            default:
                break;
            }
        } else {
            throw new SOSFileOperationsException(String.format("Compare operator not known: %s", comparator));
        }
        return result;
    }

    private class FileDescriptor {

        private long lastModificationDate;
        private long lastFileLength;
        private String fileName;
        private boolean isSteady;

        private FileDescriptor(final File file) {
            lastFileLength = file.length();
            lastModificationDate = file.lastModified();
            fileName = file.getAbsolutePath();
        }

        private void update(final File file) {
            lastFileLength = file.length();
            lastModificationDate = file.lastModified();
        }

        private long getLastModificationDate() {
            return lastModificationDate;
        }

        private long getLastFileLength() {
            return lastFileLength;
        }

        private String getFileName() {
            return fileName;
        }

        private boolean isSteady() {
            return isSteady;
        }

        private void setSteady(boolean val) {
            isSteady = val;
        }

    }

}
