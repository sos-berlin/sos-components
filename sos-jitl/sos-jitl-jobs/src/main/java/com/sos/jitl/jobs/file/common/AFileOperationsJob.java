package com.sos.jitl.jobs.file.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.file.exception.SOSFileOperationsException;

public abstract class AFileOperationsJob extends ABlockingInternalJob<FileOperationsJobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AFileOperationsJob.class);

    public AFileOperationsJob(JobContext jobContext) {
        super(jobContext, FileOperationsJobArguments.class);
    }

    public void checkArguments(FileOperationsJobArguments args) throws Exception {
        if (args.getSourceFile().getValue() == null) {
            throw new SOSFileOperationsException(String.format("missing '%s'", args.getSourceFile().getName()));
        }

        if (SOSString.isEmpty(args.getReplacing().getValue()) && !SOSString.isEmpty(args.getReplacement().getValue())) {
            throw new SOSFileOperationsException(String.format("'%s' is missing but required for '%s'", args.getReplacing().getName(), args
                    .getReplacement().getName()));
        }
        setCreateOrder(args);
        setFlags(args);
    }

    private void setCreateOrder(FileOperationsJobArguments args) throws Exception {
        if (args.getCreateOrder().getValue() || args.getCreateOrders4AllFiles().getValue()) {
            if (SOSString.isEmpty(args.getOrderJobchainName().getValue())) {
                throw new SOSFileOperationsException(String.format("missing '%s'", args.getOrderJobchainName().getName()));
            }
        }
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

    public boolean checkSteadyStateOfFiles(FileOperationsJobArguments args, List<File> files) throws Exception {
        if (files == null || files.size() == 0) {
            return true;
        }
        Job.setTimeAsSeconds(args.getCheckSteadyStateInterval());

        LOGGER.debug("checking file(s) for steady state");
        List<FileDescriptor> list = new ArrayList<FileDescriptor>();
        for (File file : files) {
            list.add(new FileDescriptor(file));
        }
        try {
            Thread.sleep(args.getCheckSteadyStateInterval().getNumberValue().longValue() * 1_000);
        } catch (InterruptedException e1) {
            LOGGER.error(e1.getMessage(), e1);
        }

        boolean result = true;
        for (int i = 0; i < args.getSteadyStateCount().getValue(); i++) {
            result = true;
            for (FileDescriptor fd : list) {
                File file = new File(fd.getFileName());
                LOGGER.debug("result is : " + file.lastModified() + ", " + fd.getLastModificationDate() + ", " + file.length() + ", " + fd
                        .getLastFileLength());
                if (args.getUseNioLock().getValue()) {
                    try {
                        RandomAccessFile raf = new RandomAccessFile(file, "rw");
                        FileChannel channel = raf.getChannel();
                        FileLock lock = channel.lock();
                        try {
                            lock = channel.tryLock();
                            LOGGER.debug(String.format("lock for file '%1$s' ok", file.getAbsolutePath()));
                            break;
                        } catch (OverlappingFileLockException e) {
                            result = false;
                            LOGGER.info(String.format("File '%1$s' is open by someone else", file.getAbsolutePath()));
                            break;
                        } finally {
                            lock.release();
                            LOGGER.debug(String.format("release lock for '%1$s'", file.getAbsolutePath()));
                            if (raf != null) {
                                channel.close();
                                raf.close();
                                raf = null;
                            }
                        }
                    } catch (FileNotFoundException e) {
                        LOGGER.error(e.getMessage(), e);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                if (file.lastModified() != fd.getLastModificationDate() || file.length() != fd.getLastFileLength()) {
                    result = false;
                    fd.update(file);
                    fd.setSteady(false);
                    LOGGER.info(String.format("File '%1$s' changed during checking steady state", file.getAbsolutePath()));
                    break;
                } else {
                    fd.setSteady(true);
                }
            }
            if (!result) {
                try {
                    Thread.sleep(args.getCheckSteadyStateInterval().getNumberValue().longValue() * 1_000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                break;
            }
        }
        if (!result) {
            LOGGER.error("not all files are steady");
            for (FileDescriptor fd : list) {
                if (!fd.isSteady()) {
                    LOGGER.info(String.format("File '%1$s' is not steady", fd.getFileName()));
                }
            }
            throw new SOSFileOperationsException("not all files are steady");
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
